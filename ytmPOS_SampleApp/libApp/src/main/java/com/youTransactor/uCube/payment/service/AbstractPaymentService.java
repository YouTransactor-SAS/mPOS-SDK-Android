/*
 * Copyright (C) 2016, YouTransactor. All Rights Reserved.
 *
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youTransactor.uCube.payment.service;

import com.youTransactor.uCube.AbstractService;
import com.youTransactor.uCube.ITask;
import com.youTransactor.uCube.ITaskMonitor;
import com.youTransactor.uCube.LogManager;
import com.youTransactor.uCube.TLV;
import com.youTransactor.uCube.TaskEvent;
import com.youTransactor.uCube.payment.task.IApplicationSelectionTask;
import com.youTransactor.uCube.payment.task.IAuthorizationTask;
import com.youTransactor.uCube.payment.task.IPaymentTask;
import com.youTransactor.uCube.payment.task.IRiskManagementTask;
import com.youTransactor.uCube.payment.PaymentContext;
import com.youTransactor.uCube.payment.PaymentState;
import com.youTransactor.uCube.rpc.command.DisplayMessageCommand;
import com.youTransactor.uCube.rpc.command.ExitSecureSessionCommand;

import java.util.Map;

/**
 * @author gbillard on 5/19/16.
 */
abstract public class AbstractPaymentService extends AbstractService implements IPaymentTask {

    protected PaymentContext context;
    IApplicationSelectionTask applicationSelectionProcessor;
    IRiskManagementTask riskManagementTask;
    IAuthorizationTask authorizationProcessor;

    public AbstractPaymentService(PaymentContext context) {
        this.context = context;
    }

    protected void riskManagement() {
        LogManager.d("riskManagement");

        if (riskManagementTask == null) {
            onRiskManagementDone();
            return;
        }

        riskManagementTask.setContext(context);
        riskManagementTask.execute((event, params) -> {
            switch (event) {
                case FAILED:
                    failedTask = riskManagementTask;
                    notifyMonitor(TaskEvent.FAILED);
                    break;

                case SUCCESS:
                    context.setTvr(riskManagementTask.getTVR());
                    onRiskManagementDone();
                    break;
            }
        });
    }

    protected void onRiskManagementDone() {
        processTransaction();
    }

    public void onAuthorizationDone() {
        LogManager.d("onAuthorizationResponse");

        Map<Integer, byte[]> authResponse = TLV.parse(context.getAuthorizationResponse());

        if (authResponse != null) {
            byte[] tag8a = authResponse.get(0x8A);

            if (TLV.equalValue(tag8a, new byte[]{0x30, 0x30})) {
                end(PaymentState.APPROVED);
                return;
            }
        }

        end(PaymentState.DECLINED);
    }

    public PaymentContext getContext() {
        return context;
    }

    public void setContext(PaymentContext context) {
        this.context = context;
    }

    public void setApplicationSelectionProcessor(IApplicationSelectionTask applicationSelectionProcessor) {
        this.applicationSelectionProcessor = applicationSelectionProcessor;
    }

    public void setRiskManagementTask(IRiskManagementTask riskManagementTask) {
        this.riskManagementTask = riskManagementTask;
    }

    public void setAuthorizationProcessor(IAuthorizationTask authorizationProcessor) {
        this.authorizationProcessor = authorizationProcessor;
    }

    protected void processTransaction() {
        LogManager.d("processTransaction");

        doAuthorization();
    }

    protected void doAuthorization() {
        LogManager.d("doAuthorization");


        if (authorizationProcessor == null) {
            onAuthorizationDone();
            return;
        }

        context.setPaymentStatus(PaymentState.AUTHORIZE);

        displayMessage(context.getString("LBL_authorization"), (event, params) -> {
            switch (event) {
                case FAILED:
                    failedTask = (ITask) params[0];
                    end(PaymentState.ERROR);
                    break;

                case SUCCESS:
                    performAuthorization();
                    break;
            }
        });
    }

    protected void performAuthorization() {
        authorizationProcessor.setContext(context);

        authorizationProcessor.execute((event, params) -> {
            switch (event) {
                case FAILED:
                    failedTask = authorizationProcessor;
                    notifyMonitor(TaskEvent.FAILED);
                    break;

                case SUCCESS:
                    context.setAuthorizationResponse(authorizationProcessor.getAuthorizationResponse());

                    displayMessage(context.getString("LBL_wait"), new ITaskMonitor() {
                        @Override
                        public void handleEvent(TaskEvent event, Object... params) {
                            if (event == TaskEvent.PROGRESS) {
                                return;
                            }

                            onAuthorizationDone();
                        }
                    });
                    break;
            }
        });
    }

    private void exitSecureSession() {
        LogManager.d("exitSecureSession");

        new ExitSecureSessionCommand().execute((event, params) -> {
            if (event == TaskEvent.PROGRESS) {
                return;
            }

            displayResultThenNotifyMonitor((event1, params1) -> {
                if (event1 == TaskEvent.PROGRESS) {
                    return;
                }

                switch (event1) {
                    case FAILED:
                        notifyMonitor(TaskEvent.FAILED);
                        break;

                    case SUCCESS:
                        notifyMonitor(TaskEvent.SUCCESS);
                        break;
                }
            });
        });
    }

    protected void displayResultThenNotifyMonitor(ITaskMonitor monitor) {
        displayResult(monitor);
    }

    protected void displayResult(ITaskMonitor monitor) {
        LogManager.d("displayResult");

        String msgKey;

        switch (context.getPaymentStatus()) {
            case APPROVED:
                msgKey = "LBL_approved";
                break;

            case CHIP_REQUIRED:
                msgKey = "LBL_use_chip";
                break;

            case UNSUPPORTED_CARD:
                msgKey = "LBL_unsupported_card";
                break;

            case REFUSED_CARD:
                msgKey = "LBL_refused_card";
                break;

            case CARD_WAIT_FAILED:
                msgKey = "LBL_no_card_detected";
                break;

            case CANCELLED:
                msgKey = "LBL_cancelled";
                break;

            case TRY_OTHER_INTERFACE:
                msgKey = "LBL_try_other_interface";
                break;

            case NFC_MPOS_ERROR:
                msgKey = "LBL_cfg_error";
                break;

            default:
                msgKey = "LBL_declined";
                break;
        }

        displayMessage(context.getString(msgKey), monitor);
    }

    protected void end(final PaymentState state) {
        LogManager.debug(this.getClass().getSimpleName(), "end: " + state.name());

        context.setPaymentStatus(state);

        exitSecureSession();
    }

    protected void displayMessage(String msg, ITaskMonitor callback) {
        DisplayMessageCommand displayMessageCommand = new DisplayMessageCommand(msg);

        displayMessageCommand.setCentered(context.getByteFromKey("GLOBAL_centered", (byte) 0x00));
        displayMessageCommand.setYPosition(context.getByteFromKey("GLOBAL_yposition", (byte) 0x00));
        displayMessageCommand.setFont(context.getByteFromKey("GLOBAL_font_id", (byte) 0x00));

        displayMessageCommand.execute(callback);
    }

    @Override
    protected void notifyMonitor(TaskEvent event, Object... params) {
        super.notifyMonitor(event, this);
    }

}
