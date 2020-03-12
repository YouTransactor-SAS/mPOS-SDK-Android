/**
 * Copyright (C) 2011-2016, YouTransactor. All Rights Reserved.
 * <p/>
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youTransactor.uCube.rpc.command;

import com.youTransactor.uCube.Tools;
import com.youTransactor.uCube.rpc.Constants;
import com.youTransactor.uCube.rpc.RPCCommand;
import com.youTransactor.uCube.rpc.RPCMessage;

/**
 * @author gbillard on 4/4/16.
 */
public class InstallForLoadKeyCommand extends RPCCommand {

	private byte[] caProd;
	private byte[] kKek;

	public InstallForLoadKeyCommand() {
		super(Constants.INSTALL_FOR_LOAD_KEY_COMMAND);
	}

	public byte[] getCaProd() {
		return caProd;
	}

	public byte[] getkKek() {
		return kKek;
	}

	public byte[] getFullData() {
		return response.getData();
	}

	protected boolean parseResponse(RPCMessage response) {
		byte[] data = response.getData();
		int offset = 0;

		short length = Tools.makeShort(data[offset++], data[offset++]);

		if (length <= 0) {
			return false;
		}

		caProd = new byte[length];
		System.arraycopy(data, offset, caProd, 0, length);

		offset += length;

		length = Tools.makeShort(data[offset++], data[offset++]);

		if (length <= 0) {
			caProd = null;
			return false;
		}

		kKek = new byte[length];
		System.arraycopy(data, offset, caProd, 0, length);

		return true;
	}

}
