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

import java.util.Arrays;

/**
 * @author gbillard on 6/3/16.
 */
public class SetInfoFieldCommand extends RPCCommand {

	private int powerTimeout = -1;

	public SetInfoFieldCommand() {
		super(Constants.SET_INFO_FIELD_COMMAND);
	}

	/**
	 * set the power off timeout
	 * 0 mean no power off.
	 *
	 * @param powerTimeout The timeout in second (min = 10, max = 255)
	 */
	public void setPowerTimeout(int powerTimeout) {
		if (powerTimeout < 0 || (powerTimeout > 0 && powerTimeout < 32)) {
			this.powerTimeout = 32;

		} else if (powerTimeout > 255) {
			this.powerTimeout = 255;

		} else {
			this.powerTimeout = powerTimeout;
		}
	}

	@Override
	protected byte[] createPayload() {
		byte[] payload = new byte[255];
		int offset = 0;

		if (powerTimeout >= 0) {
			payload[offset++] = (byte) 0xC6;
			payload[offset++] = 0x01;
			payload[offset++] = (byte) powerTimeout;
		}

		return Arrays.copyOfRange(payload, 0, offset);
	}

}
