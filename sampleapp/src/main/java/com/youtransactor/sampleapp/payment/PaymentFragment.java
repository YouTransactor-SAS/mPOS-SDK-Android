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

import static com.youTransactor.uCube.rpc.Constants.EMVTag.TAG_5F24_APPLICATION_EXPIRATION_DATE;
import static com.youTransactor.uCube.rpc.Constants.EMVTag.TAG_5F30_SERVICE_CODE;
import static com.youTransactor.uCube.rpc.Constants.EMVTag.TAG_SECURE_56_TRACK_1_DATA;
import static com.youTransactor.uCube.rpc.Constants.EMVTag.TAG_SECURE_57_TRACK_2_EQUIVALENT_DATA;
import static com.youTransactor.uCube.rpc.Constants.EMVTag.TAG_SECURE_5A_APPLICATION_PRIMARY_ACCOUNT_NUMBER;
import static com.youTransactor.uCube.rpc.Constants.EMVTag.TAG_5F20_CARDHOLDER_NAME;
import static com.youTransactor.uCube.rpc.Constants.EMVTag.TAG_9F0B_CARDHOLDER_NAME_EXTENDED;
import static com.youTransactor.uCube.rpc.Constants.EMVTag.TAG_SECURE_9F6B_TRACK_2_DATA;
import static com.youTransactor.uCube.rpc.Constants.TAG_EMV_TEMPLATE_73;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jps.secureService.api.entity.ViewIdentifier;
import com.jps.secureService.api.product_manager.ProductIdentifier;
import com.jps.secureService.api.product_manager.ProductManager;
import com.youTransactor.uCube.ITaskMonitor;
import com.youTransactor.uCube.TLV;
import com.youTransactor.uCube.TaskEvent;
import com.youTransactor.uCube.Tools;
import com.youTransactor.uCube.api.UCubeAPI;
import com.youTransactor.uCube.api.UCubeLibPaymentServiceListener;
import com.youTransactor.uCube.api.UCubePaymentRequest;
import com.youTransactor.uCube.connexion.IConnexionManager;
import com.youTransactor.uCube.payment.PaymentContext;
import com.youTransactor.uCube.payment.PaymentService;
import com.youTransactor.uCube.payment.PaymentState;
import com.youTransactor.uCube.payment.PaymentStatus;
import com.youTransactor.uCube.payment.PaymentUtils;
import com.youTransactor.uCube.payment.task.IAuthorizationTask;
import com.youTransactor.uCube.rpc.CardReaderType;
import com.youTransactor.uCube.rpc.Constants;
import com.youTransactor.uCube.rpc.Currency;
import com.youTransactor.uCube.rpc.OnlinePinBlockFormatType;
import com.youTransactor.uCube.rpc.RPCCommand;
import com.youTransactor.uCube.rpc.TransactionType;
import com.youTransactor.uCube.rpc.command.CancelCommand;
import com.youTransactor.uCube.rpc.command.EnterSecureSessionCommand;
import com.youTransactor.uCube.rpc.command.ExitSecureSessionCommand;
import com.youTransactor.uCube.rpc.command.GetInfosCommand;
import com.youTransactor.uCube.rpc.command.GetStatusCommand;
import com.youtransactor.sampleapp.R;
import com.youtransactor.sampleapp.SetupActivity;
import com.youtransactor.sampleapp.UIUtils;
import com.youtransactor.sampleapp.payment.authorization.BypassAuthorizationTask;
import com.youtransactor.sampleapp.payment.authorization.UserChoiceAuthorizationTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

public class PaymentFragment extends Fragment {
    public static final String SDSE_MODE_PREF_NAME = "SDSE mode";

    public static final String TAG = PaymentFragment.class.getName();

