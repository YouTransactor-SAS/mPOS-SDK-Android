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

import com.youTransactor.uCube.ITaskMonitor;
import com.youTransactor.uCube.LogManager;
import com.youTransactor.uCube.TaskEvent;
import com.youTransactor.uCube.payment.PaymentContext;
import com.youTransactor.uCube.payment.PaymentState;
import com.youTransactor.uCube.rpc.Constants;
import com.youTransactor.uCube.rpc.command.GetPlainTagCommand;
import com.youTransactor.uCube.rpc.command.GetSecuredTagCommand;
import com.youTransactor.uCube.rpc.command.SimplifiedOnlinePINCommand;

import java.text.MessageFormat;

/**
 * @author gbillard on 5/11/16.
 */
public class MSPaymentService extends AbstractPaymentService {

	public MSPaymentService(PaymentContext context) {
		super(context);
	}

	@Override
	protected void start() {
		LogManager.debug(this.getClass().getSimpleName(), "start");

		getSecuredTag();
	}

	private void getSecuredTag() {
		LogManager.debug(this.getClass().getSimpleName(), "getSecuredTag");

		final GetSecuredTagCommand cmd = new GetSecuredTagCommand(context.getRequestedSecuredTagList());
		cmd.execute(new ITaskMonitor() {
			@Override
			public void handleEvent(TaskEvent event, Object... params) {
				switch (event) {
				case FAILED:
					end(PaymentState.ERROR);
					break;

				case SUCCESS:
					context.setSecuredTagBlock(cmd.getResponseData());
					getPlainTag();
					break;
				}
			}
		});
	}

	private void getPlainTag() {
		LogManager.debug(this.getClass().getSimpleName(), "getPlainTag");

		final GetPlainTagCommand cmd = new GetPlainTagCommand(context.getRequestedPlainTagList());
		cmd.execute(new ITaskMonitor() {
			@Override
			public void handleEvent(TaskEvent event, Object... params) {
				switch (event) {
				case FAILED:
					end(PaymentState.ERROR);
					break;

				case SUCCESS:
					context.setPlainTagTLV(cmd.getResult());
					checkMSRAction();
					break;
				}
			}
		});
	}

	private void checkMSRAction() {
		LogManager.debug(this.getClass().getSimpleName(), "checkMSRAction");

		byte[] actionCode = context.getPlainTagTLV().get(new Integer(Constants.TAG_MSR_ACTION ));

		if (actionCode == null || actionCode.length == 0) {
			end(PaymentState.ERROR);
			return;
		}

		switch (actionCode[0]) {
		case Constants.MSR_ACTION_CHIP_REQUIRED:
			end(PaymentState.CHIP_REQUIRED);
			break;

		case Constants.MSR_ACTION_DECLINE:
			end(PaymentState.DECLINED);
			break;

		case Constants.MSR_ACTION_NONE:
			if (context.isForceOnlinePIN()) {
				onlinePIN();
			} else {
				riskManagement();
			}
			break;

		case Constants.MSR_ACTION_ONLINE_PIN_REQUIRED:
			onlinePIN();
			break;

		default:
			end(PaymentState.ERROR);
			break;
		}
	}

	private void onlinePIN() {
		LogManager.debug(this.getClass().getSimpleName(), "onlinePIN");

		final SimplifiedOnlinePINCommand cmd = new SimplifiedOnlinePINCommand(context.getAmount(), context.getCurrency(), context.getOnlinePinBlockFormat());
		cmd.setPINRequestLabel(MessageFormat.format(context.getString("MSG_wait_card"), context.getCurrency().getLabel(), context.getAmount()));
		cmd.setWaitLabel(context.getString("LBL_wait"));
		cmd.execute(new ITaskMonitor() {
			@Override
			public void handleEvent(TaskEvent event, Object... params) {
				switch(event) {
				case FAILED:
					end(PaymentState.ERROR);
					break;

				case SUCCESS:
					context.setOnlinePinBlock(cmd.getResponseData());
					doAuthorization();
					break;
				}
			}
		});
	}

}
