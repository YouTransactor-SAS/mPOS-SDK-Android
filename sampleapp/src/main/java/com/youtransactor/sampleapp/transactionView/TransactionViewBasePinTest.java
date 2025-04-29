/*
 * 2006-2025 YOUTRANSACTOR ALL RIGHTS RESERVED. YouTransactor,
 * 32, rue Brancion 75015 Paris France, RCS PARIS: B 491 208 500, YOUTRANSACTOR
 * CONFIDENTIAL AND PROPRIETARY INFORMATION , CONTROLLED DISTRIBUTION ONLY,
 * THEREFORE UNDER NDA ONLY. YOUTRANSACTOR Authorized Parties and who have
 * signed NDA do not have permission to distribute this documentation further.
 * Unauthorized recipients must destroy any electronic and hard-copies
 * and without reading and notify Gregory Mardinian, CTO, YOUTRANSACTOR
 * immediately at gregory_mardinian@jabil.com.
 *
 * @package: com.youtransactor.sampleapp.transactionView
 *
 * @date: août 08, 2024
 *
 * @author: Thomas JEANNETTE (thomas_jeannette@jabil.com)
 */
package com.youtransactor.sampleapp.transactionView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.jps.secureService.api.product_manager.ProductManager;
import com.youTransactor.uCube.rpc.EventListener;
import com.youTransactor.uCube.rpc.RPCManager;
import com.youTransactor.uCube.rpc.command.event.EventCommand;
import com.youTransactor.uCube.rpc.command.event.ppt.EventPptPin;
import com.youtransactor.sampleapp.transactionView.view_factory.view_manager;

import java.util.List;

public abstract class TransactionViewBasePinTest extends AppCompatActivity {

    private static Class<?> homeActivity = null;
    private List<Intent> intents;
    private final EventListener eventListener = event -> {
        onEventViewCreate(event);
        onEventViewUpdate(event);
    };

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intents = view_manager.getApplicableIntents(this, ProductManager.id);
        getSupportActionBar().hide();
    }

    @Override
    protected void onResume() {
        super.onResume();
        RPCManager.getInstance().registerSvppEventListener(eventListener);
    }

    @Override
    protected void onPause() {
        RPCManager.getInstance().unregisterSvppEventListener(eventListener);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void setHomeActivity(Class<?> homeActivity) {
        this.homeActivity = homeActivity;
    }

    private void finishTransactionView() {
        if (this.homeActivity != this.getClass()) {
            // do not close home view
            this.finish();
        }
    }

    protected void onEventViewUpdate(EventCommand event) {
        Log.d("onEventViewUpdate", "onEventViewUpdate");
    }

    private void onEventViewCreate(EventCommand eventCmd) {
        Intent intent;
        switch (eventCmd.getEvent()) {
            case ppt_pin:
                intent = new Intent(this, TestPinPrompt.class);
                intent.putExtra(PinPrompt.INTENT_EXTRA_PIN_MSG, ((EventPptPin) eventCmd).getMessage());
                intent.putExtra(PinPrompt.INTENT_EXTRA_PIN_AMOUNT, ((EventPptPin) eventCmd).getAmount());
                intent.putExtra(PinPrompt.INTENT_EXTRA_PIN_MSG_TAG, ((EventPptPin) eventCmd).getMessageId());
                startActivity(intent);
                this.finishTransactionView();
                break;

            case dsp_idle:
                this.finishTransactionView();
                break;

            default:
                break;
        }
    }
}