    private SharedPreferences prefs;
    private Button doPaymentBtn;
    private Button cancelPaymentBtn;
    private Button getLogsL1, getStatusBtn;
    private EditText cardWaitTimeoutFld;
    private EditText posEntryModeFld;
    private EditText dukpt_key_slotFld;
    private Spinner trxTypeChoice;
    private CurrencyEditText amountFld;
    private CurrencyEditText cashbackAmountFld;
    private Spinner currencyChooser;
    private Switch forceOnlinePINBtn;
    private Spinner onlinePinBlockFormatChoice;
    private Switch forceAuthorisationBtn;
    private Switch keepSecureSessionBtn;
    private Switch amountSrcSwitch;
    private Switch contactItf;
    private Switch nfcItf;
    private Switch msrItf;
    private Switch forceDebugSwitch;
    private Switch allowPinBypassSwitch;
    private Switch skipCardRemovalSwitch;
    private Switch skipStartingStepsSwitch;
    private Switch retrieveF5TagSwitch;
    private Switch tipSwitch;
    private Switch paymentLoopSwitch;
    private Switch emvParamOverride;

    private TextView trxResultFld;
    private EditText startCancelDelayEditText;
    private Button uPresentCard;
    private Button uEnterPin;
    private Button crEnterPin;
    private Spinner sdse_mode_spinner;
    private LinearLayout measurementSection;
    private PaymentState autoCancelState;
    private int startCancelDelay;
    private PaymentState autoDisconnectState;
    private boolean testModeEnabled = false;
    private boolean measureModeEnabled;
    private PaymentService paymentService;

    private PaymentMeasure paymentMeasure;

    private final Activity underlyingActivity;

    public PaymentFragment(Activity underlyingActivity) {
        this.underlyingActivity = underlyingActivity;
    }

    public enum pay_sdse_mode {
        SRED(0),
        SDSE_RFU(1),
        VOLTAGE(2);
        private final int code;

        pay_sdse_mode(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    private static pay_sdse_mode sdse_mode = pay_sdse_mode.VOLTAGE;
    private boolean forceDebug;
    private boolean isPinBypassAllowed = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = underlyingActivity.getSharedPreferences(SetupActivity.SETUP_SHARED_PREF_NAME, Context.MODE_PRIVATE);
        testModeEnabled = prefs.getBoolean(SetupActivity.TEST_MODE_PREF_NAME, false);
        measureModeEnabled = prefs.getBoolean(SetupActivity.MEASURES_MODE_PREF_NAME, false);
        if (measureModeEnabled) {
            paymentMeasure = new PaymentMeasure();
        }

        View view = inflater.inflate(R.layout.fragment_payment, container, false);

        initView(view);
        return view;
    }

