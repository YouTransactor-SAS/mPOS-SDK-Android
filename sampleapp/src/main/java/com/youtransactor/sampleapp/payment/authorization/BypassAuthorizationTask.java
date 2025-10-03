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

import com.youTransactor.uCube.ITaskCancelListener;
import com.youTransactor.uCube.ITaskMonitor;
import com.youTransactor.uCube.TaskEvent;
import com.youTransactor.uCube.payment.PaymentContext;
import com.youTransactor.uCube.payment.task.IAuthorizationTask;

public class BypassAuthorizationTask implements IAuthorizationTask {

    private PaymentContext paymentContext;

    @Override
    public byte[] getAuthorizationResponse() {
        return AuthorizationOutcomes.APPROVED.correspondingResponse;
    }

    @Override
    public void execute(ITaskMonitor monitor) {
        monitor.handleEvent(TaskEvent.SUCCESS);
    }

    @Override
    public void cancel(ITaskCancelListener taskCancelListener) {
    }

    @Override
    public PaymentContext getContext() {
        return this.paymentContext;
    }

    @Override
    public void setContext(PaymentContext paymentContext) {
        this.paymentContext = paymentContext;

    }
}