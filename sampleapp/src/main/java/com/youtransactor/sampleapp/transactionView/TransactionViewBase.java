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

import com.youTransactor.uCube.rpc.EventListener;
import com.youTransactor.uCube.rpc.RPCManager;
import com.youTransactor.uCube.rpc.command.event.EventCommand;
import com.youTransactor.uCube.rpc.command.event.dsp.EventDspAuthorisation;
import com.youTransactor.uCube.rpc.command.event.dsp.EventDspListbox;
import com.youTransactor.uCube.rpc.command.event.dsp.EventDspPaymentRslt;
import com.youTransactor.uCube.rpc.command.event.dsp.EventDspReadCard;
import com.youTransactor.uCube.rpc.command.event.dsp.EventDspTxt;
import com.youTransactor.uCube.rpc.command.event.dsp.EventDspWaitCard;
import com.youTransactor.uCube.rpc.command.event.ppt.EventPptPin;
import com.youtransactor.sampleapp.transactionView.view_factory.view_manager;
import static com.youtransactor.sampleapp.transactionView.view_factory.View_index.*;

import com.jps.secureService.api.product_manager.ProductManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public abstract class TransactionViewBase extends AppCompatActivity {

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
            case dsp_wait_card:
                intent = new Intent(this, WaitCard.class);
                intent.putExtra(WaitCard.INTENT_EXTRA_WAIT_CARD_AMOUNT, ((EventDspWaitCard) eventCmd).getAmount());
                intent.putExtra(WaitCard.INTENT_EXTRA_WAIT_CARD_MSG, ((EventDspWaitCard) eventCmd).getMessage());
                intent.putExtra(WaitCard.INTENT_EXTRA_WAIT_CARD_ITF, ((EventDspWaitCard) eventCmd).getInterfaces());
                intent.putExtra(WaitCard.INTENT_EXTRA_WAIT_CARD_MSG_TAG, ((EventDspWaitCard) eventCmd).getMessageTag());
                startActivity(intent);
                this.finishTransactionView();
                break;

            case dsp_payment_result:
                intent = intents.get(dsp_msg.ordinal());
                intent.putExtra(DisplayMsg.INTENT_EXTRA_DISPLAY_MSG_MSG, ((EventDspPaymentRslt) eventCmd).getMessage());
                intent.putExtra(DisplayMsg.INTENT_EXTRA_DISPLAY_MSG_RSLT, ((EventDspPaymentRslt) eventCmd).getResult());
                intent.putExtra(DisplayMsg.INTENT_EXTRA_DISPLAY_MSG_TAG, ((EventDspPaymentRslt) eventCmd).getResultTag());
                startActivity(intent);
                this.finishTransactionView();
                break;

            case dsp_authorisation:
                intent = intents.get(dsp_msg.ordinal());
                intent.putExtra(DisplayMsg.INTENT_EXTRA_DISPLAY_MSG_MSG, ((EventDspAuthorisation) eventCmd).getMessage());
                intent.putExtra(DisplayMsg.INTENT_EXTRA_DISPLAY_MSG_RSLT, DisplayMsg.DISPLAY_MSG_ID_WAITING);
                intent.putExtra(DisplayMsg.INTENT_EXTRA_DISPLAY_MSG_TAG, ((EventDspAuthorisation) eventCmd).getMessageTag());
                startActivity(intent);
                this.finishTransactionView();
                break;

            case dsp_read_card:
                intent = intents.get(dsp_msg.ordinal());
                intent.putExtra(DisplayMsg.INTENT_EXTRA_DISPLAY_MSG_MSG, ((EventDspReadCard) eventCmd).getMessage());
                intent.putExtra(DisplayMsg.INTENT_EXTRA_DISPLAY_MSG_RSLT, DisplayMsg.DISPLAY_MSG_ID_WAITING);
                intent.putExtra(DisplayMsg.INTENT_EXTRA_DISPLAY_MSG_TAG, ((EventDspReadCard) eventCmd).getMessageTag());
                startActivity(intent);
                this.finishTransactionView();
                break;

            case ppt_pin:
                intent = intents.get(pin.ordinal());
                intent.putExtra(PinPrompt.INTENT_EXTRA_PIN_MSG, ((EventPptPin) eventCmd).getMessage());
                intent.putExtra(PinPrompt.INTENT_EXTRA_PIN_AMOUNT, ((EventPptPin) eventCmd).getAmount());
                intent.putExtra(PinPrompt.INTENT_EXTRA_PIN_MSG_TAG, ((EventPptPin) eventCmd).getMessageId());
                startActivity(intent);
                this.finishTransactionView();
                break;
            case dsp_listbox:
                intent = intents.get(list.ordinal());
                intent.putExtra(DisplayList.INTENT_EXTRA_DISPLAY_LIST_MSG, ((EventDspListbox) eventCmd).getChoiceList());
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


