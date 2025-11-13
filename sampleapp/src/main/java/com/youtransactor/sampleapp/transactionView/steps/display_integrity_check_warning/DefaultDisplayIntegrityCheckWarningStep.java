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
 * @date: oct. 28, 2025
 * @author: Emmanuel COLAS (emmanuel_colas@jabil.com)
 */

package com.youtransactor.sampleapp.transactionView.steps.display_integrity_check_warning;

import android.widget.Toast;

import com.youTransactor.uCube.rpc.command.event.EventCommand;
import com.youtransactor.sampleapp.transactionView.TransactionViewBase;

public class DefaultDisplayIntegrityCheckWarningStep implements DisplayIntegrityCheckWarningStep {

    private final TransactionViewBase transactionViewBase;

    public DefaultDisplayIntegrityCheckWarningStep(TransactionViewBase transactionViewBase) {
        this.transactionViewBase = transactionViewBase;
    }

    @Override
    public void execute(EventCommand eventCmd) {
        this.transactionViewBase.runOnUiThread(() -> Toast.makeText(this.transactionViewBase,
                "24H hour check is imminent",
                Toast.LENGTH_LONG).show());
    }
}