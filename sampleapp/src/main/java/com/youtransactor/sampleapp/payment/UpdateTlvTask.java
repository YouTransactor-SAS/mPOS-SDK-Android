/*
 * ============================================================================
 *
 * Copyright (c) 2022 YouTransactor
 *
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of YouTransactor
 * ("Confidential Information"). You  shall not disclose or redistribute such
 * Confidential Information and shall use it only in accordance with the terms of
 * the license agreement you entered into with YouTransactor.
 *
 * This software is provided by YouTransactor AS IS, and YouTransactor
 * makes no representations or warranties about the suitability of the software,
 * either express or implied, including but not limited to the implied warranties
 * of merchantability, fitness for a particular purpose or non-infringement.
 * YouTransactor shall not be liable for any direct, indirect, incidental,
 * special, exemplary, or consequential damages suffered by licensee as the
 * result of using, modifying or distributing this software or its derivatives.
 *
 * ==========================================================================
 */
package com.youtransactor.sampleapp.payment;

import com.youTransactor.uCube.ITaskCancelListener;
import com.youTransactor.uCube.ITaskMonitor;
import com.youTransactor.uCube.TLV;
import com.youTransactor.uCube.Tools;
import com.youTransactor.uCube.payment.PaymentContext;
import com.youTransactor.uCube.payment.PaymentUtils;
import com.youTransactor.uCube.payment.task.ITlvUpdateTask;

import java.util.Map;

public class UpdateTlvTask implements ITlvUpdateTask {
    private PaymentContext paymentContext;

    @Override
    public Map<Integer, byte[]> getTlvMap() {
        return paymentContext.TlvListToUpdate;
    }

    @Override
    public PaymentContext getContext() {
        return paymentContext;
    }

    @Override
    public void setContext(PaymentContext context) {
        this.paymentContext = context;
    }

    @Override
    public void execute(ITaskMonitor monitor) {
        byte[] dynamicParam = new byte[0];
        if(paymentContext.overrideParameter) {
            dynamicParam =
                    PaymentTagOverrideFactory.getContactTerminalCapabilities(
                            new byte[]{(byte) 0xE0, (byte) 0x20, (byte) 0xC0});
        }
        dynamicParam =
            Tools.appendBytes(dynamicParam,
                    PaymentTagOverrideFactory.getContactPinParam((byte)paymentContext.pin_min_digit,
                            (byte)paymentContext.pin_max_digit,
                            paymentContext.firstDigitTimeout,
                            paymentContext.interDigitTimeout,
                            paymentContext.globalTimeout));
        if(dynamicParam.length > 0) {
            paymentContext.TlvListToUpdate = TLV.parse(dynamicParam);
        }
    }

    @Override
    public void cancel(ITaskCancelListener taskCancelListener) {
        taskCancelListener.onCancelFinish(true);
    }

}
