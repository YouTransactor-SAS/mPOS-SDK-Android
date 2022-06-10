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

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.youTransactor.uCube.ITaskCancelListener;
import com.youTransactor.uCube.ITaskMonitor;
import com.youTransactor.uCube.TaskEvent;
import com.youTransactor.uCube.Tools;
import com.youTransactor.uCube.payment.task.IAuthorizationTask;
import com.youTransactor.uCube.payment.PaymentContext;

public class AuthorizationTask implements IAuthorizationTask {
    private static final String TAG = AuthorizationTask.class.getName();

    private final Context context;
    private MeasureStatesListener measureStatesListener;
    private byte[] authResponse;
    private ITaskMonitor monitor;
    private PaymentContext paymentContext;
    private AlertDialog alertDialog;

    public AuthorizationTask(Context context) {
        this.context = context;
    }

    public void setMeasureStatesListener(MeasureStatesListener measureStatesListener) {
        this.measureStatesListener = measureStatesListener;
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
    public void execute(ITaskMonitor monitor) {/*
        try {
            throw new NullPointerException();
        }catch (Exception e) {
            monitor.handleEvent(TaskEvent.FAILED);
        }*/
        //throw new NullPointerException();
        this.monitor = monitor;

        if (paymentContext.authorizationSecuredTagsValues != null)
            Log.d(TAG,"authorization secured tags " + Tools.bytesToHex(paymentContext.authorizationSecuredTagsValues));

        //todo send this to backend to check the integrity
        if (paymentContext.authorizationGetPlainTagsResponse != null)
            Log.d(TAG,"authorization plain tags response " + Tools.bytesToHex(paymentContext.authorizationGetPlainTagsResponse));

        if (paymentContext.authorizationPlainTagsValues != null) {

            for (Integer tag : paymentContext.authorizationPlainTagsValues.keySet()) {
                Log.d(TAG, String.format("Plain Tag : 0x%x : %s", tag, Tools.bytesToHex(paymentContext.authorizationPlainTagsValues.get(tag))));
            }
        }

        //todo here you can call the host
        if(context == null) {
            this.authResponse = new byte[]{(byte) 0x8A, 0x02, 0x30, 0x30};
            monitor.handleEvent(TaskEvent.SUCCESS);
            return;
        }

        new Handler(Looper.getMainLooper()).post(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);

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

        monitor.handleEvent(TaskEvent.CANCELLED);
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
                monitor.handleEvent(TaskEvent.FAILED);
                return;
        }

        if (measureStatesListener != null)
            measureStatesListener.onAuthorizationResponse();
        monitor.handleEvent(TaskEvent.SUCCESS);
    }

}
