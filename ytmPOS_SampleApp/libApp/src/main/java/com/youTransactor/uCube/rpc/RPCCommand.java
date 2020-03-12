/**
 * Copyright (C) 2016, YouTransactor. All Rights Reserved.
 *
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youTransactor.uCube.rpc;

import com.youTransactor.uCube.AbstractTask;
import com.youTransactor.uCube.LogManager;
import com.youTransactor.uCube.TaskEvent;

/**
 * @author gbillard on 3/9/16.
 */
public class RPCCommand extends AbstractTask implements IRPCMessageHandler {

	protected RPCCommandStatus state;
    protected short commandId;
	protected boolean ciphered = false;
	protected byte[] payload;
	protected RPCMessage response;

	// returned by the getResponseStatus method
	public static class ResponseStatus {
		public enum RPCState {
			ERROR,
			OKAY,
		}

		public RPCState rpcState;
		public short cmdStatus;

		ResponseStatus (RPCState rpcState, short cmdStatus) {
			this.rpcState = rpcState;
			this.cmdStatus = cmdStatus;
		}
	}

	public RPCCommand(short commandId) {
		this.commandId = commandId;
		state = RPCCommandStatus.READY;
	}

	public RPCCommand(short commandId, boolean ciphered) {
		this(commandId);
		this.ciphered = ciphered;
	}

    public short getCommandId() {
        return commandId;
    }

	public ResponseStatus getResponseStatus() {
		return response == null ?
				new ResponseStatus(ResponseStatus.RPCState.ERROR, (short) 0) :
				new ResponseStatus(ResponseStatus.RPCState.OKAY, response.getStatus());
	}

	public byte[] getResponseData() {
		return response == null ? null : response.getData();
	}

	@Override
	public void start() {
		RPCManager.getInstance().send(this);
	}

	public byte[] getPayload() {
		if (payload == null) {
			try {
				payload = createPayload();
			} catch (Exception e) {
				LogManager.error(getClass().getSimpleName(), "create payload error", e);
				notifyMonitor(TaskEvent.FAILED, this);
			}
		}

		return payload;
	}

	@Override
	public void processMessage(RPCMessage message) {
		response = message;

		try {
			if (isValidResponse() && parseResponse()) {
				setState(getCommandStatusValue());
				return;
			}
		} catch (Exception e) {
			LogManager.error(getClass().getSimpleName(), "parse response exception", e);
		}

		LogManager.debug(RPCCommand.class.getName(), "RPC command failed. Status: " + (response != null ? response.getStatus() : "none"));

		setState(RPCCommandStatus.FAILED);
	}

	protected RPCCommandStatus getCommandStatusValue() {
		return RPCCommandStatus.SUCCESS;
	}

	public void setState(RPCCommandStatus newStatus) {
		if (newStatus == state) {
			return;
		}

		state = newStatus;

		switch (newStatus) {
		case CONNECT_ERROR:
		case FAILED:
			notifyMonitor(TaskEvent.FAILED, this);
			break;

		case CANCELED:
			notifyMonitor(TaskEvent.CANCELLED, this);
			break;

		case SUCCESS:
			notifyMonitor(TaskEvent.SUCCESS, this);
			break;

		case SENDING:
		case CONNECT:
			notifyMonitor(TaskEvent.PROGRESS, this);
			break;
		}
	}

	protected boolean isValidResponse() {
		return !(!ciphered && (response == null || response.getStatus() != Constants.SUCCESS_STATUS));
	}

	protected boolean parseResponse() {
		return true;
	}

	protected byte[] createPayload() {
		return new byte[0];
	}

}
