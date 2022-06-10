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
package com.youtransactor.sampleapp.payment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.youTransactor.uCube.ITaskMonitor;
import com.youTransactor.uCube.TLV;
import com.youTransactor.uCube.TaskEvent;
import com.youTransactor.uCube.Tools;
import com.youTransactor.uCube.api.UCubeAPI;
import com.youTransactor.uCube.api.UCubeLibControlServiceListener;
import com.youTransactor.uCube.api.UCubeLibPaymentServiceListener;
import com.youTransactor.uCube.api.UCubePaymentRequest;
import com.youTransactor.uCube.connexion.IConnexionManager;
import com.youTransactor.uCube.control.ControlContext;
import com.youTransactor.uCube.control.ControlService;
import com.youTransactor.uCube.rpc.CardReaderType;
import com.youTransactor.uCube.control.ControlState;
import com.youTransactor.uCube.rpc.Currency;
import com.youTransactor.uCube.payment.PaymentContext;
import com.youTransactor.uCube.payment.PaymentService;
import com.youTransactor.uCube.payment.PaymentState;
import com.youTransactor.uCube.rpc.RPCCommand;
import com.youTransactor.uCube.rpc.TransactionType;
import com.youTransactor.uCube.rpc.Constants;
import com.youTransactor.uCube.rpc.command.CancelCommand;
import com.youTransactor.uCube.rpc.command.EnterSecureSessionCommand;
import com.youTransactor.uCube.rpc.command.ExitSecureSessionCommand;
import com.youTransactor.uCube.rpc.command.GetInfosCommand;
import com.youTransactor.uCube.rpc.command.GetStatusCommand;
import com.youtransactor.sampleapp.R;
import com.youtransactor.sampleapp.SetupActivity;
import com.youtransactor.sampleapp.UIUtils;
import com.youtransactor.sampleapp.YTProduct;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.youTransactor.uCube.rpc.Constants.EMVTag.*;
import static com.youtransactor.sampleapp.SetupActivity.YT_PRODUCT;

public class PaymentActivity extends AppCompatActivity {
    public static final String TAG = PaymentActivity.class.getName();

    private SharedPreferences prefs;
    /* Device */
    private YTProduct ytProduct = YTProduct.uCubeTouch;

    private Button doPaymentBtn;
    private Button cancelPaymentBtn;
    private Button getLogsL1, getStatusBtn;
    private EditText cardWaitTimeoutFld;
    private Spinner trxTypeChoice;
    private CurrencyEditText amountFld;
    private Spinner currencyChooser;
    private Switch forceOnlinePINBtn;
    private Switch forceAuthorisationBtn;
    private Switch amountSrcSwitch;
    private Switch contactOnlySwitch;
    private Switch forceDebugSwitch;
    private Switch skipCardRemovalSwitch;
    private Switch skipStartingStepsSwitch;
    private Switch retrieveF5TagSwitch;
    private Switch tipSwitch;
    private TextView trxResultFld;
    private EditText startCancelDelayEditText;
    private Button uPresentCard;
    private Button uEnterPin;
    private Button crEnterPin;

    private PaymentState autoCancelState;
    private int startCancelDelay;
    private PaymentState autoDisconnectState;
    private boolean testModeEnabled = false;
    private boolean measureModeEnabled;
    private PaymentService paymentService;
    private ControlService controlService;

    private PaymentMeasure paymentMeasure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences(SetupActivity.SETUP_SHARED_PREF_NAME, Context.MODE_PRIVATE);
        testModeEnabled = prefs.getBoolean(SetupActivity.TEST_MODE_PREF_NAME, false);
        measureModeEnabled = prefs.getBoolean(SetupActivity.MEASURES_MODE_PREF_NAME, false);
        if(measureModeEnabled) {
            paymentMeasure = new PaymentMeasure();
        }

        setContentView(R.layout.activity_payment);

        if (getIntent() != null) {
            if (getIntent().hasExtra(YT_PRODUCT))
                ytProduct = YTProduct.valueOf(getIntent().getStringExtra(YT_PRODUCT));
        }

