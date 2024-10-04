/*
 * ============================================================================
 *
 * Copyright (c) 2024 JABIL Payment Solution
 *
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of JABIL Payment Solution
 * ("Confidential Information"). You  shall not disclose or redistribute such
 * Confidential Information and shall use it only in accordance with the terms of
 * the license agreement you entered into with JABIL Payment Solution.
 *
 * This software is provided by JABIL Payment Solution AS IS, and JABIL Payment Solution
 * makes no representations or warranties about the suitability of the software,
 * either express or implied, including but not limited to the implied warranties
 * of merchantability, fitness for a particular purpose or non-infringement.
 * JABIL Payment Solution shall not be liable for any direct, indirect, incidental,
 * special, exemplary, or consequential damages suffered by licensee as the
 * result of using, modifying or distributing this software or its derivatives.
 *
 * ==========================================================================
 */
package com.youtransactor.sampleapp.connexion;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.Snackbar;
import com.youTransactor.uCube.api.UCubeAPI;
import com.youTransactor.uCube.connexion.ScanError;
import com.youTransactor.uCube.connexion.ScanListener;
import com.youTransactor.uCube.connexion.UCubeDevice;
import com.youtransactor.sampleapp.MainActivity;
import com.youtransactor.sampleapp.R;
import com.youtransactor.sampleapp.UIUtils;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DeviceScanActivity extends AppCompatActivity implements ScanListener {

    private static final String TAG = DeviceScanActivity.class.getSimpleName();

    public static final String INTENT_EXTRA_DEVICE_SCANNER_CLASSNAME = "INTENT_EXTRA_DEVICE_SCANNER_CLASSNAME";
    private static final int REQUEST_PERMISSION = 0;
    private static final int REQUEST_ENABLE_BT = 1;

    private IDeviceScanner deviceSCanner;
    protected DeviceAdaptor adaptor;
    protected TextView filterFld;
    protected ListView device_list_fld;
    private TimerTask restartScanTask;
    private boolean scanning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_ACTION_BAR);

        setContentView(R.layout.list_devices_layout);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(R.string.title_select_device);
        }

        filterFld = findViewById(R.id.scanFilter);
        filterFld.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                restartScan(1000);
            }
        });
        filterFld.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                restartScan(0);
            }
        });

        findViewById(R.id.clearFilterBtn).setOnClickListener(v -> filterFld.setText(""));

        adaptor = new DeviceAdaptor(this);

        Intent intent = getIntent();
        if (intent != null) {
            String classname = intent.getStringExtra(INTENT_EXTRA_DEVICE_SCANNER_CLASSNAME);
            if (classname != null) {
                try {
                    deviceSCanner = (IDeviceScanner) Class.forName(classname).newInstance();
                }
                catch (Exception e) {
                    Log.w(TAG, "invalid device scanner class supplied: " + classname);
                }
            }
        }

        if (deviceSCanner == null) {
            Log.w(TAG, "no device scanner instance supplied");
            finish();
            return;
        }

        filterFld.setText(deviceSCanner.getDefaultFilter());
        adaptor.imageResource(deviceSCanner.getDeviceImageResourceId());

        device_list_fld = findViewById(R.id.device_list_fld);
        device_list_fld.setAdapter(adaptor);
        device_list_fld.setOnItemClickListener((parent, view, position, id) -> {
            stopScan();

            UCubeDevice selectedDevice = adaptor.getItem(position);

            if (selectedDevice == null) {
                return;
            }

            Intent resultIntent = new Intent();
            resultIntent.putExtra(MainActivity.INTENT_EXTRA_DEVICE_NAME, selectedDevice.getName());
            resultIntent.putExtra(MainActivity.INTENT_EXTRA_DEVICE_ADDRESS, selectedDevice.getAddress());

            setResult(MainActivity.SCAN_REQUEST, resultIntent);

            finish();
        });
    }
    @Override
    protected void onResume() {
        super.onResume();

        /* this is mandatory before doing a BLE scan */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!checkPermissionAndroid12()) {
                requestPermissionAndroid12();
                return;
            }
        } else if (!checkPermission()) {
            requestPermission();
            return;
        }

        // Ensures Bluetooth is enabled on the device. If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (!bluetoothManager.getAdapter().isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        startScan();
    }

    protected void onPause() {
        stopScan();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        final Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bt_scan, menu);

        if (scanning) {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setActionView(R.layout.actionbar_indeterminate_progress);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setActionView(null);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_scan) {
            startScan();
            return true;
        }

        if (item.getItemId() == R.id.menu_stop) {
            stopScan();
            return true;
        }

        if (item.getItemId() == android.R.id.home) {
            stopScan();
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode != REQUEST_ENABLE_BT) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (!bluetoothManager.getAdapter().isEnabled()) {
            UIUtils.showOptionDialog(this, getString(R.string.enable_bt_msg),
                    getString(R.string.enable_bt_yes_label),
                    getString(R.string.enable_bt_no_label), (dialog, which) -> {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    requestPermissions(new String[] { Manifest.permission.BLUETOOTH_CONNECT }, REQUEST_ENABLE_BT);
                                }
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                return;
                            }
                            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);
                        } else {
                            Toast.makeText(this, getString(R.string.enable_bt_msg), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(getClass().getName(), "onRequestPermissionsResult()");

        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(getClass().getName(), "Permission granted");
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    requestPermissionAndroid12();
                } else {
                    requestPermission();
                }
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onError(ScanError scanError) {
        Log.d(TAG, "BT scan error: " + scanError);
        scanning = false;
        invalidateOptionsMenu();
        showSnackBar(getString(R.string.scan_devices_error));
    }

    @Override
    public void onDeviceDiscovered(UCubeDevice uCubeDevice) {
        Log.d(TAG, "found device: " + uCubeDevice);
        adaptor.add(uCubeDevice);
    }

    @Override
    public void onScanComplete(List<UCubeDevice> discoveredUCubeDevices) {
        Log.d(TAG, "BT scan completed");
        scanning = false;
        invalidateOptionsMenu();
        runOnUiThread(() -> {
            adaptor.addAll(discoveredUCubeDevices);
        });
    }

    protected void startScan() {
        invalidateOptionsMenu();
        runOnUiThread(() -> {
            adaptor.clear();
            deviceSCanner.scan(filterFld.getText().toString(),  this);
        });
    }

    protected void stopScan() {
        if (restartScanTask != null) {
            restartScanTask.cancel();
            restartScanTask = null;
        }
        deviceSCanner.stop();
        scanning = false;
        invalidateOptionsMenu();
        UCubeAPI.getConnexionManager().stopScan();
    }

    private void restartScan(long delay) {
        if (restartScanTask != null) {
            restartScanTask.cancel();
            restartScanTask = null;
        }

        restartScanTask = new TimerTask() {
            @Override
            public void run() {
                restartScanTask = null;
                stopScan();
                startScan();
            }
        };

        new Timer().schedule(restartScanTask, delay);
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private boolean checkPermissionAndroid12() {
        int  btScanPermissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN);
        int  btConnectPermissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT);
        int  fineLocationPermissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        return btScanPermissionState == PackageManager.PERMISSION_GRANTED
                && btConnectPermissionState == PackageManager.PERMISSION_GRANTED
                && fineLocationPermissionState == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkPermission() {
        int coarsePermissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return coarsePermissionState == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void requestPermissionAndroid12() {
        String[] permissions = new String[] {
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
        };

        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BLUETOOTH_SCAN)
                        && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BLUETOOTH_CONNECT)
                        && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (shouldProvideRationale) {
            Log.d(getClass().getName(), "Displaying permission rationale to " +
                    "provide additional context.");

            showSnackBar("Permissions are necessary!", android.R.string.ok,
                    view -> {
                        ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION);
                    });
        } else {
            Log.d(getClass().getName(), "Requesting permissions");
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION);
        }
    }

    private void requestPermission() {
        String[] permissions = new String[] { Manifest.permission.ACCESS_FINE_LOCATION };

        boolean shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (shouldProvideRationale) {
            Log.d(getClass().getName(), "Displaying permission rationale to provide additional context.");
            showSnackBar("Location Permission is necessary!", android.R.string.ok,
                    view -> ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION));
        } else {
            Log.d(getClass().getName(), "Requesting permission");
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION);
        }
    }

     private void showSnackBar(final String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
     }

    private void showSnackBar(final String message, final int actionStringId, View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener)
                .show();
    }

}
