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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.youTransactor.uCube.TaskEvent;
import com.youTransactor.uCube.api.UCubeAPI;
import com.youTransactor.uCube.api.UCubeLibMDMServiceListener;
import com.youTransactor.uCube.api.UCubeLibState;
import com.youTransactor.uCube.api.UCubeLibTaskListener;
import com.youTransactor.uCube.connexion.BleConnectionManager;
import com.youTransactor.uCube.connexion.BtClassicConnexionManager;
import com.youTransactor.uCube.connexion.ConnectionListener;
import com.youTransactor.uCube.connexion.ConnectionStatus;
import com.youTransactor.uCube.connexion.IConnexionManager;
import com.youTransactor.uCube.connexion.UCubeDevice;
import com.youTransactor.uCube.log.LogManager;
import com.youTransactor.uCube.mdm.Config;
import com.youTransactor.uCube.mdm.BinaryUpdate;
import com.youTransactor.uCube.mdm.service.ServiceState;
import com.youTransactor.uCube.rpc.Constants;
import com.youTransactor.uCube.rpc.DeviceInfos;
import com.youTransactor.uCube.rpc.RPCCommandStatus;
import com.youTransactor.uCube.rpc.command.DisplayMessageCommand;
import com.youTransactor.uCube.rpc.command.EnterSecureSessionCommand;
import com.youTransactor.uCube.rpc.command.ExitSecureSessionCommand;
import com.youTransactor.uCube.rpc.command.GetInfosCommand;
import com.youTransactor.uCube.rpc.command.SetInfoFieldCommand;
import com.youtransactor.sampleapp.connexion.ListPairedUCubeActivity;
import com.youtransactor.sampleapp.connexion.ListPairedUCubeTouchActivity;
import com.youtransactor.sampleapp.connexion.UCubeTouchScanActivity;
import com.youtransactor.sampleapp.mdm.CheckUpdateResultDialog;
import com.youtransactor.sampleapp.mdm.DeviceConfigDialogFragment;
import com.youtransactor.sampleapp.payment.PaymentActivity;
import com.youtransactor.sampleapp.rpc.FragmentDialogGetInfo;
import com.youtransactor.sampleapp.test.TestActivity;

import java.util.ArrayList;
import java.util.List;

