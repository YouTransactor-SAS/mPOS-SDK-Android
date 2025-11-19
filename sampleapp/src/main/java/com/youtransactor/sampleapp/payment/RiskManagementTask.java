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

import com.youTransactor.uCube.ITaskCancelListener;
import com.youTransactor.uCube.ITaskMonitor;
import com.youTransactor.uCube.TLV;
import com.youTransactor.uCube.TaskEvent;
import com.youTransactor.uCube.Tools;
import com.youTransactor.uCube.payment.task.IRiskManagementTask;
import com.youTransactor.uCube.payment.PaymentContext;
import com.youTransactor.uCube.rpc.EMVApplicationDescriptor;

public class RiskManagementTask implements IRiskManagementTask {

	private final Context context;
	private ITaskMonitor monitor;
	private PaymentContext paymentContext;
	private byte[] tvr;
	private AlertDialog alertDialog;

	public RiskManagementTask(Context context) {
		this.context = context;
	}

	@Override
	public byte[] getTVR() {
		return tvr;
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

		new Handler(Looper.getMainLooper()).post(() -> {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);

			builder.setTitle("Risk management");
			builder.setCancelable(false);
			builder.setMessage("Is card stolen ?");

			builder.setPositiveButton("Yes", (dialog, which) -> {
				dialog.dismiss();

				end(new byte[] {0, 0b10000, 0, 0, 0});
			});

			builder.setNegativeButton("No", (dialog, which) -> {
				dialog.dismiss();

				end(new byte[] {0, 0, 0, 0, 0});
			});

			if(paymentContext.overrideParameter){
				byte[] dynamicParam =
					PaymentTagOverrideFactory.getContactTerminalCapabilities(
						new byte[] {(byte) 0xE0, (byte) 0x20, (byte) 0xC0});
				paymentContext.TlvListToUpdate = TLV.parse(dynamicParam);
			}

			alertDialog = builder.create();
			alertDialog.show();
		});
	}

	@Override
	public void cancel(ITaskCancelListener taskCancelListener) {
		if(alertDialog != null && alertDialog.isShowing())
			alertDialog.dismiss();

		monitor.handleEvent(TaskEvent.CANCELLED);
		taskCancelListener.onCancelFinish(true);
	}

	private void end(byte[] tvr) {
		this.tvr = tvr;

		EMVApplicationDescriptor selectedApplication = paymentContext.selectedApplication;
		if (selectedApplication != null) {
			String selectedAID = Tools.bytesToHex(selectedApplication.getAid()).substring(0, 10);

			switch (selectedAID) {
				case "A000000003":
					paymentContext.applicationVersion = 140;
					break;
				case "A000000004":
					paymentContext.applicationVersion = 202;
					break;
				case "A000000042":
					paymentContext.applicationVersion = 203;
					break;
			}
		}

		monitor.handleEvent(TaskEvent.SUCCESS);
	}


}
