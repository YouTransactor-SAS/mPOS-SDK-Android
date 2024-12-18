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

import static com.youtransactor.sampleapp.transactionView.view_factory.View_index.dsp_msg;
import static com.youtransactor.sampleapp.transactionView.view_factory.View_index.wait_card;

import com.youTransactor.uCube.api.UCubeAPI;
import com.youTransactor.uCube.rpc.command.event.EventCommand;
import com.youTransactor.uCube.rpc.command.event.dsp.EventDspAuthorisation;
import com.youTransactor.uCube.rpc.command.event.dsp.EventDspPaymentRslt;
import com.youTransactor.uCube.rpc.command.event.dsp.EventDspReadCard;
import com.youTransactor.uCube.rpc.command.event.dsp.EventDspWaitCard;
import com.youtransactor.sampleapp.product_manager.product_id;
import com.youtransactor.sampleapp.product_manager.product_manager;
import com.youtransactor.sampleapp.transactionView.view_factory.view_manager;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public abstract class TransactionViewBase extends AppCompatActivity {

    private static Class<?> homeActivity = null;
    private product_id prod_id;
    private List<Intent> intents;
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        product_manager prod_manager = new product_manager();
        prod_manager.update_id();
        prod_id = prod_manager.product_get_id();
        intents = view_manager.getApplicableIntents(this, prod_id);
    }

    @Override
    protected void onResume() {
        super.onResume();
        UCubeAPI.setDisplayEventListener(event -> {
            onEventViewCreate(event);
            onEventViewUpdate(event);
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        UCubeAPI.removeDisplayEventListener();
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

    }

    private void onEventViewCreate(EventCommand eventCmd) {
        Intent intent;
        switch (eventCmd.getEvent()) {
            case dsp_wait_card:
                intent = intents.get(wait_card.ordinal());
                intent.putExtra(WaitCard.INTENT_EXTRA_WAIT_CARD_AMOUNT, ((EventDspWaitCard) eventCmd).getAmount());
                intent.putExtra(WaitCard.INTENT_EXTRA_WAIT_CARD_MSG, ((EventDspWaitCard) eventCmd).getMessage());
                intent.putExtra(WaitCard.INTENT_EXTRA_WAIT_CARD_ITF, ((EventDspWaitCard) eventCmd).getInterfaces());
                startActivity(intent);
                this.finishTransactionView();
                break;

            case dsp_payment_result:
                intent = intents.get(dsp_msg.ordinal());
                intent.putExtra(DisplayMsg.INTENT_EXTRA_DISPLAY_MSG_MSG, ((EventDspPaymentRslt) eventCmd).getMessage());
                intent.putExtra(DisplayMsg.INTENT_EXTRA_DISPLAY_MSG_RSLT, ((EventDspPaymentRslt) eventCmd).getResult());
                startActivity(intent);
                this.finishTransactionView();
                break;

            case dsp_authorisation:
                intent = intents.get(dsp_msg.ordinal());
                intent.putExtra(DisplayMsg.INTENT_EXTRA_DISPLAY_MSG_MSG, ((EventDspAuthorisation) eventCmd).getMessage());
                intent.putExtra(DisplayMsg.INTENT_EXTRA_DISPLAY_MSG_RSLT, DisplayMsg.DISPLAY_MSG_ID_WAITING);
                startActivity(intent);
                this.finishTransactionView();
                break;

            case dsp_read_card:
                intent = intents.get(dsp_msg.ordinal());
                intent.putExtra(DisplayMsg.INTENT_EXTRA_DISPLAY_MSG_MSG, ((EventDspReadCard) eventCmd).getMessage());
                intent.putExtra(DisplayMsg.INTENT_EXTRA_DISPLAY_MSG_RSLT, DisplayMsg.DISPLAY_MSG_ID_WAITING);
                startActivity(intent);
                this.finishTransactionView();
                break;

            case dsp_listbox:
                // Todo
                break;

            case dsp_idle:
                this.finishTransactionView();
                break;

            default:
                break;
        }
    }
}


