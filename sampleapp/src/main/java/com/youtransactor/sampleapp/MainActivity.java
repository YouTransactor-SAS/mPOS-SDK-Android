/*
 * ============================================================================
 *
 * Copyright (c) 2022 YouTransactor
 *
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of YouTransactor
 * ("Confidential Information"). You  shall not disclose or redistribute such
 * Confidential Information and shall use it only in accordance with the terms of
 * the license agreement you entered into with YouTransactor.
 *
 * This software is provided by YouTransactor AS IS, and YouTransactor
 * makes no representations or warranties about the suitability of the software,
 * either express or implied, including but not limited to the implied warranties
 * of merchantability, fitness for a particular purpose or non-infringement.
 * YouTransactor shall not be liable for any direct, indirect, incidental,
 * special, exemplary, or consequential damages suffered by licensee as the
 * result of using, modifying or distributing this software or its derivatives.
 *
 * ==========================================================================
 */
package com.youtransactor.sampleapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
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
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.youTransactor.uCube.TaskEvent;
import com.youTransactor.uCube.Tools;
import com.youTransactor.uCube.api.UCubeAPI;
import com.youTransactor.uCube.api.UCubeLibMDMServiceListener;
import com.youTransactor.uCube.api.UCubeLibRKIServiceListener;
import com.youTransactor.uCube.api.UCubeLibState;
import com.youTransactor.uCube.api.UCubeLibTaskListener;
import com.youTransactor.uCube.connexion.BatteryLevelListener;
import com.youTransactor.uCube.connexion.ConnectionListener;
import com.youTransactor.uCube.connexion.ConnectionStatus;
import com.youTransactor.uCube.connexion.SVPPRestartListener;
import com.youTransactor.uCube.connexion.UCubeDevice;
import com.youTransactor.uCube.connexion.USBConnectionManager;
import com.youTransactor.uCube.mdm.Config;
import com.youTransactor.uCube.mdm.BinaryUpdate;
import com.youTransactor.uCube.mdm.ServiceState;
import com.youTransactor.uCube.rki.RKIServiceState;
import com.youTransactor.uCube.rpc.DeviceInfos;
import com.youTransactor.uCube.rpc.RPCCommand;
import com.youTransactor.uCube.rpc.RPCCommandStatus;
import com.youTransactor.uCube.rpc.LostPacketListener;
import com.youTransactor.uCube.rpc.RPCCommunicationErrorListener;
import com.youTransactor.uCube.rpc.SecurityMode;
import com.youTransactor.uCube.rpc.command.DisplayMessageCommand;
import com.youTransactor.uCube.rpc.command.EchoCommand;
import com.youTransactor.uCube.rpc.command.EnterSecureSessionCommand;
import com.youTransactor.uCube.rpc.command.ExitSecureSessionCommand;
import com.youTransactor.uCube.rpc.command.GetInfosCommand;
import com.youTransactor.uCube.rpc.command.GetKeyInfoCommand;
import com.youTransactor.uCube.rpc.command.InstallForLoadKeyCommand;
import com.youTransactor.uCube.rpc.command.RTCGetCommand;
import com.youTransactor.uCube.rpc.command.RTCSetCommand;
import com.youTransactor.uCube.rpc.command.RebootCommand;
import com.youTransactor.uCube.rpc.command.ResetCommand;
import com.youTransactor.uCube.rpc.command.SetInfoFieldCommand;
import com.youtransactor.sampleapp.connexion.ListPairedUCubeActivity;
import com.youtransactor.sampleapp.connexion.ListPairedUCubeTouchActivity;
import com.youtransactor.sampleapp.connexion.UCubeTouchScanActivity;
import com.youtransactor.sampleapp.connexion.YTSOMScanActivity;
import com.youtransactor.sampleapp.mdm.CheckUpdateResultDialog;
import com.youtransactor.sampleapp.mdm.DeviceConfigDialogFragment;
import com.youtransactor.sampleapp.payment.PaymentActivity;
import com.youtransactor.sampleapp.rpc.FragmentDialogGetInfo;
import com.youtransactor.sampleapp.rpc.GetInfoDialog;
import com.youtransactor.sampleapp.test.TestActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.youTransactor.uCube.connexion.ConnectionService.ConnectionManagerType.*;
import static com.youTransactor.uCube.rpc.Constants.*;
import static com.youtransactor.sampleapp.MainActivity.State.*;
import static com.youtransactor.sampleapp.SetupActivity.YT_PRODUCT;

public class MainActivity extends AppCompatActivity implements BatteryLevelListener, SVPPRestartListener, LostPacketListener, RPCCommunicationErrorListener {
    private static final String TAG = MainActivity.class.getName();
    private static final String SHARED_PREF_NAME = "main";

    public static final int SCAN_REQUEST = 1234;
    public static final String SCAN_FILTER = "SCAN_FILTER";
    public static final String DEVICE_NAME = "DEVICE_NAME";
    public static final String DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private static final String TDES = "74646573";

    /* UI */
    private LinearLayout uCubeSection;
    private TextView uCubeNameTv;
    private TextView uCubeAddressTv;
    private EditText scanFilter;
    private EditText connectionTimeoutEditText;
    private Button disconnectBtn;
    private Button payBtn;
    private Button getInfoBtn;
    private Button displayBtn;
    private Button mdmRegisterBtn;
    private Button mdmCheckUpdateBtn;
    private Button mdmSendLogBtn;
    private Button mdmGetConfigBtn;
    private LinearLayout connectionSection;
    private TextInputLayout powerOffTimeoutInputLayout;
    private Button echoBtn;

