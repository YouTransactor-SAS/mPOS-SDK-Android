package com.youtransactor.sampleapp.transactionView;

import android.os.Bundle;

import com.jps.secureService.api.product_manager.ProductManager;
import com.youTransactor.uCube.rpc.Constants;
import com.youTransactor.uCube.rpc.Event;
import com.youtransactor.sampleapp.R;
import com.youtransactor.sampleapp.transactionView.components.WaitCardFragment;
import com.youtransactor.sampleapp.transactionView.configuration.ConfigurableTransactionStepHandler;
import com.youtransactor.sampleapp.transactionView.configuration.TransactionStepHandler;
import com.youtransactor.sampleapp.transactionView.steps.display_idle.DefaultDisplayIdleStep;
import com.youtransactor.sampleapp.transactionView.steps.pin_prompt.DtePinPromptStep;

import java.util.Objects;

public class WaitCard_Dte extends TransactionViewBase {
    public static final String INTENT_EXTRA_WAIT_CARD_AMOUNT = "INTENT_EXTRA_WAIT_CARD_AMOUNT";
    public static final String INTENT_EXTRA_WAIT_CARD_MSG = "INTENT_EXTRA_WAIT_CARD_MSG";
    public static final String INTENT_EXTRA_WAIT_CARD_MSG_TAG = "INTENT_EXTRA_WAIT_CARD_MSG_TAG";
    public static final String INTENT_EXTRA_ITF = "INTENT_EXTRA_ITF";

    private final TransactionStepHandler transactionStepHandler = buildTransactionStepHandler();

    @Override
    protected TransactionStepHandler getTransactionStepHandler() {
        return transactionStepHandler;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();

        final String amount;
        if (getIntent().getStringExtra(INTENT_EXTRA_WAIT_CARD_AMOUNT) != null) {
            amount = getIntent().getStringExtra(INTENT_EXTRA_WAIT_CARD_AMOUNT);
        } else {
            amount = "";
        }

        final int messageTag = getIntent().getIntExtra(INTENT_EXTRA_WAIT_CARD_MSG_TAG, -1);
        final String message = getIntent().getStringExtra(INTENT_EXTRA_WAIT_CARD_MSG);
        int reader = getIntent().getByteExtra(INTENT_EXTRA_ITF, Constants.NFC_READER);


        final WaitCardFragment.Params params = new WaitCardFragment.Params(
                amount,
                message,
                new int[]{reader},
                messageTag
        );
        this.setHomeFragment(WaitCardFragment.newInstance(params));
        setContentView(R.layout.activity_wait_card_dte);

        if (savedInstanceState == null) {
            this.backToHomeFragment();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private TransactionStepHandler buildTransactionStepHandler() {
        return ConfigurableTransactionStepHandler
                .defaultForProductAndTransactionViewBase(ProductManager.id, this)
                .forEvent(Event.dsp_wait_card, null)
                .forEvent(Event.ppt_pin, new DtePinPromptStep(this)::execute)
                .forEvent(Event.pay_select_aid, null)
                .forEvent(Event.dsp_idle, new DefaultDisplayIdleStep(this)::execute)
                .build();
    }
}
