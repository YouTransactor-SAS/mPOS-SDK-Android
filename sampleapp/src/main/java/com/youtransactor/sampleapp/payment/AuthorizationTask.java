/*
 * Copyright (C) 2011-2020, YouTransactor. All Rights Reserved.
 * <p/>
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youtransactor.sampleapp.payment;

import android.app.Activity;
import android.app.AlertDialog;

import com.youTransactor.uCube.ITaskCancelListener;
import com.youTransactor.uCube.ITaskMonitor;
import com.youTransactor.uCube.TLV;
import com.youTransactor.uCube.TaskEvent;
import com.youTransactor.uCube.Tools;
import com.youTransactor.uCube.log.LogManager;
import com.youTransactor.uCube.payment.task.IAuthorizationTask;
import com.youTransactor.uCube.payment.PaymentContext;

import java.util.Map;

public class AuthorizationTask implements IAuthorizationTask {

    private Activity activity;
    private byte[] authResponse;
    private ITaskMonitor monitor;
    private PaymentContext paymentContext;
    private AlertDialog alertDialog;

    public AuthorizationTask(Activity activity) {
        this.activity = activity;
    }

    @Override
    public byte[] getAuthorizationResponse() {
        return authResponse;
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
        this.monitor = monitor;

        if (paymentContext.authorizationSecuredTagsValues != null)
            LogManager.d("authorization secured tags " + Tools.bytesToHex(paymentContext.authorizationSecuredTagsValues));

        //todo send this to backend to check MAC paymentContext.authorizationGetPlainTagsResponse
        if (paymentContext.authorizationPlainTagsValues != null) {

            for (Integer tag : paymentContext.authorizationPlainTagsValues.keySet()) {
                LogManager.d( String.format("Plain Tag : 0x%x : %s", tag, Tools.bytesToHex(paymentContext.authorizationPlainTagsValues.get(tag))));
            }
        }

        //todo here you can call the host

        activity.runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);

            builder.setCancelable(true);
            builder.setTitle("Authorization response");

            builder.setItems(new String[]{"Approved", "SCA (0x1A)", "SCA (0x70)", "Declined", "Unable to go online",  "Failed"}, (dialog, which) -> {
                dialog.dismiss();
                end(which);
            });

            alertDialog = builder.create();
            alertDialog.show();
        });
    }

    @Override
    public void cancel(ITaskCancelListener taskCancelListener){
        if(alertDialog != null && alertDialog.isShowing())
            alertDialog.dismiss();

        new Thread(() -> monitor.handleEvent(TaskEvent.CANCELLED)).start();
        taskCancelListener.onCancelFinish(true);
    }


    private void end(int choice) {
        switch (choice) {
            case 0:
                this.authResponse = new byte[]{(byte) 0x8A, 0x02, 0x30, 0x30};
                break;

            case 1:
                this.authResponse = new byte[]{(byte) 0x8A, 0x02, 0x31, 0x41};
                break;

            case 2:
                this.authResponse = new byte[]{(byte) 0x8A, 0x02, 0x37, 0x30};
                break;

            case 3:
                this.authResponse = new byte[]{(byte) 0x8A, 0x02, 0x30, 0x35};
                break;

            case 4:
                this.authResponse = new byte[]{(byte) 0x8A, 0x02, 0x39, 0x38};
                break;

            case 5:
                new Thread(() -> monitor.handleEvent(TaskEvent.FAILED)).start();
                return;
        }

        new Thread(() -> monitor.handleEvent(TaskEvent.SUCCESS)).start();
    }

}
