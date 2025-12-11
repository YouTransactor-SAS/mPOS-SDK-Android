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
 * @date: oct. 07, 2025
 * @author: Emmanuel COLAS (emmanuel_colas@jabil.com)
 */

package com.youtransactor.sampleapp.manual_entry;

import android.os.Bundle;

import com.jps.secureService.api.product_manager.ProductManager;
import com.youTransactor.uCube.rpc.Event;
import com.youtransactor.sampleapp.R;
import com.youtransactor.sampleapp.features.SdseSession;
import com.youtransactor.sampleapp.transactionView.TransactionViewBase;
import com.youtransactor.sampleapp.transactionView.configuration.ConfigurableTransactionStepHandler;
import com.youtransactor.sampleapp.transactionView.configuration.TransactionStepHandler;

public class SdsePromptActivity extends TransactionViewBase {

    public static final String INTENT_EXTRA_SDSE_PROMPT_MSG = "INTENT_EXTRA_SDSE_PROMPT_MSG";
    public static final String INTENT_EXTRA_SDSE_PROMPT_TYPE = "INTENT_EXTRA_SDSE_PROMPT_TYPE";

    private final TransactionStepHandler transactionStepHandler = buildTransactionStepHandler();

    @Override
    protected TransactionStepHandler getTransactionStepHandler() {
        return transactionStepHandler;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SdsePromptFragment.SdsePromptParams params = new SdsePromptFragment.SdsePromptParams(
                getIntent().getStringExtra(INTENT_EXTRA_SDSE_PROMPT_MSG),
                getIntent().getIntExtra(INTENT_EXTRA_SDSE_PROMPT_TYPE, SdseSession.SDSE_TYPE_PAN)
        );
        this.setHomeFragment(SdsePromptFragment.newInstance(params));

        setContentView(R.layout.activity_pan_prompt);

        if (savedInstanceState == null) {
            this.backToHomeFragment();
        }
    }

    private TransactionStepHandler buildTransactionStepHandler() {
        return ConfigurableTransactionStepHandler
                .defaultForProductAndTransactionViewBase(ProductManager.id, this)
                .forEvent(Event.dsp_idle, eventCommand -> finish())
                .build();
    }

}