/*
 * ============================================================================
 *
 * Copyright (c) 2024 YouTransactor
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

import static com.youTransactor.uCube.connexion.ConnectionService.ConnectionManagerType.SECURE_SERVICE;
import static com.youTransactor.uCube.connexion.ConnectionService.ConnectionManagerType.SOCKET;
import static com.youTransactor.uCube.connexion.ConnectionService.ConnectionManagerType.SOCKET_JSON;
import static com.youTransactor.uCube.rpc.Constants.ICC_READER;
import static com.youTransactor.uCube.rpc.Constants.MAX_RPC_PACKET_SIZE;
import static com.youTransactor.uCube.rpc.Constants.QUICK_MODE;
import static com.youTransactor.uCube.rpc.Constants.TAG_ACE_LAYER_VERSION;
import static com.youTransactor.uCube.rpc.Constants.TAG_AGNOS_LIB_VERSION;
import static com.youTransactor.uCube.rpc.Constants.TAG_BATTERY_STATE;
import static com.youTransactor.uCube.rpc.Constants.TAG_BLE_FIRMWARE_VERSION;
import static com.youTransactor.uCube.rpc.Constants.TAG_BOOT_LOADER_CHECKSUM;
import static com.youTransactor.uCube.rpc.Constants.TAG_CF_ENHANCED_SRED_CONFIGURATION;
import static com.youTransactor.uCube.rpc.Constants.TAG_CONFIGURATION_MERCHANT_INTERFACE_LOCALE;
import static com.youTransactor.uCube.rpc.Constants.TAG_E7_NFC_CARD_DETECT_CONFIGURATION;
import static com.youTransactor.uCube.rpc.Constants.TAG_EMVL1_CLESS_LIB_VERSION;
import static com.youTransactor.uCube.rpc.Constants.TAG_EMVL3_VERSION;
import static com.youTransactor.uCube.rpc.Constants.TAG_EMV_ICC_CONFIG_VERSION;
import static com.youTransactor.uCube.rpc.Constants.TAG_EMV_L1_CHECKSUM;
import static com.youTransactor.uCube.rpc.Constants.TAG_EMV_L2_CHECKSUM;
import static com.youTransactor.uCube.rpc.Constants.TAG_EMV_NFC_CONFIG_VERSION;
import static com.youTransactor.uCube.rpc.Constants.TAG_F3_BUILD_CONFIGURATION;
import static com.youTransactor.uCube.rpc.Constants.TAG_FB_CHARGING_STATUS;
import static com.youTransactor.uCube.rpc.Constants.TAG_FC_SPEED_MODE;
import static com.youTransactor.uCube.rpc.Constants.TAG_FD_NON_SECURE_FIRMWARE_VERSION;
import static com.youTransactor.uCube.rpc.Constants.TAG_FIRMWARE_VERSION;
import static com.youTransactor.uCube.rpc.Constants.TAG_FULL_SVPP_IDENTIFICATION;
import static com.youTransactor.uCube.rpc.Constants.TAG_GPI_VERSION;
import static com.youTransactor.uCube.rpc.Constants.TAG_INTEGRITY_CHECK_TIME;
import static com.youTransactor.uCube.rpc.Constants.TAG_MPOS_MODULE_STATE;
import static com.youTransactor.uCube.rpc.Constants.TAG_NFC_INFOS;
import static com.youTransactor.uCube.rpc.Constants.TAG_OS_VERSION;
import static com.youTransactor.uCube.rpc.Constants.TAG_PCI_PED_CHECKSUM;
import static com.youTransactor.uCube.rpc.Constants.TAG_PCI_PED_VERSION;
import static com.youTransactor.uCube.rpc.Constants.TAG_POWER_OFF_TIMEOUT;
import static com.youTransactor.uCube.rpc.Constants.TAG_RESOURCE_FILE_VERSION;
import static com.youTransactor.uCube.rpc.Constants.TAG_SECURE_MOD;
import static com.youTransactor.uCube.rpc.Constants.TAG_SUPPORTED_LOCALE_LIST;
import static com.youTransactor.uCube.rpc.Constants.TAG_SYSTEM_FAILURE_LOG_RECORD_1;
import static com.youTransactor.uCube.rpc.Constants.TAG_TERMINAL_PN;
import static com.youTransactor.uCube.rpc.Constants.TAG_TERMINAL_SN;
import static com.youTransactor.uCube.rpc.Constants.TAG_TERMINAL_STATE;
import static com.youTransactor.uCube.rpc.Constants.TAG_TST_LOOPBACK_VERSION;
import static com.youTransactor.uCube.rpc.Constants.TAG_USB_CAPABILITY;
import static com.youtransactor.sampleapp.MainActivity.State.DEVICE_CONNECTED;
import static com.youtransactor.sampleapp.MainActivity.State.DEVICE_NOT_CONNECTED;
import static com.youtransactor.sampleapp.MainActivity.State.NO_DEVICE_SELECTED;
import static com.youtransactor.sampleapp.SetupActivity.YT_PRODUCT;
import static android.view.View.GONE;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.google.gson.Gson;
import com.jps.secureService.api.entity.ViewIdentifier;
import com.youTransactor.uCube.TaskEvent;
import com.youTransactor.uCube.Tools;
import com.youTransactor.uCube.api.UCubeAPI;
import com.youTransactor.uCube.api.UCubeLibMDMServiceListener;
import com.youTransactor.uCube.api.UCubeLibRKIServiceListener;
import com.youTransactor.uCube.api.UCubeLibState;
import com.youTransactor.uCube.api.UCubeLibTaskListener;
import com.youTransactor.uCube.api.YTMPOSProduct;
import com.youTransactor.uCube.connexion.BatteryLevelListener;
import com.youTransactor.uCube.connexion.ConnectionListener;
import com.youTransactor.uCube.connexion.ConnectionService;
import com.youTransactor.uCube.connexion.ConnectionState;
import com.youTransactor.uCube.connexion.ConnectionStateChangeListener;
import com.youTransactor.uCube.connexion.ConnectionStatus;
import com.youTransactor.uCube.connexion.IConnexionManager;
import com.youTransactor.uCube.connexion.SVPPRestartListener;
import com.youTransactor.uCube.connexion.SecureServiceConnectionManager;
import com.youTransactor.uCube.connexion.SocketConnectionManager;
import com.youTransactor.uCube.connexion.SocketJSONConnectionManager;
import com.youTransactor.uCube.connexion.UCubeDevice;
import com.youTransactor.uCube.log.LogManager;
import com.youTransactor.uCube.mdm.BinaryUpdate;
import com.youTransactor.uCube.mdm.Config;
import com.youTransactor.uCube.mdm.ServiceState;
import com.youTransactor.uCube.rki.RKIServiceState;
import com.youTransactor.uCube.rpc.DeviceInfos;
import com.youTransactor.uCube.rpc.LostPacketListener;
import com.youTransactor.uCube.rpc.RPCCommandStatus;
import com.youTransactor.uCube.rpc.RPCCommunicationErrorListener;
import com.youTransactor.uCube.rpc.command.DisplayMessageCommand;
import com.youTransactor.uCube.rpc.command.EchoCommand;
import com.youTransactor.uCube.rpc.command.EnterSecureSessionCommand;
import com.youTransactor.uCube.rpc.command.ExitSecureSessionCommand;
import com.youTransactor.uCube.rpc.command.GetInfosCommand;
import com.youTransactor.uCube.rpc.command.GetKeyInfoCommand;
import com.youTransactor.uCube.rpc.command.InstallForLoadKeyCommand;
import com.youTransactor.uCube.rpc.command.RTCGetCommand;
import com.youTransactor.uCube.rpc.command.RebootCommand;
import com.youTransactor.uCube.rpc.command.ResetCommand;
import com.youTransactor.uCube.rpc.command.SetInfoFieldCommand;
import com.youtransactor.sampleapp.connexion.DeviceScanActivity;
import com.youtransactor.sampleapp.connexion.ListPairedUCubeScanner;
import com.youtransactor.sampleapp.connexion.ListPairedUCubeTouchScanner;
import com.youtransactor.sampleapp.connexion.YTKeyScanner;
import com.youtransactor.sampleapp.connexion.YTSOMScanner;
import com.youtransactor.sampleapp.emvParamUpdate.EmvParamEnableDisableAIDActivity;
import com.youtransactor.sampleapp.emvParamUpdate.EmvParamUpdateActivity;
import com.youtransactor.sampleapp.features.Disconnect;
import com.youtransactor.sampleapp.features.SdseSession;
import com.youtransactor.sampleapp.features.SetIntegrityCheckTime;
import com.youtransactor.sampleapp.features.SetRtc;
import com.youtransactor.sampleapp.localUpdate.LocalUpdateActivity;
import com.youtransactor.sampleapp.mdm.CheckUpdateResultDialog;
import com.youtransactor.sampleapp.mdm.DeviceConfigDialogFragment;
import com.youtransactor.sampleapp.payment.Localization;
import com.youtransactor.sampleapp.payment.PaymentActivity;
import com.youtransactor.sampleapp.rpc.GetInfoDialog;
import com.youtransactor.sampleapp.test.TestActivity;
import com.youtransactor.sampleapp.transactionView.WaitCard_Dte;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import com.youTransactor.uCube.info.log.LogLight;
import com.youTransactor.uCube.info.log.LogLightService;

public class MainActivity extends AppCompatActivity implements BatteryLevelListener,
        SVPPRestartListener, LostPacketListener, RPCCommunicationErrorListener {

    private static final String TAG = MainActivity.class.getName();
    private static final String SHARED_PREF_NAME = "main";

    public static final int SCAN_REQUEST = 1234;

    public static final String INTENT_EXTRA_DEVICE_NAME = "DEVICE_NAME";
    public static final String INTENT_EXTRA_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private static final String TDES = "74646573";

    enum State {NO_DEVICE_SELECTED, DEVICE_NOT_CONNECTED, DEVICE_CONNECTED}

    enum MDMState {IDLE, DEVICE_REGISTERED, DEVICE_NOT_REGISTERED}

    private View actionPanel;
    private TextView uCubeNameTv;
    private TextView uCubeAddressTv;
    private EditText connectionTimeoutEditText;
    private ImageButton forgetButton;
    private Button connectBtn;
    private Button disconnectBtn;
    private Button mdmRegisterBtn;
    private Button mdmCheckUpdateBtn;
    private Button mdmSendLogBtn;
    private Button mdmGetConfigBtn;
    private Button setPan;
    private Button setCvv;
    private Button setExpDate;
    private Button dtebut;
    private Button testPinbut;
    private Button contactCertifBut;
    private YTProduct ytProduct;
    private boolean checkOnlyFirmwareVersion = false;
    private boolean forceUpdate = false;
    private final Queue<Integer> remainingTags = new ConcurrentLinkedQueue<>();
    private final List<byte[]> tlvParts = new ArrayList<>();

    private final ConnectionStateChangeListener connectionStateChangeListener = newState -> runOnUiThread(
            () -> updateConnectionUI(newState == ConnectionState.CONNECTED ? DEVICE_CONNECTED : DEVICE_NOT_CONNECTED)
    );

    @Override
    public void onLevelChanged(int newLevel) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, String.format(getString(R.string.battery_level), newLevel), Toast.LENGTH_LONG).show());
        Log.d(TAG, String.format(getString(R.string.battery_level), newLevel));
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
        runOnUiThread(() -> {
            UIUtils.hideProgressDialog();
            UIUtils.showMessageDialog(MainActivity.this, getString(R.string.rpc_com_issue));
        });
        Log.d(TAG, "Notification of RPC Com issue !");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        YTMPOSProduct lastProduct = null;
        ConnectionService.ConnectionManagerType lastConnectionType;
        String connectionTypeName;
        requestWindowFeature(Window.FEATURE_ACTION_BAR);

        setContentView(R.layout.activity_main);
        SharedPreferences setupSharedPref = getSharedPreferences(
                SetupActivity.SETUP_SHARED_PREF_NAME, Context.MODE_PRIVATE);
        Localization.Localization_init(UCubeAPI.getContext());

        if (getIntent() != null && getIntent().hasExtra(YT_PRODUCT)) {
            ytProduct = YTProduct.valueOf(getIntent().getStringExtra(YT_PRODUCT));
        }

        if (ytProduct == null) {
            finish();
            return;
        }
        if (UCubeAPI.getYtmposProduct() != null) {
            lastProduct = UCubeAPI.getYtmposProduct();
        }

        switch (ytProduct) {
            case uCubeTouch:
                UCubeAPI.setYTmPOSProduct(YTMPOSProduct.uCube_touch);
                break;
            case YT_Key:
                UCubeAPI.setYTmPOSProduct(YTMPOSProduct.YT_Key);
                break;
            case YT_SOM:
                UCubeAPI.setYTmPOSProduct(YTMPOSProduct.YT_SOM);
                break;
            case AndroidPOS:
                UCubeAPI.setYTmPOSProduct(YTMPOSProduct.AndroidPOS);
                break;
            default:
                UCubeAPI.setYTmPOSProduct(YTMPOSProduct.uCube);
                break;
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(ytProduct.getName());
        }

        lastConnectionType = UCubeAPI.getConnexionManagerType();
        connectionTypeName = setupSharedPref.getString(SetupActivity.COMMUNICATION_TYPE_PREF_NAME, SECURE_SERVICE.name());
        ConnectionService.ConnectionManagerType currentConnexionType;
        try {
            /* protect again invalid connection type name */
            currentConnexionType = ConnectionService.ConnectionManagerType.valueOf(connectionTypeName);
        } catch (Exception e) {
            setupSharedPref.edit().remove(SetupActivity.COMMUNICATION_TYPE_PREF_NAME).apply();
            currentConnexionType = SECURE_SERVICE;
        }

        UCubeAPI.setConnexionManagerType(currentConnexionType);

        UCubeAPI.getConnexionManager().registerBatteryLevelChangeListener(this);
        UCubeAPI.registerSVPPRestartListener(this);
        UCubeAPI.registerLostPacketListener(this);
        UCubeAPI.registerRPCCommunicationErrorListener(this);
        UCubeAPI.getConnexionManager().registerConnectionStateListener(connectionStateChangeListener);

        initViews(setupSharedPref);

        if (lastProduct != null
                && (lastProduct != UCubeAPI.getYtmposProduct()
                || (lastConnectionType != null && lastConnectionType != currentConnexionType))) {
            /* Avoid connect with last address if another product is chosen or another connection mode */
            forgetDevice(lastProduct.toString());
        }

        selectedDeviceChanged(currentConnexionType == SECURE_SERVICE
                ? UCubeAPI.getConnexionManager().getDevice()
                : getDevice());
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (UCubeAPI.getConnexionManager().getDevice() == null) {
            updateConnectionUI(NO_DEVICE_SELECTED);
            updateMDMUI(MDMState.IDLE);
        } else {
            updateConnectionUI(UCubeAPI.getConnexionManager().isConnected()
                    ? DEVICE_CONNECTED
                    : DEVICE_NOT_CONNECTED);

            // if user app will use YT TMS use this to get MDM Manager state & then update UI
            updateMDMUI(UCubeAPI.isMdmManagerReady() ? MDMState.DEVICE_REGISTERED : MDMState.DEVICE_NOT_REGISTERED);
        }
    }

    @Override
    protected void onDestroy() {
        disconnect(false);
        UCubeAPI.unregisterSVPPRestartListener();
        UCubeAPI.unregisterLostPacketListener();
        UCubeAPI.unregisterRPCCommunicationErrorListener();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SCAN_REQUEST && data != null) {
            String deviceName = data.getStringExtra(INTENT_EXTRA_DEVICE_NAME);
            String deviceAddress = data.getStringExtra(INTENT_EXTRA_DEVICE_ADDRESS);

            Log.d(TAG, "device : " + deviceName + " : " + deviceAddress);

            setSelectedDevice(new UCubeDevice(deviceName, deviceAddress));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.getItem(0).setVisible(ConnectionService.getInstance().getConnectionManagerType() != SECURE_SERVICE);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_select_device) {
            selectDevice();
            return true;
        }

        if (item.getItemId() == android.R.id.home) {
            disconnect(false);
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initViews(SharedPreferences setupSharedPref) {
        forgetButton = findViewById(R.id.forgetButton);
        forgetButton.setOnClickListener((v) -> forgetDevice());

        connectBtn = findViewById(R.id.connectBtn);
        connectBtn.setOnClickListener(v -> connect());
        connectBtn.setOnLongClickListener(v -> {
            Toast.makeText(this, "display connect timeout dialog", Toast.LENGTH_LONG).show();
            return true;
        });

        disconnectBtn = findViewById(R.id.disconnectBtn);
        disconnectBtn.setOnClickListener(v -> disconnect(true));

        actionPanel = findViewById(R.id.actionPanel);

        connectionTimeoutEditText = findViewById(R.id.connection_timeout_edit_text);

        uCubeNameTv = findViewById(R.id.ucube_name);
        uCubeAddressTv = findViewById(R.id.ucube_address);

        mdmRegisterBtn = findViewById(R.id.registerBtn);
        mdmRegisterBtn.setOnClickListener(v -> mdmRegister());

        mdmGetConfigBtn = findViewById(R.id.getConfigBtn);
        mdmGetConfigBtn.setOnClickListener(v -> mdmGetConfig());

        mdmCheckUpdateBtn = findViewById(R.id.checkUpdateBtn);
        mdmCheckUpdateBtn.setOnClickListener(v -> mdmCheckUpdate());

        mdmSendLogBtn = findViewById(R.id.sendLogBtn);
        mdmSendLogBtn.setOnClickListener(v -> mdmSendLogs());

        setPan = findViewById(R.id.setPanCvvDateBtn);
        setPan.setOnClickListener(v -> setPanCvvDate());

        dtebut = findViewById(R.id.dteButton);
        dtebut.setOnClickListener(v -> startdte());

        testPinbut = findViewById(R.id.testPinButton);
        testPinbut.setOnClickListener(v -> startTestPin());

        contactCertifBut = findViewById(R.id.contactCertifButton);
        contactCertifBut.setOnClickListener(v -> startcontactCertif());

        TextView versionFld = findViewById(R.id.version_name);
        versionFld.setText(getString(R.string.versionName, BuildConfig.VERSION_NAME));

        findViewById(R.id.get_terminal_state).setOnClickListener(v -> getTerminalState());
        findViewById(R.id.enter_secure_session).setOnClickListener(v -> enterSecureSession());
        findViewById(R.id.exit_secure_session).setOnClickListener(v -> exitSecureSession());
        findViewById(R.id.payBtn).setOnClickListener(v -> payment());
        Button displayBtn = findViewById(R.id.displayBtn);
        displayBtn.setOnClickListener(v -> displayHelloWorld());
        findViewById(R.id.getInfoBtn).setOnClickListener(v -> getInfo());
        Button echoBtn = findViewById(R.id.echo_btn);
        findViewById(R.id.get_sys_log).setOnClickListener(v -> getLogLight());
        // comment to test log light retrieval in the sample app
        findViewById(R.id.get_sys_log).setVisibility(GONE);
        echoBtn.setOnClickListener(v -> {
            final View customLayout = getLayoutInflater().inflate(R.layout.echo_msg_layout, null);
            final EditText echoData = customLayout.findViewById(R.id.echoDataFld);

            new AlertDialog.Builder(this)
                    .setTitle(R.string.echo_msg)
                    .setCancelable(true)
                    .setPositiveButton(R.string.ok, (dialog, whichButton) -> {
                        String msg = echoData.getText().toString();
                        echo(msg);
                    })
                    .setNegativeButton(R.string.cancel, (dialog, whichButton) -> dialog.dismiss())
                    .setView(customLayout)
                    .show();
        });
        Button powerTimeoutBtn = findViewById(R.id.powerTimeoutBtn);
        powerTimeoutBtn.setOnClickListener(v -> {
            final View customLayout = getLayoutInflater().inflate(R.layout.poweroff_timeout_dlg, null);
            final EditText timeoutFld = customLayout.findViewById(R.id.poweroff_timeout_fld);

            new AlertDialog.Builder(this)
                    .setTitle(R.string.power_off_timeout)
                    .setCancelable(true)
                    .setPositiveButton(R.string.ok, (dialog, whichButton) -> {
                        int timeout;
                        try {
                            timeout = Integer.parseInt(timeoutFld.getText().toString());
                        } catch (Exception e) {
                            timeout = -1;
                        }
                        if (timeout != 0 && (timeout > 255 || timeout < 32)) {
                            Toast.makeText(this, R.string.error_wrong_value, Toast.LENGTH_LONG).show();
                        } else {
                            powerOffTimeout(timeout);
                        }
                    })
                    .setNegativeButton(R.string.cancel, (dialog, whichButton) -> dialog.dismiss())
                    .setView(customLayout)
                    .show();
        });
        findViewById(R.id.set_locale).setOnClickListener(v -> setLocale());
        Button quickModeBtn = findViewById(R.id.quick_mode);
        quickModeBtn.setOnClickListener(v -> enableQuickMode());
        Button slowModeBtn = findViewById(R.id.slow_mode);
        slowModeBtn.setOnClickListener(v -> enableSlowMode());
        Button buzzer_on = findViewById(R.id.buzzer_on);
        buzzer_on.setOnClickListener(v -> BuzzerOn());
        Button buzzer_off = findViewById(R.id.buzzer_off);
        buzzer_off.setOnClickListener(v -> BuzzerOff());
        findViewById(R.id.get_rtc).setOnClickListener(v -> getRtc());
        findViewById(R.id.set_rtc).setOnClickListener(v -> askForDateAndSetRtc());
        findViewById(R.id.get_pub_key).setOnClickListener(v -> getPubKey());
        findViewById(R.id.get_key_info).setOnClickListener(v -> {
            final View customLayout = getLayoutInflater().inflate(R.layout.key_slot_dlg_layout, null);
            final EditText slot = customLayout.findViewById(R.id.key_slot_fld);

            new AlertDialog.Builder(this)
                    .setTitle(R.string.key_slot)
                    .setCancelable(true)
                    .setPositiveButton(R.string.ok, (dialog, whichButton) -> {
                        String msg = slot.getText().toString();
                        getKeyInfo(msg);
                    })
                    .setNegativeButton(R.string.cancel, (dialog, whichButton) -> dialog.dismiss())
                    .setView(customLayout)
                    .show();
        });

        findViewById(R.id.set_integrity_check_time).setOnClickListener(v -> askForTimeAndSetIntegrityCheckTime());

        findViewById(R.id.reboot).setOnClickListener(v -> reboot());
        findViewById(R.id.get_som_sys_log).setOnClickListener(
                v -> getSomSyslog());
        Button rkiButton = findViewById(R.id.rkiButton);
        rkiButton.setOnClickListener(v -> doRemoteKeyInjection());
        findViewById(R.id.localUpdateBtn).setOnClickListener(v -> localUpdate());
        findViewById(R.id.emvParamUpdBtn).setOnClickListener(v -> emvParamUpd());
        findViewById(R.id.emvParamEnableDisableAID).setOnClickListener(v -> emvParamEnableDisableAID());

        boolean testModeEnabled = setupSharedPref.getBoolean(SetupActivity.TEST_MODE_PREF_NAME, false);
        Button testBtn = findViewById(R.id.testBtn);
        testBtn.setVisibility(testModeEnabled ? View.VISIBLE : GONE);
        testBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, TestActivity.class)));

        boolean certifModeEnabled = setupSharedPref.getBoolean(SetupActivity.CERTIF_MODE_PREF_NAME, false);
        View dteLaout = findViewById(R.id.dteLayout);
        dteLaout.setVisibility(certifModeEnabled ? View.VISIBLE : GONE);
        View contactCertifLaout = findViewById(R.id.contactCertifLayout);
        contactCertifLaout.setVisibility(certifModeEnabled ? View.VISIBLE : GONE);
        if (ytProduct == YTProduct.AndroidPOS) {
            displayBtn.setVisibility(GONE);
            echoBtn.setVisibility(GONE);
            powerTimeoutBtn.setVisibility(GONE);
            quickModeBtn.setVisibility(GONE);
            slowModeBtn.setVisibility(GONE);
            mdmRegisterBtn.setVisibility(GONE);
            rkiButton.setVisibility(GONE);
        }
    }

    private void updateConnectionUI(State state) {
        if (state == NO_DEVICE_SELECTED) {
            actionPanel.setVisibility(GONE);
            disconnectBtn.setVisibility(GONE);
            connectBtn.setVisibility(GONE);
            forgetButton.setVisibility(GONE);
            displayDeviceInfos(null);
            return;
        }

        forgetButton.setVisibility(
                ConnectionService.getInstance().getConnectionManagerType() != SECURE_SERVICE
                        ? View.VISIBLE
                        : GONE);
        displayDeviceInfos(UCubeAPI.getConnexionManager().getDevice());

        switch (state) {
            case DEVICE_NOT_CONNECTED:
                actionPanel.setVisibility(GONE);
                connectBtn.setVisibility(View.VISIBLE);
                disconnectBtn.setVisibility(GONE);
                break;

            case DEVICE_CONNECTED:
                actionPanel.setVisibility(View.VISIBLE);
                connectBtn.setVisibility(GONE);
                disconnectBtn.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void displayDeviceInfos(UCubeDevice device) {
        if (device == null) {
            uCubeNameTv.setText(R.string.no_device_selected);
            uCubeAddressTv.setText("");
        } else {
            uCubeNameTv.setText(getString(R.string.ucube_name, device.getName()));
            uCubeAddressTv.setText(getString(R.string.ucube_address, device.getAddress()));
        }
    }

    private void updateMDMUI(MDMState state) {
        switch (state) {
            case IDLE:
            case DEVICE_NOT_REGISTERED:
                if (ytProduct != YTProduct.AndroidPOS) mdmRegisterBtn.setVisibility(View.VISIBLE);
                mdmGetConfigBtn.setVisibility(GONE);
                mdmCheckUpdateBtn.setVisibility(GONE);
                mdmSendLogBtn.setVisibility(GONE);
                break;

            case DEVICE_REGISTERED:
                mdmRegisterBtn.setVisibility(GONE);

                mdmGetConfigBtn.setVisibility(View.VISIBLE);
                mdmCheckUpdateBtn.setVisibility(View.VISIBLE);
                mdmSendLogBtn.setVisibility(View.VISIBLE);
                break;
        }
    }

    private UCubeDevice getDevice() {
        SharedPreferences mainSharedPref = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);

        String val = mainSharedPref.getString(ytProduct.getName(), null);

        if (!StringUtils.isBlank(val)) {
            try {
                return new Gson().fromJson(val, UCubeDevice.class);
            } catch (Exception e) {
                Log.d(TAG, "invalid stored device infos");
                forgetDevice();
            }
        }

        return null;
    }

    private void setSelectedDevice(UCubeDevice device) {
        getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(ytProduct.getName(), new Gson().toJson(device))
                .apply();

        selectedDeviceChanged(device);
    }

    private void selectedDeviceChanged(UCubeDevice device) {
        UCubeAPI.getConnexionManager().setDevice(device);
        displayDeviceInfos(device);
        connect();
    }

    private void forgetDevice(String productName) {
        UCubeAPI.getConnexionManager().setDevice(null);

        getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(productName)
                .apply();

        updateConnectionUI(NO_DEVICE_SELECTED);
    }

    private void forgetDevice() {
        UCubeAPI.getConnexionManager().setDevice(null);

        getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(ytProduct.getName())
                .apply();

        updateConnectionUI(NO_DEVICE_SELECTED);
    }

    private void selectSocketURL(String defaultHost, int defaultPort) {
        final View customLayout = getLayoutInflater().inflate(R.layout.simulator_url_layout, null);
        final TextView hostFld = customLayout.findViewById(R.id.simu_adress_fld);
        final TextView portFld = customLayout.findViewById(R.id.simu_port_fld);

        hostFld.setText(defaultHost);
        portFld.setText(String.valueOf(defaultPort));

        UCubeDevice device = getDevice();
        if (device != null && device.getAddress() != null) {
            String[] infos = device.getAddress().split(":");
            hostFld.setText(infos[0]);
            if (infos.length > 1) portFld.setText(infos[1]);
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.simulator_url)
                .setCancelable(true)
                .setPositiveButton(R.string.ok, (dialog, whichButton) -> {
                    String host = hostFld.getText().toString();
                    String port = portFld.getText().toString();
                    if (StringUtils.isNotBlank(port)) {
                        host += ':' + port;
                    }
                    setSelectedDevice(new UCubeDevice(ytProduct.getName(), host));
                })
                .setNegativeButton(R.string.cancel, (dialog, whichButton) -> dialog.dismiss())
                .setView(customLayout)
                .show();
    }

    private void selectDevice() {
        if (UCubeAPI.getConnexionManager().isConnected()) {
            disconnect(true);
        }

        /*
           if user app will use YT TMS
           an unregister of last device should be called
           to delete current ssl certificate
         */
        boolean res = UCubeAPI.mdmUnregister(this);
        if (!res) {
            Log.e(TAG, "FATAL Error! error to unregister current device");
        }

        String cnxType = getSharedPreferences(SetupActivity.SETUP_SHARED_PREF_NAME, Context.MODE_PRIVATE).getString(SetupActivity.COMMUNICATION_TYPE_PREF_NAME, null);

        if (SOCKET.name().equals(cnxType)) {
            selectSocketURL(SocketConnectionManager.DEFAULT_HOST, SocketConnectionManager.DEFAULT_PORT);
            return;
        }

        if (SOCKET_JSON.name().equals(cnxType)) {
            selectSocketURL(SocketJSONConnectionManager.DEFAULT_HOST, SocketJSONConnectionManager.DEFAULT_PORT);
            return;
        }

        String deviceScannerClassname = null;
        switch (ytProduct) {
            case YT_Key:
                deviceScannerClassname = YTKeyScanner.class.getName();
                break;

            case uCube:
                deviceScannerClassname = ListPairedUCubeScanner.class.getName();
                break;

            case YT_SOM:
            case AndroidPOS:
                deviceScannerClassname = YTSOMScanner.class.getName();
                break;

            case uCubeTouch:
                deviceScannerClassname = ListPairedUCubeTouchScanner.class.getName();
                break;
        }

        Intent intent = new Intent(this, DeviceScanActivity.class);
        intent.putExtra(DeviceScanActivity.INTENT_EXTRA_DEVICE_SCANNER_CLASSNAME, deviceScannerClassname);

        startActivityForResult(intent, SCAN_REQUEST);
    }

    private void connect() {
        if (UCubeAPI.getConnexionManager().isConnected()) {
            updateConnectionUI(DEVICE_CONNECTED);
            return;
        }

        UIUtils.showProgress(
                this,
                getString(R.string.connecting),
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
                        Log.e(TAG, "connection failed status: " + status);
                        runOnUiThread(() -> {
                            UIUtils.hideProgressDialog();
                            UIUtils.showMessageDialog(MainActivity.this,
                                    getString(R.string.connect_failed, status.name(), error));
                        });
                    }

                    @Override
                    public void onConnectionSuccess() {
                        Log.e(TAG, "connection success");
                        runOnUiThread(() -> {
                            UIUtils.hideProgressDialog();
                            updateConnectionUI(DEVICE_CONNECTED);
                        });
                    }

                    @Override
                    public void onConnectionCancelled() {
                        Log.e(TAG, "connection cancelled");
                        runOnUiThread(() -> {
                            UIUtils.hideProgressDialog();
                            Toast.makeText(MainActivity.this, getString(R.string.connection_cancelled),
                                    Toast.LENGTH_LONG).show();
                        });
                    }
                });
    }

    private void disconnect(boolean showProgress) {
        final Disconnect disconnect = new Disconnect(UCubeAPI.getConnexionManager());

        // Disconnect could be called by closing this activity: do not update UI
        if (showProgress) {
            UIUtils.showProgress(this, getString(R.string.disconnecting));
        }
        disconnect.execute((disconnectionStatus) -> {
            if (!showProgress) {
                return;
            }
            UIUtils.hideProgressDialog();
            if (disconnectionStatus == Disconnect.DisconnectionStatus.DISCONNECTED) {
                updateConnectionUI(DEVICE_NOT_CONNECTED);
            }
        });
    }

    private void payment() {
        Intent paymentIntent = new Intent(this, PaymentActivity.class);
        startActivity(paymentIntent);
    }

    private void localUpdate() {
        Intent paymentIntent = new Intent(this, LocalUpdateActivity.class);
        startActivity(paymentIntent);
    }

    private void emvParamUpd() {
        Intent emvParamUpdIntent = new Intent(this, EmvParamUpdateActivity.class);
        startActivity(emvParamUpdIntent);
    }

    private void emvParamEnableDisableAID() {
        Intent emvParamEnableDisableAIDIntent = new Intent(this, EmvParamEnableDisableAIDActivity.class);
        startActivity(emvParamEnableDisableAIDIntent);
    }

    private void mdmRegister() {
        final AlertDialog progressDlg = UIUtils.showProgress(this, getString(R.string.register_progress));
        UCubeAPI.mdmRegister(new UCubeLibMDMServiceListener() {
            @Override
            public void onProgress(ServiceState state) {
                runOnUiThread(() -> UIUtils.setProgressMessage(getString(R.string.progress, state.name())));
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
        final AlertDialog progressDlg = UIUtils.showProgress(this, getString(R.string.get_config_progress));
        UCubeAPI.mdmGetConfig(new UCubeLibMDMServiceListener() {
            @Override
            public void onProgress(ServiceState state) {
                runOnUiThread(() -> UIUtils.setProgressMessage(getString(R.string.progress, state.name())));
            }

            @Override
            public void onFinish(boolean status, Object... params) {
                runOnUiThread(() -> {
                    progressDlg.dismiss();
                    Log.d(TAG, "mdm get config status: " + status);

                    if (status && params != null && params.length > 0 && params[0] instanceof List<?>) {
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

                                final AlertDialog progressDlg = UIUtils.showProgress(this,
                                        getString(R.string.check_update_progress), false);

                                UCubeAPI.mdmCheckUpdate(
                                        forceUpdate,
                                        checkOnlyFirmwareVersion,
                                        new UCubeLibMDMServiceListener() {

                                            @Override
                                            public void onProgress(ServiceState state) {
                                                runOnUiThread(() -> UIUtils.setProgressMessage(getString(R.string.progress, state.name())));
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

                                                        if (updateList == null || updateList.isEmpty()) {
                                                            Toast.makeText(MainActivity.this,
                                                                    getString(R.string.ucube_up_to_date),
                                                                    Toast.LENGTH_SHORT).show();
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

            final AlertDialog progressDlg = UIUtils.showProgress(this,
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
                    runOnUiThread(() -> UIUtils.setProgressMessage(getString(R.string.progress, state.name())));
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
        final AlertDialog progressDlg = UIUtils.showProgress(this, getString(R.string.send_log_progress));

        UCubeAPI.mdmSendLogs(new UCubeLibMDMServiceListener() {
            @Override
            public void onProgress(ServiceState state) {
                runOnUiThread(() -> UIUtils.setProgressMessage(getString(R.string.progress, state.name())));
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
        final AlertDialog progressDlg = UIUtils.showProgress(this, getString(R.string.display_msg));
        DisplayMessageCommand displayMessageCommand = new DisplayMessageCommand("Hello world");
        // displayMessageCommand.setTimeout(2);
        displayMessageCommand.setClearConfig((byte) 0x04);
        displayMessageCommand.setXPosition((byte) 0xFF);
        displayMessageCommand.setYPosition((byte) 0x00);

        displayMessageCommand.execute((event1, params1) -> runOnUiThread(() -> {
            switch (event1) {
                case PROGRESS:
                    Log.d(TAG, "message display progress state " + ((RPCCommandStatus) params1[1]).name());
                    return;
                case FAILED:
                case CANCELLED:
                    Log.d(TAG, "display message : " + event1);
                    UIUtils.showMessageDialog(this, getString(R.string.display_msg_failure));
                    break;

                case SUCCESS:
                    Log.d(TAG, "display message : " + event1);
                    Toast.makeText(this, getString(R.string.display_msg_success), Toast.LENGTH_LONG).show();
                    break;
            }

            progressDlg.dismiss();
        }));
    }

    private void setPanCvvDate() {
        Intent sdseIntent = new Intent(this, SdsePromptActivity.class);
        sdseIntent.putExtra(SdsePromptActivity.INTENT_EXTRA_SDSE_PROMPT_MSG, getString(R.string.set_pan));
        sdseIntent.putExtra(SdsePromptActivity.INTENT_EXTRA_SDSE_PROMPT_TYPE, SdseSession.SDSE_TYPE_PAN);
        startActivity(sdseIntent);
        new SdseSession(this).execute();
    }

    private void startdte() {
        IConnexionManager cnxManager = ConnectionService.getInstance().getActiveManager();
        ((SecureServiceConnectionManager) cnxManager).registerPaymentViewDelegate(ViewIdentifier.PIN_PROMPT);
        Intent dteIntent = new Intent(this, WaitCard_Dte.class);
        startActivity(dteIntent);
    }

    private void startcontactCertif() {
        IConnexionManager cnxManager = ConnectionService.getInstance().getActiveManager();
        ((SecureServiceConnectionManager) cnxManager).registerPaymentViewDelegate(ViewIdentifier.PIN_PROMPT);
        Intent contactCertifIntent = new Intent(this, WaitCard_Dte.class);
        contactCertifIntent.putExtra("INTENT_EXTRA_ITF", ICC_READER);
        Log.d(TAG, "contactCertifIntent : " + ICC_READER);
        startActivity(contactCertifIntent);
    }

    private void startTestPin() {
        IConnexionManager cnxManager = ConnectionService.getInstance().getActiveManager();
        ((SecureServiceConnectionManager) cnxManager).registerPaymentViewDelegate(ViewIdentifier.PIN_PROMPT);
        Intent TestPinIntent = new Intent(this, OnlinePinTestActivity.class);
        startActivity(TestPinIntent);
    }

    private void sendGetInfocommand(int[] tagList){
        Executors.newSingleThreadExecutor().execute(() -> {
            new GetInfosCommand(tagList).execute((event, params) -> runOnUiThread(() -> {
                Log.d(TAG, "get info : " + event);

                switch (event) {
                    case PROGRESS:
                        Log.d(TAG, "Get info state " + ((RPCCommandStatus) params[1]).name());
                        break;

                    case FAILED:
                    case CANCELLED:
                        retrieveTagInfoOneByOne(tagList);
                        break;

                    case SUCCESS:
                        byte[] data = ((GetInfosCommand) params[0]).getResponseData();
                        if (LogManager.getLogLevel().getCode() >= LogManager.LogLevel.RPC.getCode()) {
                            LogManager.d(TAG, Hex.encodeHexString(data));
                        }
                        onDeviceInfoRetrieved(new DeviceInfos(data));
                        break;
                }
            }));
        });
    }

    private void getInfo() {
        final int[] uCubeInfoTagList = {
                TAG_SECURE_MOD,
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
                TAG_INTEGRITY_CHECK_TIME,
                TAG_NFC_INFOS
        };
        sendGetInfocommand(uCubeInfoTagList);
        UIUtils.showProgress(this, getString(R.string.get_info), false);
    }

    private void onDeviceInfoRetrieved(DeviceInfos deviceInfos) {
        runOnUiThread(() -> {
            FragmentManager fm = MainActivity.this.getSupportFragmentManager();
            GetInfoDialog Dialog = new GetInfoDialog(deviceInfos, ytProduct);
            Dialog.show(fm, "GET_INFO");
            UIUtils.hideProgressDialog();
        });
    }

    private void retrieveTagInfoOneByOne(int[] tagList) {
        tlvParts.clear();

        for (int tag : tagList) {
            remainingTags.add(tag);
        }

        retrieveInfoRemainingTags();
    }

    private void retrieveInfoRemainingTags() {
        if (remainingTags == null || remainingTags.isEmpty()) {
            onDeviceInfoRetrieved(null);
            return;
        }

        Integer tag = remainingTags.poll();
        assert tag != null;
        String tagAsHex = String.format("%04X", tag & 0xFFFF);
        Log.d(TAG, "Retrieve Tag " + tagAsHex);

        runOnUiThread(() -> UIUtils.setProgressMessage(getString(R.string.get_info_progress, tagAsHex)));

        new GetInfosCommand(tag).execute((event, params) -> {
            Log.d(TAG, "Retrieve tag event: " + event);

            if (event == TaskEvent.PROGRESS) return;

            if (event == TaskEvent.SUCCESS) {
                tlvParts.add(((GetInfosCommand) params[0]).getResponseData());
            }

            if (!remainingTags.isEmpty()) {
                retrieveInfoRemainingTags();
                return;
            }

            int offset = 0;
            byte[] tlv = new byte[MAX_RPC_PACKET_SIZE * 2];

            for (byte[] part : tlvParts) {
                System.arraycopy(part, 0, tlv, offset, part.length);
                offset += part.length;
            }

            byte[] tmp = new byte[offset];
            System.arraycopy(tlv, 0, tmp, 0, offset);

            DeviceInfos deviceInfos = new DeviceInfos(tmp);
            onDeviceInfoRetrieved(deviceInfos);
        });
    }

    private void powerOffTimeout(int powerOffValue) {
        final AlertDialog progressDlg = UIUtils.showProgress(this, getString(R.string.set_power_off_timeout_value));
        progressDlg.setCancelable(false);

        SetInfoFieldCommand setInfoFieldCommand = new SetInfoFieldCommand();
        setInfoFieldCommand.setPowerTimeout((byte) powerOffValue);
        setInfoFieldCommand.execute((event1, params1) -> runOnUiThread(() -> {
            Log.d(TAG, "set power off timeout : " + event1);
            if (event1 == TaskEvent.FAILED) {
                Toast.makeText(this, "Set power off timeout failed! ", Toast.LENGTH_LONG).show();
            } else if (event1 == TaskEvent.SUCCESS) {
                Toast.makeText(this, "Set power off timeout SUCCESS! ", Toast.LENGTH_LONG).show();
            }
            progressDlg.dismiss();
        }));
    }

    private void BuzzerOn() {
        final AlertDialog progressDlg = UIUtils.showProgress(this, getString(R.string.setup_buzzer_on));
        progressDlg.setCancelable(false);

        SetInfoFieldCommand setInfoFieldCommand = new SetInfoFieldCommand();
        setInfoFieldCommand.setBuzzerOnOff(true);
        setInfoFieldCommand.execute((event1, params1) -> runOnUiThread(() -> {
            if (event1 == TaskEvent.PROGRESS)
                return;

            progressDlg.dismiss();
        }));
    }

    private void BuzzerOff() {
        final AlertDialog progressDlg = UIUtils.showProgress(this, getString(R.string.setup_buzzer_off));
        progressDlg.setCancelable(false);

        SetInfoFieldCommand setInfoFieldCommand = new SetInfoFieldCommand();
        setInfoFieldCommand.setBuzzerOnOff(false);
        setInfoFieldCommand.execute((event1, params1) -> runOnUiThread(() -> {
            if (event1 == TaskEvent.PROGRESS)
                return;

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
                        Log.d(TAG, "locale : " + locales.get(i));
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

    private void getLogLight(){
        LogLightService srv = new LogLightService();

        srv.execute((event, param) -> {
                switch (event) {
                    case PROGRESS:
                        break;

                    case FAILED:
                    case CANCELLED:
                        break;

                    case SUCCESS:
                        if (param == null || param.length == 0 || !(param[0] instanceof List)) {
                            Log.e(TAG, "Log light: invalid handler");
                        }else {
                            List<LogLight> logLightList = (List<LogLight>) param[0];
                            for(int i = 0; i < logLightList.size(); i++){
                                logLightList.get(i).print();
                            }
                        }
                        break;
                }
            });
    }
    private void enterSecureSession() {
        final AlertDialog progressDlg = UIUtils.showProgress(this, getString(R.string.enter_secure_session));

        new EnterSecureSessionCommand().execute((event1, params1) -> runOnUiThread(() -> {
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
        final AlertDialog progressDlg = UIUtils.showProgress(this, getString(R.string.exit_secure_session));

        new ExitSecureSessionCommand().execute((event1, params1) -> runOnUiThread(() -> {
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
        final AlertDialog progressDlg = UIUtils.showProgress(this, getString(R.string.get_terminal_state));

        new GetInfosCommand(TAG_TERMINAL_STATE).execute((event1, params1) -> runOnUiThread(() -> {
            Log.d(TAG, "Get terminal state  : " + event1);
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

                    if (deviceInfos.getTerminalState() == null) // data are null in secured mode because the response is ciphered
                        Toast.makeText(this, "Terminal State: SECURED", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(this, "Terminal State: " + deviceInfos.getTerminalState().label, Toast.LENGTH_LONG).show();
                    break;
            }
        }));
    }

    private void enableQuickMode() {
        final AlertDialog progressDlg = UIUtils.showProgress(this, getString(R.string.setup_quick_mode));
        progressDlg.setCancelable(false);

        SetInfoFieldCommand setInfoFieldCommand = new SetInfoFieldCommand();
        setInfoFieldCommand.setMode(QUICK_MODE);
        setInfoFieldCommand.execute((event1, params1) -> runOnUiThread(() -> {
            if (event1 == TaskEvent.PROGRESS)
                return;

            progressDlg.dismiss();
        }));
    }

    private void enableSlowMode() {
        final AlertDialog progressDlg = UIUtils.showProgress(this, getString(R.string.setup_slow_mode));
        progressDlg.setCancelable(false);

        SetInfoFieldCommand setInfoFieldCommand = new SetInfoFieldCommand();
        setInfoFieldCommand.setMode(0);
        setInfoFieldCommand.execute((event1, params1) -> runOnUiThread(() -> {
            if (event1 == TaskEvent.PROGRESS)
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

    private AlertDialog progressDlg;

    private void askForTimeAndPerformAction(final Consumer<LocalTime> action) {
        final LocalTime now = LocalTime.now();

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (timePicker, hourOfDay, minute) -> {
                    final LocalTime chosenTime = LocalTime.of(hourOfDay, minute);
                    action.accept(chosenTime);
                }, now.getHour(), now.getMinute(), false);
        timePickerDialog.show();
    }

    private void askForDateTimeAndPerformAction(final Consumer<LocalDateTime> action) {
        final LocalDateTime now = LocalDateTime.now();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (datePicker, year, monthOfYear, dayOfMonth) -> {
                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            this,
                            (timePicker, hourOfDay, minute) -> {
                                final LocalDateTime chosenDateTime = LocalDateTime.of(year, monthOfYear + 1, dayOfMonth, hourOfDay, minute);
                                action.accept(chosenDateTime);
                            }, now.getHour(), now.getMinute(), false);
                    timePickerDialog.show();
                }, now.getYear(), now.getMonthValue() - 1, now.getDayOfMonth());
        datePickerDialog.show();
    }

    private void askForDateAndSetRtc() {
        this.askForDateTimeAndPerformAction(this::setRtc);
    }

    private void setRtc(final LocalDateTime localDateTimeToSet) {
        runOnUiThread(() -> progressDlg = UIUtils.showProgress(MainActivity.this, getString(R.string.set_rtc)));
        final SetRtc setRtc = new SetRtc(UCubeAPI.getConnexionManager());
        setRtc.execute(localDateTimeToSet.atZone(ZoneId.systemDefault()).toInstant(),
                (status) -> {
                    switch (status) {
                        case FAILED:
                            runOnUiThread(() -> {
                                progressDlg.dismiss();
                                UIUtils.showMessageDialog(MainActivity.this,
                                        getString(R.string.set_rtc_failed));
                            });
                            break;
                        case SUCCESS_REBOOT_NEEDED:
                            runOnUiThread(() -> {
                                progressDlg.dismiss();
                                Toast.makeText(MainActivity.this, getString(R.string.set_rtc_success), Toast.LENGTH_LONG).show();
                                updateConnectionUI(DEVICE_NOT_CONNECTED);
                            });
                            break;
                    }
                });
    }

    private void askForTimeAndSetIntegrityCheckTime() {
        this.askForTimeAndPerformAction(this::setIntegrityCheckTime);
    }

    private void setIntegrityCheckTime(final LocalTime timeToSet) {
        runOnUiThread(() -> progressDlg = UIUtils.showProgress(MainActivity.this, getString(R.string.set_integrity_check_time)));
        final SetIntegrityCheckTime setIntegrityCheckTime = new SetIntegrityCheckTime(UCubeAPI.getConnexionManager());
        setIntegrityCheckTime.execute(timeToSet,
                (status) -> {
                    switch (status) {
                        case FAILED:
                            runOnUiThread(() -> {
                                progressDlg.dismiss();
                                UIUtils.showMessageDialog(MainActivity.this,
                                        getString(R.string.set_integrity_check_time_failed));
                            });
                            break;
                        case SUCCESS_REBOOT_NEEDED:
                            runOnUiThread(() -> {
                                progressDlg.dismiss();
                                Toast.makeText(MainActivity.this, getString(R.string.set_integrity_check_time_success), Toast.LENGTH_LONG).show();
                                updateConnectionUI(DEVICE_NOT_CONNECTED);
                            });
                            break;
                    }
                });
    }

    public static String formatDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.ENGLISH);
        return dateFormat.format(date);
    }

    private void getRtc() {
        final AlertDialog progressDlg = UIUtils.showProgress(MainActivity.this, getString(R.string.get_rtc));

        RTCGetCommand rtcGetCommand = new RTCGetCommand();
        rtcGetCommand.execute((event, params) -> {
            switch (event) {
                case PROGRESS:
                    return;
                case FAILED:
                case CANCELLED:
                    Log.e(TAG, "get rtc : " + event);
                    runOnUiThread(() -> UIUtils.showMessageDialog(MainActivity.this, getString(R.string.get_rtc_failed)));
                    break;

                case SUCCESS:
                    Date deviceDate = rtcGetCommand.getDate();
                    Log.d(TAG, "get rtc success date : " + formatDate(deviceDate));
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, getString(R.string.get_rtc_success, formatDate(deviceDate)), Toast.LENGTH_LONG).show());
                    break;
            }
            progressDlg.dismiss();
        });
    }

    private void echo(String data) {
        final AlertDialog progressDlg = UIUtils.showProgress(MainActivity.this, getString(R.string.echo));

        EchoCommand cmd = new EchoCommand();
        cmd.setData(Tools.hexStringToByteArray(data));
        cmd.execute((event, params) -> {
            switch (event) {
                case PROGRESS:
                    return;
                case FAILED:
                case CANCELLED:
                    Log.e(TAG, "echo cm : " + event);
                    runOnUiThread(() ->
                            UIUtils.showMessageDialog(MainActivity.this, getString(R.string.echo_failed)));
                    break;

                case SUCCESS:
                    String echoResponse = Tools.bytesToHex(cmd.response.getData());
                    Log.d(TAG, "echo success response : " + echoResponse);
                    runOnUiThread(() ->
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
                    runOnUiThread(() ->
                            UIUtils.showMessageDialog(MainActivity.this, getString(R.string.get_pub_key_failed)));
                    break;

                case SUCCESS:
                    Log.d(TAG, "ca prod : " + Tools.bytesToHex(cmd.getCaProd()));
                    Log.d(TAG, "k key : " + Tools.bytesToHex(cmd.getkKek()));
                    Log.d(TAG, "KSN DUKPT Data : " + Tools.bytesToHex(cmd.getiKsnDukpt()));
                    Log.d(TAG, "KSN DUKPT PIN : " + Tools.bytesToHex(cmd.getiKsnPin()));

                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, getString(R.string.get_pub_key_success), Toast.LENGTH_LONG).show());
                    break;
            }
            UIUtils.hideProgressDialog();
        });
    }

    private void getKeyInfo(String keySlotId) {
        final AlertDialog progressDlg = UIUtils.showProgress(MainActivity.this, getString(R.string.get_key_info));

        if (keySlotId.isEmpty()) {
            runOnUiThread(() -> UIUtils.showMessageDialog(MainActivity.this, "key slot can not be empty"));
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
                    runOnUiThread(() ->
                            UIUtils.showMessageDialog(MainActivity.this, getString(R.string.get_key_info_failed)));
                    break;

                case SUCCESS:
                    Log.d(TAG, "key Info : " + cmd.keyInfo);

                    runOnUiThread(() ->
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

    private void getSomSyslog(){
        final int [] sysLogTags = {
                TAG_SYSTEM_FAILURE_LOG_RECORD_1
        };
        sendGetInfocommand(sysLogTags);
        UIUtils.showProgress(this, getString(R.string.get_info), false);
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
