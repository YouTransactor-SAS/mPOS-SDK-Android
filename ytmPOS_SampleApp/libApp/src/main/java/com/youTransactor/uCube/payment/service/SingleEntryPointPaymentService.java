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

import com.youTransactor.uCube.ITask;
import com.youTransactor.uCube.ITaskMonitor;
import com.youTransactor.uCube.TaskEvent;
import com.youTransactor.uCube.payment.PaymentContext;
import com.youTransactor.uCube.payment.PaymentState;
import com.youTransactor.uCube.payment.service.ICCPaymentService;
import com.youTransactor.uCube.payment.service.MSPaymentService;
import com.youTransactor.uCube.payment.service.NFCPaymentService;
import com.youTransactor.uCube.payment.service.PaymentService;
import com.youTransactor.uCube.rpc.Constants;
import com.youTransactor.uCube.rpc.RPCCommand;
import com.youTransactor.uCube.rpc.command.EnterSecureSessionCommand;
import com.youTransactor.uCube.rpc.command.StartNFCTransactionCommand;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gbillard on 5/31/16.
 */
public class SingleEntryPointPaymentService extends PaymentService {

	public SingleEntryPointPaymentService(PaymentContext paymentContext, byte[] enabledReaders) {
		super(paymentContext, enabledReaders);
	}

	@Override
	protected void onGetInfos() {
		final EnterSecureSessionCommand cmd = new EnterSecureSessionCommand();
		cmd.execute(new ITaskMonitor() {
			@Override
			public void handleEvent(TaskEvent event, Object... params) {
				switch (event) {
				case FAILED:
					failedTask = (ITask) params[0];
					notifyMonitor(TaskEvent.FAILED);
					break;

				case SUCCESS:
					context.setKsn(cmd.getKsn());

					context.setPaymentStatus(PaymentState.ENTER_SECURE_SESSION);
					notifyMonitor(TaskEvent.PROGRESS);

					startTransaction();
					break;
				}
			}
		});
	}

	private void startTransaction() {
		final StartNFCTransactionCommand cmd = new StartNFCTransactionCommand(enabledReaders, context.getCurrency());

		cmd.setDate(context.getTransactionDate());
		cmd.setNoAmount(context.getAmount() < 0);
		cmd.setAmount(context.getAmount());
		cmd.setTimeout(cardWaitTimeout);
		cmd.setTransactionType(context.getTransactionType().getCode());
		cmd.setInputProprietaryTLVStream(context.getStartNFCTransactionInputProprietaryTLVStream());

		// FIXME: 16/11/16 This code is here to fix a problem in the dongle,
		// FIXME: no amount is returned on NFC payment when amount is set on the dongle itself.
		// FIXME: by adding this tag C1, the amount is return normally.
		byte[] ba = {(byte) 0x9f, (byte) 0x02};
		List<byte[]> list = new ArrayList<>();
		list.add(ba);
		cmd.setRequestedTagList(list);

		cmd.execute((event, params) -> {
			switch (event) {
			case FAILED:
				RPCCommand.ResponseStatus responseStatus = cmd.getResponseStatus();
				if (responseStatus.rpcState == RPCCommand.ResponseStatus.RPCState.ERROR) {
					failedTask = cmd;
					notifyMonitor(TaskEvent.FAILED);
					break;
				}
				switch (responseStatus.cmdStatus) {
				case Constants.CANCELLED_STATUS:
				case Constants.TIMEOUT_STATUS:
					end(PaymentState.CANCELLED);
					break;

				default:
					if(responseStatus.cmdStatus >= Constants.MPOS_ERROR_END &&
							responseStatus.cmdStatus <= Constants.MPOS_ERROR_START)
					{
						end(PaymentState.NFC_MPOS_ERROR);
					} else {
						failedTask = cmd;
						notifyMonitor(TaskEvent.FAILED);
					}
					break;
				}
				break;

			case CANCELLED:
				notifyMonitor(TaskEvent.CANCELLED);
				break;

			case SUCCESS:
				context.setActivatedReader(cmd.getActivatedReader());

				displayMessage(context.getString("LBL_wait_card_ok"), new ITaskMonitor() {
						@Override
					public void handleEvent(TaskEvent event, Object... params) {
						if (event == TaskEvent.PROGRESS) {
							return;
						}

						switch (context.getActivatedReader()) {
						case Constants.ICC_READER:
							paymentService = new ICCPaymentService(context);
							paymentService.setApplicationSelectionProcessor(applicationSelectionProcessor);
							break;

						case Constants.MS_READER:
							paymentService = new MSPaymentService(context);
							break;

						case Constants.NFC_READER:
							context.setNFCOutcome(cmd.getNFCOutcome());
							paymentService = new NFCPaymentService(context);
							break;

						default:
							failedTask = cmd;
							notifyMonitor(TaskEvent.FAILED);
							return;
						}

						if (context.getAmount() == -1) {
							context.setAmount(cmd.getAmount());
						}

						paymentService.setRiskManagementTask(riskManagementTask);
						paymentService.setAuthorizationProcessor(authorizationProcessor);

						context.setPaymentStatus(PaymentState.STARTED);
						notifyMonitor(TaskEvent.PROGRESS);

						paymentService.execute(monitor);
					}
				});
			}
		});
	}

}
