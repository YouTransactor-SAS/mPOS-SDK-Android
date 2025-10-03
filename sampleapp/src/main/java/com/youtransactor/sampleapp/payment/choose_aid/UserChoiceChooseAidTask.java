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

package com.youtransactor.sampleapp.payment.choose_aid;

import android.content.Context;
import android.content.Intent;

import com.youTransactor.uCube.rpc.EMVClessApplicationDescriptor;
import com.youTransactor.uCube.rpc.command.event.pay.EventPaySelectAid;
import com.youtransactor.sampleapp.transactionView.DisplayList;

import java.util.ArrayList;
import java.util.List;

public class UserChoiceChooseAidTask implements ChooseAidTask {

    private final Context context;

    private final BypassChooseAidTask bypassChooseAidTask = new BypassChooseAidTask();

    public UserChoiceChooseAidTask(final Context context) {
        this.context = context;
    }

    @Override
    public void execute(EventPaySelectAid eventCmd) {
        final List<EMVClessApplicationDescriptor> candidates = eventCmd.getCandidateList();

        if (candidates.size() == 1) {
            bypassChooseAidTask.execute(eventCmd);
            return;
        }

        int i;
        ArrayList<String> app_label = new ArrayList<>();
        for (i = 0; i < candidates.size(); i++) {
            app_label.add(candidates.get(i).getLabel());
        }
        Intent intent = new Intent(context, DisplayList.class);
        intent.putExtra(DisplayList.INTENT_EXTRA_DISPLAY_LIST_MSG, app_label);
        intent.putExtra(DisplayList.INTENT_EXTRA_DISPLAY_LIST_TYPE,
                DisplayList.INTENT_EXTRA_DISPLAY_LIST_AID);
        context.startActivity(intent);

    }
}