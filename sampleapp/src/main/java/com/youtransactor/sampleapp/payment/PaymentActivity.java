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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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
import com.youTransactor.uCube.rpc.Constants;
import com.youtransactor.sampleapp.R;
import com.youtransactor.sampleapp.UIUtils;

import org.apache.commons.codec.binary.Hex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class PaymentActivity extends AppCompatActivity {

    public static final String TAG = PaymentActivity.class.getName();

    private EditText cardWaitTimeoutFld;
    private Spinner trxTypeChoice;
    private EditText amountFld;
    private Spinner currencyChooser;
    private Switch forceOnlinePINBtn;
    private Switch forceAuthorisationBtn;
    private Switch useSingleEntryPointBtn;
    private Switch amountSrcSwitch;
    private Switch contactOnlySwitch;
    private Switch displayResultSwitch;
    private TextView trxResultFld;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_payment);

        initView();
    }

    private void initView() {

        Button doPaymentBtn = findViewById(R.id.doPaymentBtn);
        cardWaitTimeoutFld = findViewById(R.id.cardWaitTimeoutFld);
        trxResultFld = findViewById(R.id.trxResultFld);
        trxTypeChoice = findViewById(R.id.trxTypeChoice);
        amountFld = findViewById(R.id.amountFld);
        currencyChooser = findViewById(R.id.currencyChooser);
        forceOnlinePINBtn = findViewById(R.id.forceOnlinePINBtn);
        amountSrcSwitch = findViewById(R.id.amountSrcBtn);
        contactOnlySwitch = findViewById(R.id.contactOnlyBtn);
        forceAuthorisationBtn = findViewById(R.id.forceAuthorisationBtn);
        useSingleEntryPointBtn = findViewById(R.id.useSingleEntryPointBtn);
        displayResultSwitch = findViewById(R.id.displayResultOnUCubeBtn);

        amountFld.setText(getString(R.string._1_00));
        trxTypeChoice.setAdapter(new TransactionTypeAdapter());

        final CurrencyAdapter currencyAdapter = new CurrencyAdapter();
        currencyAdapter.add(UCubePaymentRequest.CURRENCY_EUR);
        currencyAdapter.add(UCubePaymentRequest.CURRENCY_USD);

        currencyChooser.setAdapter(currencyAdapter);
        currencyChooser.setSelection(0);

        amountSrcSwitch.setOnClickListener(v -> amountFld.setEnabled(!amountSrcSwitch.isChecked()));

        doPaymentBtn.setOnClickListener(v -> startPayment());
    }

    private void startPayment() {

        int timeout = Integer.valueOf(cardWaitTimeoutFld.getText().toString());

        Currency currency = (Currency) currencyChooser.getSelectedItem();

        TransactionType trxType = (TransactionType) trxTypeChoice.getSelectedItem();

        boolean forceOnlinePin = forceOnlinePINBtn.isChecked(); // only for NFC & MSR

        boolean forceAuthorisation = forceAuthorisationBtn.isChecked();

        boolean useSingleEntryPoint = useSingleEntryPointBtn.isChecked();

        boolean enterAmountOnuCube = amountSrcSwitch.isChecked();

        boolean contactOnly = contactOnlySwitch.isChecked();

        boolean displayResultOnUCube = displayResultSwitch.isChecked();

        double amount = -1;

        String msg;

        if (!enterAmountOnuCube) {

            try {
                amount = Double.parseDouble(amountFld.getText().toString());

                msg = getString(R.string.payment_progress_with_amount, amount, currency.getLabel());

            } catch (Exception e) {

                amountSrcSwitch.setChecked(true);

                msg = getString(R.string.payment_progress_without_amount, currency.getLabel());
            }

        } else {
            msg = getString(R.string.payment_progress_without_amount, currency.getLabel());
        }

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
            altMsgBundle.putString("GLOBAL_centered", "FF");
            altMsgBundle.putString("GLOBAL_yposition", "0C");
            altMsgBundle.putString("GLOBAL_font_id", "00");
        }

        UIUtils.showProgress(this, msg);

        List<CardReaderType> readerList = new ArrayList<>();
        readerList.add(CardReaderType.ICC);
        readerList.add(CardReaderType.MSR);

        if (!contactOnly)
            readerList.add(CardReaderType.NFC);

        UCubePaymentRequest paymentRequest = new UCubePaymentRequest.Builder()
                .setAmount(amount)
                .setCurrency(currency)
                // .setTransactionDate(new Date())
                .setDisplayResult(displayResultOnUCube)
                .setUseSingleEntryPoint(useSingleEntryPoint)
                .setReaderList(readerList)
                .setForceOnlinePin(forceOnlinePin)
                .setForceAuthorisation(forceAuthorisation)
                .setAuthorizationTask(new AuthorizationTask(this))
                .setRiskManagementTask(new RiskManagementTask(this))
                .setCardWaitTimeout(timeout)
                .setTransactionType(trxType)
                // .setSystemFailureInfo(true)
                .setSystemFailureInfo2(true)
                .setMsgBundle(msgBundle)
                .setAltMsgBundle(altMsgBundle)
                .setPreferredLanguageList(Collections.singletonList("en")) // each language represented by 2 alphabetical characters according to ISO 639
                .setRequestedAuthorizationTagList(Constants.TAG_TVR, Constants.TAG_TSI)
                .setRequestedSecuredTagList(Constants.TAG_TRACK2_EQU_DATA)
                .setRequestedPlainTagList(Constants.TAG_MSR_BIN)
                .build();


        try {
            UCubeAPI.pay(this, paymentRequest,
                    new UCubeLibPaymentServiceListener() {

                @Override
                public void onProgress(PaymentState state, PaymentContext context) {

                    Toast.makeText(PaymentActivity.this, state.name(), Toast.LENGTH_SHORT).show();
                    /*
                        // COMMON STATES
                        CANCEL_ALL,
                        GET_INFO,
                        WAIT_CARD, //Contact only state
                        ENTER_SECURE_SESSION,
                        KSN_AVAILABLE,

                        // SMC STATES
                        SMC_BUILD_CANDIDATE_LIST,
                        SMC_SELECT_APPLICATION,
                        SMC_USER_SELECT_APPLICATION,
                        SMC_INIT_TRANSACTION,
                        SMC_RISK_MANAGEMENT,
                        SMC_PROCESS_TRANSACTION,
                        SMC_FINALIZE_TRANSACTION,
                        SMC_REMOVE_CARD,

                        // MSR STATES
                        MSR_GET_SECURED_TAGS,
                        MSR_GET_PLAIN_TAGS,
                        MSR_ONLINE_PIN,

                        // SingleEntryPoint / NFC STATES
                        START_NFC_TRANSACTION,
                        NFC_GET_SECURED_TAGS,
                        NFC_GET_PLAIN_TAGS,
                        COMPLETE_NFC_TRANSACTION,

                        // COMMON STATES
                        AUTHORIZATION,
                        EXIT_SECURE_SESSION,
                        DISPLAY_RESULT,
                        GET_L1_LOG,
                        GET_L2_LOG,
                    */
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

                    UIUtils.hideProgressDialog();

                    if (status && context != null) {
                        Log.d(TAG, "Payment status : " + context.getPaymentStatus());

                        trxResultFld.setText(context.getPaymentStatus().name());

                        /* uCube info */
                        byte[] ucubeFirmware = TLV.parse(context.getuCubeInfos()).get(Constants.TAG_FIRMWARE_VERSION);
                        if(ucubeFirmware != null)
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

                        Log.d(TAG, "system failure log1: " + bytesToHex(context.getSystemFailureInfo()));
                        Log.d(TAG, "system failure log2: " + bytesToHex(context.getSystemFailureInfo2()));

                        if (context.getPlainTagTLV() != null) {

                            for (Integer tag :context.getPlainTagTLV().keySet())
                            Log.d(TAG, "Plain Tag : " + tag + " : " + bytesToHex(context.getPlainTagTLV().get(tag)));

                        }
                        if (context.getSecuredTagBlock() != null)
                            Log.d(TAG, "secure tag block: " + bytesToHex(context.getSecuredTagBlock()));

                    } else {
                        UIUtils.showMessageDialog(PaymentActivity.this, getString(R.string.payment_failed));
                    }
                }
            });

        } catch (Exception e) {

            e.printStackTrace();

            UIUtils.hideProgressDialog();
        }
    }

    private String bytesToHex(byte[] bytes) {
        return bytes == null || bytes.length == 0 ? "" : new String(Hex.encodeHex(bytes)).toUpperCase();
    }
}