    private void initView(final View view) {

        view.findViewById(R.id.test_section).setVisibility(testModeEnabled ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.measures_section).setVisibility(measureModeEnabled ? View.VISIBLE : View.GONE);

        doPaymentBtn =  view.findViewById(R.id.doPaymentBtn);
        cancelPaymentBtn =  view.findViewById(R.id.cancelPaymentBtn);
        cardWaitTimeoutFld =  view.findViewById(R.id.cardWaitTimeoutFld);
        posEntryModeFld =  view.findViewById(R.id.posEntryModeFld);
        dukpt_key_slotFld =  view.findViewById(R.id.dukpt_slotFld);

        trxTypeChoice =  view.findViewById(R.id.trxTypeChoice);
        onlinePinBlockFormatChoice =  view.findViewById(R.id.onlinePinBlockFormatChoice);
        onlinePinBlockFormatChoice.setAdapter(new ArrayAdapter<>(
                underlyingActivity,
                android.R.layout.simple_spinner_item,
                OnlinePinBlockFormatType.values()
        ));
        amountFld =  view.findViewById(R.id.amountFld);
        cashbackAmountFld =  view.findViewById(R.id.cashbackAmountFld);
        currencyChooser =  view.findViewById(R.id.currencyChooser);
        forceOnlinePINBtn =  view.findViewById(R.id.forceOnlinePINSwitch);
        amountSrcSwitch =  view.findViewById(R.id.amountSrcSwitch);
        contactItf =  view.findViewById(R.id.contact_itf);
        nfcItf =  view.findViewById(R.id.nfc_itf);
        msrItf =  view.findViewById(R.id.msr_itf);
        forceAuthorisationBtn =  view.findViewById(R.id.forceAuthorisationSwitch);
        keepSecureSessionBtn =  view.findViewById(R.id.keepSecureSessionSwitch);
        forceDebugSwitch =  view.findViewById(R.id.forceDebugSwitch);
        allowPinBypassSwitch =  view.findViewById(R.id.allowPinBypassSwitch);
        trxResultFld =  view.findViewById(R.id.trxResultFld);
        startCancelDelayEditText =  view.findViewById(R.id.start_cancel_delay);
        skipCardRemovalSwitch =  view.findViewById(R.id.skipCardRemovalSwitch);
        skipStartingStepsSwitch =  view.findViewById(R.id.skipStartingStepsSwitch);
        retrieveF5TagSwitch =  view.findViewById(R.id.retrieveF5Tag);
        tipSwitch =  view.findViewById(R.id.tipSwitch);
        paymentLoopSwitch =  view.findViewById(R.id.paymentLoopSwitch);
        trxTypeChoice.setAdapter(new TransactionTypeAdapter());
        getLogsL1 =  view.findViewById(R.id.getSvppLogL1);
        getStatusBtn =  view.findViewById(R.id.getStatusBtn);
        uPresentCard =  view.findViewById(R.id.u_present_card);
        uEnterPin =  view.findViewById(R.id.u_enter_pin);
        crEnterPin =  view.findViewById(R.id.cr_enter_pin);
        measurementSection =  view.findViewById(R.id.measurement_section);
        sdse_mode_spinner =  view.findViewById(R.id.sdse_mode_spinner);
        sdse_mode_spinner.setAdapter(new ArrayAdapter<>(
                underlyingActivity,
                android.R.layout.simple_spinner_item,
                pay_sdse_mode.values()
        ));
        sdse_mode_spinner.setSelection(2);
        emvParamOverride =  view.findViewById(R.id.emvParameterOverride);
        sdse_mode_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                prefs.edit().putInt(SDSE_MODE_PREF_NAME, ((pay_sdse_mode) sdse_mode_spinner.getSelectedItem()).getCode()).apply();
                sdse_mode = (pay_sdse_mode) sdse_mode_spinner.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        final CurrencyAdapter currencyAdapter = new CurrencyAdapter();
        currencyAdapter.add(UCubePaymentRequest.CURRENCY_EUR);
        currencyAdapter.add(UCubePaymentRequest.CURRENCY_USD);
        currencyAdapter.add(UCubePaymentRequest.CURRENCY_CAD);
        currencyAdapter.add(UCubePaymentRequest.CURRENCY_GBP);
        currencyAdapter.add(UCubePaymentRequest.CURRENCY_TWD);

        currencyChooser.setAdapter(currencyAdapter);
        currencyChooser.setSelection(0);

        amountSrcSwitch.setOnClickListener(v -> amountFld.setEnabled(!amountSrcSwitch.isChecked()));

        doPaymentBtn.setOnClickListener(v -> startPayment());

        cancelPaymentBtn.setOnClickListener(v -> cancelPayment(true));
        cancelPaymentBtn.setVisibility(View.GONE);


        final Spinner cancelEventSwitch = view.findViewById(R.id.cancelEventSwitch);
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

        final Spinner disconnectEventSwitch = view.findViewById(R.id.disconnectEventSwitch);
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
        measurementSection.setVisibility(View.GONE);
        trxResultFld.setText("");

        pay();
    }

    private Supplier<Context> getActivityOnWhichAuthorizationPopupWillBeDisplayed() {
        return () -> underlyingActivity;
    }

