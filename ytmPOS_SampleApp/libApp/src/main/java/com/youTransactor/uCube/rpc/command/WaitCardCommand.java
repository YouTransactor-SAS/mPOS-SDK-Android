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
 * @author gbillard on 5/12/16.
 */
public class WaitCardCommand extends RPCCommand {

	private byte[] readerList;
	private int timeout = 0x00;
	private byte activatedReader;
	private byte iccStatus;
	private byte[] atr;

	public WaitCardCommand(byte[] readerList) {
		super(Constants.CARD_WAIT_INSERTION_COMMAND);

		this.readerList = readerList;
	}

	public WaitCardCommand(byte[] readerList, int timeout) {
		this(readerList);

		setTimeout(timeout);
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public byte getActivatedReader() {
		return activatedReader;
	}

	@Override
	protected byte[] createPayload() {
		byte[] payload = new byte[readerList.length + 1];

		System.arraycopy(readerList, 0, payload, 0, readerList.length);

		payload[payload.length - 1] = (byte) timeout;

		return payload;
	}

	@Override
	protected boolean parseResponse() {
		activatedReader = response.getData()[0];

		if (activatedReader == Constants.ICC_READER) {
			iccStatus = response.getData()[1];

			atr = new byte[response.getData()[2]];

			System.arraycopy(response.getData(), 3, atr, 0, atr.length);
		}

		return true;
	}

}