    /* Device */
    private YTProduct ytProduct;

    /* update */
    boolean checkOnlyFirmwareVersion = false;
    boolean forceUpdate = false;

    EditText keySlotEditText;

    @Override
    public void onLevelChanged(int newLevel) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, String.format(getString(R.string.battery_level), newLevel), Toast.LENGTH_LONG).show());
        Log.d(TAG,String.format(getString(R.string.battery_level), newLevel));
    }

    @Override
    public void onSVPPRestart() {
        runOnUiThread(() -> UIUtils.showMessageDialog(MainActivity.this, getString(R.string.device_get_stuck)));
        Log.d(TAG, "Notification of SVPP Restart event !");
    }

    @Override
    public void onPacketLost() {
        runOnUiThread(() -> UIUtils.showMessageDialog(MainActivity.this, getString(R.string.lost_packet)));
        Log.d(TAG, "Notification of lost packet !");
    }

    @Override
    public void onCommunicationError() {
        runOnUiThread(() -> UIUtils.showMessageDialog(MainActivity.this, getString(R.string.rpc_com_issue)));
        Log.d(TAG, "Notification of RPC Com issue !");
    }

    /*@Override
    public void onDisconnect(boolean status) {
        updateConnectionUI(DEVICE_NOT_CONNECTED);
        if(UCubeAPI.getConnexionManager() instanceof USBConnectionManager) {
            removeDevice();
            uCubeSection.setVisibility(View.GONE);
        }
    }*/

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
                UCubeAPI.setConnexionManagerType(BT);
                break;

            case simulator:
                UCubeAPI.setConnexionManagerType(SOCKET);
                break;

            case uCubeTouch:
                UCubeAPI.setConnexionManagerType(BLE);
                break;

            case YT_SOM:
                UCubeAPI.setConnexionManagerType(USB);
                break;
        }

        UCubeAPI.enableRecoveryMechanism(false);
        UCubeAPI.getConnexionManager().registerBatteryLevelChangeListener(this);
       // UCubeAPI.getConnexionManager().registerDisconnectListener(this);
        UCubeAPI.registerSVPPRestartListener(this);
        UCubeAPI.registerLostPacketListener(this);
        UCubeAPI.registerRPCCommunicationErrorListener(this);

        if (getDevice() != null) {
            UCubeAPI.getConnexionManager().setDevice(getDevice());
        }

        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (UCubeAPI.getConnexionManager().getDevice() == null) {
            updateConnectionUI(NO_DEVICE_SELECTED);
            updateMDMUI(MDMState.IDLE);
        } else {
            if (!UCubeAPI.getConnexionManager().isConnected())
                updateConnectionUI(DEVICE_NOT_CONNECTED);
            else
                updateConnectionUI(DEVICE_CONNECTED);

            // if user app will use YT TMS use this to get MDM Manager state & then update UI
            updateMDMUI(UCubeAPI.isMdmManagerReady() ? MDMState.DEVICE_REGISTERED : MDMState.DEVICE_NOT_REGISTERED);
        }
    }

    @Override
    protected void onDestroy() {
        UCubeAPI.unregisterSVPPRestartListener();
        UCubeAPI.unregisterLostPacketListener();
        UCubeAPI.unregisterRPCCommunicationErrorListener();
        super.onDestroy();
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
            UCubeAPI.getConnexionManager().setDevice(device);

            //to avoid saving the device in case of USB connection
            if(UCubeAPI.getConnexionManager() instanceof USBConnectionManager)
                return;

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
        connectionSection = findViewById(R.id.connection_section);
        Button scanBtn = findViewById(R.id.scanBtn);
        Button connectBtn = findViewById(R.id.connectBtn);
        connectionTimeoutEditText = findViewById(R.id.connection_timeout_edit_text);
        disconnectBtn = findViewById(R.id.disconnectBtn);
        getInfoBtn = findViewById(R.id.getInfoBtn);
        displayBtn = findViewById(R.id.displayBtn);
        EditText powerTimeoutFld = findViewById(R.id.powerTimeoutFld);
        powerOffTimeoutInputLayout = findViewById(R.id.poweroff_timeout_input_layout);
        Button powerOffTimeoutBtn = findViewById(R.id.powerTimeoutBtn);
        Button setLocaleBtn = findViewById(R.id.set_locale);
        Button getTerminalState = findViewById(R.id.get_terminal_state);
        Button enterSecureSession = findViewById(R.id.enter_secure_session);
        Button exitSecureSession = findViewById(R.id.exit_secure_session);
        getTerminalState.setOnClickListener(v -> getTerminalState());
        enterSecureSession.setOnClickListener(v -> enterSecureSession());
        exitSecureSession.setOnClickListener(v -> exitSecureSession());
        Button testBtn = findViewById(R.id.testBtn);
        Button quickModeBtn = findViewById(R.id.quick_mode);
        Button slowModeBtn = findViewById(R.id.slow_mode);
        Button rkiBtn = findViewById(R.id.rkiButton);
        Button getRtc = findViewById(R.id.get_rtc);
        Button setRtc = findViewById(R.id.set_rtc);
        echoBtn = findViewById(R.id.echo_btn);
        EditText echoData = findViewById(R.id.echoDataFld);
        Button getPublicKey = findViewById(R.id.get_pub_key);
        Button getKeyInfo = findViewById(R.id.get_key_info);
        Button resetButton = findViewById(R.id.reset);
        Button rebootButton = findViewById(R.id.reboot);
        //,qrCodeBtn;
        payBtn = findViewById(R.id.payBtn);
        mdmRegisterBtn = findViewById(R.id.registerBtn);
        mdmGetConfigBtn = findViewById(R.id.getConfigBtn);
        mdmCheckUpdateBtn = findViewById(R.id.checkUpdateBtn);
        mdmSendLogBtn = findViewById(R.id.sendLogBtn);
        uCubeSection = findViewById(R.id.ucube_section);
        uCubeNameTv = findViewById(R.id.ucube_name);
        uCubeAddressTv = findViewById(R.id.ucube_address);
        String versionName = BuildConfig.VERSION_NAME;
        TextView versionNameTv = findViewById(R.id.version_name);
        versionNameTv.setText(getString(R.string.versionName, versionName));
        TextView uCubeModelTv = findViewById(R.id.ucube_model);
        keySlotEditText = findViewById(R.id.key_slot);
        uCubeModelTv.setText(getString(R.string.ucube_model, ytProduct.name()));

        String nameFilter = "";
        switch (ytProduct) {
            case uCube:
                nameFilter = "uCube";
                break;

            case uCubeTouch:
                nameFilter = "uTouch";
                break;

            case YT_SOM:
                nameFilter = ytProduct.getName();
        }
        scanFilter.setText(nameFilter);

        scanBtn.setOnClickListener(v -> scan());
        connectBtn.setOnClickListener(v -> connect());
        disconnectBtn.setOnClickListener(v -> disconnect());
        payBtn.setOnClickListener(v -> payment());
        displayBtn.setOnClickListener(v -> displayHelloWorld());
        getInfoBtn.setOnClickListener(v -> getInfo());
        echoBtn.setOnClickListener(v-> echo(echoData.getText().toString()));
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
        mdmRegisterBtn.setOnClickListener(v -> mdmRegister());
        mdmGetConfigBtn.setOnClickListener(v -> mdmGetConfig());
        mdmCheckUpdateBtn.setOnClickListener(v -> mdmCheckUpdate());
        mdmSendLogBtn.setOnClickListener(v -> mdmSendLogs());
        testBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, TestActivity.class)));
        quickModeBtn.setOnClickListener(v -> enableQuickMode());
        slowModeBtn.setOnClickListener(v -> enableSlowMode());
        getRtc.setOnClickListener(v -> getRtc());
        setRtc.setOnClickListener(v -> setRtc());
        getPublicKey.setOnClickListener(v-> getPubKey());
        getKeyInfo.setOnClickListener(v-> getKeyInfo());
        resetButton.setOnClickListener(v -> reset());
        rebootButton.setOnClickListener(v-> reboot());

        SharedPreferences setupSharedPref = getSharedPreferences(SetupActivity.SETUP_SHARED_PREF_NAME, Context.MODE_PRIVATE);
        boolean testModeEnabled = setupSharedPref.getBoolean(SetupActivity.TEST_MODE_PREF_NAME, false);
        testBtn.setVisibility(testModeEnabled ? View.VISIBLE : View.GONE);

        rkiBtn.setOnClickListener(v-> doRemoteKeyInjection());
    }

    private void updateConnectionUI(State state) {

        displaySelectedDevice();

        switch (state) {

            case NO_DEVICE_SELECTED:

                connectionSection.setVisibility(View.GONE);
                disconnectBtn.setVisibility(View.GONE);
                break;

            case DEVICE_NOT_CONNECTED:

                connectionSection.setVisibility(View.VISIBLE);
                disconnectBtn.setVisibility(View.GONE);
                break;

            case DEVICE_CONNECTED:

                connectionSection.setVisibility(View.GONE);
                disconnectBtn.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void displaySelectedDevice() {

        UCubeDevice device = getDevice();

        if (device == null) {
            uCubeSection.setVisibility(View.GONE);
            return;
        }

        uCubeSection.setVisibility(View.VISIBLE);

        uCubeNameTv.setText(getString(R.string.ucube_name, device.getName()));
        uCubeAddressTv.setText(getString(R.string.ucube_address, device.getAddress()));
    }

    private void updateMDMUI(MDMState state) {
        switch (state) {
            case IDLE:
                mdmRegisterBtn.setVisibility(View.VISIBLE);

                mdmGetConfigBtn.setVisibility(View.GONE);
                mdmCheckUpdateBtn.setVisibility(View.GONE);
                mdmSendLogBtn.setVisibility(View.GONE);
                break;

            case DEVICE_NOT_REGISTERED:
                mdmRegisterBtn.setVisibility(View.VISIBLE);
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

    /*
     * Shared preference uses
     * */
    private UCubeDevice getDevice() {
        SharedPreferences mainSharedPref = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String name = mainSharedPref.getString(DEVICE_NAME, null);
        String address = mainSharedPref.getString(DEVICE_ADDRESS, null);

        if (name != null && address != null)
            return new UCubeDevice(name, address);

        return null;
    }

    private void saveDevice(UCubeDevice device) {
        getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE).edit()
                .putString(DEVICE_NAME, device.getName())
                .putString(DEVICE_ADDRESS, device.getAddress())
                .apply();
    }

    private void removeDevice() {
        getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE).edit()
                .remove(DEVICE_ADDRESS)
                .remove(DEVICE_NAME)
                .apply();
    }

    /*
     * UCUBE API Calls
     * */
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

        String filter = scanFilter.getText().toString();

        switch (ytProduct) {
            case uCube:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    intent = new Intent(this, ListPairedUCubeActivity.class);
                else
                    intent = new Intent(this, ListPairedUCubeActivity.class);
                break;

            case YT_SOM:
                intent = new Intent(this, YTSOMScanActivity.class);
                break;

            default:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    intent = new Intent(this, UCubeTouchScanActivity.class);
                else
                    intent = new Intent(this, ListPairedUCubeTouchActivity.class);
                break;

        }

        intent.putExtra(MainActivity.SCAN_FILTER, filter);
        startActivityForResult(intent, SCAN_REQUEST);
    }

    private void connect() {
        if (UCubeAPI.getConnexionManager().isConnected()) {
            updateConnectionUI(DEVICE_CONNECTED);
            return;
        }

        UIUtils.showProgress(
                this,
                getString(R.string.connect_progress),
                true,
                dialog -> UCubeAPI.getConnexionManager().cancelConnection()
        );

        int timeout = Integer.parseInt(connectionTimeoutEditText.getText().toString());
        UCubeAPI.getConnexionManager().connect(
                timeout * 1000,
                3,
                new ConnectionListener() {
                    @Override
                    public void onConnectionFailed(ConnectionStatus status, int error) {
                        Log.e(TAG, "connection failed status: "+ status);
                        runOnUiThread(() -> {
                            UIUtils.hideProgressDialog();

                            UIUtils.showMessageDialog(MainActivity.this,
                                    getString(R.string.connect_failed, status.name(), error));
                        });
                    }

                    @Override
                    public void onConnectionSuccess() {
                        Log.e(TAG,"connection success");
                        runOnUiThread(() -> {
                            UIUtils.hideProgressDialog();

                            UIUtils.showMessageDialog(MainActivity.this,
                                    getString(R.string.connect_success));

                            updateConnectionUI(DEVICE_CONNECTED);
                        });
                    }

                    @Override
                    public void onConnectionCancelled() {
                        Log.e(TAG,"connection cancelled");
                        runOnUiThread(() -> {
                            UIUtils.hideProgressDialog();
                            Toast.makeText(MainActivity.this, getString(R.string.connection_cancelled),
                                    Toast.LENGTH_LONG).show();
                        });
                    }
                });
    }

    private void disconnect() {

        if (!UCubeAPI.getConnexionManager().isConnected()) {
            updateConnectionUI(DEVICE_NOT_CONNECTED);
            return;
        }

        UIUtils.showProgress(this, getString(R.string.disconnect_progress));
        UCubeAPI.getConnexionManager().disconnect(status -> runOnUiThread(() -> {
            Log.d(TAG,"disconnection status:" + status);
            UIUtils.hideProgressDialog();
            if(status)
                updateConnectionUI(DEVICE_NOT_CONNECTED);

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
                runOnUiThread(() -> progressDlg.setMessage(getString(R.string.progress, state.name())));
            }

            @Override
            public void onFinish(boolean status, Object... params) {
                runOnUiThread(() -> {
                    progressDlg.dismiss();

                    Log.d(TAG, "mdm register status: " + status);
                    if (status) {
                        Toast.makeText(MainActivity.this,
                                getString(R.string.register_success), Toast.LENGTH_SHORT).show();
                    } else {
                        UIUtils.showMessageDialog(MainActivity.this, getString(R.string.register_failed));
                    }

                    updateMDMUI(status ? MDMState.DEVICE_REGISTERED : MDMState.DEVICE_NOT_REGISTERED);
                });
            }
        });
    }

    private void mdmGetConfig() {
        final ProgressDialog progressDlg = UIUtils.showProgress(this, getString(R.string.get_config_progress));
        UCubeAPI.mdmGetConfig(new UCubeLibMDMServiceListener() {
            @Override
            public void onProgress(ServiceState state) {
                runOnUiThread(() ->  progressDlg.setMessage(getString(R.string.progress, state.name())));
            }

            @Override
            public void onFinish(boolean status, Object... params) {
                runOnUiThread(() -> {
                    progressDlg.dismiss();
                    Log.d(TAG, "mdm get config status: " + status);

                    if (status && params!= null && params.length > 0 && params[0] instanceof List<?>) {
                        DeviceConfigDialogFragment dlg = new DeviceConfigDialogFragment();
                        dlg.init((List<Config>) params[0]);
                        dlg.show(MainActivity.this.getSupportFragmentManager(), DeviceConfigDialogFragment.class.getSimpleName());
                    }
                });
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
                                                runOnUiThread(() ->progressDlg.setMessage(getString(R.string.progress, state.name())));
                                            }

                                            @Override
                                            public void onFinish(boolean status, Object... params) {
                                                runOnUiThread(() -> {
                                                    progressDlg.dismiss();

                                                    if (status && params != null && params.length > 1) {
                                                        List<BinaryUpdate> updateList = null;

                                                        if (params[1] instanceof List) {
                                                            updateList = (List<BinaryUpdate>) params[1];
                                                        }

                                                        if (updateList == null || updateList.size() == 0) {
                                                            Toast.makeText(MainActivity.this,
                                                                    getString(R.string.ucube_up_to_date), Toast.LENGTH_SHORT).show();
                                                        } else {

                                                            CheckUpdateResultDialog dlg = new CheckUpdateResultDialog();

                                                            List<BinaryUpdate> finalUpdateList = updateList;

                                                            dlg.init(
                                                                    MainActivity.this,
                                                                    updateList,
                                                                    (dialog, which) -> showBinUpdateListDialog(finalUpdateList)
                                                            );
                                                            dlg.show(
                                                                    MainActivity.this.getSupportFragmentManager(),
                                                                    CheckUpdateResultDialog.class.getSimpleName()
                                                            );
                                                        }

                                                    } else {
                                                        UIUtils.showMessageDialog(MainActivity.this, getString(R.string.check_update_failed));
                                                    }
                                                });
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
                    runOnUiThread(() ->progressDlg.setMessage(getString(R.string.progress, state.name())));
                }

                @Override
                public void onFinish(boolean status, Object... params) {
                    runOnUiThread(() -> {
                        progressDlg.dismiss();

                        Toast.makeText(getApplicationContext(),
                                status ? getString(R.string.update_success) : getString(R.string.update_failed),
                                Toast.LENGTH_LONG
                        ).show();
                    });
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
                runOnUiThread(() -> progressDlg.setMessage(getString(R.string.progress, state.name())));
            }

            @Override
            public void onFinish(boolean status, Object... params) {
                runOnUiThread(() -> {
                    progressDlg.dismiss();

                    Toast.makeText(
                            MainActivity.this,
                            status ? getString(R.string.send_log_success) : getString(R.string.send_log_failed),
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }

    private void displayHelloWorld() {
        final ProgressDialog progressDlg = UIUtils.showProgress(this, getString(R.string.display_msg));
        DisplayMessageCommand displayMessageCommand = new DisplayMessageCommand("Hello world");
       // displayMessageCommand.setTimeout(2);
        displayMessageCommand.setClearConfig((byte) 0x04);
        displayMessageCommand.setXPosition((byte) 0xFF);
        displayMessageCommand.setYPosition((byte) 0x00);

        displayMessageCommand.execute((event1, params1) -> runOnUiThread(() -> {
            switch (event1) {
                case PROGRESS:
                    Log.d(TAG,"message display progress state "+ ((RPCCommandStatus) params1[1]).name());
                    return;
                case FAILED:
                case CANCELLED:
                    Log.d(TAG,"display message : "+ event1);
                    UIUtils.showMessageDialog(this, getString(R.string.display_msg_failure));
                    break;

                case SUCCESS:
                    Log.d(TAG,"display message : "+ event1);
                    Toast.makeText(this, getString(R.string.display_msg_success), Toast.LENGTH_LONG).show();
                    break;
            }

            progressDlg.dismiss();
        }));

    }

    private void getInfo() {
        final int[] uCubeInfoTagList = {
                TAG_TERMINAL_PN,
                TAG_TERMINAL_SN,
                TAG_FIRMWARE_VERSION,
                TAG_EMV_ICC_CONFIG_VERSION,
                TAG_EMV_NFC_CONFIG_VERSION,
                TAG_TERMINAL_STATE,
                TAG_BATTERY_STATE,
                TAG_POWER_OFF_TIMEOUT,
                TAG_CONFIGURATION_MERCHANT_INTERFACE_LOCALE,
                TAG_SUPPORTED_LOCALE_LIST,
                TAG_EMVL1_CLESS_LIB_VERSION,
                TAG_USB_CAPABILITY,
                TAG_OS_VERSION,
                TAG_MPOS_MODULE_STATE,
                TAG_TST_LOOPBACK_VERSION,
                TAG_AGNOS_LIB_VERSION,
                TAG_ACE_LAYER_VERSION,
                TAG_GPI_VERSION,
                TAG_EMVL3_VERSION,
                TAG_PCI_PED_VERSION,
                TAG_PCI_PED_CHECKSUM,
                TAG_EMV_L1_CHECKSUM,
                TAG_BOOT_LOADER_CHECKSUM,
                TAG_EMV_L2_CHECKSUM,
                TAG_BLE_FIRMWARE_VERSION,
                TAG_RESOURCE_FILE_VERSION,
                TAG_FB_CHARGING_STATUS,
                TAG_FC_SPEED_MODE,
                TAG_F3_BUILD_CONFIGURATION,
                TAG_FD_NON_SECURE_FIRMWARE_VERSION,
                TAG_E7_NFC_CARD_DETECT_CONFIGURATION,
                TAG_CF_ENHANCED_SRED_CONFIGURATION,
                TAG_FULL_SVPP_IDENTIFICATION,
        };

        UIUtils.showProgress(this, getString(R.string.get_info),false);

        new GetInfosCommand(uCubeInfoTagList).execute((event1, params1) -> runOnUiThread(() -> {
            switch (event1) {
                case PROGRESS:
                    Log.d(TAG,"Get info state "+ ((RPCCommandStatus) params1[1]).name());
                    break;
                case FAILED:
                case CANCELLED:
                    Log.d(TAG,"get info : "+ event1);
                    retrieveTagInfoOneByOne(uCubeInfoTagList);
                    return;

                case SUCCESS:
                    Log.d(TAG,"get info : "+ event1);
                    DeviceInfos deviceInfos = new DeviceInfos(((GetInfosCommand) params1[0]).getResponseData());
                    onDeviceInfoRetrieved(deviceInfos);
                    break;
            }
        }));
    }

    private void onDeviceInfoRetrieved(DeviceInfos deviceInfos) {
        runOnUiThread(() -> {
            UIUtils.hideProgressDialog();
            FragmentManager fm = MainActivity.this.getSupportFragmentManager();
            GetInfoDialog Dialog = new GetInfoDialog(deviceInfos, ytProduct);
            Dialog.show(fm, "GET_INFO");
        });
    }
    Queue<Integer> remainingTags = new ConcurrentLinkedQueue<>();
    List<byte[]> tlvParts = new ArrayList<>();
    private void retrieveTagInfoOneByOne(int[] tagList) {
        for (int tag: tagList)
            remainingTags.add(tag);

        retrieveInfoRemainingTags();
    }

    private void retrieveInfoRemainingTags() {
        if(remainingTags == null || remainingTags.isEmpty()) {
            onDeviceInfoRetrieved(null);
            return;
        }

        Integer tag = remainingTags.poll();
        Log.d(TAG, "Retrieve Tag " + tag);

        GetInfosCommand cmd = new GetInfosCommand(tag);
        cmd.execute((event, params) -> {
            switch (event) {
                case SUCCESS:
                    tlvParts.add(((GetInfosCommand) params[0]).getResponseData());
                case FAILED:
                case CANCELLED:
                    Log.e(TAG, "Retrieve tag "+ event);

                    if(remainingTags.isEmpty()) {
                        int offset = 0;
                        byte[] tlv = new byte[MAX_RPC_PACKET_SIZE];

                        for (byte[] part: tlvParts) {
                            System.arraycopy(part, 0, tlv, offset, part.length);
                            offset += part.length;
                        }

                        byte[] tmp = new byte[offset];
                        System.arraycopy(tlv, 0, tmp, 0, offset);

                        DeviceInfos deviceInfos = new DeviceInfos(tmp);
                        onDeviceInfoRetrieved(deviceInfos);
                    }else
                        retrieveInfoRemainingTags();
                    break;
            }
        });
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
            Log.d(TAG,"set power off timeout : "+ event1);
            if(event1 == TaskEvent.FAILED) {
                Toast.makeText(this, "Set power off timeout failed! ", Toast.LENGTH_LONG).show();
            } else if(event1 == TaskEvent.SUCCESS) {
                Toast.makeText(this, "Set power off timeout SUCCESS! ", Toast.LENGTH_LONG).show();
            }
            progressDlg.dismiss();
        }));
    }

    private void setLocale() {

        UIUtils.showProgress(MainActivity.this, getString(R.string.get_supported_locales));
        UCubeAPI.getSupportedLocaleList(new UCubeLibTaskListener() {
            @Override
            public void onProgress(UCubeLibState uCubeLibState) {
            }

            @Override
            public void onFinish(boolean status, Object... params) {

                runOnUiThread(() -> {

                    if (!status || params == null || params.length == 0) {
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
                        Log.d(TAG,"locale : " + locales.get(i));
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

                                            runOnUiThread(() -> {
                                                UIUtils.hideProgressDialog();

                                                if (status1) {
                                                    Toast.makeText(MainActivity.this, getString(R.string.set_locale_success), Toast.LENGTH_LONG).show();
                                                } else {
                                                    UIUtils.showMessageDialog(MainActivity.this, getString(R.string.set_locale_failed));
                                                }
                                            });
                                        }
                                    }
                            ));
                });
            }
        });
    }

    private void enterSecureSession() {
        final ProgressDialog progressDlg = UIUtils.showProgress(this, getString(R.string.enter_secure_session));

        new EnterSecureSessionCommand().execute((event1, params1) -> runOnUiThread(() ->  {
            switch (event1) {
                case PROGRESS:
                    Log.d(TAG, "progress state " + ((RPCCommandStatus) params1[1]).name());
                    return;
                case FAILED:
                case CANCELLED:
                    Log.d(TAG, "enter secure session  : " + event1);
                    UIUtils.showMessageDialog(this, getString(R.string.enter_secure_session_failure));
                    break;

                case SUCCESS:
                    Log.d(TAG, "enter secure session : " + event1);
                    Toast.makeText(this, getString(R.string.enter_secure_session_success), Toast.LENGTH_LONG).show();
                    break;
            }
            progressDlg.dismiss();
        }));
    }

    private void exitSecureSession() {
        final ProgressDialog progressDlg = UIUtils.showProgress(this, getString(R.string.exit_secure_session));

        new ExitSecureSessionCommand().execute((event1, params1) -> runOnUiThread(() ->  {
            switch (event1) {
                case PROGRESS:
                    Log.d(TAG, "progress state " + ((RPCCommandStatus) params1[1]).name());
                    return;
                case FAILED:
                case CANCELLED:
                    Log.d(TAG, "exit secure session  : " + event1);
                    UIUtils.showMessageDialog(this, getString(R.string.exit_secure_session_failure));
                    break;

                case SUCCESS:
                    Log.d(TAG, "exit secure session : " + event1);
                    Toast.makeText(this, getString(R.string.exit_secure_session_success), Toast.LENGTH_LONG).show();
                    break;
            }
            progressDlg.dismiss();
        }));
    }

    private void getTerminalState() {
        final ProgressDialog progressDlg = UIUtils.showProgress(this, getString(R.string.get_terminal_state));

        new GetInfosCommand(TAG_TERMINAL_STATE).execute((event1, params1) -> runOnUiThread(() ->  {
            Log.d(TAG,"Get terminal state  : "+ event1);
            switch (event1) {
                case PROGRESS:
                    break;
                case FAILED:
                case CANCELLED:
                    progressDlg.dismiss();
                    UIUtils.showMessageDialog(MainActivity.this, getString(R.string.get_terminal_state_failure));
                    return;

                case SUCCESS:
                    progressDlg.dismiss();

                    DeviceInfos deviceInfos = new DeviceInfos(((GetInfosCommand) params1[0]).getResponseData());

                    if(deviceInfos.getTerminalState() == null) // data are null in secured mode because the response is ciphered
                        Toast.makeText(this, "Terminal State: SECURED", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(this, "Terminal State: "+ deviceInfos.getTerminalState(), Toast.LENGTH_LONG).show();
                    break;
            }
        }));
    }

    private void sendCipheredPayload() {

        //todo enter secure session
        RPCCommand cmd = new RPCCommand();
        cmd.setInputSecurityMode(SecurityMode.SIGNED_CIPHERED);
        cmd.setOutputSecurityMode(SecurityMode.SIGNED);
        cmd.setPayload(Tools.hexStringToByteArray("020000000202462003"));
        cmd.execute((event1, params1) -> {
            switch (event1) {
                case PROGRESS:
                    Log.d(TAG, "progress state " + ((RPCCommandStatus) params1[1]).name());
                    return;
                case FAILED:
                case CANCELLED:
                    Log.d(TAG, "unknown cmd  : " + event1);
                    break;

                case SUCCESS:
                    Log.d(TAG, "unknown cmd: " + event1);
                    break;
            }
        });

        //todo exit secure session
    }

    private void enableQuickMode() {
        final ProgressDialog progressDlg = UIUtils.showProgress(this, getString(R.string.setup_quick_mode));
        progressDlg.setCancelable(false);

        SetInfoFieldCommand setInfoFieldCommand = new SetInfoFieldCommand();
        setInfoFieldCommand.setMode(QUICK_MODE);
        setInfoFieldCommand.execute((event1, params1) -> runOnUiThread(() -> {
            if(event1 == TaskEvent.PROGRESS)
                return;

            progressDlg.dismiss();
        }));
    }

    private void enableSlowMode() {
        final ProgressDialog progressDlg = UIUtils.showProgress(this, getString(R.string.setup_slow_mode));
        progressDlg.setCancelable(false);

        SetInfoFieldCommand setInfoFieldCommand = new SetInfoFieldCommand();
        setInfoFieldCommand.setMode(0);
        setInfoFieldCommand.execute((event1, params1) -> runOnUiThread(() -> {
            if(event1 == TaskEvent.PROGRESS)
                return;

            progressDlg.dismiss();
        }));
    }

    private void doRemoteKeyInjection() {
        UIUtils.showProgress(MainActivity.this, getString(R.string.remote_keys_injection));

        UCubeAPI.updateKeys(Tools.hexStringToByteArray(TDES),
                Tools.hexStringToByteArray(TDES),
                new UCubeLibRKIServiceListener() {
                    @Override
                    public void onProgress(RKIServiceState state) {
                    }

                    @Override
                    public void onFinish(boolean status, Object... params) {
                        runOnUiThread(() -> {
                            UIUtils.hideProgressDialog();
                            if (!status)
                                UIUtils.showMessageDialog(MainActivity.this,
                                        getString(R.string.remote_keys_injection_failed));
                            else
                                UIUtils.showMessageDialog(MainActivity.this,
                                        getString(R.string.remote_keys_injection_success));
                        });
                    }
                });
    }

    private ProgressDialog progressDlg;
    private int mYear, mMonth, mDay, mHour, mMinute;
    private void setRtc() {

        // Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, monthOfYear, dayOfMonth) -> {

                    //save the selected date
                    mYear = year;
                    mMonth = monthOfYear;
                    mDay = dayOfMonth;

                    // Get Current Time
                    final Calendar c1 = Calendar.getInstance();
                    mHour = c1.get(Calendar.HOUR_OF_DAY);
                    mMinute = c1.get(Calendar.MINUTE);

                    // Launch Time Picker Dialog
                    TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this,
                            new TimePickerDialog.OnTimeSetListener() {

                                @Override
                                public void onTimeSet(TimePicker view, int hourOfDay,
                                                      int minute) {

                                    //save selected time
                                    mHour = hourOfDay;
                                    mMinute = minute;

                                    //ui
                                    runOnUiThread(() -> progressDlg = UIUtils.showProgress(MainActivity.this, getString(R.string.set_rtc)));
                                     //start enter secure session
                                    new EnterSecureSessionCommand().execute((event, params) -> {
                                        switch (event) {
                                            case PROGRESS:
                                                return;
                                            case FAILED:
                                            case CANCELLED:
                                                Log.e(TAG, "set rtc : " + event);
                                                runOnUiThread(() -> UIUtils.showMessageDialog(MainActivity.this,
                                                        getString(R.string.set_rtc_failed)));
                                                return;

                                            case SUCCESS:
                                                Calendar c = Calendar.getInstance();
                                                c.set(mYear, mMonth, mDay, mHour, minute);
                                                RTCSetCommand rtcSetCommand = new RTCSetCommand(c.getTime());
                                                rtcSetCommand.execute((event1, param1) -> {
                                                    switch (event1) {
                                                        case PROGRESS:
                                                            return;
                                                        case FAILED:
                                                        case CANCELLED:
                                                            Log.e(TAG, "set rtc : " + event1);
                                                            runOnUiThread(() ->  {
                                                                progressDlg.dismiss();
                                                                UIUtils.showMessageDialog(MainActivity.this, getString(R.string.set_rtc_failed));
                                                            });
                                                            break;

                                                        case SUCCESS:
                                                            new ExitSecureSessionCommand().execute((event2, params2) -> {
                                                                switch (event2) {
                                                                    case PROGRESS:
                                                                        return;
                                                                    case FAILED:
                                                                    case CANCELLED:
                                                                        Log.e(TAG, "set rtc : " + event2);
                                                                        runOnUiThread(() -> UIUtils.showMessageDialog(MainActivity.this,
                                                                                getString(R.string.set_rtc_failed)));
                                                                        return;

                                                                    case SUCCESS:
                                                                        runOnUiThread(() -> {
                                                                            progressDlg.dismiss();
                                                                            Toast.makeText(MainActivity.this, getString(R.string.get_rtc_success), Toast.LENGTH_LONG).show();
                                                                        });
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
                            }, mHour, mMinute, false);
                    timePickerDialog.show();
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    public static String formatDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm", Locale.ENGLISH);
        return dateFormat.format(date);
    }

    private void getRtc() {
        final ProgressDialog progressDlg = UIUtils.showProgress(MainActivity.this, getString(R.string.get_rtc));

        RTCGetCommand rtcGetCommand = new RTCGetCommand();
        rtcGetCommand.execute((event, params) -> {
            switch (event) {
                case PROGRESS:
                    return;
                case FAILED:
                case CANCELLED:
                    Log.e(TAG, "get rtc : " + event);
                    UIUtils.showMessageDialog(MainActivity.this, getString(R.string.get_rtc_failed));
                    break;

                case SUCCESS:
                    Date deviceDate = rtcGetCommand.getDate();
                    Log.d(TAG, "get rtc success date : " + formatDate(deviceDate));
                    runOnUiThread(()->
                            Toast.makeText(MainActivity.this, getString(R.string.get_rtc_success, formatDate(deviceDate)), Toast.LENGTH_LONG).show());
                    break;
            }
            progressDlg.dismiss();
        });

    }

    private void echo(String data) {
        final ProgressDialog progressDlg = UIUtils.showProgress(MainActivity.this, getString(R.string.echo));

        EchoCommand cmd = new EchoCommand();
       cmd.setData(Tools.hexStringToByteArray(data));
       cmd.execute((event, params) -> {
           switch (event) {
               case PROGRESS:
                   return;
               case FAILED:
               case CANCELLED:
                   Log.e(TAG, "echo cm : " + event);
                   runOnUiThread(()->
                           UIUtils.showMessageDialog(MainActivity.this, getString(R.string.echo_failed)));
                   break;

               case SUCCESS:
                   String echoResponse = Tools.bytesToHex(cmd.response.getData());
                   Log.d(TAG, "echo success response : " + echoResponse);
                   runOnUiThread(()->
                           Toast.makeText(MainActivity.this, getString(R.string.echo_success, echoResponse), Toast.LENGTH_LONG).show());
                   break;
           }
           progressDlg.dismiss();
       });
    }

    private void getPubKey() {
        UIUtils.showProgress(MainActivity.this, getString(R.string.get_public_key));

        InstallForLoadKeyCommand cmd = new InstallForLoadKeyCommand();
        cmd.execute((event, params) -> {
            switch (event) {
                case PROGRESS:
                    return;
                case FAILED:
                case CANCELLED:
                    Log.e(TAG, "get pub key cm : " + event);
                    runOnUiThread(()->
                            UIUtils.showMessageDialog(MainActivity.this, getString(R.string.get_pub_key_failed)));
                    break;

                case SUCCESS:
                    Log.d(TAG, "ca prod : " + Tools.bytesToHex(cmd.getCaProd()));
                    Log.d(TAG, "k key : " + Tools.bytesToHex(cmd.getkKek()));
                    Log.d(TAG, "KSN DUKPT Data : " + Tools.bytesToHex(cmd.getiKsnDukpt()));
                    Log.d(TAG, "KSN DUKPT PIN : " + Tools.bytesToHex(cmd.getiKsnPin()));

                    runOnUiThread(()->
                            Toast.makeText(MainActivity.this, getString(R.string.get_pub_key_success), Toast.LENGTH_LONG).show());
                    break;
            }
            UIUtils.hideProgressDialog();
        });
    }

    private void getKeyInfo() {
        final ProgressDialog progressDlg = UIUtils.showProgress(MainActivity.this, getString(R.string.get_key_info));

        String keySlotId = keySlotEditText.getText().toString();
        if(keySlotId.isEmpty()) {
            runOnUiThread(()->
                    UIUtils.showMessageDialog(MainActivity.this, "key slot can not be empty"));
            return;
        }

        GetKeyInfoCommand cmd = new GetKeyInfoCommand(keySlotId);
        cmd.execute((event, params) -> {
            switch (event) {
                case PROGRESS:
                    return;
                case FAILED:
                case CANCELLED:
                    Log.e(TAG, "get key info : " + event);
                    runOnUiThread(()->
                            UIUtils.showMessageDialog(MainActivity.this, getString(R.string.get_key_info_failed)));
                    break;

                case SUCCESS:
                    Log.d(TAG, "key Info : " + cmd.keyInfo);

                    runOnUiThread(()->
                            Toast.makeText(MainActivity.this,
                                    getString(R.string.get_key_info_success, cmd.keyInfo),
                                    Toast.LENGTH_LONG).show());
                    break;
            }
            progressDlg.dismiss();
        });
    }

    private void reboot() {
        UIUtils.showProgress(MainActivity.this, getString(R.string.reboot));

        RebootCommand rebootCommand = new RebootCommand();
        rebootCommand.execute((event, params) -> {
            switch (event) {
                case PROGRESS:
                    return;
                case FAILED:
                case CANCELLED:
                case SUCCESS:
                    Log.e(TAG, "reboot command : " + event);
                    runOnUiThread(() -> {
                        UIUtils.hideProgressDialog();
                        UIUtils.showMessageDialog(MainActivity.this, getString(R.string.reboot_event, event));
                    });
                    break;
            }
        });
    }

    private void reset() {
        UIUtils.showProgress(MainActivity.this, getString(R.string.reset));

        ResetCommand resetCommand = new ResetCommand();
        resetCommand.execute((event, params) -> {
            switch (event) {
                case PROGRESS:
                    return;
                case FAILED:
                case CANCELLED:
                case SUCCESS:
                    Log.e(TAG, "reset command : " + event);
                    runOnUiThread(() -> {
                        UIUtils.hideProgressDialog();

                        UIUtils.showMessageDialog(MainActivity.this, getString(R.string.reset_event, event));
                    });
                    break;
            }
        });
    }
}