    private UCubePaymentRequest preparePaymentRequest() {
        int timeout = Integer.parseInt(cardWaitTimeoutFld.getText().toString());

        Currency currency = (Currency) currencyChooser.getSelectedItem();

        TransactionType trxType = (TransactionType) trxTypeChoice.getSelectedItem();

        boolean forceOnlinePin = forceOnlinePINBtn.isChecked(); // only for NFC & MSR

        OnlinePinBlockFormatType onlinePinBlockFormat = (OnlinePinBlockFormatType) onlinePinBlockFormatChoice.getSelectedItem();

        boolean forceAuthorisation = forceAuthorisationBtn.isChecked();
        if (ProductManager.id == ProductIdentifier.stick) {
            forceAuthorisation = false;
        }
        forceDebug = forceDebugSwitch.isChecked();
        isPinBypassAllowed = allowPinBypassSwitch.isChecked();
        boolean keepSecureSession = keepSecureSessionBtn.isChecked();

        boolean skipCardRemoval = skipCardRemovalSwitch.isChecked();

        boolean skipStartingSteps = skipStartingStepsSwitch.isChecked();

        boolean tipRequired = tipSwitch.isChecked();
        boolean msrIsActivate = msrItf.isChecked();
        boolean nfcIsActivate = nfcItf.isChecked();
        boolean contactIsActivate = contactItf.isChecked();
        boolean retrieveF5Tag = retrieveF5TagSwitch.isChecked();
        boolean overrideParameter;
        if(emvParamOverride.isChecked()){
            overrideParameter = true;
            byte[] dynamicParam =
                    PaymentTagOverrideFactory.getClessCVMReqLimit(
                            new byte[] {0x00, 0x00, 0x00, 0x01, 0x00, 0x00});
            PaymentUtils.setEmvClessDynamicParam(Tools.appendBytes(new byte[] {
                            (byte) TAG_EMV_TEMPLATE_73, (byte) dynamicParam.length},
                    dynamicParam));
        }else{
            overrideParameter = false;
            PaymentUtils.setEmvClessDynamicParam(new byte[] {});
        }

        long amount = amountFld.getCleanIntValue();
        boolean loopMode = paymentLoopSwitch.isChecked();
        Log.d(TAG, "Amount : " + amount);
        int cashbackAmount = Math.toIntExact(cashbackAmountFld.getCleanIntValue());
        Log.d(TAG, "Cashback Amount : " + cashbackAmount);

        List<CardReaderType> readerList = new ArrayList<>();

        if (nfcIsActivate) {
            readerList.add(CardReaderType.NFC);
        }
        if (contactIsActivate) {
            readerList.add(CardReaderType.ICC);
        }
        if (msrIsActivate) {
            readerList.add(CardReaderType.MSR);
        }

        IAuthorizationTask authorizationTask;
        if (loopMode) {
            authorizationTask = new BypassAuthorizationTask();
        } else {
            authorizationTask = new UserChoiceAuthorizationTask(this.getActivityOnWhichAuthorizationPopupWillBeDisplayed());
            ((UserChoiceAuthorizationTask) authorizationTask).setMeasureStatesListener(paymentMeasure);
        }

        int posEntryMode = Integer.parseInt(posEntryModeFld.getText().toString());
        int dukpt_key_slot = Integer.parseInt(dukpt_key_slotFld.getText().toString());
        UCubePaymentRequest uCubePaymentRequest = new UCubePaymentRequest(amount, currency, trxType,
                readerList, authorizationTask, Collections.singletonList("en"));

        //Add optional variables
        uCubePaymentRequest
                .setForceOnlinePin(forceOnlinePin)
                .setTransactionDate(new Date())
                .setForceAuthorisation(forceAuthorisation)
                .setKeepSecureSession(keepSecureSession)
                .setOnlinePinBlockFormat(onlinePinBlockFormat)
                // .setRiskManagementTask(new RiskManagementTask(
                //                               this.underlyingActivity))
                //.setBeforeContactlessOnlinePinTask(new BeforeContactlessOnlinePinTaskExample(BeforeContactlessOnlinePinTaskExample.TaskAction.SUCCESS))
                .setCardWaitTimeout(timeout)
                .setForceDebug(forceDebug)
                .setSkipCardRemoval(skipCardRemoval)
                .setSkipStartingSteps(skipStartingSteps)
                .setRetrieveF5Tag(retrieveF5Tag)
                .setTipRequired(tipRequired)
                .setPinRequestLabel("Pin ?")
                .setPinBypassAuthorisation(isPinBypassAllowed)
                .setPinRequestLabelFont(1)
                .setPinRequestLabelXPosition((byte) 0xFF)
                .setDataEncryptionMechanism(sdse_mode.getCode())
                .withViewDelegate(ViewIdentifier.PIN_PROMPT)
                .setPosEntryMode(posEntryMode)
                .setDukptSlotKey(dukpt_key_slot)
                .setUpdateTlvTask(new UpdateTlvTask())
                .setCashbackAmount(cashbackAmount)
                //CLIENT TAGs
                .setAuthorizationPlainTags(
                        0x9C, 0x9F10, 0x9F1A, 0x4F, 0xDF, 0x81, 0x29, 0xD4, 0x9F41, 0xDF02, 0x8E, 0x9F39,
                        0x9F37, 0x9F27, 0x9A, 0x50, 0x95, 0x9F7C, 0x9F71, 0xDF, 0xC302, 0x9F36, 0x9F34,
                        0x9B, 0x9F12, 0x82, 0x9F66, 0x9F26, 0x9F6E, 0xD3, 0x84, 0x9F33, 0x9F06,
                        0x8F, 0x9F02, 0x9F03, 0x9F09, 0x9F1E, 0xDF63, 0x9F34,
                        TAG_5F24_APPLICATION_EXPIRATION_DATE,
                        TAG_5F20_CARDHOLDER_NAME,
                        TAG_9F0B_CARDHOLDER_NAME_EXTENDED)

                .setAuthorizationSecuredTags(
                        TAG_SECURE_5A_APPLICATION_PRIMARY_ACCOUNT_NUMBER,
                        TAG_SECURE_57_TRACK_2_EQUIVALENT_DATA,
                        TAG_SECURE_56_TRACK_1_DATA,
                        TAG_SECURE_9F6B_TRACK_2_DATA

                )
                .setFinalizationPlainTags(
                        0x9C, 0x9F10, 0x9F1A, 0x4F, 0xDF, 0x81, 0x29, 0xD4, 0x9F41, 0xDF02, 0x8E, 0x9F39,
                        0x9F37, 0x9F27, 0x9A, 0x50, 0x95, 0x9F7C, 0x9F71, 0xDF, 0xC302, 0x9F36, 0x9F34,
                        0x9B, 0x9F12, 0x82, 0x9F66, 0x9F26, 0x9F6E, 0xD3, 0x84, 0x9F33, 0x9F06,
                        0x8F, 0x9F02, 0x9F03, 0x9F09, 0x9F1E, 0xDF63, 0x9F34,
                        TAG_5F24_APPLICATION_EXPIRATION_DATE,
                        TAG_5F30_SERVICE_CODE,
                        TAG_5F20_CARDHOLDER_NAME,
                        TAG_9F0B_CARDHOLDER_NAME_EXTENDED)

                .setFinalizationSecuredTags(
                        TAG_SECURE_5A_APPLICATION_PRIMARY_ACCOUNT_NUMBER,
                        TAG_SECURE_57_TRACK_2_EQUIVALENT_DATA,
                        TAG_SECURE_56_TRACK_1_DATA,
                        TAG_SECURE_9F6B_TRACK_2_DATA
                )
                .setOverrideParameter(overrideParameter);
        ;

        return uCubePaymentRequest;
    }

