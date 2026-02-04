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
import static com.youTransactor.uCube.rpc.Constants.EMVTag.TAG_SECURE_DF5A_PAN_CVV_DATA;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
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
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class PaymentFragment extends Fragment {
    public static final String SDSE_MODE_PREF_NAME = "SDSE mode";

    public static final String TAG = PaymentFragment.class.getName();

    private SharedPreferences prefs;
    private Button doPaymentBtn;
    private Button cancelPaymentBtn;
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
    private Spinner sdse_mode_spinner;
    private LinearLayout measurementSection;
    private View cashBackSection;
    private PaymentState autoCancelState;
    private int startCancelDelay;
    private PaymentState autoDisconnectState;
    private boolean testModeEnabled = false;
    private boolean measureModeEnabled;
    private PaymentService paymentService;
    private PaymentMeasure paymentMeasure;
    private final PaymentSettings paymentSettings = new PaymentSettings();
    private final Activity underlyingActivity;
    private int lastTabSelection = -1;

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
        initTabLayout(view);
        return view;
    }

    private void initTabLayout(final View view) {
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        ViewPager2 viewPager = view.findViewById(R.id.viewPager);

        PaymentTabAdapter adapter = new PaymentTabAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText(R.string.payment_tab_title);
            } else {
                tab.setText(R.string.payment_settings_tab_title);
            }
        }).attach();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (lastTabSelection == 1) {
                    updatePaymentSettingsFromViews();
                }
                lastTabSelection = position;
            }
        });
    }

    public void initPaymentTabViews(final View view) {
        view.findViewById(R.id.test_section).setVisibility(testModeEnabled ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.measures_section).setVisibility(measureModeEnabled ? View.VISIBLE : View.GONE);

        doPaymentBtn =  view.findViewById(R.id.doPaymentBtn);
        cancelPaymentBtn =  view.findViewById(R.id.cancelPaymentBtn);
        amountFld =  view.findViewById(R.id.amountFld);
        cashbackAmountFld =  view.findViewById(R.id.cashbackAmountFld);
        currencyChooser =  view.findViewById(R.id.currencyChooser);
        contactItf =  view.findViewById(R.id.contact_itf);
        nfcItf =  view.findViewById(R.id.nfc_itf);
        msrItf =  view.findViewById(R.id.msr_itf);
        trxResultFld =  view.findViewById(R.id.trxResultFld);
        startCancelDelayEditText =  view.findViewById(R.id.start_cancel_delay);
        measurementSection =  view.findViewById(R.id.measurement_section);
        trxTypeChoice =  view.findViewById(R.id.trxTypeChoice);
        cashBackSection = view.findViewById(R.id.cashback_pane);

        CurrencyAdapter currencyAdapter = new CurrencyAdapter(
                UCubePaymentRequest.CURRENCY_EUR,
                UCubePaymentRequest.CURRENCY_USD,
                UCubePaymentRequest.CURRENCY_CAD,
                UCubePaymentRequest.CURRENCY_GBP,
                UCubePaymentRequest.CURRENCY_TWD);
        currencyChooser.setAdapter(currencyAdapter);
        currencyChooser.setSelection(currencyAdapter.getItemPosition(UCubePaymentRequest.CURRENCY_USD));

        trxTypeChoice.setAdapter(new TransactionTypeAdapter(underlyingActivity));
        trxTypeChoice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                TransactionType trxType = (TransactionType) trxTypeChoice.getSelectedItem();
                cashBackSection.setVisibility(trxType == TransactionType.PURCHASE_CASHBACK ? View.VISIBLE : View.GONE);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                /* never occur */
            }
        });

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

        view.findViewById(R.id.getSvppLogL1).setOnClickListener(v -> getCBTag());
        view.findViewById(R.id.getStatusBtn).setOnClickListener(v -> getStatus());
        view.findViewById(R.id.u_present_card).setOnClickListener(v -> {
            if (paymentMeasure != null) paymentMeasure.onUserPresentCard();
        });
        view.findViewById(R.id.u_enter_pin).setOnClickListener(v -> {
            if (paymentMeasure != null) paymentMeasure.onUEnterPin();
        });
        view.findViewById(R.id.cr_enter_pin).setOnClickListener(v -> {
            if (paymentMeasure != null) paymentMeasure.onCREnterPin();
        });
    }

    public void initSettingsTabViews(final View view) {
        cardWaitTimeoutFld =  view.findViewById(R.id.cardWaitTimeoutFld);
        cardWaitTimeoutFld.setText(String.valueOf(paymentSettings.cardWaitTimeout));
        posEntryModeFld =  view.findViewById(R.id.posEntryModeFld);
        dukpt_key_slotFld =  view.findViewById(R.id.dukpt_slotFld);
        onlinePinBlockFormatChoice =  view.findViewById(R.id.onlinePinBlockFormatChoice);
        forceOnlinePINBtn =  view.findViewById(R.id.forceOnlinePINSwitch);
        amountSrcSwitch =  view.findViewById(R.id.amountSrcSwitch);
        forceAuthorisationBtn =  view.findViewById(R.id.forceAuthorisationSwitch);
        keepSecureSessionBtn =  view.findViewById(R.id.keepSecureSessionSwitch);
        forceDebugSwitch =  view.findViewById(R.id.forceDebugSwitch);
        allowPinBypassSwitch =  view.findViewById(R.id.allowPinBypassSwitch);
        skipCardRemovalSwitch =  view.findViewById(R.id.skipCardRemovalSwitch);
        skipStartingStepsSwitch =  view.findViewById(R.id.skipStartingStepsSwitch);
        retrieveF5TagSwitch =  view.findViewById(R.id.retrieveF5Tag);
        paymentLoopSwitch =  view.findViewById(R.id.paymentLoopSwitch);
        emvParamOverride =  view.findViewById(R.id.emvParameterOverride);
        sdse_mode_spinner =  view.findViewById(R.id.sdse_mode_spinner);
        tipSwitch =  view.findViewById(R.id.tipSwitch);
        if((ProductManager.id == ProductIdentifier.blade) ||
                (ProductManager.id == ProductIdentifier.stick)) {
            tipSwitch.setClickable(false);
        }
        onlinePinBlockFormatChoice.setAdapter(new ArrayAdapter<>(
                underlyingActivity,
                android.R.layout.simple_spinner_item,
                OnlinePinBlockFormatType.values()
        ));
        onlinePinBlockFormatChoice.setSelection(0);
        sdse_mode_spinner.setAdapter(new ArrayAdapter<>(
                underlyingActivity,
                android.R.layout.simple_spinner_item,
                pay_sdse_mode.values()
        ));
        sdse_mode_spinner.setSelection(2);

        sdse_mode_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                paymentSettings.sdse_mode = (pay_sdse_mode) sdse_mode_spinner.getSelectedItem();
                prefs.edit().putInt(SDSE_MODE_PREF_NAME, paymentSettings.sdse_mode.getCode()).apply();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        amountSrcSwitch.setOnClickListener(v -> {
            if (amountFld != null) {
                amountFld.setEnabled(!amountSrcSwitch.isChecked());
            }
        });
    }

    private void updatePaymentSettingsFromViews() {
        paymentSettings.cardWaitTimeout = Integer.parseInt(cardWaitTimeoutFld.getText().toString());
        paymentSettings.keepSecureSession = keepSecureSessionBtn.isChecked();
        paymentSettings.forceAuthorisation = forceAuthorisationBtn.isChecked();
        paymentSettings.forceOnlinePin = forceOnlinePINBtn.isChecked(); // only for NFC & MSR
        paymentSettings.onlinePinBlockFormat = (OnlinePinBlockFormatType) onlinePinBlockFormatChoice.getSelectedItem();
        paymentSettings.forceDebug = forceDebugSwitch.isChecked();
        paymentSettings.isPinBypassAllowed = allowPinBypassSwitch.isChecked();
        paymentSettings.retrieveF5Tag = retrieveF5TagSwitch.isChecked();
        paymentSettings.skipCardRemoval = skipCardRemovalSwitch.isChecked();
        paymentSettings.skipStartingSteps = skipStartingStepsSwitch.isChecked();
        paymentSettings.loopMode = paymentLoopSwitch.isChecked();
        paymentSettings.overrideParameter = emvParamOverride.isChecked();
        paymentSettings.posEntryMode = Integer.parseInt(posEntryModeFld.getText().toString());
        paymentSettings.dukpt_key_slot = Integer.parseInt(dukpt_key_slotFld.getText().toString());
        paymentSettings.tipRequired = tipSwitch.isChecked();

    }

    private void startPayment() {
        doPaymentBtn.setVisibility(View.GONE);
        cancelPaymentBtn.setVisibility(View.VISIBLE);
        if (measurementSection != null) {
            measurementSection.setVisibility(View.GONE);
        }
        trxResultFld.setText("");

        pay();
    }

    private Supplier<Context> getActivityOnWhichAuthorizationPopupWillBeDisplayed() {
        return () -> underlyingActivity;
    }

    private UCubePaymentRequest preparePaymentRequest() {
        Currency currency = (Currency) currencyChooser.getSelectedItem();
        TransactionType trxType = (TransactionType) trxTypeChoice.getSelectedItem();
        List<CardReaderType> readerList = new ArrayList<>();
        if (nfcItf.isChecked()) {
            readerList.add(CardReaderType.NFC);
        }
        if (contactItf.isChecked()) {
            readerList.add(CardReaderType.ICC);
        }
        if (msrItf.isChecked()) {
            readerList.add(CardReaderType.MSR);
        }

        // ugly workaround to prevent use of this option on stick
        // TO BE REMOVED WHEN POSSIBLE
        if (ProductManager.id == ProductIdentifier.stick) {
            paymentSettings.forceAuthorisation = false;
        }

        if (paymentSettings.overrideParameter) {
            byte[] dynamicParam = PaymentTagOverrideFactory.getClessCVMReqLimit(
                            new byte[] {0x00, 0x00, 0x00, 0x00, 0x30, 0x00});
            PaymentUtils.setEmvClessDynamicParam(dynamicParam);
        } else {
            PaymentUtils.setEmvClessDynamicParam(new byte[] {});
        }

        long amount = amountFld.getCleanIntValue();
        int cashbackAmount = 0;
        if (cashbackAmountFld.getVisibility() == View.VISIBLE) {
            cashbackAmount = Math.toIntExact(cashbackAmountFld.getCleanIntValue());
        }
        IAuthorizationTask authorizationTask;
        if (paymentSettings.loopMode) {
            authorizationTask = new BypassAuthorizationTask();
        } else {
            authorizationTask = new UserChoiceAuthorizationTask(this.getActivityOnWhichAuthorizationPopupWillBeDisplayed());
            ((UserChoiceAuthorizationTask) authorizationTask).setMeasureStatesListener(paymentMeasure);
        }

        UCubePaymentRequest uCubePaymentRequest = new UCubePaymentRequest(amount, currency, trxType,
                readerList, authorizationTask, Collections.singletonList("en"));

        //Add optional variables
        uCubePaymentRequest
                .setTransactionDate(new Date())
                .setForceOnlinePin(paymentSettings.forceOnlinePin)
                .setForceAuthorisation(paymentSettings.forceAuthorisation)
                .setKeepSecureSession(paymentSettings.keepSecureSession)
                .setOnlinePinBlockFormat(paymentSettings.onlinePinBlockFormat)
                .setCardWaitTimeout(paymentSettings.cardWaitTimeout)
                .setForceDebug(paymentSettings.forceDebug)
                .setSkipCardRemoval(paymentSettings.skipCardRemoval)
                .setSkipStartingSteps(paymentSettings.skipStartingSteps)
                .setRetrieveF5Tag(paymentSettings.retrieveF5Tag)
                .setTipRequired(paymentSettings.tipRequired)
                .setPinBypassAuthorisation(paymentSettings.isPinBypassAllowed)
                // .setRiskManagementTask(new RiskManagementTask(
                //                               this.underlyingActivity))
                //.setBeforeContactlessOnlinePinTask(new BeforeContactlessOnlinePinTaskExample(BeforeContactlessOnlinePinTaskExample.TaskAction.SUCCESS))

                .setContactlessEndReadingTask(new ContactlessEndReadingTaskExample(ContactlessEndReadingTaskExample.TaskAction.SUCCESS))
                .setTipRequired(paymentSettings.tipRequired)
                .setPinRequestLabel("Pin ?")
                .setPinRequestLabelFont(1)
                .setPinRequestLabelXPosition((byte) 0xFF)
                .setDataEncryptionMechanism(paymentSettings.sdse_mode.getCode())
                .withViewDelegate(ViewIdentifier.PIN_PROMPT)
                .setPosEntryMode(paymentSettings.posEntryMode)
                .setDukptSlotKey(paymentSettings.dukpt_key_slot)
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
                .setOverrideParameter(paymentSettings.overrideParameter);

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
        return paymentSettings.loopMode;
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

                                    if (paymentSettings.forceDebug & (context.tagF4 == null || context.tagF4.length <= 21)) {
                                        underlyingActivity.runOnUiThread(() -> UIUtils.showMessageDialog(underlyingActivity, getString(R.string.F4_tag_is_empty)));
                                    }

                                    parsePaymentResponse(context);

                                    boolean triggerNewPayment = paymentSettings.loopMode && context.paymentStatus == PaymentStatus.APPROVED;
                                    if (triggerNewPayment) {
                                        startPayment();
                                    } else {
                                        if(context.paymentStatus == PaymentStatus.APPROVED) {
                                            displaySensitiveData(context);
                                        }
                                        doPaymentBtn.setVisibility(View.VISIBLE);
                                        cancelPaymentBtn.setVisibility(View.GONE);
                                    }

                                });

                            }
                        });

            } catch (Exception e) {
                Log.w(TAG, "Payment error", e);
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

    private TextView addLine(String tagName, String value){
        TextView lineTv = new TextView(underlyingActivity);
        lineTv.setText(tagName + " : " + value);
        lineTv.setTextSize(16f);
        lineTv.setLineSpacing(0f, 1.2f);
        lineTv.setTextIsSelectable(true);
        lineTv.setTypeface(Typeface.MONOSPACE);
        lineTv.setSingleLine(false);
        lineTv.setMaxLines(Integer.MAX_VALUE);
        lineTv.setHorizontallyScrolling(true);
        return lineTv;
    }
    public void displaySensitiveData(@NonNull PaymentContext context) {
        Map<Integer, String> tagNameMap = new HashMap<>();
        tagNameMap.put(TAG_SECURE_5A_APPLICATION_PRIMARY_ACCOUNT_NUMBER, "PAN");
        tagNameMap.put(TAG_SECURE_57_TRACK_2_EQUIVALENT_DATA, "Equ TRACK2(Tag 0x57)");
        tagNameMap.put(TAG_SECURE_56_TRACK_1_DATA, "TRACK1");
        tagNameMap.put(TAG_SECURE_9F6B_TRACK_2_DATA, "TRACK2(Tag 9F6B)");
        tagNameMap.put(TAG_SECURE_DF5A_PAN_CVV_DATA, "PAN:CVV");

        LinearLayout container = new LinearLayout(underlyingActivity);
        container.setOrientation(LinearLayout.VERTICAL);
        int padH = 50, padV = 24;
        container.setPadding(padH, padV, padH, padV);
        container.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        for (Integer tag : context.finalizationSecuredTagsValues.keySet()) {
            String tagName = tagNameMap.get(tag);
            if (tagName == null) continue;
            String value;
            if (!Tools.isPrintableAscii(context.finalizationSecuredTagsValues.get(tag))) {
                value = Tools.bytesToHex(context.finalizationSecuredTagsValues.get(tag));
            } else {
                value = Tools.hexToAscii((context.finalizationSecuredTagsValues.get(tag)));
            }
            container.addView(addLine(tagName, value));
        }
        if (context.onlinePinBlock != null) {
            container.addView(addLine("PIN Block",
                    Tools.bytesToHex(context.onlinePinBlock)));
        }
        HorizontalScrollView hsv = new HorizontalScrollView(underlyingActivity);
        hsv.addView(container);

        ScrollView vScroll = new ScrollView(underlyingActivity);
        vScroll.addView(hsv);
        new AlertDialog.Builder(underlyingActivity)
                .setTitle("Information")
                .setView(vScroll)
                .setPositiveButton("OK", (d, w) -> d.dismiss())
                .show();
    }
}
