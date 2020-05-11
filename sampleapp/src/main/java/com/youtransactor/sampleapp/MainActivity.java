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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.youTransactor.uCube.api.UCubeAPI;
import com.youTransactor.uCube.api.UCubeLibMDMServiceListener;
import com.youTransactor.uCube.connexion.BleConnectionManager;
import com.youTransactor.uCube.connexion.BtClassicConnexionManager;
import com.youTransactor.uCube.connexion.BtConnectionManager;
import com.youTransactor.uCube.connexion.IConnexionManager;
import com.youTransactor.uCube.connexion.UCubeDevice;
import com.youTransactor.uCube.mdm.Config;
import com.youTransactor.uCube.mdm.service.BinaryUpdate;
import com.youTransactor.uCube.mdm.service.ServiceState;
import com.youTransactor.uCube.rpc.Constants;
import com.youTransactor.uCube.rpc.DeviceInfos;
import com.youTransactor.uCube.rpc.command.DisplayMessageCommand;
import com.youTransactor.uCube.rpc.command.GetInfosCommand;
import com.youtransactor.sampleapp.connexion.ListPairedUCubeActivity;
import com.youtransactor.sampleapp.connexion.UCubeTouchScanActivity;
import com.youtransactor.sampleapp.mdm.CheckUpdateResultDialog;
import com.youtransactor.sampleapp.mdm.DeviceConfigDialogFragment;
import com.youtransactor.sampleapp.payment.PaymentActivity;
import com.youtransactor.sampleapp.rpc.FragmentDialogGetInfo;

import java.util.ArrayList;
import java.util.List;

