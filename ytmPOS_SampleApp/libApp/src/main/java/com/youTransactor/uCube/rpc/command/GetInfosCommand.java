/**
 * Copyright (C) 2011-2016, YouTransactor. All Rights Reserved.
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
 * @author gbillard on 3/15/16.
 */
public class GetInfosCommand extends RPCCommand {

	public GetInfosCommand(int... tagList) {
		super(Constants.GET_INFO_COMMAND);

		/* usable tags are all on 1 bytes  */
		payload = new byte[tagList.length];

		for (int i = 0; i < tagList.length; i++) {
			payload[i] = (byte) tagList[i];
		}
	}

	/**
	 * @deprecated use getResponseData() instead
	 */
	public byte[] getTlv() {
		return getResponseData();
	}

}
