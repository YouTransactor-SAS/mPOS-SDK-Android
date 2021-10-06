/*
 * Copyright (C) 2011-2021, YouTransactor. All Rights Reserved.
 *
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youtransactor.sampleapp.connexion;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.youTransactor.uCube.api.UCubeAPI;
import com.youTransactor.uCube.connexion.UCubeDevice;
import com.youtransactor.sampleapp.MainActivity;
import com.youtransactor.sampleapp.R;
import com.youtransactor.sampleapp.UIUtils;

import java.util.List;

public class ListPairedUCubeTouchActivity extends AppCompatActivity {

    private static final int ENABLE_BT_REQUEST_CODE = 1234;

    uCubeTouchPairedListAdapter adapter;

    private String filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_ACTION_BAR);

        setContentView(R.layout.list_devices_layout);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle(R.string.title_list_devices);
        }
        if (getIntent() != null && getIntent().getStringExtra(MainActivity.SCAN_FILTER) != null)
            filter = getIntent().getStringExtra(MainActivity.SCAN_FILTER);

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        if (!bluetoothManager.getAdapter().isEnabled())
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), ENABLE_BT_REQUEST_CODE);
        else
            init();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == ENABLE_BT_REQUEST_CODE) {
            final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

            if (!bluetoothManager.getAdapter().isEnabled()) {
                UIUtils.showOptionDialog(this, getString(R.string.enable_bt_msg),
                        getString(R.string.enable_bt_yes_label),
                        getString(R.string.enable_bt_no_label), (dialog, which) -> {

                            if (which == DialogInterface.BUTTON_POSITIVE)
                                startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), ENABLE_BT_REQUEST_CODE);
                            else
                                Toast.makeText(this, getString(R.string.enable_bt_msg), Toast.LENGTH_SHORT).show();
                        });
            } else
                init();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            final Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void init() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        List<UCubeDevice> devices = UCubeAPI.getConnexionManager().getPairedUCubes(filter);

        adapter = new uCubeTouchPairedListAdapter(ListPairedUCubeTouchActivity.this, devices, view -> {
            final int childAdapterPosition = recyclerView.getChildAdapterPosition(view);
            final UCubeDevice selectedDevice = adapter.getItemAtPosition(childAdapterPosition);

            final Intent intent = new Intent();
            intent.putExtra(MainActivity.DEVICE_NAME, selectedDevice.getName());
            intent.putExtra(MainActivity.DEVICE_ADDRESS, selectedDevice.getAddress());

            setResult(MainActivity.SCAN_REQUEST, intent);
            finish();
        });

        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        final Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
