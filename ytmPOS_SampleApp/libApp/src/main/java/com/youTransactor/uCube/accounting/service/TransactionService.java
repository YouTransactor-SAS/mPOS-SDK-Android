/*
 * Copyright (C) 2016, YouTransactor. All Rights Reserved.
 *
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */

package com.youTransactor.uCube.accounting.service;

import android.content.Context;

import com.youTransactor.uCube.ITask;
import com.youTransactor.uCube.ITaskMonitor;
import com.youTransactor.uCube.TaskEvent;
import com.youTransactor.uCube.accounting.task.SendTransactionDataTask;

/**
 * Created by gmx on 25/07/17.
 */

public class TransactionService extends AbstractAccountingService {

    private Context context;
    private String  merchantContract;
    private String  transactionID;
    private int     transactionStatus;

    public TransactionService(Context context, String merchantContract, String transactionID, int transactionStatus) {

        this.context           = context;
        this.merchantContract  = merchantContract;
        this.transactionID     = transactionID;
        this.transactionStatus = transactionStatus;
    }

    @Override
    protected void onDeviceInfosRetrieved() {
        sendTransactionData();
    }

    private void sendTransactionData() {
        new SendTransactionDataTask(deviceInfos, merchantContract, transactionID, transactionStatus).execute(new ITaskMonitor() {
            @Override
            public void handleEvent(TaskEvent event, Object... params) {
                switch (event) {
                    case FAILED:
                        failedTask = (ITask) params[0];
                        notifyMonitor(TaskEvent.FAILED, this);
                        break;

                    case SUCCESS:
                        notifyMonitor(TaskEvent.SUCCESS, this);
                }
            }
        });
    }
}
