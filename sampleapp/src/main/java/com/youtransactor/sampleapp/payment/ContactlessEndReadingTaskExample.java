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

import static com.youTransactor.uCube.rpc.Constants.*;

import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.youTransactor.uCube.ITaskCancelListener;
import com.youTransactor.uCube.ITaskMonitor;
import com.youTransactor.uCube.TaskEvent;
import com.youTransactor.uCube.Tools;
import com.youTransactor.uCube.payment.PaymentContext;
import com.youTransactor.uCube.payment.task.IContactlessEndReadingTask;
import com.youTransactor.uCube.rpc.command.GetPlainTagCommand;
import com.youtransactor.sampleapp.R;
import com.youtransactor.sampleapp.transactionView.TransactionViewBase;
import com.youtransactor.sampleapp.transactionView.components.DisplayListFragment;

import java.util.ArrayList;
import java.util.Map;

public class ContactlessEndReadingTaskExample implements IContactlessEndReadingTask {

    private final static String TAG = ContactlessEndReadingTaskExample.class.getSimpleName();

    private ITaskMonitor monitor;
    private PaymentContext paymentContext;
    private final TaskAction taskAction;

    public enum TaskAction {
        SUCCESS,
        FAILURE,
        NO_OP
    }

    public ContactlessEndReadingTaskExample(final TaskAction taskAction) {
        this.taskAction = taskAction;
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
    public void execute(ITaskMonitor monitor){
        this.monitor = monitor;

        final int[][] tags = {new int[]{TAG_APP_LANGUAGE}};
        new GetPlainTagCommand(tags[0]).execute((event, params) -> {
            switch (event) {
                case PROGRESS:
                    break;

                case CANCELLED:
                    monitor.handleEvent(TaskEvent.CANCELLED);
                    break;

                case FAILED:
                    monitor.handleEvent(TaskEvent.FAILED);
                    break;

                case SUCCESS:
                    Map<Integer, byte[]> values = ((GetPlainTagCommand) params[0]).getResult();
                    Log.d(TAG, String.format("Language list : 0x%x : %s", TAG_APP_LANGUAGE,
                            Tools.bytesToHex(values.get(TAG_APP_LANGUAGE))));
                    monitor.handleEvent(TaskEvent.SUCCESS);
                    break;
            }
        });
        // display list
    }

    @Override
    public void cancel(ITaskCancelListener taskCancelListener) {
        Log.d(TAG, "Task cancelled");
        monitor.handleEvent(TaskEvent.CANCELLED);
        taskCancelListener.onCancelFinish(true);
    }

}
