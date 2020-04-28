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

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.youTransactor.uCube.api.listener.UCubeAPISettingUpListener;
import com.youTransactor.uCube.api.listener.UCubeCheckUpdateListener;
import com.youTransactor.uCube.api.UCubeInfo;
import com.youTransactor.uCube.api.UCubeAPI;
import com.youTransactor.uCube.api.listener.UCubeAPIListener;
import com.youTransactor.uCube.api.UCubeAPIState;
import com.youTransactor.uCube.api.listener.UCubeConnectListener;

import com.youTransactor.uCube.api.YTMPOSProduct;
import com.youTransactor.uCube.bluetooth.UCubeDevice;
import com.youTransactor.uCube.mdm.Config;
import com.youTransactor.uCube.mdm.service.BinaryUpdate;
import com.youTransactor.uCube.rpc.Constants;
import com.youTransactor.uCube.rpc.DeviceInfos;
import com.youTransactor.uCube.rpc.command.DisplayMessageCommand;
import com.youTransactor.uCube.rpc.command.GetInfosCommand;
import com.youtransactor.sampleapp.task.PrepareBankParametersTask;

import java.util.ArrayList;
import java.util.List;

import static com.youtransactor.sampleapp.MainActivity.Action.*;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private ToggleButton scanTb, connectTb;
    private Button bankParamDownloadsBtn, payBtn, getInfoBtn, checkUpdateBtn, sendLogBtn, displayBtn;

    private LinearLayout ucubeSection, ucubeInfoSection;
    private TextView ucubeNameTv, ucubeAddressTv, ucubeSerialNumTv, ucubePartNumTv,
            ucubeFirmwareVersionTv, ucubeFirmwareSTVersionTv, ucubeIICConfigTv, ucubeNFCConfigTv,
            nfcNotSupportedTv;

    private UCubeInfo uCubeInfo;
    private UCubeDevice uCubeDevice;
    private YTMPOSProduct model;
    private boolean isConnected;

    boolean checkOnlyFirmwareVersion = false;
    boolean forceUpdate = false;

    enum Action {
        INIT,
        SCAN_UCUBE,
        CONNECT_OR_REMOVE_UCUBE,
        DISCONNECT_UCUBE,
    }

    CompoundButton.OnCheckedChangeListener scanBtCheckListener = (buttonView, isChecked) -> {
        if (!isChecked) {
            scan();
        } else {
            deleteSelectedDevice();
        }
    };

    CompoundButton.OnCheckedChangeListener connectBtCheckListener = (buttonView, isChecked) -> {
        if (!isChecked) {
            connect();
        } else {
            disconnect();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        initVariables();

        if(model == null) {
            Toast.makeText(this, "Fatal error model can't be null",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //do not call UCubeAPI.connect() here
    }

    private void initView() {

        scanTb = findViewById(R.id.scanTb);
        connectTb = findViewById(R.id.connectTb);

        bankParamDownloadsBtn = findViewById(R.id.bankParamDownloadBtn);
        payBtn = findViewById(R.id.payBtn);
        getInfoBtn = findViewById(R.id.getInfoBtn);
        checkUpdateBtn = findViewById(R.id.checkUpdateBtn);
        sendLogBtn = findViewById(R.id.sendLogBtn);
        displayBtn = findViewById(R.id.displayBtn);

        ucubeSection = findViewById(R.id.ucube_section);
        ucubeInfoSection = findViewById(R.id.ucube_info_section);

        ucubeNameTv = findViewById(R.id.ucube_name);
        ucubeAddressTv = findViewById(R.id.ucube_address);
        ucubeSerialNumTv = findViewById(R.id.ucube_serial_num);
        ucubePartNumTv = findViewById(R.id.ucube_part_number);
        ucubeFirmwareVersionTv = findViewById(R.id.ucube_firmware_version);
        ucubeFirmwareSTVersionTv = findViewById(R.id.ucube_firmware_st_version);
        ucubeIICConfigTv = findViewById(R.id.ucube_icc_config);
        ucubeNFCConfigTv = findViewById(R.id.ucube_nfc_config);
        nfcNotSupportedTv = findViewById(R.id.nfc_not_supported);

        String versionName = BuildConfig.VERSION_NAME;
        TextView versionNametv = findViewById(R.id.version_name);
        versionNametv.setText(getString(R.string.versionName, versionName));

        TextView ucubeModelTv = findViewById(R.id.ucube_model);
        ucubeModelTv.setText(getString(R.string.ucube_model, model.name()));

        scanTb.setOnCheckedChangeListener(null);
        connectTb.setOnCheckedChangeListener(null);

        final Intent intent = getIntent();
        String mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        String mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        if(mDeviceAddress != null && mDeviceName != null)
            uCubeDevice = new UCubeDevice(mDeviceName, mDeviceAddress);

        updateUI(INIT);

        bankParamDownloadsBtn.setOnClickListener(v -> bankParamDownloads() );

        payBtn.setOnClickListener(v -> payment());

        getInfoBtn.setOnClickListener(v -> getInfo());

        checkUpdateBtn.setOnClickListener(v -> checkUpdate());

        sendLogBtn.setOnClickListener(v -> sendLogs());

        displayBtn.setOnClickListener(v -> displayHelloWorld());
    }

    private void initVariables() {

        try {
            uCubeDevice = UCubeAPI.getSelectedUCubeDevice();
            uCubeInfo = UCubeAPI.getUCubeInfo();
            isConnected = UCubeAPI.isConnected();
            model = UCubeAPI.getYTMPOSProduct();

        } catch (Exception ignore) {
            Toast.makeText(this, "Error to retrieve info " +
                    "from UCubeAPI, maybe setup not called", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void updateUI(Action action) {

        switch (action) {
            case INIT:

                if(uCubeDevice == null) {
                    updateUI(SCAN_UCUBE);
                    break;
                }

                if(uCubeInfo == null) {
                    updateUI(CONNECT_OR_REMOVE_UCUBE);
                    break;
                }

                if(!isConnected) {
                    updateUI(CONNECT_OR_REMOVE_UCUBE);
                }else {
                    updateUI(DISCONNECT_UCUBE);
                }

                break;

            case SCAN_UCUBE:

                scanTb.setChecked(true);
                scanTb.setOnCheckedChangeListener(scanBtCheckListener);

                scanTb.setEnabled(true);
                connectTb.setEnabled(false);

                bankParamDownloadsBtn.setEnabled(false);
                payBtn.setEnabled(false);
                checkUpdateBtn.setEnabled(false);
                sendLogBtn.setEnabled(false);
                getInfoBtn.setEnabled(false);
                displayBtn.setEnabled(false);

                displayUCube();
                displayUCubeInfo();
                break;

            case CONNECT_OR_REMOVE_UCUBE:

                scanTb.setEnabled(true);
                scanTb.setChecked(false);
                scanTb.setOnCheckedChangeListener(scanBtCheckListener);

                connectTb.setEnabled(true);
                connectTb.setChecked(true);
                connectTb.setOnCheckedChangeListener(connectBtCheckListener);

                bankParamDownloadsBtn.setEnabled(false);
                payBtn.setEnabled(false);
                checkUpdateBtn.setEnabled(false);
                sendLogBtn.setEnabled(false);
                getInfoBtn.setEnabled(false);
                displayBtn.setEnabled(false);

                displayUCube();
                displayUCubeInfo();
                break;

            case DISCONNECT_UCUBE:

                scanTb.setEnabled(false);

                connectTb.setEnabled(true);
                connectTb.setChecked(false);
                connectTb.setOnCheckedChangeListener(connectBtCheckListener);

                bankParamDownloadsBtn.setEnabled(true);
                payBtn.setEnabled(true);
                checkUpdateBtn.setEnabled(true);
                sendLogBtn.setEnabled(true);
                getInfoBtn.setEnabled(true);
                displayBtn.setEnabled(true);

                displayUCube();
                displayUCubeInfo();
                break;
        }
    }

    private void displayUCube() {

        if(uCubeDevice == null) {
            ucubeSection.setVisibility(View.GONE);
            return;
        }

        ucubeSection.setVisibility(View.VISIBLE);

        ucubeNameTv.setText(getString(R.string.ucube_name, uCubeDevice.getName()));
        ucubeAddressTv.setText(getString(R.string.ucube_address, uCubeDevice.getAddress()));
    }

    private void displayUCubeInfo() {

        if (uCubeInfo == null) {
            ucubeInfoSection.setVisibility(View.GONE);
            return;
        }

        ucubeInfoSection.setVisibility(View.VISIBLE);

        ucubeSerialNumTv.setText(getString(R.string.ucube_serial_num, uCubeInfo.serialNum));
        ucubePartNumTv.setText(getString(R.string.ucube_part_number, uCubeInfo.partNum));
        ucubeFirmwareVersionTv.setText(getString(R.string.ucube_firmware_version, uCubeInfo.firmwareVersion));
        ucubeIICConfigTv.setText(getString(R.string.ucube_icc_config, uCubeInfo.iccConfig));

        if (model == YTMPOSProduct.uCube_touch) {
            ucubeFirmwareSTVersionTv.setVisibility(View.GONE);
            ucubeNFCConfigTv.setVisibility(View.VISIBLE);
            ucubeNFCConfigTv.setText(getString(R.string.ucube_nfc_config, uCubeInfo.nfcConfig));
        } else {
            if (uCubeInfo.supportNFC) {
                ucubeFirmwareSTVersionTv.setVisibility(View.VISIBLE);
                ucubeNFCConfigTv.setVisibility(View.VISIBLE);
                nfcNotSupportedTv.setVisibility(View.GONE);
                ucubeFirmwareSTVersionTv.setText(getString(R.string.ucube_firmware_st_version, uCubeInfo.firmwareSTVersion));
                ucubeNFCConfigTv.setText(getString(R.string.ucube_nfc_config, uCubeInfo.nfcConfig));
            } else {
                ucubeFirmwareSTVersionTv.setVisibility(View.GONE);
                ucubeNFCConfigTv.setVisibility(View.GONE);
                nfcNotSupportedTv.setVisibility(View.VISIBLE);
            }
        }

    }

    private void scan() {
        try {

            Intent intent;

            if(model == YTMPOSProduct.uCube_touch) {
                intent = new Intent(this, UCubeTouchScanActivity.class);
            } else {
                intent = new Intent(this, ListPairedUCubeActivity.class);
            }

            startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteSelectedDevice() {

        try {
            UIUtils.showProgress(this, getString(R.string.delete_selected_ucube), true);

            UCubeAPI.deleteSelectedUCube(this, new UCubeAPIListener() {
                @Override
                public void onProgress(UCubeAPIState uCubeAPIState) {
                    runOnUiThread(() -> UIUtils.setProgressMessage(getString(R.string.progress, uCubeAPIState.name())));
                }

                @Override
                public void onFinish(boolean status) {
                    UIUtils.hideProgressDialog();

                    if(!status) {

                        scanTb.setOnCheckedChangeListener(null);
                        scanTb.setChecked(true);
                        scanTb.setOnCheckedChangeListener(scanBtCheckListener);


                        UIUtils.showMessageDialog(MainActivity.this, getString(R.string.delete_ucube_failed));
                    } else {
                        Toast.makeText(MainActivity.this, getString(R.string.delete_ucube_success),
                                Toast.LENGTH_LONG).show();
                        updateUI(SCAN_UCUBE);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void disconnect() {
        try {
            UIUtils.showProgress(this, getString(R.string.disconnect_progress), true);

            UCubeAPI.disconnect(this, new UCubeAPIListener() {
                @Override
                public void onProgress(UCubeAPIState uCubeAPIState) {
                    runOnUiThread(() -> UIUtils.setProgressMessage(getString(R.string.progress, uCubeAPIState.name())));
                }

                @Override
                public void onFinish(boolean status) {
                    UIUtils.hideProgressDialog();

                    if(!status) {
                        connectTb.setOnCheckedChangeListener(null);
                        connectTb.setChecked(false);
                        connectTb.setOnCheckedChangeListener(connectBtCheckListener);

                        UIUtils.showMessageDialog(MainActivity.this, getString(R.string.disconnect_failed));
                    } else {
                        Toast.makeText(MainActivity.this, getString(R.string.disconnect_success),
                                Toast.LENGTH_LONG).show();
                        updateUI(CONNECT_OR_REMOVE_UCUBE);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();

            UIUtils.hideProgressDialog();

            connectTb.setOnCheckedChangeListener(null);
            connectTb.setChecked(false);
            connectTb.setOnCheckedChangeListener(connectBtCheckListener);

            UIUtils.showMessageDialog(MainActivity.this, getString(R.string.connect_failed));
        }
    }

    private void connect() {

        try {
            UIUtils.showProgress(this, getString(R.string.connect_progress), true);

            UCubeAPI.connect(this, uCubeDevice,  new UCubeConnectListener() {

                @Override
                public void onProgress(UCubeAPIState uCubeAPIState) {

                    runOnUiThread(() -> {
                        if (uCubeAPIState.equals(UCubeAPIState.INIT_RPC_MANAGER)) { // this is the state juste after connection done
                            Toast.makeText(MainActivity.this, "Device connected", Toast.LENGTH_LONG).show();
                        }

                        UIUtils.setProgressMessage(getString(R.string.progress, uCubeAPIState.name()));
                    });
                }

                @Override
                public void onFinish(boolean status, UCubeInfo uCubeInfo) {

                    UIUtils.hideProgressDialog();

                    if (!status) {

                        connectTb.setOnCheckedChangeListener(null);
                        connectTb.setChecked(true);
                        connectTb.setOnCheckedChangeListener(connectBtCheckListener);

                        UIUtils.showMessageDialog(MainActivity.this, getString(R.string.connect_failed));

                    } else {

                        Toast.makeText(MainActivity.this, getString(R.string.connect_success),
                                Toast.LENGTH_LONG).show();

                        MainActivity.this.uCubeInfo = uCubeInfo;

                        updateUI(DISCONNECT_UCUBE);
                    }

                }
            });

        } catch (Exception e) {
            e.printStackTrace();

            UIUtils.hideProgressDialog();

            connectTb.setOnCheckedChangeListener(null);
            connectTb.setChecked(true);
            connectTb.setOnCheckedChangeListener(connectBtCheckListener);

            UIUtils.showMessageDialog(MainActivity.this, getString(R.string.connect_failed));
        }
    }

    private void bankParamDownloads() {

        PrepareBankParametersTask prepareBankParametersTask = new PrepareBankParametersTask();

        final ProgressDialog progressDlg = UIUtils.showProgress(this, getString(R.string.send_log_progress), true);
        progressDlg.setCancelable(false);

        try {
            UCubeAPI.sendBankParamToDevice(MainActivity.this,
                    prepareBankParametersTask, new UCubeAPISettingUpListener() {

                        @Override
                        public void onProgress(UCubeAPIState uCubeAPIState, int step) {
                            runOnUiThread(() -> {
                                switch (uCubeAPIState) {
                                    case PREPARE_BANK_PARAMETERS:
                                    case EXIT_SECURE_SESSION:
                                    case ENTER_SECURE_SESSION:
                                        progressDlg.setMessage(getString(R.string.bank_param_donwloads_service_progress,
                                                uCubeAPIState.name().toLowerCase()));
                                        break;

                                    case DOWNLOAD_BANK_PARAMETER:
                                        progressDlg.setMessage(getString(R.string.bank_param_donwloads_progress,
                                                step,
                                                prepareBankParametersTask.getBankParameters().size()));
                                        break;

                                }
                            });
                        }

                        @Override
                        public void onFinish(boolean status) {

                            progressDlg.dismiss();

                            Toast.makeText(
                                    MainActivity.this,
                                    status ? getString(R.string.bank_param_donwloads_success) :
                                            getString(R.string.bank_param_donwloads_failed),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();

            progressDlg.dismiss();

            Toast.makeText(
                    MainActivity.this,
                    getString(R.string.bank_param_donwloads_failed),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void payment() {
        Intent paymentIntent = new Intent(this, PaymentActivity.class);
        startActivity(paymentIntent);
    }

    private void checkUpdate() {

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

                                try {
                                    UCubeAPI.checkUpdate(
                                            this,
                                            forceUpdate,
                                            checkOnlyFirmwareVersion,
                                            new UCubeCheckUpdateListener() {
                                                @Override
                                                public void onProgress(UCubeAPIState state) {
                                                    runOnUiThread(() -> progressDlg.setMessage(getString(R.string.progress, state.name())));
                                                }

                                                @Override
                                                public void onFinish(boolean status, List<BinaryUpdate> updateList, List<Config> cfgList) {
                                                    progressDlg.dismiss();

                                                    if (status) {
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
                                } catch (Exception e) {
                                    e.printStackTrace();

                                    progressDlg.dismiss();

                                    UIUtils.showMessageDialog(MainActivity.this, getString(R.string.check_update_failed));
                                }

                            });
                });


    }

    private void sendLogs() {
        final ProgressDialog progressDlg = UIUtils.showProgress(this, getString(R.string.send_log_progress), true);
        progressDlg.setCancelable(false);

        try {
            UCubeAPI.sendLogs(MainActivity.this, new UCubeAPIListener() {
                @Override
                public void onProgress(UCubeAPIState uCubeAPIState) {
                    runOnUiThread(() -> progressDlg.setMessage(getString(R.string.progress, uCubeAPIState.name())));
                }

                @Override
                public void onFinish(boolean status) {

                    progressDlg.dismiss();

                    Toast.makeText(
                            MainActivity.this,
                            status ? getString(R.string.send_log_success) : getString(R.string.send_log_failed),
                            Toast.LENGTH_LONG
                    ).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();

            progressDlg.dismiss();

            Toast.makeText(
                    MainActivity.this,
                    getString(R.string.send_log_failed),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void displayHelloWorld() {
        final ProgressDialog progressDlg = UIUtils.showProgress(this, getString(R.string.display_msg));
        progressDlg.setCancelable(false);

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

            UIUtils.showProgress(this, "Start update", false);

            // new Thread(() -> {
            List<BinaryUpdate> selectedUpdateList = new ArrayList<>(selectedItems1.size());

            for (int i = 0; i < selectedItems1.size(); i++) {
                if (selectedItems1.get(i)) {
                    selectedUpdateList.add(updateList.get(i));
                }
            }

            try {
                UCubeAPI.update(
                        this,
                        selectedUpdateList,
                        new UCubeAPIListener() {
                            @Override
                            public void onProgress(UCubeAPIState state) {
                                runOnUiThread(() -> UIUtils.setProgressMessage(getString(R.string.update_progress)));
                            }

                            @Override
                            public void onFinish(boolean status) {
                                UIUtils.hideProgressDialog();

                                Toast.makeText(getApplicationContext(),
                                        status ? getString(R.string.update_success) : getString(R.string.update_failed),
                                        Toast.LENGTH_LONG
                                ).show();

                                initVariables();
                                displayUCube();
                                displayUCubeInfo();
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();

                UIUtils.hideProgressDialog();
            }

            //  }).start();
        });

        builder.create().show();
    }
}
