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
package com.youtransactor.sampleapp.payment.authorization;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.youTransactor.uCube.ITaskCancelListener;
import com.youTransactor.uCube.ITaskMonitor;
import com.youTransactor.uCube.TaskEvent;
import com.youTransactor.uCube.Tools;
import com.youTransactor.uCube.payment.PaymentContext;
import com.youTransactor.uCube.payment.task.IAuthorizationTask;
import com.youtransactor.sampleapp.payment.MeasuresStatesListener;

import java.util.function.Supplier;
import java.util.stream.Stream;

public class UserChoiceAuthorizationTask implements IAuthorizationTask {
    private static final String TAG = UserChoiceAuthorizationTask.class.getName();

    private final Supplier<Context> androidContextSupplier;
    private MeasuresStatesListener measureStatesListener;
    private byte[] authResponse;
    private ITaskMonitor monitor;
    private PaymentContext paymentContext;
    private AlertDialog alertDialog;

    public UserChoiceAuthorizationTask(Supplier<Context> androidContextSupplier) {
        this.androidContextSupplier = androidContextSupplier;
    }

    public void setMeasureStatesListener(MeasuresStatesListener measureStatesListener) {
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
    public void execute(ITaskMonitor monitor) {
        this.monitor = monitor;

        if (paymentContext.authorizationSecuredTagsResponse != null)
            Log.d(TAG, "authorization secured tags response " + Tools.bytesToHex(paymentContext.authorizationSecuredTagsResponse));

        if (paymentContext.authorizationSecuredTagsValues != null) {
            for (Integer tag : paymentContext.authorizationSecuredTagsValues.keySet()) {
                Log.d(TAG, String.format("authorization secured tags: 0x%x : %s", tag, Tools.bytesToHex(paymentContext.authorizationSecuredTagsValues.get(tag))));
            }
        }
        //todo send this to backend to check the integrity
        if (paymentContext.authorizationGetPlainTagsResponse != null)
            Log.d(TAG, "authorization plain tags response " + Tools.bytesToHex(paymentContext.authorizationGetPlainTagsResponse));

        if (paymentContext.authorizationPlainTagsValues != null) {

            for (Integer tag : paymentContext.authorizationPlainTagsValues.keySet()) {
                Log.d(TAG, String.format("Plain Tag : 0x%x : %s", tag, Tools.bytesToHex(paymentContext.authorizationPlainTagsValues.get(tag))));
            }
        }
        final Context androidContext = androidContextSupplier.get();

        //todo here you can call the host
        if (androidContext == null) {
            this.authResponse = AuthorizationOutcomes.APPROVED.correspondingResponse;
            monitor.handleEvent(TaskEvent.SUCCESS);
            return;
        }

        if (paymentContext.isBypassAuthorization()) {
            end(AuthorizationOutcomes.APPROVED);
            return;
        }

        new Handler(Looper.getMainLooper()).post(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(androidContext);

            builder.setCancelable(true);
            builder.setTitle("Authorization response");

            final String[] labels = Stream.of(AuthorizationOutcomes.values())
                    .map(a -> a.label)
                    .toArray(String[]::new);
            builder.setItems(labels, (dialog, which) -> {
                dialog.dismiss();
                end(AuthorizationOutcomes.values()[which]);
            });
            builder.setCancelable(false);
            alertDialog = builder.create();
            alertDialog.show();
            alertDialog.setCanceledOnTouchOutside(false);
        });
    }

    @Override
    public void cancel(ITaskCancelListener taskCancelListener) {
        if (alertDialog != null && alertDialog.isShowing())
            alertDialog.dismiss();

        monitor.handleEvent(TaskEvent.CANCELLED);
        taskCancelListener.onCancelFinish(true);
    }

    private void end(final AuthorizationOutcomes outcome) {
        this.authResponse = outcome.correspondingResponse;
        if (outcome == AuthorizationOutcomes.FAILED) {
            monitor.handleEvent(TaskEvent.FAILED);
            return;
        }
        if (measureStatesListener != null)
            measureStatesListener.onAuthorizationResponse();
        monitor.handleEvent(TaskEvent.SUCCESS);
    }

}
