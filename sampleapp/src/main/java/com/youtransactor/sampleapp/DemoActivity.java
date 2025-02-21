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

import static com.youTransactor.uCube.log.LogManager.LogLevel.SYSTEM;
import static com.youTransactor.uCube.rpc.Constants.EMVTag.TAG_SECURE_56_TRACK_1_DATA;
import static com.youTransactor.uCube.rpc.Constants.EMVTag.TAG_SECURE_57_TRACK_2_EQUIVALENT_DATA;
import static com.youTransactor.uCube.rpc.Constants.EMVTag.TAG_SECURE_5A_APPLICATION_PRIMARY_ACCOUNT_NUMBER;
import static com.youTransactor.uCube.rpc.Constants.EMVTag.TAG_SECURE_5F20_CARDHOLDER_NAME;
import static com.youTransactor.uCube.rpc.Constants.EMVTag.TAG_SECURE_5F24_APPLICATION_EXPIRATION_DATE;
import static com.youTransactor.uCube.rpc.Constants.EMVTag.TAG_SECURE_5F30_SERVICE_CODE;
import static com.youTransactor.uCube.rpc.Constants.EMVTag.TAG_SECURE_9F0B_CARDHOLDER_NAME_EXTENDED;
import static com.youTransactor.uCube.rpc.Constants.EMVTag.TAG_SECURE_9F6B_TRACK_2_DATA;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.youTransactor.uCube.api.UCubeAPI;
import com.youTransactor.uCube.api.UCubeLibPaymentServiceListener;
import com.youTransactor.uCube.api.UCubePaymentRequest;
import com.youTransactor.uCube.api.YTMPOSProduct;
import com.youTransactor.uCube.connexion.ConnectionListener;
import com.youTransactor.uCube.connexion.ConnectionService;
import com.youTransactor.uCube.connexion.ConnectionStatus;
import com.youTransactor.uCube.payment.PaymentContext;
import com.youTransactor.uCube.payment.PaymentService;
import com.youTransactor.uCube.payment.PaymentState;
import com.youTransactor.uCube.rpc.CardReaderType;
import com.youTransactor.uCube.rpc.Currency;
import com.youTransactor.uCube.rpc.TransactionType;
import com.youtransactor.sampleapp.payment.AuthorizationTask;
import com.youtransactor.sampleapp.payment.PaymentMeasure;
import com.youtransactor.sampleapp.transactionView.TransactionViewBase;
import com.youtransactor.sampleapp.payment.Localization;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class DemoActivity extends TransactionViewBase {
    private TextView textViewAmount;
    private Button buttonKey1;
    private Button buttonKey2;
    private Button buttonKey3;
    private Button buttonKey4;
    private Button buttonKey5;
    private Button buttonKey6;
    private Button buttonKey7;
    private Button buttonKey8;
    private Button buttonKey9;
    private Button buttonKey0;
    private Button buttonKey00;
    private Button buttonDel;
    private Button buttonPay;

    private CheckBox checkBoxInterfaceNFC;
    private CheckBox checkBoxInterfaceSMC;
    private CheckBox checkBoxInterfaceMSR;


    private String amountStr = "";

    private PaymentMeasure paymentMeasure;
    private PaymentService paymentService;

    @Override
    protected void onDestroy() {
        UCubeAPI.close();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.amountStr = "";
        this.amountUpdate();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setHomeActivity(this.getClass());

        setContentView(R.layout.activity_demo);
        getSupportActionBar().hide();

        String versionName = BuildConfig.VERSION_NAME;
        TextView versionNametv = findViewById(R.id.version_name);
        versionNametv.setText(getString(R.string.versionName, versionName));

        UCubeAPI.setLogLevel(SYSTEM);
        UCubeAPI.setYTmPOSProduct(YTMPOSProduct.AndroidPOS);
        UCubeAPI.setConnexionManagerType(ConnectionService.ConnectionManagerType.PAYMENT_SERVICE);
        Localization.Localization_init(UCubeAPI.getContext());
        UCubeAPI.getConnexionManager().connect(
                10 * 1000,
                3,
                new ConnectionListener() {
                    @Override
                    public void onConnectionFailed(ConnectionStatus status, int error) {}
                    @Override
                    public void onConnectionSuccess() {}
                    @Override
                    public void onConnectionCancelled() {}
                });

        textViewAmount = findViewById(R.id.textViewAmount);
        buttonKey1 = findViewById(R.id.button1);
        buttonKey1.setOnClickListener(v -> amountAppendStr(buttonKey1.getText().toString()));
        buttonKey2 = findViewById(R.id.button2);
        buttonKey2.setOnClickListener(v -> amountAppendStr(buttonKey2.getText().toString()));
        buttonKey3 = findViewById(R.id.button3);
        buttonKey3.setOnClickListener(v -> amountAppendStr(buttonKey3.getText().toString()));
        buttonKey4 = findViewById(R.id.button4);
        buttonKey4.setOnClickListener(v -> amountAppendStr(buttonKey4.getText().toString()));
        buttonKey5 = findViewById(R.id.button5);
        buttonKey5.setOnClickListener(v -> amountAppendStr(buttonKey5.getText().toString()));
        buttonKey6 = findViewById(R.id.button6);
        buttonKey6.setOnClickListener(v -> amountAppendStr(buttonKey6.getText().toString()));
        buttonKey7 = findViewById(R.id.button7);
        buttonKey7.setOnClickListener(v -> amountAppendStr(buttonKey7.getText().toString()));
        buttonKey8 = findViewById(R.id.button8);
        buttonKey8.setOnClickListener(v -> amountAppendStr(buttonKey8.getText().toString()));
        buttonKey9 = findViewById(R.id.button9);
        buttonKey9.setOnClickListener(v -> amountAppendStr(buttonKey9.getText().toString()));
        buttonKey0 = findViewById(R.id.button0);
        buttonKey0.setOnClickListener(v -> amountAppendStr(buttonKey0.getText().toString()));
        buttonKey00 = findViewById(R.id.button00);
        buttonKey00.setOnClickListener(v -> amountAppendStr(buttonKey00.getText().toString()));
        buttonDel = findViewById(R.id.buttonDel);
        buttonDel.setOnClickListener(v -> amountDeleteChar());
        buttonPay = findViewById(R.id.buttonPay);
        buttonPay.setOnClickListener(v -> pay());
        checkBoxInterfaceNFC = findViewById(R.id.checkBoxInterfaceNFC);
        checkBoxInterfaceNFC.setChecked(true);
        checkBoxInterfaceSMC = findViewById(R.id.checkBoxInterfaceSMC);
        checkBoxInterfaceSMC.setChecked(true);
        checkBoxInterfaceMSR = findViewById(R.id.checkBoxInterfaceMSR);
        checkBoxInterfaceMSR.setClickable(true);
    }

    private void amountAppendStr(String str) {
        // Amount limited for GUI
        if (((amountStr.length() + str.length()) < 8) &&
            !(str.matches("^0*$") && amountStr.matches("^0*$"))) {
            amountStr = amountStr + str;
            amountUpdate();
        }
    }

    private void amountDeleteChar() {
        if (amountStr != null && !amountStr.isEmpty()) {
            amountStr = amountStr.substring(0, amountStr.length() - 1);
        }
        amountUpdate();
    }

    private void amountUpdate() {
        String amountStrFormatted = amountStr;
        int amountStrLen = amountStrFormatted.length();
        for (int i = 0; i < (3 - amountStrLen); i++) {
            amountStrFormatted = "0" + amountStrFormatted;
        }
        int dotPosition = amountStrFormatted.length() - 2;
        amountStrFormatted = "USD " + amountStrFormatted.substring(0, dotPosition) + "."
                             + amountStrFormatted.substring(dotPosition);
        textViewAmount.setText(amountStrFormatted);
    }

    private void pay() {
        new Thread(() -> {
            try {
                UCubePaymentRequest uCubePaymentRequest = preparePaymentRequest();
                paymentService = UCubeAPI.pay(uCubePaymentRequest,
                                              new UCubeLibPaymentServiceListener() {

                                                  @Override
                                                  public void onProgress(PaymentState state, PaymentContext context) {
                                                      // todo No RPC call here
                                                  }

                                                  @Override
                                                  public void onFinish(PaymentContext context) {

                                                  }
                                              });

            } catch (Exception e) {
                e.printStackTrace();

            }
        }).start();
    }

    private UCubePaymentRequest preparePaymentRequest() {
        int timeout = 1000;
        Currency currency = UCubePaymentRequest.CURRENCY_USD;
        TransactionType trxType = TransactionType.PURCHASE;
        boolean forceOnlinePin = false;
        boolean bypassAuthorisation = true;
        boolean tipRequired = false;
        boolean retrieveF5Tag = false;
        long amount = Integer.valueOf(this.amountStr);
        List<CardReaderType> readerList = new ArrayList<>();

        if (checkBoxInterfaceNFC.isChecked()) {
            readerList.add(CardReaderType.NFC);
        }
        if (checkBoxInterfaceSMC.isChecked()) {
            readerList.add(CardReaderType.ICC);
        }
        if (checkBoxInterfaceMSR.isChecked()) {
            readerList.add(CardReaderType.MSR);
        }

        AuthorizationTask authorizationTask = new AuthorizationTask(this);
        authorizationTask.setMeasureStatesListener(paymentMeasure);

        UCubePaymentRequest uCubePaymentRequest = new UCubePaymentRequest(amount, currency, trxType,
                                                                          readerList, authorizationTask, Collections.singletonList("en"));

        //Add optional variables
        uCubePaymentRequest
                .setForceOnlinePin(forceOnlinePin)
                .setTransactionDate(new Date())
                .setBypassAuthorisation(bypassAuthorisation)
//                .setRiskManagementTask(new RiskManagementTask(this))
                .setCardWaitTimeout(timeout)
                .setSkipCardRemoval(true)
//                .setSkipStartingSteps(true)
//                .setForceDebug(true)
                .setRetrieveF5Tag(retrieveF5Tag)
                .setTipRequired(tipRequired)
                .setPinRequestLabel("Pin ?")
                .setPinRequestLabelFont(1)
                .setPinRequestLabelXPosition((byte) 0xFF)
                .setDataEncryptionMechanism(2)
//                .setDataEncryptionMechanism(sdseSwitch.isChecked() ? 1 : -1)

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

        return uCubePaymentRequest;
    }
}
