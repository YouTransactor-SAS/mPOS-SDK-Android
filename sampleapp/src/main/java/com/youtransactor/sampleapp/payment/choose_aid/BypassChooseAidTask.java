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

import com.youTransactor.uCube.payment.PaymentUtils;
import com.youTransactor.uCube.rpc.command.event.pay.EventPaySelectAid;

public class BypassChooseAidTask implements ChooseAidTask{

    @Override
    public void execute(EventPaySelectAid eventCmd) {
        byte[] tlv = new byte[0];
        PaymentUtils.send_event_filter_cless_aid(0, tlv, (event, params) -> {
            switch (event) {
                case FAILED, SUCCESS:
                    break;
            }
        });
    }
}