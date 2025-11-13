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
 * @date: oct. 06, 2025
 * @author: Emmanuel COLAS (emmanuel_colas@jabil.com)
 */

package com.youtransactor.sampleapp.transactionView.steps.pin_prompt;

import com.youTransactor.uCube.rpc.command.event.EventCommand;
import com.youTransactor.uCube.rpc.command.event.ppt.EventPptPin;
import com.youtransactor.sampleapp.R;
import com.youtransactor.sampleapp.transactionView.TransactionViewBase;
import com.youtransactor.sampleapp.transactionView.components.PinPromptDteFragment;

public class DtePinPromptStep implements PinPromptStep {

    private final TransactionViewBase transactionViewBase;

    public DtePinPromptStep(TransactionViewBase transactionViewBase) {
        this.transactionViewBase = transactionViewBase;
    }

    @Override
    public void execute(EventCommand genericCommand) {
        final EventPptPin eventCmd = (EventPptPin) genericCommand;

        final PinPromptDteFragment.Params params = new PinPromptDteFragment.Params(
                eventCmd.getMessage(),
                eventCmd.getMessageId(),
                true
        );
        final PinPromptDteFragment fragment = PinPromptDteFragment.newInstance(params);
        this.transactionViewBase.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}