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
import static com.youtransactor.sampleapp.transactionView.view_factory.View_index.list;
import static com.youtransactor.sampleapp.transactionView.view_factory.View_index.pin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.youTransactor.uCube.rpc.EventListener;
import com.youTransactor.uCube.rpc.RPCManager;
import com.youTransactor.uCube.rpc.command.event.EventCommand;
import com.youTransactor.uCube.rpc.command.event.dsp.EventDspAuthorisation;
import com.youTransactor.uCube.rpc.command.event.dsp.EventDspListSelectLang;
import com.youTransactor.uCube.rpc.command.event.dsp.EventDspPaymentRslt;
import com.youTransactor.uCube.rpc.command.event.dsp.EventDspReadCard;
import com.youTransactor.uCube.rpc.command.event.dsp.EventDspTxt;
import com.youTransactor.uCube.rpc.command.event.ppt.EventPptPin;
import com.jps.secureService.api.product_manager.ProductManager;
import com.youtransactor.sampleapp.transactionView.view_factory.view_manager;

import java.util.List;

public abstract class TransactionViewBaseDte extends AppCompatActivity {

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        RPCManager.getInstance().registerSvppEventListener(eventListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        RPCManager.getInstance().unregisterSvppEventListener(eventListener);
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
                intent = new Intent(this, PinPromptDte.class);
                intent.putExtra(PinPrompt.INTENT_EXTRA_PIN_MSG, ((EventPptPin) eventCmd).getMessage());
                intent.putExtra(PinPrompt.INTENT_EXTRA_PIN_AMOUNT, ((EventPptPin) eventCmd).getAmount());
                intent.putExtra(PinPrompt.INTENT_EXTRA_PIN_MSG_TAG, ((EventPptPin) eventCmd).getMessageId());
                intent.putExtra(PinPrompt.INTENT_EXTRA_UPDATE_KEYPAD_TAG, false);
                startActivity(intent);
                this.finishTransactionView();
                break;

            case dsp_txt:
                this.finishTransactionView();
                if (homeActivity == WaitCard_Dte.class) {
                    WaitCard_Dte.getInstance().update_text(((EventDspTxt) eventCmd).getMessage());
                }
                break;
            case dsp_listbox_select_lang:
                intent = intents.get(list.ordinal());
                intent.putExtra(DisplayList.INTENT_EXTRA_DISPLAY_LIST_MSG, ((EventDspListSelectLang) eventCmd).getChoiceList());
                startActivity(intent);
                this.finishTransactionView();
                break;
            case dsp_idle:
                this.finishTransactionView();
                if (homeActivity == WaitCard_Dte.class) {
                    WaitCard_Dte.getInstance().update_text("");
                }
                break;
            case dsp_integ_check_24h_warning:
                runOnUiThread(() -> Toast.makeText(this,
                        "24H hour check is imminent",
                        Toast.LENGTH_LONG).show());
            default:
                break;
        }
    }
}


