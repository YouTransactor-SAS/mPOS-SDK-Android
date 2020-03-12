/*
 * Copyright (C) 2020, YouTransactor. All Rights Reserved.
 *
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youtransactor.sampleapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.youTransactor.uCube.api.UCubeAPI;
import com.youTransactor.uCube.api.UCubePaymentListener;
import com.youTransactor.uCube.api.UCubePaymentResponse;
import com.youTransactor.uCube.payment.Currency;
import com.youTransactor.uCube.payment.TransactionType;
import com.youTransactor.uCube.api.UCubePaymentRequest;
import com.youTransactor.uCube.rpc.Constants;

import com.youtransactor.sampleapp.adapter.CurrencyAdapter;
import com.youtransactor.sampleapp.adapter.TransactionTypeAdapter;
import com.youtransactor.sampleapp.task.AuthorizationTask;
import com.youtransactor.sampleapp.task.RiskManagementTask;

import org.apache.commons.codec.binary.Hex;

import java.util.Arrays;
import java.util.Collections;

public class PaymentActivity extends AppCompatActivity {

    public static final String TAG = PaymentActivity.class.getName();

    private EditText cardWaitTimeoutFld;
    private Spinner trxTypeChoice;
    private EditText amountFld;
    private Spinner currencyChooser;
    private Switch forceOnlinePINBtn;
    private Switch amountSrcSwitch;
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

        boolean forceOnline = forceOnlinePINBtn.isChecked();

        boolean enterAmountOnuCube = amountSrcSwitch.isChecked();


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

        UIUtils.showProgress(this,  msg);

        UCubePaymentRequest paymentRequest = new UCubePaymentRequest.Builder()
                .setAmount(amount)
                .setCurrency(currency)
                .setForceOnlinePin(forceOnline)
                .setAuthorizationTask(new AuthorizationTask(this))
                .setRiskManagementTask(new RiskManagementTask(this))
                .setCardWaitTimeout(timeout)
                .setTransactionType(trxType)
                .setSystemFailureInfo(false)
                .setSystemFailureInfo2(false)
                .setPreferredLanguageList(Collections.singletonList("en")) // each language represented by 2 alphabetical characters according to ISO 639
                .setRequestedAuthorizationTagList(Constants.TAG_TVR, Constants.TAG_TSI)
                .setRequestedSecuredTagList(Constants.TAG_TRACK2_EQU_DATA)
                .setRequestedPlainTagList(Constants.TAG_MSR_BIN)
                .build();

        try {
            UCubeAPI.pay(this, paymentRequest, new UCubePaymentListener() {
                @Override
                public void onStart(byte[] ksn) {

                    Log.d(TAG, "KSN : " + Arrays.toString(ksn));

                    //TODO Send KSN to the acquirer server
                }

                @Override
                public void onFinish(boolean status, UCubePaymentResponse uCubePaymentResponse) {
                    UIUtils.hideProgressDialog();

                    Log.d(TAG, "payment status - " + status);

                    if (status && uCubePaymentResponse != null) {
                        Log.d(TAG, "Payment state : " + uCubePaymentResponse.paymentContext.getPaymentStatus());

                        trxResultFld.setText(uCubePaymentResponse.paymentContext.getPaymentStatus().name());

                        Log.d(TAG, "ucube name: " + uCubePaymentResponse.uCube.ucubeName);
                        Log.d(TAG, "ucube address: " + uCubePaymentResponse.uCube.ucubeAddress);
                        Log.d(TAG, "ucube part number: " + uCubePaymentResponse.uCube.ucubePartNumber);
                        Log.d(TAG, "ucube serial number: " + uCubePaymentResponse.uCube.ucubeSerialNumber);

                        Log.d(TAG, "card label: " + uCubePaymentResponse.cardLabel);

                        Log.d(TAG, "amount: " + uCubePaymentResponse.paymentContext.getAmount());
                        Log.d(TAG, "currency: " + uCubePaymentResponse.paymentContext.getCurrency().getLabel());
                        Log.d(TAG, "tx date: " + uCubePaymentResponse.paymentContext.getTransactionDate());
                        Log.d(TAG, "tx type: " + uCubePaymentResponse.paymentContext.getTransactionType().getLabel());

                        if (uCubePaymentResponse.paymentContext.getSelectedApplication() != null) {
                            Log.d(TAG, "app ID: " + uCubePaymentResponse.paymentContext.getSelectedApplication().getLabel());
                            Log.d(TAG, "app version: " + uCubePaymentResponse.paymentContext.getApplicationVersion());
                        }

                        Log.d(TAG, "system failure log1: " + bytesToHex(uCubePaymentResponse.paymentContext.getSystemFailureInfo()));
                        Log.d(TAG, "system failure log2: " + bytesToHex(uCubePaymentResponse.paymentContext.getSystemFailureInfo2()));

                        if (uCubePaymentResponse.paymentContext.getPlainTagTLV() != null)
                            for (Integer tag : uCubePaymentResponse.paymentContext.getPlainTagTLV().keySet())
                                Log.d(TAG, "Plain Tag : " + tag + " : " + bytesToHex(uCubePaymentResponse.paymentContext.getPlainTagTLV().get(tag)));

                        if (uCubePaymentResponse.paymentContext.getSecuredTagBlock() != null)
                            Log.d(TAG, "secure tag block: " + bytesToHex(uCubePaymentResponse.paymentContext.getSecuredTagBlock()));

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
