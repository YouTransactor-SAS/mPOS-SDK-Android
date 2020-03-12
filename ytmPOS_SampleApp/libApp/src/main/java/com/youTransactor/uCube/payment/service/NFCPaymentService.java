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
import com.youTransactor.uCube.LogManager;
import com.youTransactor.uCube.TaskEvent;
import com.youTransactor.uCube.payment.PaymentContext;
import com.youTransactor.uCube.payment.PaymentState;
import com.youTransactor.uCube.rpc.command.CompleteNFCTransactionCommand;
import com.youTransactor.uCube.rpc.command.GetPlainTagCommand;
import com.youTransactor.uCube.rpc.command.GetSecuredTagCommand;

/**
 * @author gbillard on 5/31/16.
 */
public class NFCPaymentService extends AbstractPaymentService {

	public NFCPaymentService(PaymentContext context) {
		super(context);
	}

	@Override
	protected void start() {
		switch (context.getNFCOutcome()[1]) {

		case 0x36: // APPROVED
		case 0x37: // DECLINED
		case 0x3E: // ONLINE REQUEST
		// In all this cases, we ask for TLVs
			getSecuredTag();
			break;

		case 0x31:
			end(PaymentState.TRY_OTHER_INTERFACE);
			break;

		case 0x3A:
			end(PaymentState.CANCELLED);
			break;

		case 0x3F:
		case 0x38:
		default:
			end(PaymentState.ERROR);
			break;
		}

	}


	private void getSecuredTag() {
		LogManager.debug(this.getClass().getSimpleName(), "getSecuredTag");

		// Check if the list is not empty
		if( context.getRequestedSecuredTagList() == null ){
			// Try getPlainTags too
			getPlainTag();
			return;
		}

		final GetSecuredTagCommand cmd = new GetSecuredTagCommand(context.getRequestedSecuredTagList());
		cmd.execute((event, params) -> {
			switch (event) {
			case FAILED:
				end(PaymentState.ERROR);
				break;

			case SUCCESS:
				context.setSecuredTagBlock(cmd.getResponseData());
				getPlainTag();
				break;
			}
		});
	}

	private void getPlainTag() {
		LogManager.debug(this.getClass().getSimpleName(), "getPlainTag");

		// Check if the list is not empty
		if( context.getRequestedPlainTagList() == null ){
			// Safeguard - In this implementation, you need at least one
			LogManager.debug (this.getClass().getSimpleName(), "getRequestedPlainTagList is empty");
			end(PaymentState.ERROR);
			return;
		}

		final GetPlainTagCommand cmd = new GetPlainTagCommand(context.getRequestedPlainTagList());
		cmd.execute((event, params) -> {
			switch (event) {
			case FAILED:
				end(PaymentState.ERROR);
				break;

			case SUCCESS:
				context.setPlainTagTLV(cmd.getResult());

				switch (context.getNFCOutcome()[1]) {

				case 0x36: // APPROVED
					end(PaymentState.APPROVED);
					break;

				case 0x37: // DECLINED
					end(PaymentState.DECLINED);
					break;

				default:
					doAuthorization();
					break;
				}
				break;
			}
		});
	}

	@Override
	public void onAuthorizationDone() {
		CompleteNFCTransactionCommand cmd = new CompleteNFCTransactionCommand(context.getAuthorizationResponse());
		cmd.execute((event, params) -> {
			switch (event) {
			case FAILED:
				failedTask = (ITask) params[0];
				notifyMonitor(TaskEvent.FAILED);
				break;

			case SUCCESS:
				context.setNFCOutcome(((CompleteNFCTransactionCommand) params[0]).getNFCOutcome());

				switch (context.getNFCOutcome()[1]) {
				case 0x36:
					end(PaymentState.APPROVED);
					break;

				case 0x3A:
					end(PaymentState.CANCELLED);
					break;

				case 0x37:
					end(PaymentState.DECLINED);
					break;

				case 0x38:
				default:
					end(PaymentState.ERROR);
					break;
				}

				break;
			}
		});
	}

	@Override
	protected void displayResult(ITaskMonitor monitor) {
		LogManager.debug(this.getClass().getSimpleName(), "displayResult");

		if(!context.getDisplayResultForNFC()){
			return;
		}
		
		String msgKey;

		switch (context.getPaymentStatus()) {
			case APPROVED:
				msgKey = "LBL_approved";
				break;

			case CHIP_REQUIRED:
				msgKey = "LBL_use_chip";
				break;

			case UNSUPPORTED_CARD:
				msgKey = "LBL_unsupported_card";
				break;

			case REFUSED_CARD:
				msgKey = "LBL_refused_card";
				break;

			case CARD_WAIT_FAILED:
				msgKey = "LBL_no_card_detected";
				break;

			case CANCELLED:
				msgKey = "LBL_cancelled";
				break;

			case TRY_OTHER_INTERFACE:
				msgKey = "LBL_try_other_interface";
				break;

			case NFC_MPOS_ERROR:
				msgKey = "LBL_cfg_error";
				break;

			default:
				msgKey = "LBL_declined";
				break;
		}

		super.displayMessage(context.getString(msgKey), monitor);
	}

	/*
	* NOTE: this override displayMessage method does not print at all !
	* */
	protected void displayMessage(String msg, ITaskMonitor callback) {
		if (callback != null) {
			callback.handleEvent(TaskEvent.SUCCESS);
		}
	}

}
