package com.youtransactor.sampleapp.connexion;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.youTransactor.uCube.api.UCubeAPI;
import com.youTransactor.uCube.connexion.ScanError;
import com.youTransactor.uCube.connexion.ScanListener;
import com.youTransactor.uCube.connexion.UCubeDevice;
import com.youtransactor.sampleapp.MainActivity;
import com.youtransactor.sampleapp.R;

import java.util.List;

public class YTSOMScanActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    private YTSOMListAdapter adapter;

    private boolean mScanning;
    private String scanFilter;


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

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_USB_HOST)) {
            Toast.makeText(this, R.string.USB_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initializes recycle view adapter.
        adapter = new YTSOMListAdapter(this, view -> {
            final int childAdapterPosition = recyclerView.getChildAdapterPosition(view);
            final UCubeDevice selectedDevice = adapter.getDevice(childAdapterPosition);

            final Intent intent = new Intent();
            intent.putExtra(MainActivity.DEVICE_NAME, selectedDevice.getName());
            intent.putExtra(MainActivity.DEVICE_ADDRESS, selectedDevice.getAddress());
            setResult(MainActivity.SCAN_REQUEST, intent);
            finish();
        });

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        if(getIntent() != null && getIntent().getStringExtra(MainActivity.SCAN_FILTER) != null) {
            scanFilter = getIntent().getStringExtra(MainActivity.SCAN_FILTER);
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
                scanDevice(true);
                break;
            case R.id.menu_stop:
                scanDevice(false);
                break;
            case android.R.id.home:
                scanDevice(false);
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

        scanDevice(true);
    }

    @Override
    protected void onPause() {
        super.onPause();

        scanDevice(false);
        adapter.clearScanResults();
    }

    @Override
    public void onBackPressed() {
        final Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }


    private void unableToScanBT() {
        Toast.makeText(this, "Scan Fails!", Toast.LENGTH_SHORT).show();
    }

    private void scanDevice(boolean enable) {
        try {
            if (enable) {
                mScanning = true;
                invalidateOptionsMenu();

                UCubeAPI.getConnexionManager().startScan(scanFilter, 1000,
                        new ScanListener() {
                            @Override
                            public void onError(ScanError scanStatus) {
                                Log.e(getClass().getName(), "error to scan BLE : "+ scanStatus);

                                unableToScanBT();
                                mScanning = false;
                                invalidateOptionsMenu();
                            }

                            @Override
                            public void onDeviceDiscovered(UCubeDevice uCubeDevice) {
                                Log.d(getClass().getName(), "on device discovered");
                                adapter.addDevice(uCubeDevice);
                            }

                            @Override
                            public void onScanComplete(List<UCubeDevice> discoveredUCubeDevices) {
                                Log.d(getClass().getName(), "on scan complete");
                                mScanning = false;
                                invalidateOptionsMenu();
                            }
                        });
            } else {
                mScanning = false;

                invalidateOptionsMenu();
                UCubeAPI.getConnexionManager().stopScan();
            }
        } catch (Exception e) {
            e.printStackTrace();
            unableToScanBT();
        }
    }
}
