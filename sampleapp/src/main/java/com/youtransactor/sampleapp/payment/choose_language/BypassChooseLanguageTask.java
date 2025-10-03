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

import static com.youTransactor.uCube.rpc.Constants.DISPLAY_LIST_NO_ITEM_SELECTED;
import static com.youTransactor.uCube.rpc.Constants.EVT_APP_SELECT_LANG;

import com.youTransactor.uCube.payment.PaymentUtils;
import com.youTransactor.uCube.rpc.command.event.dsp.EventDspListSelectLang;

public class BypassChooseLanguageTask implements ChooseLanguageTask {

    @Override
    public void execute(EventDspListSelectLang eventCmd) {
        PaymentUtils.evtSelectedItem(EVT_APP_SELECT_LANG, DISPLAY_LIST_NO_ITEM_SELECTED,
                (event, params) -> {
                    switch (event) {
                        case FAILED, SUCCESS:
                            break;
                    }
                });
    }
}