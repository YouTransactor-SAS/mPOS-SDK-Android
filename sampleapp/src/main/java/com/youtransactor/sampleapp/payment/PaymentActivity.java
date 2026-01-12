/**
 * 2006-2025 YOUTRANSACTOR ALL RIGHTS RESERVED. YouTransactor,
 * 32, rue Brancion 75015 Paris France, RCS PARIS: B 491 208 500, YOUTRANSACTOR
 * CONFIDENTIAL AND PROPRIETARY INFORMATION , CONTROLLED DISTRIBUTION ONLY,
 * THEREFORE UNDER NDA ONLY. YOUTRANSACTOR Authorized Parties and who have
 * signed NDA do not have permission to distribute this documentation further.
 * Unauthorized recipients must destroy any electronic and hard-copies
 * and without reading and notify Gregory Mardinian, CTO, YOUTRANSACTOR
 * immediately at gregory_mardinian@jabil.com.
 *
 * @date: oct. 12, 2025
 * @author: Emmanuel COLAS (emmanuel_colas@jabil.com)
 */

package com.youtransactor.sampleapp.payment;

import android.os.Bundle;
import android.util.Log;

import com.youTransactor.uCube.payment.PaymentState;
import com.youTransactor.uCube.rpc.command.event.dsp.EventDspTxt;
import com.youTransactor.uCube.rpc.command.event.pay.EventPayPinPrompt;
import com.youtransactor.sampleapp.R;
import com.youtransactor.sampleapp.transactionView.TransactionViewBase;
import com.youtransactor.sampleapp.transactionView.configuration.TransactionStepHandler;
import com.youtransactor.sampleapp.transactionView.steps.display_idle.DefaultDisplayIdleStep;
import com.youtransactor.sampleapp.transactionView.steps.display_integrity_check_warning.DefaultDisplayIntegrityCheckWarningStep;
import com.youtransactor.sampleapp.transactionView.steps.pin_prompt.DefaultPinPromptStep;
import com.youtransactor.sampleapp.transactionView.steps.select_aid.BypassSelectAidStep;
import com.youtransactor.sampleapp.transactionView.steps.select_aid.SelectAidStep;
import com.youtransactor.sampleapp.transactionView.steps.select_aid.UserChoiceSelectAidStep;
import com.youtransactor.sampleapp.transactionView.steps.select_language.BypassSelectLanguageStep;
import com.youtransactor.sampleapp.transactionView.steps.select_language.SelectLanguageStep;
import com.youtransactor.sampleapp.transactionView.steps.select_language.UserChoiceSelectLanguageStep;

public class PaymentActivity extends TransactionViewBase {

    private static final String TAG = PaymentActivity.class.getSimpleName();
    private final PaymentFragment paymentFragment = new PaymentFragment(this);

    private final TransactionStepHandler transactionStepHandler = (eventCmd) -> {
        Log.d(TAG, "Handling event: " + eventCmd.getEvent());
        switch (eventCmd.getEvent()) {
            case dsp_wait_card:
                paymentFragment.displayProgress(PaymentState.WAITING_CARD);
                break;
            case ppt_pin:
                new DefaultPinPromptStep(this).execute(eventCmd);
                break;
            case pay_pin_prompt:
                switch (((EventPayPinPrompt) eventCmd).getPinPromptMode()) {
                    case EVENT_PAY_PIN_PROMPT_MODE_OFFLINE, EVENT_PAY_PIN_PROMPT_MODE_ONLINE:
                        //add you code
                        break;
                }
                break;
            case dsp_listbox_select_lang:
                this.buildChooseLanguageTask().execute(eventCmd);
                break;
            case pay_select_aid:
                // TO ADD: update the received list and tlv value
                this.buildChooseAidTask().execute(eventCmd);
                break;
            case dsp_txt:
                paymentFragment.displaytxt(((EventDspTxt) eventCmd).getMessage());
                break;
            case dsp_idle:
            case dsp_authorisation:
                new DefaultDisplayIdleStep(this).execute(eventCmd);
                break;
            case dsp_integ_check_24h_warning:
                new DefaultDisplayIntegrityCheckWarningStep(this).execute(eventCmd);
                break;
            default:
                Log.d(TAG, "PAY: unused event: " + eventCmd.getEvent().name());
                break;
        }
    };

    private SelectLanguageStep buildChooseLanguageTask() {
        if (paymentFragment.isLoopMode()) {
            return new BypassSelectLanguageStep();
        } else {
            return new UserChoiceSelectLanguageStep(this);
        }
    }

    private SelectAidStep buildChooseAidTask() {
        if (paymentFragment.isLoopMode()) {
            return new BypassSelectAidStep();
        } else {
            return new UserChoiceSelectAidStep(this);
        }
    }

    @Override
    protected TransactionStepHandler getTransactionStepHandler() {
        return this.transactionStepHandler;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setHomeFragment(this.paymentFragment);

        setContentView(R.layout.activity_payment);

        if (savedInstanceState == null) {
            this.backToHomeFragment();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        paymentFragment.cancelPayment(false);
    }
}