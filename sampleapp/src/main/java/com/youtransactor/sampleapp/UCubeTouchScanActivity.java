/*
 * Copyright (C) 2011-2020, YouTransactor. All Rights Reserved.
 * <p/>
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youtransactor.sampleapp;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.youTransactor.uCube.LogManager;
import com.youTransactor.uCube.api.UCubeAPI;
import com.youTransactor.uCube.api.listener.UCubeAPIScanListener;
import com.youTransactor.uCube.bluetooth.UCubeDevice;
import com.youtransactor.sampleapp.adapter.uCubeTouchListAdapter;

import java.util.List;

public class UCubeTouchScanActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    private uCubeTouchListAdapter adapter;
    private BluetoothAdapter mBluetoothAdapter;

    private boolean mScanning;

    private static final int REQUEST_ENABLE_SCAN = 0;
    private static final int REQUEST_DISABLE_SCAN = 1;
    private static final int REQUEST_ENABLE_BT = 1;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_ACTION_BAR);

        setContentView(R.layout.list_devices_layout);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle(R.string.title_scan_devices);
        }

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if(bluetoothManager == null) {
            finish();
            return;
        }

        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan)
                    .setActionView(R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                adapter.clearScanResults();
                checkPermissionsThenScan(true);
                break;
            case R.id.menu_stop:
                checkPermissionsThenScan(false);
                break;
            case android.R.id.home:
                final Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device. If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        // Initializes recycle view adapter.
        adapter = new uCubeTouchListAdapter(this, view -> {
            final int childAdapterPosition = recyclerView.getChildAdapterPosition(view);
            final UCubeDevice selectedDevice = adapter.getDevice(childAdapterPosition);

            final Intent intent = new Intent(UCubeTouchScanActivity.this, MainActivity.class);
            intent.putExtra(MainActivity.EXTRAS_DEVICE_NAME, selectedDevice.getName());
            intent.putExtra(MainActivity.EXTRAS_DEVICE_ADDRESS, selectedDevice.getAddress());

            startActivity(intent);
            finish();
        });

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        checkPermissionsThenScan(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();

        checkPermissionsThenScan(false);

        adapter.clearScanResults();
    }

    @Override
    public void onBackPressed() {
        final Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void unableToScanBT() {
        Toast.makeText(this, "BLE Scan Fail! Unable to do a BLE Scan!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ENABLE_SCAN:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    scanLeDevice(true);
                else
                    unableToScanBT();
                break;
            case REQUEST_DISABLE_SCAN:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    scanLeDevice(false);
                else
                    unableToScanBT();
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void checkPermissionsThenScan(boolean enable) {
        // New in Android 6.0 Marshmallow, we now have runtime permissions.
        // BLE Scan requires location permissions and we have to ask the user to approve on runtime
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasLocationsPermission =
                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);

            if (hasLocationsPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        enable ? REQUEST_ENABLE_SCAN : REQUEST_DISABLE_SCAN);
                return;
            }
        }

        scanLeDevice(enable);
    }

    private void scanLeDevice(boolean enable) {
        try {
            if (enable) {
                mScanning = true;
                invalidateOptionsMenu();

                UCubeAPI.scanUCubeDevices(this, new UCubeAPIScanListener() {
                    @Override
                    public void onError() {
                        LogManager.e("error to scan BLE");

                        unableToScanBT();
                        mScanning = false;
                        invalidateOptionsMenu();
                    }

                    @Override
                    public void onDeviceDiscovered(UCubeDevice uCubeDevice) {
                        adapter.addDevice(uCubeDevice);
                    }

                    @Override
                    public void onScanComplete(List<UCubeDevice> discoveredUCubeDevices) {
                        mScanning = false;
                        invalidateOptionsMenu();
                    }
                });
            } else {
                mScanning = false;

                invalidateOptionsMenu();
                UCubeAPI.stopScan();
            }
        } catch (Exception e) {
            e.printStackTrace();
            unableToScanBT();
        }
    }
}