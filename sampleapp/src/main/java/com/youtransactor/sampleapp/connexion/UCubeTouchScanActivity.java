/*
 * Copyright (C) 2011-2020, YouTransactor. All Rights Reserved.
 * <p/>
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youtransactor.sampleapp.connexion;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.youTransactor.uCube.BuildConfig;
import com.youTransactor.uCube.connexion.BleConnectionManager;
import com.youTransactor.uCube.connexion.ScanListener;
import com.youTransactor.uCube.connexion.UCubeDevice;
import com.youTransactor.uCube.connexion.ScanStatus;
import com.youtransactor.sampleapp.MainActivity;
import com.youtransactor.sampleapp.R;

import java.util.List;

public class UCubeTouchScanActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    private uCubeTouchListAdapter adapter;
    private BluetoothAdapter mBluetoothAdapter;

    private boolean mScanning;
    private String scanFilter;

    private static final int REQUEST_LOCATION_PERMISSION = 0;
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
            return;
        }

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            finish();
            return;
        }

        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (MainActivity.connexionManager == null) {
            Log.e(getClass().getName(), "Error Connexion manager is not initialized");
            finish();
            return;
        }

        if (!(MainActivity.connexionManager instanceof BleConnectionManager)) {
            Log.e(getClass().getName(), "Error Connexion manager is not an instance of BleConnexionManager");
            finish();
            return;
        }

        // Initializes recycle view adapter.
        adapter = new uCubeTouchListAdapter(this, view -> {
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
                scanLeDevice(true);
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                break;
            case android.R.id.home:
                scanLeDevice(false);
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
                return;
            }
        }

        // this is mandatory before doing a BLE scan
        if(!checkPermission()) {
            requestPermission();
            return;
        }

        scanLeDevice(true);
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

        scanLeDevice(false);

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(getClass().getName(), "onRequestPermissionsResult()");

        switch (requestCode) {

            case REQUEST_LOCATION_PERMISSION:
                if (grantResults.length <= 0) {
                    // If user interaction was interrupted, the permission request is cancelled and you
                    // receive empty arrays.
                    Log.d(getClass().getName(), "User interaction was cancelled.");
                } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(getClass().getName(), "Permission granted");
                } else {
                    showSnackbar("Location Permission is necessary!",
                            R.string.settings, view -> {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.LIBRARY_PACKAGE_NAME, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            });
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean checkPermission() {
        // New in Android 6.0 Marshmallow, we now have runtime permissions.
        // BLE Scan requires location permissions and we have to ask the user to approve on runtime
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionState = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION);
            return permissionState == PackageManager.PERMISSION_GRANTED;
        }

        return true;
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION);

            if (shouldProvideRationale) {
                Log.d(getClass().getName(), "Displaying permission rationale to " +
                        "provide additional context.");

                showSnackbar("Location Permission is necessary!",
                        android.R.string.ok, view -> {
                            // Request permission
                            ActivityCompat.requestPermissions(this,
                                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                    REQUEST_LOCATION_PERMISSION);
                        });
            } else {
                Log.d(getClass().getName(), "Requesting permission");

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_LOCATION_PERMISSION);
            }
        }
    }

    private void showSnackbar(final String message, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(
                findViewById(android.R.id.content),
                message,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    private void scanLeDevice(boolean enable) {
        try {
            if (enable) {
                mScanning = true;
                invalidateOptionsMenu();

                ((BleConnectionManager) MainActivity.connexionManager).startScan(scanFilter,
                        new ScanListener() {
                            @Override
                            public void onError(ScanStatus scanStatus) {
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

                ((BleConnectionManager) MainActivity.connexionManager).stopScan();
            }
        } catch (Exception e) {
            e.printStackTrace();
            unableToScanBT();
        }
    }
}