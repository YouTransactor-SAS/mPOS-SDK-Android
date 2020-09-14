/*
 * Copyright (C) 2011-2020, YouTransactor. All Rights Reserved.
 * <p/>
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youtransactor.sampleapp.payment;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.youTransactor.uCube.TLV;
import com.youTransactor.uCube.Tools;
import com.youTransactor.uCube.api.UCubeAPI;
import com.youTransactor.uCube.api.UCubeLibPaymentServiceListener;
import com.youTransactor.uCube.api.UCubePaymentRequest;
import com.youTransactor.uCube.payment.CardReaderType;
import com.youTransactor.uCube.payment.Currency;
import com.youTransactor.uCube.payment.PaymentContext;
import com.youTransactor.uCube.payment.PaymentState;
import com.youTransactor.uCube.payment.TransactionType;
import com.youTransactor.uCube.payment.service.PaymentService;
import com.youTransactor.uCube.rpc.Constants;
import com.youtransactor.sampleapp.R;
import com.youtransactor.sampleapp.UIUtils;
import com.youtransactor.sampleapp.YTProduct;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import static com.youtransactor.sampleapp.SetupActivity.YT_PRODUCT;

public class PaymentActivity extends AppCompatActivity {

    public static final String TAG = PaymentActivity.class.getName();

    /* Device */
    private YTProduct ytProduct = YTProduct.uCubeTouch;

    private Button doPaymentBtn;
    private Button cancelPaymentBtn;
    private EditText cardWaitTimeoutFld;
    private Spinner trxTypeChoice;
    private EditText amountFld;
    private Spinner currencyChooser;
    private Switch forceOnlinePINBtn;
    private Switch forceAuthorisationBtn;
    private Switch amountSrcSwitch;
    private Switch contactOnlySwitch;
    private Switch displayResultSwitch;
    private TextView trxResultFld;
    private LinearLayout progressSection;
    private TextView progressMessage;
    private ProgressBar progressBar;
    int progress = 0;
    private static final int STEP = 10;

    PaymentService paymentService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_payment);

        if (getIntent() != null) {
            if (getIntent().hasExtra(YT_PRODUCT))
                ytProduct = YTProduct.valueOf(getIntent().getStringExtra(YT_PRODUCT));
        }

        initView();
    }

    private void initView() {

        doPaymentBtn = findViewById(R.id.doPaymentBtn);
        cancelPaymentBtn = findViewById(R.id.cancelPaymentBtn);
        cardWaitTimeoutFld = findViewById(R.id.cardWaitTimeoutFld);
        trxResultFld = findViewById(R.id.trxResultFld);
        trxTypeChoice = findViewById(R.id.trxTypeChoice);
        amountFld = findViewById(R.id.amountFld);
        currencyChooser = findViewById(R.id.currencyChooser);
        forceOnlinePINBtn = findViewById(R.id.forceOnlinePINBtn);
        amountSrcSwitch = findViewById(R.id.amountSrcBtn);
        contactOnlySwitch = findViewById(R.id.contactOnlyBtn);
        forceAuthorisationBtn = findViewById(R.id.forceAuthorisationBtn);
        displayResultSwitch = findViewById(R.id.displayResultOnUCubeBtn);
        progressSection = findViewById(R.id.progress_section);
        progressMessage = findViewById(R.id.progress_msg);
        progressBar = findViewById(R.id.progress_bar);

        amountFld.setText(getString(R.string._1_00));
        trxTypeChoice.setAdapter(new TransactionTypeAdapter());

        final CurrencyAdapter currencyAdapter = new CurrencyAdapter();
        currencyAdapter.add(UCubePaymentRequest.CURRENCY_EUR);
        currencyAdapter.add(UCubePaymentRequest.CURRENCY_USD);

        currencyChooser.setAdapter(currencyAdapter);
        currencyChooser.setSelection(0);

        amountSrcSwitch.setOnClickListener(v -> amountFld.setEnabled(!amountSrcSwitch.isChecked()));

        doPaymentBtn.setOnClickListener(v -> startPayment());

        cancelPaymentBtn.setOnClickListener(v -> cancelPayment());
    }

    private void startPayment() {

        UCubePaymentRequest uCubePaymentRequest = preparePaymentRequest();

        //update UI
        String msg;
        if (uCubePaymentRequest.getAmount() > 0) {
            msg = getString(
                    R.string.payment_progress_with_amount,
                    uCubePaymentRequest.getAmount(),
                    uCubePaymentRequest.getCurrency().getLabel()
            );
        } else {
            msg = getString(
                    R.string.payment_progress_without_amount,
                    uCubePaymentRequest.getCurrency().getLabel()
            );
        }

        doPaymentBtn.setEnabled(false);
        cancelPaymentBtn.setEnabled(true);
        trxResultFld.setText("");
        progressSection.setVisibility(View.VISIBLE);
        progressMessage.setText(msg);
        progress = 0;
        progressBar.setProgress(progress);

        try {

            paymentService = UCubeAPI.pay(this, uCubePaymentRequest,
                    new UCubeLibPaymentServiceListener() {

                        @Override
                        public void onProgress(PaymentState state, PaymentContext context) {
                            // todo No RPC call here

                            Log.d(TAG, " Payment progress : " + state);

                            String msg = "";

                            switch (state) {
                                case CANCEL_ALL:
                                    msg = "cancel all...";
                                    break;

                                case GET_INFO:
                                    msg = "get device info...";
                                    break;

                                case WAIT_CARD:
                                    msg = "Waiting for card insertion...";
                                    break;

                                case ENTER_SECURE_SESSION:
                                case KSN_AVAILABLE:
                                    msg = "starting secure session...";
                                    break;

                                case SMC_BUILD_CANDIDATE_LIST:
                                case SMC_SELECT_APPLICATION:
                                case SMC_USER_SELECT_APPLICATION:
                                    msg = "App selection...";
                                    break;

                                case SMC_INIT_TRANSACTION:
                                    msg = "Transaction initialization ...";
                                    break;

                                case START_NFC_TRANSACTION:
                                    msg = "Starting...";
                                    break;

                                case SMC_RISK_MANAGEMENT:
                                    msg = "Risk management processing...";
                                    break;

                                case SMC_PROCESS_TRANSACTION:
                                    msg = "Transaction processing ...";
                                    break;

                                case MSR_GET_SECURED_TAGS:
                                case MSR_GET_PLAIN_TAGS:
                                case NFC_GET_SECURED_TAGS:
                                case NFC_GET_PLAIN_TAGS:
                                    msg = "Retrieving tags...";
                                    break;

                                case SMC_FINALIZE_TRANSACTION:
                                    msg = "Transaction finalization ...";
                                    break;

                                case COMPLETE_NFC_TRANSACTION:
                                    msg = "NFC transaction Finalisation...";
                                    break;

                                case SMC_REMOVE_CARD:
                                    msg = "Please remove card";
                                    break;

                                case MSR_ONLINE_PIN:
                                    msg = "Pin online...";
                                    break;

                                case AUTHORIZATION:
                                    msg = "Authorization processing...";
                                    break;

                                case EXIT_SECURE_SESSION:
                                    msg = "secure session closing...";
                                    break;

                                case DISPLAY_RESULT:
                                    msg = "Displaying result on device...";
                                    break;

                                case GET_L1_LOG:
                                    msg = "Getting Transaction Logs L1...";
                                    break;

                                case GET_L2_LOG:
                                    msg = "Getting Transaction Logs L2...";
                                    break;
                            }

                            progress = progress + STEP;
                            if (progress > 200)
                                progress = 200;

                            progressBar.setProgress(progress);

                            progressMessage.setText(msg);

                            if (state == PaymentState.KSN_AVAILABLE) {
                                Log.d(TAG, "KSN : " + Arrays.toString(context.getSredKsn()));
                                return;
                            }

                            if (state == PaymentState.SMC_PROCESS_TRANSACTION) {
                                Log.d(TAG, "init data : " + Arrays.toString(context.getTransactionInitData()));
                            }
                        }

                        @Override
                        public void onFinish(boolean status, PaymentContext context) {
                            Log.d(TAG, "payment finish status : " + status);

                            //update UI
                            doPaymentBtn.setEnabled(true);
                            cancelPaymentBtn.setEnabled(false);
                            progressSection.setVisibility(View.INVISIBLE);

                            if (!status || context == null) {
                                UIUtils.showMessageDialog(PaymentActivity.this, getString(R.string.payment_failed));
                            } else {
                                parsePaymentResponse(context);
                            }
                        }
                    });

        } catch (Exception e) {

            e.printStackTrace();

            //update UI
            doPaymentBtn.setEnabled(true);
            cancelPaymentBtn.setEnabled(false);
            progressSection.setVisibility(View.INVISIBLE);
        }
    }

    private UCubePaymentRequest preparePaymentRequest() {
        int timeout = Integer.parseInt(cardWaitTimeoutFld.getText().toString());

        Currency currency = (Currency) currencyChooser.getSelectedItem();

        TransactionType trxType = (TransactionType) trxTypeChoice.getSelectedItem();

        boolean forceOnlinePin = forceOnlinePINBtn.isChecked(); // only for NFC & MSR

        boolean forceAuthorisation = forceAuthorisationBtn.isChecked();

        boolean contactOnly = contactOnlySwitch.isChecked();

        boolean displayResultOnUCube = displayResultSwitch.isChecked();

        double amount = -1;

        if (!amountSrcSwitch.isChecked()) {
            try {
                amount = Double.parseDouble(amountFld.getText().toString());
            } catch (Exception e) {
                amountSrcSwitch.setChecked(true);
            }
        }

        ResourceBundle newMsgBundle = null;
        Bundle newAltMsgBundle = null;

        try {
            newMsgBundle = new PropertyResourceBundle(getResources().openRawResource(R.raw.new_ucube_strings));

        } catch (IOException ignore) {
            newAltMsgBundle = new Bundle();
            newAltMsgBundle.putString("LBL_wait_context_reset", "Please wait");
            newAltMsgBundle.putString("LBL_wait_transaction_finalization", "Please wait");
            newAltMsgBundle.putString("LBL_wait_online_pin_process", "Please wait");
            newAltMsgBundle.putString("LBL_wait_open_new_secure_session", "Please wait");
            newAltMsgBundle.putString("LBL_approved", "Approved");
            newAltMsgBundle.putString("LBL_declined", "Declined");
            newAltMsgBundle.putString("LBL_use_chip", "Use Chip");
            newAltMsgBundle.putString("LBL_no_card_detected", "No card detected");
            newAltMsgBundle.putString("LBL_remove_card", "Remove card");
            newAltMsgBundle.putString("LBL_unsupported_card", "Unsupported card");
            newAltMsgBundle.putString("LBL_refused_card", "Card refused");
            newAltMsgBundle.putString("LBL_cancelled", "Cancelled");
            newAltMsgBundle.putString("LBL_try_other_interface", "Try other interface");
            newAltMsgBundle.putString("LBL_configuration_error", "Config error");
            newAltMsgBundle.putString("LBL_wait_card", "{0} {1}\nInsert card");
            newAltMsgBundle.putString("LBL_wait_cancel", "Cancellation \n Please wait");
            newAltMsgBundle.putString("GLOBAL_LBL_xposition", "FF");
            newAltMsgBundle.putString("GLOBAL_LBL_yposition", "0C");
            newAltMsgBundle.putString("GLOBAL_LBL_font_id", "00");
        }

        /* TODO REMOVE THIS */
        ResourceBundle msgBundle = null;
        Bundle altMsgBundle = null;

        try {
            msgBundle = new PropertyResourceBundle(getResources().openRawResource(R.raw.ucube_strings));

        } catch (IOException ignore) {

            altMsgBundle = new Bundle();
            altMsgBundle.putString("LBL_wait", "Please wait");
            altMsgBundle.putString("LBL_wait_legacy", "Please wait");
            altMsgBundle.putString("LBL_wait_card_ok", "Please wait");
            altMsgBundle.putString("LBL_approved", "Approved");
            altMsgBundle.putString("LBL_declined", "Declined");
            altMsgBundle.putString("LBL_use_chip", "Use Chip");
            altMsgBundle.putString("LBL_authorization", "Authorization");
            altMsgBundle.putString("LBL_pin_prompt", "{0} {1}\nEnter PIN");
            altMsgBundle.putString("LBL_no_card_detected", "No card detected");
            altMsgBundle.putString("LBL_remove_card", "Remove card");
            altMsgBundle.putString("LBL_unsupported_card", "Unsupported card");
            altMsgBundle.putString("LBL_refused_card", "Card refused");
            altMsgBundle.putString("LBL_cancelled", "Cancelled");
            altMsgBundle.putString("LBL_try_other_interface", "Try other interface");
            altMsgBundle.putString("LBL_cfg_error", "Config error");
            altMsgBundle.putString("MSG_wait_card", "{0} {1}\nInsert card");
            altMsgBundle.putString("LBL_wait_cancel", "Cancellation \n Please wait");
            altMsgBundle.putString("GLOBAL_centered", "FF");
            altMsgBundle.putString("GLOBAL_yposition", "0C");
            altMsgBundle.putString("GLOBAL_font_id", "00");
        }

        /* TODO REMOVE THIS */

        List<CardReaderType> readerList = new ArrayList<>();

        readerList.add(CardReaderType.ICC);

        if (ytProduct == YTProduct.uCube)
            readerList.add(CardReaderType.MSR);

        if (!contactOnly)
            readerList.add(CardReaderType.NFC);

        return new UCubePaymentRequest.Builder()
                .setAmount(amount)
                .setCurrency(currency)
                .setTransactionDate(new Date())
                .setDisplayResult(displayResultOnUCube)
                .setReaderList(readerList)
                .setForceOnlinePin(forceOnlinePin)
                .setForceAuthorisation(forceAuthorisation)
                .setAuthorizationTask(new AuthorizationTask(this))
                .setRiskManagementTask(new RiskManagementTask(this))
                .setCardWaitTimeout(timeout)
                .setTransactionType(trxType)
                .setSystemFailureInfo(false)
                .setSystemFailureInfo2(false)
                 //Deprecated use the
                .setMsgBundle(msgBundle)
                .setAltMsgBundle(altMsgBundle)

                .setNewAltMsgBundle(newAltMsgBundle)
                .setNewMsgBundle(newMsgBundle)

                .setPreferredLanguageList(Collections.singletonList("en")) // each language represented by 2 alphabetical characters according to ISO 639

                //deprecated to get plain and secured tags values at the authorisationTask
                // use setAuthorizationPlainTags & setAuthorizationSecuredTags
                .setRequestedAuthorizationTagList(Constants.TAG_TVR, Constants.TAG_TSI)

                //deprecated to get plain and secured tags values at the end of transaction use
                //setFinalizationSecuredTags & setFinalizationPlainTags
                .setRequestedSecuredTagList(0x56, 0x57, 0x5A, 0x5F34, 0x5F20, 0x5F24, 0x5F30,
                        0x9F0B, 0x9F6B, 0x9F08, 0x9F68, 0x5F2C, 0x5F2E)
                .setRequestedPlainTagList(0x50, 0x8A, 0x8F, 0x9F09, 0x9F17, 0x9F35, 0x5F28, 0x9F0A)

                .setAuthorizationPlainTags(0x50, 0x8A, 0x8F, 0x9F09, 0x9F17, 0x9F35, 0x5F28, 0x9F0A)
                .setAuthorizationSecuredTags(0x56, 0x57, 0x5A, 0x5F34, 0x5F20, 0x5F24, 0x5F30,
                        0x9F0B, 0x9F6B, 0x9F08, 0x9F68, 0x5F2C, 0x5F2E)
                .setFinalizationSecuredTags(0x56, 0x57, 0x5A, 0x5F34, 0x5F20, 0x5F24, 0x5F30,
                        0x9F0B, 0x9F6B, 0x9F08, 0x9F68, 0x5F2C, 0x5F2E)
                .setFinalizationPlainTags(0x50, 0x8A, 0x8F, 0x9F09, 0x9F17, 0x9F35, 0x5F28, 0x9F0A)

                .build();
    }

    private void parsePaymentResponse(@NonNull PaymentContext context) {

        Log.d(TAG, "Payment status : " + context.getPaymentStatus());

        trxResultFld.setText(context.getPaymentStatus().name());

        /* uCube info */
        byte[] ucubeFirmware = TLV.parse(context.getuCubeInfos()).get(Constants.TAG_FIRMWARE_VERSION);
        if (ucubeFirmware != null)
            Log.d(TAG, "ucube firmware version: " + Tools.parseVersion(ucubeFirmware));

        Log.d(TAG, "Used Interface: " + CardReaderType.getLabel(context.getActivatedReader()));

        Log.d(TAG, "amount: " + context.getAmount());
        Log.d(TAG, "currency: " + context.getCurrency().getLabel());
        Log.d(TAG, "tx date: " + context.getTransactionDate());
        Log.d(TAG, "tx type: " + context.getTransactionType().getLabel());

        if (context.getSelectedApplication() != null) {
            Log.d(TAG, "app ID: " + context.getSelectedApplication().getLabel());
            Log.d(TAG, "app version: " + context.getApplicationVersion());
        }

        Log.d(TAG, "system failure log1: " + Tools.bytesToHex(context.getSystemFailureInfo()));
        Log.d(TAG, "system failure log2: " + Tools.bytesToHex(context.getSystemFailureInfo2()));

        if (context.getPlainTagTLV() != null) {

            for (Integer tag : context.getPlainTagTLV().keySet())
                Log.d(TAG, String.format("Plain Tag : 0x%x : %s", tag, Tools.bytesToHex(context.getPlainTagTLV().get(tag))));

        }
        if (context.getSecuredTagBlock() != null)
            Log.d(TAG, "secure tag block: " + Tools.bytesToHex(context.getSecuredTagBlock()));

    }

    private void cancelPayment() {
        Log.d(TAG, "Try to cancel current Payment");

        if (paymentService != null && paymentService.isRunning()) {
            paymentService.cancel();
        }
    }
}
