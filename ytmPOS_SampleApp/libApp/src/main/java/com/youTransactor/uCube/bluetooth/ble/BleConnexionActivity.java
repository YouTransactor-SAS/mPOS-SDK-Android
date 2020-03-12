package com.youTransactor.uCube.bluetooth.ble;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.polidea.rxandroidble2.RxBleDevice;
import com.youTransactor.uCube.LogManager;
import com.youTransactor.uCube.R;
import com.youTransactor.uCube.TaskEvent;
import com.youTransactor.uCube.UIUtils;
import com.youTransactor.uCube.bluetooth.ActivityConnexionListener;
import com.youTransactor.uCube.ContextDataManager;
import com.youTransactor.uCube.rpc.Constants;
import com.youTransactor.uCube.rpc.command.GetInfosCommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BleConnexionActivity extends AppCompatActivity {

    String[] appPermissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private static final int ENABLE_BT_REQUEST_CODE = 4321;
    private static final int PERMISSIONS_REQUEST_CODE = 1234;

    RxBleScanManager rxBleScanManager;

    ProgressBar progressBar;

    public static ActivityConnexionListener listener;

    private Thread getInfoThread;

    public static void setActivityConnexionListener(ActivityConnexionListener connectionListener){
        listener = connectionListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_ACTION_BAR);

        setContentView(R.layout.activity_connexion);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        rxBleScanManager = new RxBleScanManager();

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        if (!bluetoothManager.getAdapter().isEnabled()) {
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), ENABLE_BT_REQUEST_CODE);
            return;
        }

        if(checkAndRequestPermission())
            rxBleScanManager.init(getApplicationContext(), this, findViewById(R.id.recycler_view));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            try {
                if (getInfoThread != null && getInfoThread.isAlive())
                    getInfoThread.interrupt();
            }catch (Exception e) {
                LogManager.e("error to interrupt Get info thread ", e);
            }

            if(listener != null)
                listener.onCancelled();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        if (bluetoothManager.getAdapter().isEnabled() && checkPermission()) {
            rxBleScanManager.scan(new RxBleScanManager.ScanResultListener() {
                @Override
                public void onDeviceSelected(RxBleDevice rxBleDevice) {

                    progressBar.setVisibility(View.GONE);

                    BluetoothDevice selectedDevice = rxBleDevice.getBluetoothDevice();

                    BleConnexionManager.getInstance().setDevice(getApplicationContext(), selectedDevice);

                    getInfo(selectedDevice);

                }

                @Override
                public void onError(Throwable throwable) {
                    if (listener != null)
                        listener.onError();
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        rxBleScanManager.stopScan();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == PERMISSIONS_REQUEST_CODE) {

            HashMap<String, Integer> permissionResults = new HashMap<>();
            int denialCount = 0;

            for (int i = 0; i < grantResults.length; i++) {
                if(grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    permissionResults.put(permissions[i], grantResults[i]);
                    denialCount++;
                }
            }

            if(denialCount > 0) {
                for (Map.Entry<String, Integer> entry : permissionResults.entrySet()) {
                    String permName = entry.getKey();

                    if(ActivityCompat.shouldShowRequestPermissionRationale(this, permName)) {
                        UIUtils.showOptionDialog(this, getString(R.string.permission_msg),
                                getString(R.string.permission_yes_label),
                                getString(R.string.permission_no_label), (dialog, which) -> {

                                    if(which == DialogInterface.BUTTON_POSITIVE)
                                        checkAndRequestPermission();
                                    else
                                        if(listener != null)
                                            listener.onError();

                                });
                    } else {
                        UIUtils.showOptionDialog(this, getString(R.string.permission_msg),
                                getString(R.string.permission_go_settings),
                                getString(R.string.permission_no_label), (dialog, which) -> {

                                    if(which == DialogInterface.BUTTON_POSITIVE) {

                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                Uri.fromParts("package", getPackageName(), null));

                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);

                                        if(listener != null)
                                            listener.onError();

                                    } else
                                        if(listener != null)
                                            listener.onError();

                                });
                    }
                }
            } else
                rxBleScanManager.init(getApplicationContext(), this, findViewById(R.id.recycler_view));

        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == ENABLE_BT_REQUEST_CODE) {
            final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

            if (!bluetoothManager.getAdapter().isEnabled()) {
                UIUtils.showOptionDialog(this, getString(R.string.enable_bt_msg),
                        getString(R.string.enable_bt_yes_label),
                        getString(R.string.enable_bt_no_label), (dialog, which) -> {

                            if(which == DialogInterface.BUTTON_POSITIVE)
                                startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), ENABLE_BT_REQUEST_CODE);
                            else
                            if(listener != null) listener.onError();
                        });
            } else {
                if(checkAndRequestPermission())
                    rxBleScanManager.init(getApplicationContext(), this, findViewById(R.id.recycler_view));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private boolean checkPermission() {
        List<String> listPermissionNeeded = new ArrayList<>();

        for (String perm : appPermissions)
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED)
                listPermissionNeeded.add(perm);

        return listPermissionNeeded.isEmpty();
    }

    private boolean checkAndRequestPermission() {
        List<String> listPermissionNeeded = new ArrayList<>();

        for (String perm : appPermissions)
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED)
                listPermissionNeeded.add(perm);

        if(!listPermissionNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    listPermissionNeeded.toArray(new String[listPermissionNeeded.size()]),
                    PERMISSIONS_REQUEST_CODE);

            return false;
        }

        return true;
    }

    private void getInfo(BluetoothDevice selectedDevice) {
        UIUtils.showProgress(BleConnexionActivity.this, getString(R.string.connexion_progress));

        //get info C2 C3
        getInfoThread = new Thread(() -> new GetInfosCommand(
                Constants.TAG_TERMINAL_SN,
                Constants.TAG_TERMINAL_PN
        ).execute((event, params) -> {
            if (event == TaskEvent.PROGRESS) {
                return;
            }

            runOnUiThread(() -> {
                switch (event) {
                    case FAILED:
                        if(listener != null)
                            listener.onError();
                        break;

                    case SUCCESS:
                        //DeviceInfos deviceInfos = new DeviceInfos(((GetInfosCommand) params[0]).getResponseData());

                        ContextDataManager.getInstance().setUCubeName(selectedDevice.getName());
                        ContextDataManager.getInstance().setUCubeAddress(selectedDevice.getAddress());

                        if(listener !=null)
                            listener.onConnected(selectedDevice);
                        break;

                    case CANCELLED:
                        if(listener != null)
                            listener.onCancelled();
                        break;
                }

                UIUtils.hideProgressDialog();
            });
        }));

        getInfoThread.start();
    }
}
