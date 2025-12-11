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

package com.youtransactor.sampleapp.transactionView.steps.select_language;

import static com.youTransactor.uCube.rpc.Constants.DISPLAY_LIST_NO_ITEM_SELECTED;
import static com.youTransactor.uCube.rpc.Constants.EVT_APP_SELECT_LANG;

import com.youTransactor.uCube.payment.PaymentUtils;
import com.youTransactor.uCube.rpc.command.event.EventCommand;
import com.youTransactor.uCube.rpc.command.event.dsp.EventDspListSelectLang;
import com.youtransactor.sampleapp.R;
import com.youtransactor.sampleapp.transactionView.TransactionViewBase;
import com.youtransactor.sampleapp.transactionView.components.DisplayListFragment;

import java.util.ArrayList;

public class UserChoiceSelectLanguageStep implements SelectLanguageStep {

    private final TransactionViewBase transactionViewBase;

    public UserChoiceSelectLanguageStep(TransactionViewBase transactionViewBase) {
        this.transactionViewBase = transactionViewBase;
    }

    @Override
    public void execute(EventCommand genericCommand) {
        final EventDspListSelectLang eventCmd = (EventDspListSelectLang) genericCommand;
        ArrayList<String> itemList = eventCmd.getChoiceList();

        if ((itemList == null || itemList.isEmpty())) {
            PaymentUtils.evtSelectedItem(EVT_APP_SELECT_LANG, DISPLAY_LIST_NO_ITEM_SELECTED,
                    (event, params1) -> {
                    });
            return;
        }

        final DisplayListFragment.DisplayListParams params = new DisplayListFragment.DisplayListParams(
                itemList,
                DisplayListFragment.DISPLAY_LIST_LANGUAGE
        );
        final DisplayListFragment fragment = DisplayListFragment.newInstance(params);
        this.transactionViewBase.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

}