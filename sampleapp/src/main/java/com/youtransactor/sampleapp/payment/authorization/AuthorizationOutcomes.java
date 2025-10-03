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

package com.youtransactor.sampleapp.payment.authorization;

public enum AuthorizationOutcomes {
    APPROVED("Approved", new byte[]{(byte) 0x8A, 0x02, 0x30, 0x30}),
    SCA0x1A("SCA (0x1A)", new byte[]{(byte) 0x8A, 0x02, 0x31, 0x41}),
    SCA0x70("SCA (0x70)", new byte[]{(byte) 0x8A, 0x02, 0x37, 0x30, (byte) 0xDF, 0x76, 0x01, 0x01}),
    DECLINED("Declined", new byte[]{(byte) 0x8A, 0x02, 0x30, 0x35}),
    UNABLE_TO_GO_ONLINE("Unable to go online", new byte[]{(byte) 0x8A, 0x02, 0x39, 0x38}),
    FAILED("Failed", null);

    public final String label;
    public final byte[] correspondingResponse;

    AuthorizationOutcomes(String label, byte[] correspondingResponse) {
        this.label = label;
        this.correspondingResponse = correspondingResponse;
    }
}
