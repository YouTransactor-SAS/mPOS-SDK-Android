/*
 * Copyright (C) 2016, YouTransactor. All Rights Reserved.
 *
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */

package com.youTransactor.uCube.rpc;

import com.youTransactor.uCube.TLV;
import com.youTransactor.uCube.Tools;

import java.util.Map;

/**
 * Created by bfourcade on 19/10/2017.
 */

public class TransactionData {

    public static final int TAG_TR_DATA_RECORD          = 0xE0;

    public static final int TAG_TR_DATA_TOKEN_KEY_ID    = 0x11;
    public static final int TAG_TR_DATA_TOKEN_ALGO      = 0x12;
    public static final int TAG_TR_TAG_PAN_TOKEN        = 0xDF64;

    private byte[] tlv;
    private byte[] transactionDataRecord;
    private String tokenPan;
    private String tokenKeyId;
    private String tokenAlgo;

/*
    {PAY_TR_TAG_PAN_TOKEN_KEY_ID,                                    // 0
    {PAY_TR_TAG_PAN_TOKEN_ALGO,                                      // 1
    {PAY_TR_TAG_PAN_TOKEN,                                           // 2
    {PAY_EMV_TAG_DATE,                                               // 3
    {PAY_EMV_TAG_TIME,                                               // 4
    {PAY_TAG_FINAL_AMOUNT,                                           // 5
    {PAY_EMV_TAG_CURRENCY,                                           // 6
    {PAY_EMV_TAG_MERCH_ID,                                           // 7
    {PAY_EMV_TAG_CURRENCY_EXP,                                       // 8
    {PAY_EMV_TAG_TR_TYPE,                                            // 9
    {PAY_EMV_TAG_ADF_NAME,                                           // 10
    {TAG_MSR_BIN,                                                    // 11
    {TAG_TERMINAL_PART_NUMBER,                                       // 12
*/

    public TransactionData() {}

    public TransactionData(byte[] tlv) {
        init(tlv);
    }

    public byte[] getTlv() {
        return tlv;
    }

    public String getTokenPan() {
        return tokenPan;
    }

    public String getTokenAlgo() {
        return tokenAlgo;
    }

    public String getTokenKeyId() {
        return tokenKeyId;
    }

    private void init(byte[] tlv) {

        this.tlv = tlv;

        if (tlv == null || tlv.length == 0) {
            return;
        }

        Map<Integer, byte[]> valueByTag = TLV.parseYtBerMixedLen(tlv);

        transactionDataRecord = valueByTag.get(Integer.valueOf(TAG_TR_DATA_RECORD));

        if (transactionDataRecord == null || transactionDataRecord.length == 0) {
            return;
        }

        // get the first record and process it
        Map<Integer, byte[]> transactionDatavalueByTag = TLV.parse(transactionDataRecord);

        tokenPan = Tools.bytesToHex(transactionDatavalueByTag.get(Integer.valueOf(TAG_TR_TAG_PAN_TOKEN)));

        tokenKeyId = Tools.bytesToHex(transactionDatavalueByTag.get(Integer.valueOf(TAG_TR_DATA_TOKEN_KEY_ID)));

        tokenAlgo = Tools.bytesToHex(transactionDatavalueByTag.get(Integer.valueOf(TAG_TR_DATA_TOKEN_ALGO)));

        // TODO: do the others if necessary
    }
}
