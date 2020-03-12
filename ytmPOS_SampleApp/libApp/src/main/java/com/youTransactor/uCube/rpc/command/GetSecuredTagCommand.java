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

import com.youTransactor.uCube.TLV;
import com.youTransactor.uCube.rpc.Constants;
import com.youTransactor.uCube.rpc.RPCCommand;

import java.util.Arrays;

/**
 * @author gbillard on 5/12/16.
 */
public class GetSecuredTagCommand extends RPCCommand {

	private int[] requestedTagList;

	public GetSecuredTagCommand() {
		super(Constants.GET_SECURED_TAG_VALUE_COMMAND, true);
	}

	public GetSecuredTagCommand(int[] requestedTagList) {
		this();
		this.requestedTagList = requestedTagList;
	}

	public void setRequestedTagList(int[] requestedTagList) {
		this.requestedTagList = requestedTagList;
	}

	@Override
	protected byte[] createPayload() {
		if (requestedTagList == null) {
			return super.createPayload();
		}

		byte[] payload = new byte[requestedTagList.length * 2];
		int offset = 0;

		for (int i = 0; i < requestedTagList.length; i++) {
			offset += TLV.writeTagID(requestedTagList[i], payload, offset);
		}

		payload = Arrays.copyOf(payload, offset);

		return payload;
	}

	@Override
	protected boolean parseResponse() {

		return true;
	}

}
