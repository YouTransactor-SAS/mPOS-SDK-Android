/*
 * Copyright (C) 2011-2021, YouTransactor. All Rights Reserved.
 *
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
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
import com.youTransactor.uCube.Tools;
import com.youTransactor.uCube.api.UCubeAPI;
import com.youTransactor.uCube.api.UCubeLibPaymentServiceListener;
import com.youTransactor.uCube.api.UCubePaymentRequest;
import com.youTransactor.uCube.connexion.IConnexionManager;
import com.youTransactor.uCube.rpc.CardReaderType;
import com.youTransactor.uCube.rpc.Currency;
import com.youTransactor.uCube.payment.PaymentContext;
import com.youTransactor.uCube.payment.PaymentService;
import com.youTransactor.uCube.payment.PaymentState;
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
    private TextView trxResultFld;
    private EditText startCancelDelayEditText;

    private PaymentState autoCancelState;
    private int startCancelDelay;
    private PaymentState autoDisconnectState;
    private boolean testModeEnabled = false;
    private PaymentService paymentService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences(SetupActivity.SETUP_SHARED_PREF_NAME, Context.MODE_PRIVATE);

        setContentView(R.layout.activity_payment);

        if (getIntent() != null) {
            if (getIntent().hasExtra(YT_PRODUCT))
                ytProduct = YTProduct.valueOf(getIntent().getStringExtra(YT_PRODUCT));
        }

        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        testModeEnabled = prefs.getBoolean(SetupActivity.TEST_MODE_PREF_NAME, false);
        findViewById(R.id.test_section).setVisibility(testModeEnabled ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        cancelPayment();
    }

    private void initView() {
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
        trxTypeChoice.setAdapter(new TransactionTypeAdapter());
        getLogsL1 = findViewById(R.id.getSvppLogL1);
        getStatusBtn = findViewById(R.id.getStatusBtn);

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

        getLogsL1.setOnClickListener(v -> getSvppLogsL1());
        getStatusBtn.setOnClickListener(v -> getStatus());

        findViewById(R.id.test_section).setVisibility(testModeEnabled ? View.VISIBLE : View.GONE);
    }

    private void startPayment() {
        UCubePaymentRequest uCubePaymentRequest = preparePaymentRequest();

//        String msg;
//        if (uCubePaymentRequest.getAmount() > 0) {
//            msg = getString(
//                    R.string.payment_progress_with_amount,
//                    uCubePaymentRequest.getAmount(),
//                    uCubePaymentRequest.getCurrency().getLabel()
//            );
//        } else {
//            msg = getString(
//                    R.string.payment_progress_without_amount,
//                    uCubePaymentRequest.getCurrency().getLabel()
//            );
//        }

        doPaymentBtn.setVisibility(View.GONE);
        cancelPaymentBtn.setVisibility(View.VISIBLE);
        trxResultFld.setText("");

        try {
            paymentService = UCubeAPI.pay(uCubePaymentRequest,
                    new UCubeLibPaymentServiceListener() {

                        @Override
                        public void onProgress(PaymentState state, PaymentContext context) {
                            // todo No RPC call here

                            runOnUiThread(() -> {
                                Log.d(TAG, " Payment progress : " + state);

                                displayProgress(state);

                                if (state == autoCancelState) {
                                    startCancelDelay = Integer.parseInt(startCancelDelayEditText.getText().toString());
                                    Log.d(TAG, "start cancel delay : "+ startCancelDelay);

                                    new Handler(Looper.getMainLooper()).postDelayed(() -> cancelPayment(), startCancelDelay);
                                }

                                if (state == autoDisconnectState) {
                                    disconnect();
                                }

                                if (state == PaymentState.KSN_AVAILABLE) {
                                    Log.d(TAG, "KSN : " + Arrays.toString(context.sredKsn));
                                }

                                if (state == PaymentState.SMC_PROCESS_TRANSACTION) {
                                    Log.d(TAG, "init data : " + Arrays.toString(context.transactionInitData));
                                }

                                if(state == PaymentState.CARD_READ_END) {
                                    //DISABLE CANCELLING
                                    cancelPaymentBtn.setVisibility(View.GONE);
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

        boolean retrieveF5Tag = retrieveF5TagSwitch.isChecked();

        int amount = amountFld.getCleanIntValue();
        Log.d(TAG,"Amount : "+ amount);

        List<CardReaderType> readerList = new ArrayList<>();

        readerList.add(CardReaderType.ICC);

        if (!contactOnly)
            readerList.add(CardReaderType.NFC);

        UCubePaymentRequest uCubePaymentRequest = new UCubePaymentRequest(amount, currency, trxType,
                readerList, new AuthorizationTask(this), Collections.singletonList("en"));

        //Add optional variables
        uCubePaymentRequest
                .setForceOnlinePin(forceOnlinePin)
                .setTransactionDate(new Date())
                .setForceAuthorisation(forceAuthorisation)
                .setRiskManagementTask(new RiskManagementTask(this))
                .setCardWaitTimeout(timeout)
                .setSystemFailureInfo2(false)
                .setForceDebug(forceDebug)
                .setSkipCardRemoval(skipCardRemoval)
                .setSkipStartingSteps(skipStartingSteps)
                .setRetrieveF5Tag(retrieveF5Tag)

                .setAuthorizationPlainTags(
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
                )

                .setAuthorizationSecuredTags(
                        TAG_SECURE_5A_APPLICATION_PRIMARY_ACCOUNT_NUMBER,
                        TAG_SECURE_57_TRACK_2_EQUIVALENT_DATA,
                        TAG_SECURE_56_TRACK_1_DATA,
                        TAG_SECURE_5F20_CARDHOLDER_NAME,
                        TAG_SECURE_5F24_APPLICATION_EXPIRATION_DATE,
                        TAG_SECURE_5F30_SERVICE_CODE,
                        TAG_SECURE_9F0B_CARDHOLDER_NAME_EXTENDED,
                        TAG_SECURE_9F6B_TRACK_2_DATA
                )

                .setFinalizationSecuredTags(
                        TAG_SECURE_5A_APPLICATION_PRIMARY_ACCOUNT_NUMBER,
                        TAG_SECURE_57_TRACK_2_EQUIVALENT_DATA,
                        TAG_SECURE_56_TRACK_1_DATA,
                        TAG_SECURE_5F20_CARDHOLDER_NAME,
                        TAG_SECURE_5F24_APPLICATION_EXPIRATION_DATE,
                        TAG_SECURE_5F30_SERVICE_CODE,
                        TAG_SECURE_9F0B_CARDHOLDER_NAME_EXTENDED,
                        TAG_SECURE_9F6B_TRACK_2_DATA
                )

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
                );

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

    private void disconnect() {
        Log.d(TAG, "disconnect from terminal");

        IConnexionManager connexionManager = UCubeAPI.getConnexionManager();
        if (connexionManager != null)
            connexionManager.disconnect(status -> Log.d(TAG, "Disconnected"));
    }

    private void getSvppLogsL1() {
        ITaskMonitor iTaskMonitor = (event, params) -> Log.d(TAG,"event: " + event);
        new CancelCommand().execute(iTaskMonitor);
        new ExitSecureSessionCommand().execute(iTaskMonitor);
        new EnterSecureSessionCommand().execute(iTaskMonitor);
        new GetInfosCommand(Constants.TAG_SYSTEM_FAILURE_LOG_RECORD_1).execute(iTaskMonitor);
        new ExitSecureSessionCommand().execute(iTaskMonitor);
    }

    private void getStatus() {
        new GetStatusCommand().execute((event, params) -> Log.d(TAG,"getStatus event:"+ event));
    }
}