        initView();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if(paymentService != null)
            cancelPayment();
        else
            cancelControl();
    }

    private void initView() {

        findViewById(R.id.test_section).setVisibility(testModeEnabled ? View.VISIBLE : View.GONE);
        findViewById(R.id.measures_section).setVisibility(measureModeEnabled ? View.VISIBLE : View.GONE);

        doPaymentBtn = findViewById(R.id.doPaymentBtn);
        cancelPaymentBtn = findViewById(R.id.cancelPaymentBtn);
        cardWaitTimeoutFld = findViewById(R.id.cardWaitTimeoutFld);
        trxTypeChoice = findViewById(R.id.trxTypeChoice);
        amountFld = findViewById(R.id.amountFld);
        currencyChooser = findViewById(R.id.currencyChooser);
        forceOnlinePINBtn = findViewById(R.id.forceOnlinePINSwitch);
        amountSrcSwitch = findViewById(R.id.amountSrcSwitch);
        contactOnlySwitch = findViewById(R.id.contactOnlySwitch);
        forceAuthorisationBtn = findViewById(R.id.forceAuthorisationSwitch);
        forceDebugSwitch = findViewById(R.id.forceDebugSwitch);
        trxResultFld = findViewById(R.id.trxResultFld);
        startCancelDelayEditText = findViewById(R.id.start_cancel_delay);
        skipCardRemovalSwitch = findViewById(R.id.skipCardRemovalSwitch);
        skipStartingStepsSwitch = findViewById(R.id.skipStartingStepsSwitch);
        retrieveF5TagSwitch = findViewById(R.id.retrieveF5Tag);
        tipSwitch = findViewById(R.id.tipSwitch);
        trxTypeChoice.setAdapter(new TransactionTypeAdapter());
        getLogsL1 = findViewById(R.id.getSvppLogL1);
        getStatusBtn = findViewById(R.id.getStatusBtn);
        uPresentCard = findViewById(R.id.u_present_card);
        uEnterPin = findViewById(R.id.u_enter_pin);
        crEnterPin = findViewById(R.id.cr_enter_pin);

        final CurrencyAdapter currencyAdapter = new CurrencyAdapter();
        currencyAdapter.add(UCubePaymentRequest.CURRENCY_EUR);
        currencyAdapter.add(UCubePaymentRequest.CURRENCY_USD);
        currencyAdapter.add(UCubePaymentRequest.CURRENCY_GBP);

        currencyChooser.setAdapter(currencyAdapter);
        currencyChooser.setSelection(0);

        amountSrcSwitch.setOnClickListener(v -> amountFld.setEnabled(!amountSrcSwitch.isChecked()));

        doPaymentBtn.setOnClickListener(v -> startPayment());

        cancelPaymentBtn.setOnClickListener(v -> cancelPayment());
        cancelPaymentBtn.setVisibility(View.GONE);


        final Spinner cancelEventSwitch = findViewById(R.id.cancelEventSwitch);
        cancelEventSwitch.setAdapter(new PaymentStateAdapter());
        cancelEventSwitch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                autoCancelState = (PaymentState) cancelEventSwitch.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                autoCancelState = null;
            }
        });

        final Spinner disconnectEventSwitch = findViewById(R.id.disconnectEventSwitch);
        disconnectEventSwitch.setAdapter(new PaymentStateAdapter());
        disconnectEventSwitch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                autoDisconnectState = (PaymentState) disconnectEventSwitch.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                autoDisconnectState = null;
            }
        });

        getLogsL1.setOnClickListener(v -> getCBTag());
        getStatusBtn.setOnClickListener(v -> getStatus());

        uPresentCard.setOnClickListener(v -> paymentMeasure.onUserPresentCard());
        uEnterPin.setOnClickListener(v -> paymentMeasure.onUEnterPin());
        crEnterPin.setOnClickListener(v -> paymentMeasure.onCREnterPin());
    }

    private void startPayment() {
        doPaymentBtn.setVisibility(View.GONE);
        cancelPaymentBtn.setVisibility(View.VISIBLE);
        findViewById(R.id.measurement_section).setVisibility(View.GONE);
        trxResultFld.setText("");

        pay();
    }

    private UCubePaymentRequest preparePaymentRequest() {
        int timeout = Integer.parseInt(cardWaitTimeoutFld.getText().toString());

        Currency currency = (Currency) currencyChooser.getSelectedItem();

        TransactionType trxType = (TransactionType) trxTypeChoice.getSelectedItem();

        boolean forceOnlinePin = forceOnlinePINBtn.isChecked(); // only for NFC & MSR

        boolean forceAuthorisation = forceAuthorisationBtn.isChecked();

        boolean contactOnly = contactOnlySwitch.isChecked();

        boolean forceDebug = forceDebugSwitch.isChecked();

        boolean skipCardRemoval = skipCardRemovalSwitch.isChecked();

        boolean skipStartingSteps = skipStartingStepsSwitch.isChecked();

        boolean tipRequired = tipSwitch.isChecked();

        boolean retrieveF5Tag = retrieveF5TagSwitch.isChecked();

        long amount = amountFld.getCleanIntValue();
        Log.d(TAG,"Amount : "+ amount);

        List<CardReaderType> readerList = new ArrayList<>();

        readerList.add(CardReaderType.ICC);

        if (!contactOnly)
            readerList.add(CardReaderType.NFC);

        AuthorizationTask authorizationTask = new AuthorizationTask(this);
        authorizationTask.setMeasureStatesListener(paymentMeasure);

        UCubePaymentRequest uCubePaymentRequest = new UCubePaymentRequest(amount, currency, trxType,
                readerList, authorizationTask, Collections.singletonList("en"));

        //Add optional variables
        uCubePaymentRequest
                .setForceOnlinePin(forceOnlinePin)
                .setTransactionDate(new Date())
                .setForceAuthorisation(forceAuthorisation)
              //  .setRiskManagementTask(new RiskManagementTask(this))
                .setCardWaitTimeout(timeout)
                .setSystemFailureInfo2(false)
                .setForceDebug(forceDebug)
                .setSkipCardRemoval(skipCardRemoval)
                .setSkipStartingSteps(skipStartingSteps)
                .setRetrieveF5Tag(retrieveF5Tag)
                .setTipRequired(tipRequired)

                //CLIENT TAGs
                .setAuthorizationPlainTags(
                        0x9C, 0x9F10, 0x9F1A, 0x4F, 0xDF, 0x81, 0x29, 0xD4, 0x9F41, 0xDF02, 0x8E, 0x9F39,
                        0x9F37, 0x9F27, 0x9A, 0x9F08, 0x50, 0x95, 0x9F7C, 0x9F71, 0xDF, 0xC302, 0x9F36, 0x9F34,
                        0x9B, 0x9F12, 0x82, 0x9F66, 0x9F26, 0x5F34, 0x9F6E, 0xD3, 0x84, 0x9F33, 0x9F06, 0x8F)

                .setAuthorizationSecuredTags(
                        TAG_SECURE_5A_APPLICATION_PRIMARY_ACCOUNT_NUMBER,
                        TAG_SECURE_57_TRACK_2_EQUIVALENT_DATA,
                        TAG_SECURE_56_TRACK_1_DATA,
                        TAG_SECURE_5F24_APPLICATION_EXPIRATION_DATE,
                        0x99,
                        0x5F2A,
                        0x9F02,
                        0x9F03
                )
                .setFinalizationPlainTags(0x9F1A, 0x99, 0x5F2A, 0x95, 0x4F, 0x9B, 0x5F34, 0x81, 0x8E, 0x9A, 0xDF37, 0x50)
                .setFinalizationSecuredTags(
                        TAG_SECURE_5A_APPLICATION_PRIMARY_ACCOUNT_NUMBER,
                        TAG_SECURE_57_TRACK_2_EQUIVALENT_DATA,
                        TAG_SECURE_56_TRACK_1_DATA,
                        TAG_SECURE_5F20_CARDHOLDER_NAME,
                        TAG_SECURE_5F24_APPLICATION_EXPIRATION_DATE,
                        TAG_SECURE_5F30_SERVICE_CODE,
                        TAG_SECURE_9F0B_CARDHOLDER_NAME_EXTENDED,
                        TAG_SECURE_9F6B_TRACK_2_DATA
                );
      /* complete list of optional tags C1 :
                        0x9F39
                        ,0x9F02
                        ,0x9F03
                        ,0x9F26
                        ,0x82
                        ,0x50
                        ,0x9F36
                        ,0x9F09
                        ,0x9F27
                        ,0x9F10
                        ,0x9F34
                        ,0x9F35
                        ,0x9F33
                        ,0x84
                        ,0x9F1A
                        ,0x95
                        ,0x9F53
                        ,0x5F2A
                        ,0x9A
                        ,0x9F21
                        ,0x9C
                        ,0x9F37
                        ,0x9B
                        ,0x9F66
                        ,0xDF0C
                        ,0xDF66
                        ,0x9F0D
                        ,0x9F0E
                        ,0x9F0F
                        ,0xDF20
                        ,0xDF21
                        ,0xDF22
                        ,0xDF22
                        ,0xFF50
                        ,0x82
                        ,0x9F07
                        ,0x8E
                        ,0x94
                        ,0xDF75)
                        */

                /*
                TAG_4F_APPLICATION_IDENTIFIER,
                        TAG_50_APPLICATION_LABEL,
                        TAG_5F2A_TRANSACTION_CURRENCY_CODE,
                        TAG_5F34_APPLICATION_PRIMARY_ACCOUNT_NUMBER_SEQUENCE_NUMBER,
                        TAG_81_AMOUNT_AUTHORISED,
                        TAG_8E_CARDHOLDER_VERIFICATION_METHOD_LIST,
                        TAG_95_TERMINAL_VERIFICATION_RESULTS,
                        TAG_9B_TRANSACTION_STATUS_INFORMATION,
                        TAG_99_TRANSACTION_PERSONAL_IDENTIFICATION_NUMBER_DATA,
                        TAG_9A_TRANSACTION_DATE,
                        TAG_9C_TRANSACTION_TYPE,
                        TAG_9F06_APPLICATION_IDENTIFIER__TERMINAL,
                        TAG_9F10_ISSUER_APPLICATION_DATA,
                        TAG_9F1A_TERMINAL_COUNTRY_CODE,
                        TAG_9F26_APPLICATION_CRYPTOGRAM,
                        TAG_9F27_CRYPTOGRAM_INFORMATION_DATA,
                        TAG_9F33_TERMINAL_CAPABILITIES,
                        TAG_9F34_CARDHOLDER_VERIFICATION_METHOD_RESULTS,
                        TAG_9F36_APPLICATION_TRANSACTION_COUNTER,
                        TAG_DF02_PEK_VERSION_NUMBER,
                        TAG_84_APPLICATION_ID,
                        TAG_9F12_APPLICATION_PREFERRED_NAME,
                        TAG_9F39_POINT_OF_SERVICE_ENTRY_MODE,
                        TAG_8A_AUTHORIZATION_RESPONSE_CODE,
                        TAG_91_ISSUER_AUTHENTICATION_DATA,
                        TAG_71_ISSUER_SCRIPT_TEMPLATE1,
                        TAG_72_ISSUER_SCRIPT_TEMPLATE2,
                        TAG_9F6E_NFC_FORM_FORMAT,
                        TAG_5F34_PAN_SEQUENCE_NUMBER,
                        TAG_DF37_SELECTED_CARDHOLDER_LANGUAGE,
                        TAG_9F08_APPLICATION_VERSION_NUMBER,
                        TAG_5F25_APPLICATION_EFFECTIVE_DATE,
                        TAG_82_APPLICATION_INTERCHANGE_PROFILE,
                        TAG_9F07_APPLICATION_USAGE_CONTROL,
                        TAG_9F37_UNPREDICTABLE_NUMBER
                )*/
/*

                .setFinalizationPlainTags(
                        TAG_4F_APPLICATION_IDENTIFIER,
                        TAG_50_APPLICATION_LABEL,
                        TAG_5F2A_TRANSACTION_CURRENCY_CODE,
                        TAG_5F34_APPLICATION_PRIMARY_ACCOUNT_NUMBER_SEQUENCE_NUMBER,
                        TAG_81_AMOUNT_AUTHORISED,
                        TAG_8E_CARDHOLDER_VERIFICATION_METHOD_LIST,
                        TAG_95_TERMINAL_VERIFICATION_RESULTS,
                        TAG_9B_TRANSACTION_STATUS_INFORMATION,
                        TAG_99_TRANSACTION_PERSONAL_IDENTIFICATION_NUMBER_DATA,
                        TAG_9A_TRANSACTION_DATE,
                        TAG_9C_TRANSACTION_TYPE,
                        TAG_9F06_APPLICATION_IDENTIFIER__TERMINAL,
                        TAG_9F10_ISSUER_APPLICATION_DATA,
                        TAG_9F1A_TERMINAL_COUNTRY_CODE,
                        TAG_9F26_APPLICATION_CRYPTOGRAM,
                        TAG_9F27_CRYPTOGRAM_INFORMATION_DATA,
                        TAG_9F33_TERMINAL_CAPABILITIES,
                        TAG_9F34_CARDHOLDER_VERIFICATION_METHOD_RESULTS,
                        TAG_9F36_APPLICATION_TRANSACTION_COUNTER,
                        TAG_DF02_PEK_VERSION_NUMBER,
                        TAG_84_APPLICATION_ID,
                        TAG_9F12_APPLICATION_PREFERRED_NAME,
                        TAG_9F39_POINT_OF_SERVICE_ENTRY_MODE,
                        TAG_8A_AUTHORIZATION_RESPONSE_CODE,
                        TAG_91_ISSUER_AUTHENTICATION_DATA,
                        TAG_71_ISSUER_SCRIPT_TEMPLATE1,
                        TAG_72_ISSUER_SCRIPT_TEMPLATE2,
                        TAG_9F6E_NFC_FORM_FORMAT,
                        TAG_5F34_PAN_SEQUENCE_NUMBER,
                        TAG_DF37_SELECTED_CARDHOLDER_LANGUAGE,
                        TAG_9F08_APPLICATION_VERSION_NUMBER,
                        TAG_5F25_APPLICATION_EFFECTIVE_DATE,
                        TAG_82_APPLICATION_INTERCHANGE_PROFILE,
                        TAG_9F07_APPLICATION_USAGE_CONTROL,
                        TAG_9F37_UNPREDICTABLE_NUMBER
                );*/

        return uCubePaymentRequest;
    }

    private void displayProgress(PaymentState state) {
        String msg = state.name();

//        if (testModeEnabled) {
//            msg += "\n" + trxResultFld.getText();
//        }

        trxResultFld.setText(msg);
    }

    private void parsePaymentResponse(@NonNull PaymentContext context) {
        Log.d(TAG, "Payment status : " + context.paymentStatus);

        trxResultFld.setText(context.paymentStatus.name());

        /* uCube info */
        byte[] ucubeFirmware = TLV.parse(context.uCubeInfos).get(Constants.TAG_FIRMWARE_VERSION);
        if (ucubeFirmware != null)
            Log.d(TAG, "uCube firmware version: " + Tools.parseVersion(ucubeFirmware));

        Log.d(TAG, "Used Interface: " + CardReaderType.getLabel(context.activatedReader));

        Log.d(TAG, "amount: " + context.amount);
        Log.d(TAG, "currency: " + context.currency.getLabel());
        Log.d(TAG, "tx date: " + context.transactionDate);
        Log.d(TAG, "tx type: " + context.transactionType.getLabel());

        if (context.selectedApplication != null) {
            Log.d(TAG, "app ID: " + context.selectedApplication.getLabel());
            Log.d(TAG, "app version: " + context.applicationVersion);
        }

        Log.d(TAG, "SVPP Logs level 2 Tag CC: " + Tools.bytesToHex(context.tagCC));
        Log.d(TAG, "SVPP Logs level 2 Tag F4: " + Tools.bytesToHex(context.tagF4));
        Log.d(TAG, "SVPP Logs level 2 Tag F5: " + Tools.bytesToHex(context.tagF5));

        //todo send this to backend to check the integrity
        if (context.finalizationGetPlainTagsResponse != null)
            Log.d(TAG,"finalization plain tags response " + Tools.bytesToHex(context.finalizationGetPlainTagsResponse));

        if (context.finalizationPlainTagsValues != null) {
            for (Integer tag : context.finalizationPlainTagsValues.keySet())
                Log.d(TAG, String.format("Plain Tag : 0x%x : %s", tag, Tools.bytesToHex(context.finalizationPlainTagsValues.get(tag))));
        }

        if (context.finalizationSecuredTagsValues != null)
            Log.d(TAG, "secure tag block: " + Tools.bytesToHex(context.finalizationSecuredTagsValues));

    }

    private void cancelPayment() {
        if (paymentService != null && paymentService.isRunning()) {
            Log.d(TAG, "Try to cancel current Payment");
            UIUtils.showProgress(this, "Trying cancellation");

            paymentService.cancel(status -> {
                Log.d(TAG, "cancel value : "+ status);
                runOnUiThread(() -> {
                    UIUtils.hideProgressDialog();
                    if(status)
                        Toast.makeText(PaymentActivity.this, "Cancellation success", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(PaymentActivity.this, "Cancellation failed", Toast.LENGTH_LONG).show();
                });
            });
        }
    }

    private void cancelControl() {
        if (controlService != null && controlService.isRunning()) {
            Log.d(TAG, "Try to cancel current control");
            UIUtils.showProgress(this, "Trying cancellation");

            controlService.cancel(status -> {
                Log.d(TAG, "cancel value : "+ status);
                runOnUiThread(() -> {
                    UIUtils.hideProgressDialog();
                    if(status)
                        Toast.makeText(PaymentActivity.this, "Cancellation success", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(PaymentActivity.this, "Cancellation failed", Toast.LENGTH_LONG).show();
                });
            });
        }
    }

    private void disconnect() {
        Log.d(TAG, "disconnect from terminal");

        IConnexionManager connexionManager = UCubeAPI.getConnexionManager();
        if (connexionManager != null)
            connexionManager.disconnect(status -> Log.d(TAG, "Disconnected"));
    }

    private void getCBTag() {
        ITaskMonitor iTaskMonitor = (event, params) ->  {
            Log.d(TAG,"event: " + event);

            if(event == TaskEvent.FAILED) {
                String command = "unknown";
                if(params[0] != null)
                    command =  "0x"+Integer.toHexString(((RPCCommand) params[0]).getCommandId());

                String finalCommand = command;
                runOnUiThread(() -> UIUtils.showMessageDialog(this, getString(R.string.get_cb_command_failed, finalCommand)));
            }
        };
        new CancelCommand().execute(iTaskMonitor);
        new ExitSecureSessionCommand().execute(iTaskMonitor);
        new EnterSecureSessionCommand().execute(iTaskMonitor);
        new GetInfosCommand(Constants.TAG_SYSTEM_FAILURE_LOG_RECORD_1).execute(iTaskMonitor);
        new ExitSecureSessionCommand().execute(new ITaskMonitor() {
            @Override
            public void handleEvent(TaskEvent event, Object... params) {
                if(event == TaskEvent.FAILED) {
                    runOnUiThread(() -> UIUtils.showMessageDialog(PaymentActivity.this,
                            getString(R.string.get_cb_command_failed, "0x5102")));
                } else if(event == TaskEvent.SUCCESS) {
                    runOnUiThread(() -> UIUtils.showMessageDialog(PaymentActivity.this,
                            getString(R.string.get_cb_command_success)));
                }
            }
        });
    }

    private void getStatus() {
        new GetStatusCommand().execute((event, params) -> Log.d(TAG,"getStatus event:"+ event));
    }

    private void control() {
        try {
            controlService = UCubeAPI.control(30
                    , (byte) 0x44, null,
                    new UCubeLibControlServiceListener() {
                        @Override
                        public void onProgress(ControlState state, ControlContext context) {

                            runOnUiThread(() -> {
                                Log.d(TAG, " Control progress : " + state);

                                trxResultFld.setText(state.name());
                            });
                        }

                        @Override
                        public void onFinish(ControlContext controlContext, String token) {
                            runOnUiThread(() -> {
                                Log.d(TAG, "control finish, status: "
                                        + controlContext.controlStatus
                                        + " token : " + token);

                                doPaymentBtn.setVisibility(View.VISIBLE);
                                cancelPaymentBtn.setVisibility(View.GONE);
                                trxResultFld.setText("control finish, status: "
                                        + controlContext.controlStatus
                                        + " token : " + token);
                            });
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();

            doPaymentBtn.setVisibility(View.VISIBLE);
            cancelPaymentBtn.setVisibility(View.GONE);
        }
    }

    private void pay() {

        try {
            UCubePaymentRequest uCubePaymentRequest = preparePaymentRequest();
            paymentService = UCubeAPI.pay(uCubePaymentRequest,
                    new UCubeLibPaymentServiceListener() {

                        @Override
                        public void onProgress(PaymentState state, PaymentContext context) {
                            // todo No RPC call here

                            runOnUiThread(() -> {
                                Log.d(TAG, " Payment progress : " + state);

                                displayProgress(state);

                                switch (state) {
                                    case ENTER_SECURE_SESSION:
                                        if(paymentMeasure != null)
                                            paymentMeasure.onStart();
                                        break;

                                    case KSN_AVAILABLE:
                                        Log.d(TAG, "KSN : " + Arrays.toString(context.sredKsn));
                                        break;

                                    case START_TRANSACTION:
                                        if(paymentMeasure != null)
                                            paymentMeasure.onWaitingCard();
                                        break;

                                    case AUTHORIZATION:
                                        if(paymentMeasure != null)
                                            paymentMeasure.onAuthorizationCalled();
                                        break;

                                    case OFFLINE_PIN:
                                    case ONLINE_PIN:
                                        if(paymentMeasure != null)
                                            paymentMeasure.onCREnterPin();
                                        break;

                                    case SMC_PROCESS_TRANSACTION:
                                        Log.d(TAG, "init data : " + Arrays.toString(context.transactionInitData));
                                        break;

                                    case CARD_READ_END:
                                        //DISABLE CANCELLING
                                        cancelPaymentBtn.setVisibility(View.GONE);
                                        break;
                                }

                                if (state == autoCancelState) {
                                    startCancelDelay = Integer.parseInt(startCancelDelayEditText.getText().toString());
                                    Log.d(TAG, "start cancel delay : "+ startCancelDelay);

                                    new Handler(Looper.getMainLooper()).postDelayed(() -> cancelPayment(), startCancelDelay);
                                }

                                if (state == autoDisconnectState) {
                                    disconnect();
                                }
                            });
                        }

                        @Override
                        public void onFinish(PaymentContext context) {
                            runOnUiThread(() -> {
                                if (context == null) {
                                    UIUtils.showMessageDialog(PaymentActivity.this, getString(R.string.payment_failed));
                                    return;
                                }

                                Log.d(TAG, "payment finish status : " + context.paymentStatus);

                                doPaymentBtn.setVisibility(View.VISIBLE);
                                cancelPaymentBtn.setVisibility(View.GONE);

                                if(paymentMeasure != null) {
                                    paymentMeasure.onFinish();
                                    displayMeasures(context);
                                }

                                parsePaymentResponse(context);
                            });

                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();

            doPaymentBtn.setVisibility(View.VISIBLE);
            cancelPaymentBtn.setVisibility(View.GONE);
        }
    }

    private void displayMeasures(PaymentContext context) {
        findViewById(R.id.measurement_section).setVisibility(View.VISIBLE);
        TextView measureStartToCardWait= findViewById(R.id.measure_start_to_waiting_card);
        TextView measurePresentToAuth= findViewById(R.id.measure_present_card_to_auth);
        TextView measureAuthToFinish= findViewById(R.id.measure_auth_resp_to_finish);
        TextView measureEnterPinToAuth= findViewById(R.id.measure_user_enter_pin_to_auth);
        TextView measurePresentCardToEnterPin= findViewById(R.id.measure_present_card_to_cr_enter_pin);

        String measurement;
        if(context.activatedReader == CardReaderType.NFC.getCode()) {
            measureStartToCardWait.setText(getString(R.string.measure_start_to_waiting_card,
                    paymentMeasure.calculateMeasureStartToWaitingCard()));
            measurePresentToAuth.setText(getString(R.string.measure_present_card_to_auth,
                    paymentMeasure.calculateMeasureUPresentCardToAuthorization()));
            measureAuthToFinish.setText(getString(R.string.measure_auth_resp_to_finish,
                    paymentMeasure.calculateMeasureAuthorisationRpToFinish()));

            measureStartToCardWait.setVisibility(View.VISIBLE);
            measurePresentToAuth.setVisibility(View.VISIBLE);
            measureAuthToFinish.setVisibility(View.VISIBLE);
            measureEnterPinToAuth.setVisibility(View.GONE);
            measurePresentCardToEnterPin.setVisibility(View.GONE);

            measurement = "Measurement : \n"
                    + "\n From Start To waiting card: " + paymentMeasure.calculateMeasureStartToWaitingCard()
                    + "\n From UPresentCard To Authorization: " + paymentMeasure.calculateMeasureUPresentCardToAuthorization()
                    + "\n From Authorisation Rp To Finish: " + paymentMeasure.calculateMeasureAuthorisationRpToFinish();
        }else {
            measureStartToCardWait.setText(getString(R.string.measure_start_to_waiting_card,
                    paymentMeasure.calculateMeasureStartToWaitingCard()));
            measurePresentCardToEnterPin.setText(getString(R.string.measure_present_card_to_enter_pin,
                    paymentMeasure.calculateMeasureUPresentCardToCREnterPin()));
            measureEnterPinToAuth.setText(getString(R.string.measure_enter_pin_to_auth,
                    paymentMeasure.calculateMeasureUEnterPinToAuthorization()));
            measureAuthToFinish.setText(getString(R.string.measure_auth_resp_to_finish,
                    paymentMeasure.calculateMeasureAuthorisationRpToFinish()));

            measureStartToCardWait.setVisibility(View.VISIBLE);
            measurePresentToAuth.setVisibility(View.GONE);
            measureAuthToFinish.setVisibility(View.VISIBLE);
            measureEnterPinToAuth.setVisibility(View.VISIBLE);
            measurePresentCardToEnterPin.setVisibility(View.VISIBLE);

            measurement = "Measurement : \n"
                    + "\n From Start To waiting card: " + paymentMeasure.calculateMeasureStartToWaitingCard()
                    + "\n From UPresentCard To CR Enter Pin: " + paymentMeasure.calculateMeasureUPresentCardToCREnterPin()
                    + "\n From U Enter Pin To Authorisation: " + paymentMeasure.calculateMeasureUEnterPinToAuthorization()
                    + "\n From Authorisation Rp To Finish: " + paymentMeasure.calculateMeasureAuthorisationRpToFinish();
        }

        Log.d(TAG, measurement);
        findViewById(R.id.measurement_section).setVisibility(View.VISIBLE);
    }
}
