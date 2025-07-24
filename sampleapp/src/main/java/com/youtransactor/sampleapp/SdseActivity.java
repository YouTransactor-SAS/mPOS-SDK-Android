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


import android.content.Intent;
import android.os.Bundle;

import com.youTransactor.uCube.rpc.command.event.EventCommand;
import com.youTransactor.uCube.rpc.command.event.ppt.EventPptSdse;
import com.youtransactor.sampleapp.transactionView.SdsePrompt;
import com.youtransactor.sampleapp.transactionView.StartSdseSession;
import com.youtransactor.sampleapp.transactionView.TransactionViewBase;

public class SdseActivity extends TransactionViewBase {

    public static final String INTENT_EXTRA_SDSE_TYPE = "INTENT_EXTRA_SDSE_TYPE";
    private int Sdse_type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Sdse_type = (byte) intent.getIntExtra(INTENT_EXTRA_SDSE_TYPE, -1);
        new StartSdseSession(this, (byte) Sdse_type).execute();
    }

    private void onError() {
        finish();
    }

    @Override
    protected void onEventViewUpdate(EventCommand event) {
        switch (event.getEvent()) {
            case ppt_sdse:
                Sdse_type = ((EventPptSdse) event).getSdse_type();
                Intent sdseIntent = new Intent(this, SdsePrompt.class);
                switch (Sdse_type) {
                    case 1:
                        sdseIntent.putExtra(SdsePrompt.INTENT_EXTRA_SDSE_PROMPT_MSG, getString(R.string.set_pan));
                        break;
                    case 2:
                        sdseIntent.putExtra(SdsePrompt.INTENT_EXTRA_SDSE_PROMPT_MSG, getString(R.string.set_cvv));
                        break;
                    case 3:
                        sdseIntent.putExtra(SdsePrompt.INTENT_EXTRA_SDSE_PROMPT_MSG, getString(R.string.set_exp_date));
                        break;
                }
                sdseIntent.putExtra(SdsePrompt.INTENT_EXTRA_SDSE_PROMPT_TYPE, Sdse_type);
                startActivity(sdseIntent);
                this.finish();
                break;
            default:
                onError();
                break;
        }
    }
}

