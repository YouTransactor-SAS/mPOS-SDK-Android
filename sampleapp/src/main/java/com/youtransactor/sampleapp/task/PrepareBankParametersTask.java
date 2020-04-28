/*
 * Copyright (C) 2011-2020, YouTransactor. All Rights Reserved.
 * <p/>
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youtransactor.sampleapp.task;

import com.youTransactor.uCube.ITaskMonitor;
import com.youTransactor.uCube.TaskEvent;
import com.youTransactor.uCube.Tools;
import com.youTransactor.uCube.rpc.Constants;
import com.youTransactor.uCube.settingUp.task.IPrepareBankParametersTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PrepareBankParametersTask implements IPrepareBankParametersTask {

    private byte[] ksn;
    private List<byte[]> bankParam = new ArrayList<>();

    @Override
    public void setKsn(byte[] ksn) {
        this.ksn = ksn;
    }

    @Override
    public List<byte[]> getBankParameters() {
        return bankParam;
    }

    @Override
    public boolean isPayloadSIgned() {
        return true;
    }

    @Override
    public void execute(ITaskMonitor monitor) {

        //Todo use KSN to generate MAC signature

        //Todo create list of parameters to inject in dongle every payload of param injection
        // should be structured like this Crypto header + TLV of param data (see 6.3.1 in svpp doc) + MAC

        byte [] payload = Tools.hexStringToByteArray("04009300DF37026672DF300104DF310112DF3202" +
                "003CDF3302003CDF3402003CDF350101DF360106DF3810010C53414953495220434F4445000000DF39" +
                "0E010A434F44452046415558000000DF3A12010E4445524E494552204553534149000000DF3B12010E" +
                "434152544520424C4F51554545000000DF3C0D0109434F444520424F4E000000DF3D0E010A434F4445" +
                "2046415558000000");

        byte[] message = addSignature(payload);

        bankParam.add(message);

        //todo this is mandatory
        new Thread(() -> monitor.handleEvent(TaskEvent.SUCCESS)).start();

    }

    private byte[] addSignature(byte[] payload) {
        byte[] message = new byte[payload.length + Constants.RPC_SECURED_HEADER_LEN + Constants.RPC_SRED_MAC_SIZE];

        int offset = 0;

        /* Add crypto header */
        Random randomGenerator = new Random();
        message[offset++] = (byte) randomGenerator.nextInt(255);

        /* Add payload */
        System.arraycopy(payload, 0, message, offset, payload.length);
        offset += payload.length;

        /* Add 'not checked' MAC */
        /* Padding the last 4 bytes with 0x00 (SRED OPT) */
        for (int i = 0; i < Constants.RPC_SRED_MAC_SIZE; i++){
            message[offset++] = 0x00; // todo add real signature
        }

        return message;
    }
}