import static com.youtransactor.sampleapp.MainActivity.State.DEVICE_CONNECTED;
import static com.youtransactor.sampleapp.MainActivity.State.DEVICE_NOT_CONNECTED;
import static com.youtransactor.sampleapp.MainActivity.State.NO_DEVICE_SELECTED;
import static com.youtransactor.sampleapp.SetupActivity.YT_PRODUCT;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getName();
    public static final int SCAN_REQUEST = 1234;

    public static final String DEVICE_NAME = "DEVICE_NAME";
    public static final String DEVICE_ADDRESS = "DEVICE_ADDRESS";

    /* UI */
    private TextView versionNameTv;
    private TextView uCubeModelTv;

    private LinearLayout ucubeSection;
    private TextView ucubeNameTv, ucubeAddressTv;

    private Button scanBtn, connectBtn, disconnectBtn;

    private Button payBtn, getInfoBtn, displayBtn;

    private Button mdmRegisterBtn, mdmCheckUpdateBtn, mdmSendLogBtn, mdmGetConfigBtn;

    /* Device */
    private YTProduct ytProduct;

    /* BT */
    public static IConnexionManager connexionManager;

    /* update */
    boolean checkOnlyFirmwareVersion = false;
    boolean forceUpdate = false;

    /* shared preference */
    private SharedPreferences sharedPreferences;
    private static final String SHAREDPREF_NAME = "main";

    enum State {
        NO_DEVICE_SELECTED,
        DEVICE_NOT_CONNECTED,
        DEVICE_CONNECTED,
    }

    enum MDMState {
        IDLE,
        DEVICE_REGISTERED,
        DEVICE_NOT_REGISTERED,
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (getIntent() != null) {
            if (getIntent().hasExtra(YT_PRODUCT))
                ytProduct = YTProduct.valueOf(getIntent().getStringExtra(YT_PRODUCT));
        }

        if (ytProduct == null) {
            finish();
            return;
        }

        switch (ytProduct) {
            case uCube:
                connexionManager = new BtClassicConnexionManager();
                break;

            case uCubeTouch:
                connexionManager = new BleConnectionManager();
                break;
        }

        // 1- Connexion Manager Initialisation & set
        ((BtConnectionManager) connexionManager).init(this);
        UCubeAPI.setConnexionManager(connexionManager);

        initView();

        sharedPreferences = getSharedPreferences(SHAREDPREF_NAME, Context.MODE_PRIVATE);
        if(getDevice() != null ) {
            //2- initialise the connexion manager with saved device
            connexionManager.setDevice(getDevice());
        }

        // 3- if user app will use YT TMS MDM Manager should be setup
        UCubeAPI.mdmSetup(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (connexionManager.getDevice() == null) {
            updateConnectionUI(NO_DEVICE_SELECTED);
            updateMDMUI(MDMState.IDLE);
        }
        else {
            if (!connexionManager.isConnected())
                updateConnectionUI(DEVICE_NOT_CONNECTED);
            else
                updateConnectionUI(DEVICE_CONNECTED);

            // if user app will use YT TMS use this to get MDM Manager state & then update UI
            updateMDMUI(UCubeAPI.isMdmManagerReady() ? MDMState.DEVICE_REGISTERED : MDMState.DEVICE_NOT_REGISTERED);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SCAN_REQUEST && data != null) {

            String deviceName = data.getStringExtra(DEVICE_NAME);
            String deviceAddress = data.getStringExtra(DEVICE_ADDRESS);

            Log.d(TAG, "device : " + deviceName + " : " + deviceAddress);

            UCubeDevice device = new UCubeDevice(deviceName, deviceAddress);

            //2- initialise the connexion manager with selected device
            connexionManager.setDevice(device);

            //save device
            saveDevice(device);
        }
    }

    private void initView() {

        scanBtn = findViewById(R.id.scanBtn);
        connectBtn = findViewById(R.id.connectBtn);
        disconnectBtn = findViewById(R.id.disconnectBtn);

        getInfoBtn = findViewById(R.id.getInfoBtn);
        displayBtn = findViewById(R.id.displayBtn);

        payBtn = findViewById(R.id.payBtn);

        mdmRegisterBtn = findViewById(R.id.registerBtn);
        mdmGetConfigBtn = findViewById(R.id.getConfigBtn);
        mdmCheckUpdateBtn = findViewById(R.id.checkUpdateBtn);
        mdmSendLogBtn = findViewById(R.id.sendLogBtn);

        ucubeSection = findViewById(R.id.ucube_section);
        ucubeNameTv = findViewById(R.id.ucube_name);
        ucubeAddressTv = findViewById(R.id.ucube_address);

        String versionName = BuildConfig.VERSION_NAME;
        versionNameTv = findViewById(R.id.version_name);
        versionNameTv.setText(getString(R.string.versionName, versionName));

        uCubeModelTv = findViewById(R.id.ucube_model);
        uCubeModelTv.setText(getString(R.string.ucube_model, ytProduct.name()));

        scanBtn.setOnClickListener(v -> scan());
        /* connexion management */
        connectBtn.setOnClickListener(v -> connect());
        disconnectBtn.setOnClickListener(v -> disconnect());

        /* payment service */
        payBtn.setOnClickListener(v -> payment());

        /* RPC Example calls */
        displayBtn.setOnClickListener(v -> displayHelloWorld());
        getInfoBtn.setOnClickListener(v -> getInfo());

        /* MDM SERVICE button */
        mdmRegisterBtn.setOnClickListener(v -> mdmRegister());
        mdmGetConfigBtn.setOnClickListener( v -> mdmGetConfig());
        mdmCheckUpdateBtn.setOnClickListener(v -> mdmCheckUpdate());
        mdmSendLogBtn.setOnClickListener(v -> mdmSendLogs());
    }

    private void updateConnectionUI(State state) {

        displaySelectedDevice();

        switch (state) {

            case NO_DEVICE_SELECTED:

                connectBtn.setVisibility(View.GONE);
                disconnectBtn.setVisibility(View.GONE);

                payBtn.setEnabled(false);
                getInfoBtn.setEnabled(false);
                displayBtn.setEnabled(false);
                break;

            case DEVICE_NOT_CONNECTED:

                connectBtn.setVisibility(View.VISIBLE);
                disconnectBtn.setVisibility(View.GONE);

                payBtn.setEnabled(false);
                getInfoBtn.setEnabled(false);
                displayBtn.setEnabled(false);
                break;

            case DEVICE_CONNECTED:

                connectBtn.setVisibility(View.GONE);
                disconnectBtn.setVisibility(View.VISIBLE);

                payBtn.setEnabled(true);
                getInfoBtn.setEnabled(true);
                displayBtn.setEnabled(true);
                break;
        }
    }

    private void displaySelectedDevice() {

        UCubeDevice device = getDevice();

        if (device == null) {
            ucubeSection.setVisibility(View.GONE);
            return;
        }

        ucubeSection.setVisibility(View.VISIBLE);

        ucubeNameTv.setText(getString(R.string.ucube_name, device.getName()));
        ucubeAddressTv.setText(getString(R.string.ucube_address, device.getAddress()));
    }

    private void updateMDMUI(MDMState state) {
        switch (state) {
            case IDLE:
                mdmRegisterBtn.setVisibility(View.VISIBLE);
                mdmRegisterBtn.setEnabled(false);

                mdmGetConfigBtn.setVisibility(View.GONE);
                mdmCheckUpdateBtn.setVisibility(View.GONE);
                mdmSendLogBtn.setVisibility(View.GONE);
                break;

            case DEVICE_NOT_REGISTERED:
                mdmRegisterBtn.setVisibility(View.VISIBLE);
                mdmRegisterBtn.setEnabled(true);

                mdmGetConfigBtn.setVisibility(View.GONE);
                mdmCheckUpdateBtn.setVisibility(View.GONE);
                mdmSendLogBtn.setVisibility(View.GONE);
                break;

            case DEVICE_REGISTERED:
                mdmRegisterBtn.setVisibility(View.GONE);

                mdmGetConfigBtn.setVisibility(View.VISIBLE);
                mdmCheckUpdateBtn.setVisibility(View.VISIBLE);
                mdmSendLogBtn.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void scan() {
        //disconnect
        disconnect();

        // if user app will use YT TMS
        // an unregister of last device should be called
        // to delete current ssl certificate
       boolean res = UCubeAPI.mdmUnregister(this);
       if(!res) {
           Log.e(TAG, "FATAL Error! error to unregister current device");
       }

       //remove saved device
       removeDevice();

       Intent intent = new Intent(this, ytProduct == YTProduct.uCubeTouch ?
                UCubeTouchScanActivity.class :
                ListPairedUCubeActivity.class);

       startActivityForResult(intent, SCAN_REQUEST);
    }

    private void connect() {
        if(connexionManager.isConnected()) {
            updateConnectionUI(DEVICE_CONNECTED);
            return;
        }

        UIUtils.showProgress(this, getString(R.string.connect_progress));

        connexionManager.connect(status -> {

            UIUtils.hideProgressDialog();

            if (!status) {
                UIUtils.showMessageDialog(MainActivity.this, getString(R.string.connect_failed));

            } else {

                Toast.makeText(MainActivity.this, getString(R.string.connect_success),
                        Toast.LENGTH_LONG).show();

                updateConnectionUI(DEVICE_CONNECTED);
            }
        });
    }

    private void disconnect() {

        if(!connexionManager.isConnected()) {
            updateConnectionUI(DEVICE_NOT_CONNECTED);
            return;
        }

        UIUtils.showProgress(this, getString(R.string.disconnect_progress));

        connexionManager.disconnect(status -> {

            UIUtils.hideProgressDialog();

            if (!status) {
                UIUtils.showMessageDialog(MainActivity.this, getString(R.string.disconnect_failed));
            } else {
                Toast.makeText(MainActivity.this, getString(R.string.disconnect_success),
                        Toast.LENGTH_LONG).show();
                updateConnectionUI(DEVICE_NOT_CONNECTED);
            }
        });
    }

    private void payment() {
        Intent paymentIntent = new Intent(this, PaymentActivity.class);
        startActivity(paymentIntent);
    }

    private void mdmRegister()  {
        final ProgressDialog progressDlg = UIUtils.showProgress(this, getString(R.string.register_progress));

        UCubeAPI.mdmRegister(this, new UCubeLibMDMServiceListener() {
            @Override
            public void onProgress(ServiceState state) {
                progressDlg.setMessage(getString(R.string.progress, state.name()));
            }

            @Override
            public void onFinish(boolean status, Object... params) {
                progressDlg.dismiss();

                if(status) {
                    Toast.makeText(MainActivity.this,
                            getString(R.string.register_success), Toast.LENGTH_SHORT).show();
                } else {
                    UIUtils.showMessageDialog(MainActivity.this, getString(R.string.register_failed));
                }

                updateMDMUI(status ? MDMState.DEVICE_REGISTERED : MDMState.DEVICE_NOT_REGISTERED);
            }
        });
    }

    private void mdmGetConfig() {
        final ProgressDialog progressDlg = UIUtils.showProgress(this, getString(R.string.get_config_progress));

        UCubeAPI.mdmGetConfig(this, new UCubeLibMDMServiceListener() {
            @Override
            public void onProgress(ServiceState state) {
                progressDlg.setMessage(getString(R.string.progress, state.name()));
            }

            @Override
            public void onFinish(boolean status, Object... params) {
                progressDlg.dismiss();

                if(status) {
                    DeviceConfigDialogFragment dlg = new DeviceConfigDialogFragment();
                    dlg.init((List<Config>) params[0]);
                    dlg.show(MainActivity.this.getSupportFragmentManager(), DeviceConfigDialogFragment.class.getSimpleName());
                } else {
                    UIUtils.showMessageDialog(MainActivity.this, getString(R.string.get_config_failed));
                }
            }
        });
    }

    private void mdmCheckUpdate() {

        UIUtils.showOptionDialog(this, "Force Update ? (Install same version)",
                "Yes", "No", (dialog, which) -> {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            forceUpdate = true;
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            forceUpdate = false;
                            break;
                    }

                    UIUtils.showOptionDialog(MainActivity.this,
                            "Check Only Firmware version ?",
                            "Yes", "No", (dialog1, which1) -> {
                                switch (which1) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        checkOnlyFirmwareVersion = true;
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        checkOnlyFirmwareVersion = false;
                                        break;
                                }

                                final ProgressDialog progressDlg = UIUtils.showProgress(this,
                                        getString(R.string.check_update_progress), false);

                                UCubeAPI.mdmCheckUpdate(this,
                                        forceUpdate,
                                        checkOnlyFirmwareVersion,
                                        new UCubeLibMDMServiceListener() {

                                            @Override
                                            public void onProgress(ServiceState state) {
                                                progressDlg.setMessage(getString(R.string.progress, state.name()));
                                            }

                                            @Override
                                            public void onFinish(boolean status, Object... params) {
                                                progressDlg.dismiss();

                                                if (status) {
                                                    List<BinaryUpdate> updateList = (List<BinaryUpdate>) params[0];
                                                    List<Config> cfgList = (List<Config>) params[1];

                                                    if (updateList.size() == 0) {
                                                        Toast.makeText(MainActivity.this,
                                                                getString(R.string.ucube_up_to_date), Toast.LENGTH_SHORT).show();
                                                    } else {

                                                        CheckUpdateResultDialog dlg = new CheckUpdateResultDialog();
                                                        dlg.init(MainActivity.this, updateList, (dialog, which) -> showBinUpdateListDialog(updateList));
                                                        dlg.show(MainActivity.this.getSupportFragmentManager(), CheckUpdateResultDialog.class.getSimpleName());
                                                    }

                                                } else {
                                                    UIUtils.showMessageDialog(MainActivity.this, getString(R.string.check_update_failed));
                                                }
                                            }
                                        });
                            });
                });
    }

    private void showBinUpdateListDialog(final List<BinaryUpdate> updateList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Select update");

        boolean[] selectedItems = new boolean[updateList.size()];
        String[] items = new String[updateList.size()];

        for (int i = 0; i < updateList.size(); i++) {
            selectedItems[i] = true;
            items[i] = updateList.get(i).getCfg().getLabel();
        }

        builder.setMultiChoiceItems(items, selectedItems, null);

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.setPositiveButton("Ok", (dialog, which) -> {
            final SparseBooleanArray selectedItems1 = ((AlertDialog) dialog).getListView().getCheckedItemPositions();

            dialog.dismiss();

            final ProgressDialog progressDlg = UIUtils.showProgress(this,
                    getString(R.string.update_progress), false);

            List<BinaryUpdate> selectedUpdateList = new ArrayList<>(selectedItems1.size());

            for (int i = 0; i < selectedItems1.size(); i++) {
                if (selectedItems1.get(i)) {
                    selectedUpdateList.add(updateList.get(i));
                }
            }

            UCubeAPI.mdmUpdate(this, selectedUpdateList, new UCubeLibMDMServiceListener() {
                @Override
                public void onProgress(ServiceState state) {
                    progressDlg.setMessage(getString(R.string.progress, state.name()));
                }

                @Override
                public void onFinish(boolean status, Object... params) {
                    progressDlg.dismiss();

                    Toast.makeText(getApplicationContext(),
                            status ? getString(R.string.update_success) : getString(R.string.update_failed),
                            Toast.LENGTH_LONG
                    ).show();
                }
            });

        });

        builder.create().show();
    }

    private void mdmSendLogs() {
        final ProgressDialog progressDlg = UIUtils.showProgress(this, getString(R.string.send_log_progress));

        UCubeAPI.mdmSendLogs(this, new UCubeLibMDMServiceListener() {
            @Override
            public void onProgress(ServiceState state) {
                progressDlg.setMessage(getString(R.string.progress, state.name()));
            }

            @Override
            public void onFinish(boolean status, Object... params) {
                progressDlg.dismiss();

                Toast.makeText(
                        MainActivity.this,
                        status ? getString(R.string.send_log_success) : getString(R.string.send_log_failed),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void displayHelloWorld() {
        final ProgressDialog progressDlg = UIUtils.showProgress(this, getString(R.string.display_msg));

        new Thread(() -> new DisplayMessageCommand("Hello world").execute((event, params) -> runOnUiThread(() -> {
            switch (event) {
                case FAILED:
                    UIUtils.showMessageDialog(this, getString(R.string.display_msg_failure));
                    break;

                case SUCCESS:
                    Toast.makeText(this, getString(R.string.display_msg_success), Toast.LENGTH_LONG).show();
                    break;

                default:
                    return;
            }

            progressDlg.dismiss();
        }))).start();
    }

    private void getInfo() {

        final int[] uCubeInfoTagList = {
                Constants.TAG_ATMEL_SERIAL,
                Constants.TAG_TERMINAL_PN,
                Constants.TAG_TERMINAL_SN,
                Constants.TAG_FIRMWARE_VERSION,
                Constants.TAG_EMV_ICC_CONFIG_VERSION,
                Constants.TAG_EMV_NFC_CONFIG_VERSION,
                Constants.TAG_TERMINAL_STATE,
                Constants.TAG_BATTERY_STATE,
                Constants.TAG_POWER_OFF_TIMEOUT,
                Constants.TAG_NFC_INFOS,
                Constants.TAG_EMVL1_CLESS_LIB_VERSION,
                Constants.TAG_USB_CAPABILITY,
                Constants.TAG_OS_VERSION,
                Constants.TAG_MPOS_MODULE_STATE,
                Constants.TAG_TST_LOOPBACK_VERSION,
                Constants.TAG_AGNOS_LIB_VERSION,
                Constants.TAG_ACE_LAYER_VERSION,
                Constants.TAG_GPI_VERSION,
                Constants.TAG_EMVL3_VERSION,
                Constants.TAG_PAYMENT_APP_CLESS_VERSION,
                Constants.TAG_PCI_PED_VERSION,
                Constants.TAG_PCI_PED_CHECKSUM,
                Constants.TAG_EMV_L1_CHECKSUM,
                Constants.TAG_BOOT_LOADER_CHECKSUM,
                Constants.TAG_EMV_L2_CHECKSUM,
        };

        final ProgressDialog progressDlg = UIUtils.showProgress(this, getString(R.string.get_info));
        progressDlg.setCancelable(false);

        new Thread(() -> new GetInfosCommand(uCubeInfoTagList).execute((event, params) -> runOnUiThread(() -> {
            switch (event) {
                case FAILED:
                    progressDlg.dismiss();
                    UIUtils.showMessageDialog(this, getString(R.string.get_info_failed));
                    return;

                case SUCCESS:
                    progressDlg.dismiss();
                    DeviceInfos deviceInfos = new DeviceInfos(((GetInfosCommand) params[0]).getResponseData());

                    FragmentManager fm = MainActivity.this.getSupportFragmentManager();
                    FragmentDialogGetInfo Dialog = new FragmentDialogGetInfo(deviceInfos);
                    Dialog.show(fm, "GET_INFO");

                    break;
            }

        }))).start();
    }

    private void saveDevice(UCubeDevice device) {
        sharedPreferences.edit()
                .putString(DEVICE_NAME, device.getName())
                .putString(DEVICE_ADDRESS, device.getAddress())
                .apply();
    }

    private void removeDevice() {
        sharedPreferences.edit()
                .remove(DEVICE_ADDRESS)
                .remove(DEVICE_NAME)
                .apply();
    }

    private UCubeDevice getDevice() {
        String name = sharedPreferences.getString(DEVICE_NAME, null);
        String address = sharedPreferences.getString(DEVICE_ADDRESS, null);
        if(name != null && address != null)
            return new UCubeDevice(name, address);

        return null;
    }
}
