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
 * @date: oct. 02, 2025
 * @author: Emmanuel COLAS (emmanuel_colas@jabil.com)
 */

package com.youtransactor.sampleapp.payment.choose_language;

import static com.youtransactor.sampleapp.transactionView.view_factory.View_index.list;

import android.content.Context;
import android.content.Intent;

import com.jps.secureService.api.product_manager.ProductManager;
import com.youTransactor.uCube.rpc.command.event.dsp.EventDspListSelectLang;
import com.youtransactor.sampleapp.transactionView.DisplayList;
import com.youtransactor.sampleapp.transactionView.view_factory.view_manager;

import java.util.List;

public class UserChoiceChooseLanguageTask implements ChooseLanguageTask {

    private final Context context;
    private final List<Intent> intents;

    public UserChoiceChooseLanguageTask(final Context context) {
        this.context = context;
        this.intents = view_manager.getApplicableIntents(context, ProductManager.id);
    }

    @Override
    public void execute(final EventDspListSelectLang eventCmd) {
        Intent intent;
        intent = intents.get(list.ordinal());
        intent.putExtra(DisplayList.INTENT_EXTRA_DISPLAY_LIST_MSG, eventCmd.getChoiceList());
        this.context.startActivity(intent);
    }

}