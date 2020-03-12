/*
 * Copyright (C) 2016, YouTransactor. All Rights Reserved.
 *
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youTransactor.uCube.rpc.command;

import com.youTransactor.uCube.rpc.Constants;
import com.youTransactor.uCube.rpc.RPCCommand;

/**
 * @author gbillard on 5/19/16.
 */
public class WaitCardRemovalCommand extends RPCCommand {

	private int timeout = 30;

	public WaitCardRemovalCommand() {
		super(Constants.CARD_WAIT_REMOVAL_COMMAND);
	}

	public void setTimeout(int timeout) {
		if (timeout >= 0 && timeout <= 0xFF) {
			this.timeout = timeout;
		}
	}

	@Override
	protected byte[] createPayload() {
		return new byte[] {Constants.ICC_READER, (byte) timeout};
	}

}