import static com.youtransactor.sampleapp.MainActivity.State.*;
import static com.youtransactor.sampleapp.SetupActivity.YT_PRODUCT;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getName();
    public static final int SCAN_REQUEST = 1234;

    public static final String SCAN_FILTER = "SCAN_FILTER";
    public static final String DEVICE_NAME = "DEVICE_NAME";
    public static final String DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private SharedPreferences prefs;

    /* UI */
    private LinearLayout ucubeSection;

    private TextView versionNameTv, uCubeModelTv, ucubeNameTv, ucubeAddressTv;

    private EditText scanFilter, powerTimeoutFld;//, qrCodeText;

    private Button scanBtn, connectBtn, disconnectBtn, payBtn, getInfoBtn, displayBtn, getLogsL1,
            powerOffTimeoutBtn, setLocaleBtn, testBtn, mdmRegisterBtn, mdmCheckUpdateBtn,
            mdmSendLogBtn, mdmGetConfigBtn; //,qrCodeBtn;

    private TextInputLayout powerOffTimeoutInputLayout;

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

    private boolean testModeEnabled = false;

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

        prefs = getSharedPreferences(SetupActivity.SETUP_SHARED_PREF_NAME, Context.MODE_PRIVATE);

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

        UCubeAPI.setConnexionManager(connexionManager);

        initView();

        sharedPreferences = getSharedPreferences(SHAREDPREF_NAME, Context.MODE_PRIVATE);
        if (getDevice() != null) {
            //2- initialise the connexion manager with saved device
            connexionManager.setDevice(getDevice());
        }

        // 3- if user app will use YT TMS MDM Manager should be setup
        UCubeAPI.mdmSetup(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        testModeEnabled = prefs.getBoolean(SetupActivity.TEST_MODE_PREF_NAME, false);
        testBtn.setVisibility(testModeEnabled ? View.VISIBLE : View.GONE);

        if (connexionManager.getDevice() == null) {
            updateConnectionUI(NO_DEVICE_SELECTED);
            updateMDMUI(MDMState.IDLE);
        } else {
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

        if (requestCode == SCAN_REQUEST && data != null) {

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
        Button setupBtn = findViewById(R.id.setupBtn);
        setupBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, SetupActivity.class);
            intent.putExtra(SetupActivity.NO_DEFAULT, "true");

            startActivity(intent);
        });

        scanFilter = findViewById(R.id.scanFilter);
        switch (ytProduct) {
            case uCube:
                scanFilter.setText("uCube");
                break;

            case uCubeTouch:
                scanFilter.setText("uTouch");
                break;
        }

        scanBtn = findViewById(R.id.scanBtn);
        connectBtn = findViewById(R.id.connectBtn);
        disconnectBtn = findViewById(R.id.disconnectBtn);

        getInfoBtn = findViewById(R.id.getInfoBtn);
        getLogsL1 = findViewById(R.id.getSvppLogL1);
        displayBtn = findViewById(R.id.displayBtn);
        powerTimeoutFld = findViewById(R.id.powerTimeoutFld);
        powerOffTimeoutInputLayout = findViewById(R.id.poweroff_timeout_input_layout);
        powerOffTimeoutBtn = findViewById(R.id.powerTimeoutBtn);
        setLocaleBtn = findViewById(R.id.set_locale);
        testBtn = findViewById(R.id.testBtn);
        //  qrCodeBtn = findViewById(R.id.qr_code_bt);
        /*   qrCodeText = findViewById(R.id.qr_code_data);*/

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
        getLogsL1.setOnClickListener(v -> getSvppLogsL1());
        getInfoBtn.setOnClickListener(v -> getInfo());
        powerTimeoutFld.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                powerOffTimeoutInputLayout.setError("");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        powerOffTimeoutBtn.setOnClickListener(v -> powerOffTimeout());
        setLocaleBtn.setOnClickListener(v -> setLocale());
        //  qrCodeBtn.setOnClickListener(v -> generateQRCode());

        /* MDM SERVICE button */
        mdmRegisterBtn.setOnClickListener(v -> mdmRegister());
        mdmGetConfigBtn.setOnClickListener(v -> mdmGetConfig());
        mdmCheckUpdateBtn.setOnClickListener(v -> mdmCheckUpdate());
        mdmSendLogBtn.setOnClickListener(v -> mdmSendLogs());

        testBtn.setOnClickListener(v -> test());
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

                payBtn.setEnabled(true);
                getInfoBtn.setEnabled(true);
                displayBtn.setEnabled(true);
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
        if (!res) {
            Log.e(TAG, "FATAL Error! error to unregister current device");
        }

        //remove saved device
        removeDevice();

        Intent intent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent = new Intent(this, ytProduct == YTProduct.uCubeTouch ?
                    UCubeTouchScanActivity.class :
                    ListPairedUCubeActivity.class);

        } else {
            intent = new Intent(this, ytProduct == YTProduct.uCubeTouch ?
                    ListPairedUCubeTouchActivity.class :
                    ListPairedUCubeActivity.class);
        }

        String filter = scanFilter.getText().toString();
        intent.putExtra(MainActivity.SCAN_FILTER, filter);

        startActivityForResult(intent, SCAN_REQUEST);
    }

    private void connect() {
        if (connexionManager.isConnected()) {
            updateConnectionUI(DEVICE_CONNECTED);
            return;
        }

        UIUtils.showProgress(this, getString(R.string.connect_progress), true, dialog -> {
            if (connexionManager instanceof BleConnectionManager) {
                ((BleConnectionManager) connexionManager).cancelConnect();
            }
        });

        connexionManager.connect(new ConnectionListener() {
            @Override
            public void onConnectionFailed(ConnectionStatus status, int error) {
                runOnUiThread(() -> {
                    UIUtils.hideProgressDialog();

                    UIUtils.showMessageDialog(MainActivity.this,
                            getString(R.string.connect_failed, status.name(), error));
                });
            }

            @Override
            public void onConnectionSuccess() {
                runOnUiThread(() -> {
                    UIUtils.hideProgressDialog();

                    Toast.makeText(MainActivity.this, getString(R.string.connect_success),
                            Toast.LENGTH_LONG).show();

                    updateConnectionUI(DEVICE_CONNECTED);
                });

            }

            @Override
            public void onConnectionCancelled() {
                runOnUiThread(() -> {
                    UIUtils.hideProgressDialog();
                    Toast.makeText(MainActivity.this, getString(R.string.connection_cancelled),
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void disconnect() {

        if (!connexionManager.isConnected()) {
            updateConnectionUI(DEVICE_NOT_CONNECTED);
            return;
        }

        UIUtils.showProgress(this, getString(R.string.disconnect_progress));

        connexionManager.disconnect(status -> runOnUiThread(() -> {
            UIUtils.hideProgressDialog();

            if (!status) {
                UIUtils.showMessageDialog(MainActivity.this, getString(R.string.disconnect_failed));
            } else {
                Toast.makeText(MainActivity.this, getString(R.string.disconnect_success),
                        Toast.LENGTH_LONG).show();
                updateConnectionUI(DEVICE_NOT_CONNECTED);
            }
        }));
    }

    private void payment() {
        Intent paymentIntent = new Intent(this, PaymentActivity.class);
        paymentIntent.putExtra(YT_PRODUCT, ytProduct.name());
        startActivity(paymentIntent);
    }

    private void mdmRegister() {
        final ProgressDialog progressDlg = UIUtils.showProgress(this, getString(R.string.register_progress));

        UCubeAPI.mdmRegister(new UCubeLibMDMServiceListener() {
            @Override
            public void onProgress(ServiceState state) {
                progressDlg.setMessage(getString(R.string.progress, state.name()));
            }

            @Override
            public void onFinish(boolean status, Object... params) {
                progressDlg.dismiss();

                if (status) {
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

        UCubeAPI.mdmGetConfig(new UCubeLibMDMServiceListener() {
            @Override
            public void onProgress(ServiceState state) {
                progressDlg.setMessage(getString(R.string.progress, state.name()));
            }

            @Override
            public void onFinish(boolean status, Object... params) {
                progressDlg.dismiss();

                if (status) {
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

                                UCubeAPI.mdmCheckUpdate(
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

            UCubeAPI.mdmUpdate(selectedUpdateList, new UCubeLibMDMServiceListener() {
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

        UCubeAPI.mdmSendLogs(new UCubeLibMDMServiceListener() {
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

        DisplayMessageCommand displayMessageCommand = new DisplayMessageCommand("Hello world");
        displayMessageCommand.setTimeout(5);
        displayMessageCommand.setClearConfig((byte) 0x05);
        displayMessageCommand.execute((event1, params1) -> runOnUiThread(() -> {
            switch (event1) {
                case PROGRESS:
                    LogManager.e("message display progress state "+ ((RPCCommandStatus) params1[1]).name());
                    break;
                case FAILED:
                case CANCELLED:
                    UIUtils.showMessageDialog(this, getString(R.string.display_msg_failure));
                    break;

                case SUCCESS:
                    Toast.makeText(this, getString(R.string.display_msg_success), Toast.LENGTH_LONG).show();
                    break;

                default:
                    return;
            }

            progressDlg.dismiss();
        }));

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
                Constants.TAG_CONFIGURATION_MERCHANT_INTERFACE_LOCALE,
                Constants.TAG_SUPPORTED_LOCALE_LIST,
                Constants.TAG_EMVL1_CLESS_LIB_VERSION,
                Constants.TAG_USB_CAPABILITY,
                Constants.TAG_OS_VERSION,
                Constants.TAG_MPOS_MODULE_STATE,
                Constants.TAG_TST_LOOPBACK_VERSION,
                Constants.TAG_AGNOS_LIB_VERSION,
                Constants.TAG_ACE_LAYER_VERSION,
                Constants.TAG_GPI_VERSION,
                Constants.TAG_EMVL3_VERSION,
                Constants.TAG_PCI_PED_VERSION,
                Constants.TAG_PCI_PED_CHECKSUM,
                Constants.TAG_EMV_L1_CHECKSUM,
                Constants.TAG_BOOT_LOADER_CHECKSUM,
                Constants.TAG_EMV_L2_CHECKSUM,
                Constants.TAG_BLE_FIRMWARE_VERSION,
                Constants.TAG_RESOURCE_FILE_VERSION
        };

        final ProgressDialog progressDlg = UIUtils.showProgress(this, getString(R.string.get_info));
        progressDlg.setCancelable(false);

        new GetInfosCommand(uCubeInfoTagList).execute((event1, params1) -> runOnUiThread(() -> {
            switch (event1) {
                case PROGRESS:
                    LogManager.e("Get info progress state "+ ((RPCCommandStatus) params1[1]).name());
                    break;
                case FAILED:
                case CANCELLED:
                    progressDlg.dismiss();
                    UIUtils.showMessageDialog(MainActivity.this, getString(R.string.get_info_failed));
                    return;

                case SUCCESS:
                    progressDlg.dismiss();
                    DeviceInfos deviceInfos = new DeviceInfos(((GetInfosCommand) params1[0]).getResponseData());

                    FragmentManager fm = MainActivity.this.getSupportFragmentManager();
                    FragmentDialogGetInfo Dialog = new FragmentDialogGetInfo(deviceInfos, ytProduct);
                    Dialog.show(fm, "GET_INFO");

                    break;
            }
        }));
    }

    private void powerOffTimeout() {
        EditText powerTimeoutFld = findViewById(R.id.powerTimeoutFld);
        if(powerTimeoutFld.getText().toString().isEmpty()) {
            powerOffTimeoutInputLayout.setError(getString(R.string.error_set_mandatory_field));
            return;
        }

        int powerOffValue = Integer.parseInt(powerTimeoutFld.getText().toString());
        if(powerOffValue != 0 && (powerOffValue > 255 || powerOffValue < 32))  {
            powerOffTimeoutInputLayout.setError(getString(R.string.error_wrong_value));
            return;
        }

        powerOffTimeoutInputLayout.setError("");

        final ProgressDialog progressDlg = UIUtils.showProgress(this, getString(R.string.set_power_off_timeout_value));
        progressDlg.setCancelable(false);

        SetInfoFieldCommand setInfoFieldCommand = new SetInfoFieldCommand();
        setInfoFieldCommand.setPowerTimeout((byte) powerOffValue);
        setInfoFieldCommand.execute((event1, params1) -> runOnUiThread(() -> {
            if (event1 == TaskEvent.PROGRESS)
                return;

            progressDlg.dismiss();

            switch (event1) {
                case CANCELLED:
                    UIUtils.showMessageDialog(this, getString(R.string.set_power_off_timeout_cancelled));
                    break;

                case FAILED:
                    UIUtils.showMessageDialog(this, getString(R.string.set_power_off_timeout_failed));
                    break;

                case SUCCESS:
                    Toast.makeText(this, getString(R.string.set_power_off_timeout_success), Toast.LENGTH_LONG).show();
                    break;
            }

        }));
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
        if (name != null && address != null)
            return new UCubeDevice(name, address);

        return null;
    }

    private void getSvppLogsL1() {

        final int[] uCubeInfoTagList = {
                Constants.TAG_SYSTEM_FAILURE_LOG_RECORD_1,
        };

        final ProgressDialog progressDlg = UIUtils.showProgress(this, getString(R.string.get_logs_l1));
        progressDlg.setCancelable(false);

        new EnterSecureSessionCommand().execute((event, params) -> {
            switch (event) {
                case PROGRESS:
                    LogManager.e("get SVPP Log levvel 1 progress state "+ ((RPCCommandStatus) params[1]).name());
                    break;
                case FAILED:
                case CANCELLED:
                    runOnUiThread(() -> {
                        progressDlg.dismiss();
                        UIUtils.showMessageDialog(MainActivity.this, getString(R.string.get_logs_l1_failure));
                    });

                    break;

                case SUCCESS:
                    new GetInfosCommand(uCubeInfoTagList).execute((event1, params1) -> {
                        switch (event1) {
                            case PROGRESS:
                                LogManager.e("get SVPP Log levvel 1 progress state "+ ((RPCCommandStatus) params1[1]).name());
                                break;
                            case FAILED:
                            case CANCELLED:
                                runOnUiThread(() -> {
                                    progressDlg.dismiss();
                                    UIUtils.showMessageDialog(MainActivity.this, getString(R.string.get_logs_l1_failure));
                                });
                                break;

                            case SUCCESS:
                                new ExitSecureSessionCommand().execute((event2, params2) -> {
                                    if (event2 == TaskEvent.PROGRESS)
                                        return;

                                    progressDlg.dismiss();

                                    switch (event2) {
                                        case FAILED:
                                        case CANCELLED:
                                            runOnUiThread(() -> UIUtils.showMessageDialog(MainActivity.this, getString(R.string.get_logs_l1_failure)));

                                            break;

                                        case SUCCESS:
                                            runOnUiThread(() -> UIUtils.showMessageDialog(MainActivity.this, getString(R.string.get_logs_l1_success)));
                                            break;
                                    }
                                });
                                break;

                        }
                    });
                    break;
            }
        });
    }

    private void setLocale() {

        UIUtils.showProgress(MainActivity.this, getString(R.string.get_supported_locales));

        UCubeAPI.getSupportedLocaleList(new UCubeLibTaskListener() {
            @Override
            public void onProgress(UCubeLibState uCubeLibState) {
            }

            @Override
            public void onFinish(boolean status, Object... params) {

                if (!status) {
                    UIUtils.hideProgressDialog();
                    UIUtils.showMessageDialog(MainActivity.this, getString(R.string.get_supported_locale_list_failed));
                    return;
                }

                List<String> locales = ((ArrayList<String>) params[0]);
                if (locales == null || locales.isEmpty()) {
                    UIUtils.hideProgressDialog();
                    UIUtils.showMessageDialog(MainActivity.this, getString(R.string.supported_locale_list_is_empty));
                    return;
                }

                CharSequence[] items = new CharSequence[locales.size()];
                for (int i = 0; i < locales.size(); i++) {
                    LogManager.d("locale : " + locales.get(i));
                    items[i] = locales.get(i);
                }

                UIUtils.setProgressMessage(getString(R.string.set_locale));
                UIUtils.showItemsDialog(MainActivity.this, getString(R.string.set_locale),
                        items, (dialog, which) -> UCubeAPI.setLocale(locales.get(which), new UCubeLibTaskListener() {
                                    @Override
                                    public void onProgress(UCubeLibState uCubeLibState) {
                                    }

                                    @Override
                                    public void onFinish(boolean status1, Object... params1) {

                                        UIUtils.hideProgressDialog();

                                        if (status1) {
                                            Toast.makeText(MainActivity.this, getString(R.string.set_locale_success), Toast.LENGTH_LONG).show();
                                        } else {
                                            UIUtils.showMessageDialog(MainActivity.this, getString(R.string.set_locale_failed));
                                        }
                                    }
                                }
                        ));
            }
        });

    }

    /*private void generateQRCode() {
        String text = qrCodeText.getText().toString();
        if(text.isEmpty()) {
            Toast.makeText(this, "empty data ! ", Toast.LENGTH_LONG).show();
            return;
        }
        final ProgressDialog progressDlg = UIUtils.showProgress(this, getString(R.string.display_qr_code));

        new DisplayQRCodeCommand(text).execute((event1, params1) -> runOnUiThread(() -> {
            switch (event1) {
                case FAILED:
                    UIUtils.showMessageDialog(this, getString(R.string.display_qr_code_failure));
                    break;

                case SUCCESS:
                    Toast.makeText(this, getString(R.string.display_qr_code_success), Toast.LENGTH_LONG).show();
                    break;

                default:
                    return;
            }

            progressDlg.dismiss();
        }));
    }*/

    private void test() {
        Intent testIntent = new Intent(this, TestActivity.class);
        startActivity(testIntent);
    }
}
