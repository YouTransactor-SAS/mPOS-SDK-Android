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
import com.youTransactor.uCube.payment.PaymentContext;
import com.youTransactor.uCube.payment.task.ITlvUpdateTask;

import java.util.HashMap;
import java.util.Map;

public class UpdateTlvTask implements ITlvUpdateTask {
	private PaymentContext paymentContext;
	private final Context context;

	public UpdateTlvTask(Context context) {
		this.context = context;
	}
	private void addTlvMap(int key, byte[] value) {
		paymentContext.TlvListToUpdate.put(key, value);
	}
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
		//this only an example: it should be updated
//		byte[] value = new byte[]{(byte) 0x00, 0x00, 0x00, 0x00, 0x30, 0x30};
//		addTlvMap(0x9F02, value);
//		byte[] value2 = new byte[]{(byte) 0x0A, 0x0B, 0x0C};
//		addTlvMap(0x9FFF, value2);
	}

	@Override
	public void cancel(ITaskCancelListener taskCancelListener){
		taskCancelListener.onCancelFinish(true);
	}

}
