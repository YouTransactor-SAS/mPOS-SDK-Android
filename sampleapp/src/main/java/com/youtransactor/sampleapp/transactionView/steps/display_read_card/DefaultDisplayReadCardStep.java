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

package com.youtransactor.sampleapp.transactionView.steps.display_read_card;

import com.youTransactor.uCube.rpc.command.event.EventCommand;
import com.youTransactor.uCube.rpc.command.event.dsp.EventDspReadCard;
import com.youtransactor.sampleapp.R;
import com.youtransactor.sampleapp.transactionView.TransactionViewBase;
import com.youtransactor.sampleapp.transactionView.components.DisplayMsgFragment;

public class DefaultDisplayReadCardStep implements DisplayReadCardStep {

    private final TransactionViewBase transactionViewBase;

    public DefaultDisplayReadCardStep(TransactionViewBase transactionViewBase) {
        this.transactionViewBase = transactionViewBase;
    }

    @Override
    public void execute(EventCommand genericCommand) {
        final EventDspReadCard eventCmd = (EventDspReadCard) genericCommand;

        final DisplayMsgFragment.Params params = new DisplayMsgFragment.Params(
                DisplayMsgFragment.DISPLAY_MSG_ID_WAITING,
                eventCmd.getMessage(),
                eventCmd.getMessageTag()
        );
        final DisplayMsgFragment fragment = DisplayMsgFragment.newInstance(params);
        this.transactionViewBase.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

}