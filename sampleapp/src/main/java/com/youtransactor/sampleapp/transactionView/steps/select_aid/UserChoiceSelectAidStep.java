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

package com.youtransactor.sampleapp.transactionView.steps.select_aid;

import com.youTransactor.uCube.rpc.EMVClessApplicationDescriptor;
import com.youTransactor.uCube.rpc.command.event.EventCommand;
import com.youTransactor.uCube.rpc.command.event.pay.EventPaySelectAid;
import com.youtransactor.sampleapp.R;
import com.youtransactor.sampleapp.transactionView.TransactionViewBase;
import com.youtransactor.sampleapp.transactionView.components.DisplayListFragment;

import java.util.ArrayList;
import java.util.List;

public class UserChoiceSelectAidStep implements SelectAidStep {

    private final TransactionViewBase transactionViewBase;
    private final BypassSelectAidStep bypassChooseAidTask = new BypassSelectAidStep();

    public UserChoiceSelectAidStep(TransactionViewBase transactionViewBase) {
        this.transactionViewBase = transactionViewBase;
    }

    @Override
    public void execute(EventCommand genericCommand) {
        final EventPaySelectAid eventCmd = (EventPaySelectAid) genericCommand;
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
        app_label.add("NO FILTERING");
        final DisplayListFragment.DisplayListParams params = new DisplayListFragment.DisplayListParams(
                app_label,
                DisplayListFragment.DISPLAY_LIST_AID
        );
        final DisplayListFragment fragment = DisplayListFragment.newInstance(params);
        this.transactionViewBase.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();

    }
}