    public void displayProgress(PaymentState state) {
        String msg = state.name();
        underlyingActivity.runOnUiThread(() -> trxResultFld.setText(msg));
    }

    public void displaytxt(String msg) {
        underlyingActivity.runOnUiThread(() -> trxResultFld.setText(msg));
    }

    public boolean isLoopMode() {
        return paymentLoopSwitch.isChecked();
    }

    private void parsePaymentResponse(@NonNull PaymentContext context) {
        Log.d(TAG, "Payment status : " + context.paymentStatus);
        if(context.paymentStatus != null){
            trxResultFld.setText(context.paymentStatus.name());
        }

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
            Log.d(TAG, "finalization plain tags response " + Tools.bytesToHex(context.finalizationGetPlainTagsResponse));

        if (context.finalizationPlainTagsValues != null) {
            for (Integer tag : context.finalizationPlainTagsValues.keySet())
                Log.d(TAG, String.format("finalization Plain Tag : 0x%x : %s", tag, Tools.bytesToHex(context.finalizationPlainTagsValues.get(tag))));
        }

        if (context.finalizationGetSecuredTagsResponse != null)
            Log.d(TAG, "finalization secure tag block: " + Tools.bytesToHex(context.finalizationGetSecuredTagsResponse));

        if (context.finalizationSecuredTagsValues != null) {
            for (Integer tag : context.finalizationSecuredTagsValues.keySet())
                Log.d(TAG, String.format("finalization Secured Tag : 0x%x : %s", tag, Tools.bytesToHex(context.finalizationSecuredTagsValues.get(tag))));
        }
        if (context.pinKsn != null) {
            Log.d(TAG, "pin KSN: " + Tools.bytesToHex(context.pinKsn));
        }

        if (context.onlinePinBlock != null) {
            Log.d(TAG, "pin block: " + Tools.bytesToHex(context.onlinePinBlock));
        }

        Log.d(TAG, "contains Online Pin Challenge Response: " + context.containsOnlinePinChallengeResponse);
    }

