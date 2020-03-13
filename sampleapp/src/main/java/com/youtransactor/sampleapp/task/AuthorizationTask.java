/*
 * Copyright (C) 2020, YouTransactor. All Rights Reserved.
 *
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youtransactor.sampleapp.task;

import android.app.Activity;
import android.app.AlertDialog;

import com.youTransactor.uCube.ITaskMonitor;
import com.youTransactor.uCube.TaskEvent;
import com.youTransactor.uCube.payment.task.IAuthorizationTask;
import com.youTransactor.uCube.payment.PaymentContext;
import com.youTransactor.uCube.rpc.Constants;

public class AuthorizationTask implements IAuthorizationTask {

	private Activity activity;
	private byte[] authResponse;
	private ITaskMonitor monitor;
	private PaymentContext paymentContext;

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

		activity.runOnUiThread(() -> {
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);

			builder.setTitle("Authorization response");

			builder.setItems(new String[]{"Approved", "Declined", "Unable to go online"}, (dialog, which) -> {
				dialog.dismiss();
				end(which);
			});

			builder.create().show();
		});
	}

	private void end(int choice) {
		if (paymentContext.getActivatedReader() == Constants.NFC_READER) {
			switch (choice) {
			case 0:
				this.authResponse = new byte[] {0x30, 0x30};
				break;

			case 1:
				this.authResponse = new byte[] {0x35, 0x31};
				break;

			case 2:
				this.authResponse = new byte[] {0x50, 0x50};
				break;
			}

		} else {
			switch (choice) {
			case 0:
				this.authResponse = new byte[] {(byte) 0x8A, 0x02, 0x30, 0x30};
				break;

			case 1:
				this.authResponse = new byte[] {(byte) 0x8A, 0x02, 0x30, 0x35};
				break;

			case 2:
				this.authResponse = new byte[] {(byte) 0x8A, 0x02, 0x39, 0x38};
				break;
			}
		}

		new Thread(() -> monitor.handleEvent(TaskEvent.SUCCESS)).start();
	}

}