    public void cancelPayment(boolean displayUI) {

        if (paymentService != null && paymentService.isRunning()) {
            Log.d(TAG, "Try to cancel current Payment");
            if (displayUI)
                UIUtils.showProgress(underlyingActivity, "Trying cancellation");

            paymentService.cancel(status -> {
                Log.d(TAG, "cancel value : " + status);
                if (displayUI) {
                    underlyingActivity.runOnUiThread(() -> {
                        UIUtils.hideProgressDialog();

                        if (status)
                            Toast.makeText(underlyingActivity, "Cancellation success", Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(underlyingActivity, "Cancellation failed", Toast.LENGTH_LONG).show();
                    });
                }
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
        ITaskMonitor iTaskMonitor = (event, params) -> {
            Log.d(TAG, "event: " + event);

            if (event == TaskEvent.FAILED) {
                String command = "unknown";
                if (params[0] != null)
                    command = "0x" + Integer.toHexString(((RPCCommand) params[0]).getCommandId());

                String finalCommand = command;
                underlyingActivity.runOnUiThread(() -> UIUtils.showMessageDialog(underlyingActivity, getString(R.string.get_cb_command_failed, finalCommand)));
            }
        };
        new CancelCommand().execute(iTaskMonitor);
        new ExitSecureSessionCommand().execute(iTaskMonitor);
        new EnterSecureSessionCommand().execute(iTaskMonitor);
        new GetInfosCommand(Constants.TAG_SYSTEM_FAILURE_LOG_RECORD_1).execute(iTaskMonitor);
        new ExitSecureSessionCommand().execute((event, params) -> {
            if (event == TaskEvent.FAILED) {
                underlyingActivity.runOnUiThread(() -> UIUtils.showMessageDialog(underlyingActivity,
                        getString(R.string.get_cb_command_failed, "0x5102")));
            } else if (event == TaskEvent.SUCCESS) {
                underlyingActivity.runOnUiThread(() -> UIUtils.showMessageDialog(underlyingActivity,
                        getString(R.string.get_cb_command_success)));
            }
        });
    }

    private void getStatus() {
        new GetStatusCommand().execute((event, params) -> Log.d(TAG, "getStatus event:" + event));
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

                                underlyingActivity.runOnUiThread(() -> {
                                    Log.d(TAG, " Payment progress : " + state);

                                    displayProgress(state);

                                    switch (state) {
                                        case ENTER_SECURE_SESSION:
                                            if (paymentMeasure != null)
                                                paymentMeasure.onStart();
                                            break;

                                        case KSN_AVAILABLE:
                                            Log.d(TAG, "KSN : " + Arrays.toString(context.sredKsn));
                                            break;

                                        case START_TRANSACTION:
                                            if (paymentMeasure != null)
                                                paymentMeasure.onWaitingCard();
                                            break;

                                        case AUTHORIZATION:
                                            if (paymentMeasure != null)
                                                paymentMeasure.onAuthorizationCalled();
                                            break;

                                        case OFFLINE_PIN:
                                        case ONLINE_PIN:
                                            if (paymentMeasure != null)
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
                                        Log.d(TAG, "start cancel delay : " + startCancelDelay);

                                        new Handler(Looper.getMainLooper()).postDelayed(() -> cancelPayment(true), startCancelDelay);
                                    }

                                    if (state == autoDisconnectState) {
                                        disconnect();
                                    }
                                });
                            }

                            @Override
                            public void onFinish(PaymentContext context) {
                                underlyingActivity.runOnUiThread(() -> {
                                    if (context == null) {
                                        UIUtils.showMessageDialog(underlyingActivity, getString(R.string.payment_failed));
                                        return;
                                    }

                                    Log.d(TAG, "payment finish status : " + context.paymentStatus);

                                    if (paymentMeasure != null) {
                                        paymentMeasure.onFinish();
                                        displayMeasures(context);
                                    }

                                    if (forceDebug & (context.tagF4 == null || context.tagF4.length <= 21)) {
                                        underlyingActivity.runOnUiThread(() -> UIUtils.showMessageDialog(underlyingActivity, getString(R.string.F4_tag_is_empty)));
                                    }

                                    parsePaymentResponse(context);

                                    boolean triggerNewPayment = paymentLoopSwitch.isChecked() && context.paymentStatus == PaymentStatus.APPROVED;
                                    if (triggerNewPayment) {
                                        startPayment();
                                    } else {
                                        doPaymentBtn.setVisibility(View.VISIBLE);
                                        cancelPaymentBtn.setVisibility(View.GONE);
                                    }

                                });

                            }
                        });

            } catch (Exception e) {
                e.printStackTrace();

                underlyingActivity.runOnUiThread(() -> {
                    doPaymentBtn.setVisibility(View.VISIBLE);
                    cancelPaymentBtn.setVisibility(View.GONE);
                });
            }
        }).start();
    }

    private void displayMeasures(PaymentContext context) {
        measurementSection.setVisibility(View.VISIBLE);
        TextView measureStartToCardWait = underlyingActivity.findViewById(R.id.measure_start_to_waiting_card);
        TextView measurePresentToAuth = underlyingActivity.findViewById(R.id.measure_present_card_to_auth);
        TextView measureAuthToFinish = underlyingActivity.findViewById(R.id.measure_auth_resp_to_finish);
        TextView measureEnterPinToAuth = underlyingActivity.findViewById(R.id.measure_user_enter_pin_to_auth);
        TextView measurePresentCardToEnterPin = underlyingActivity.findViewById(R.id.measure_present_card_to_cr_enter_pin);

        String measurement;
        if (context.activatedReader == CardReaderType.NFC.getCode()) {
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
        } else {
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
        measurementSection.setVisibility(View.VISIBLE);
    }
}
