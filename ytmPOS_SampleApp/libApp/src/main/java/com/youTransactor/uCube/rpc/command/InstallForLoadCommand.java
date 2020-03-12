/**
 * Copyright (C) 2011-2016, YouTransactor. All Rights Reserved.
 * <p>
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youTransactor.uCube.rpc.command;

import com.youTransactor.uCube.rpc.Constants;
import com.youTransactor.uCube.rpc.RPCCommand;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author gbillard on 4/6/16.
 */
public class InstallForLoadCommand extends RPCCommand {

	private byte[] signature;
	private byte[] cipheredKey;
	private byte encryptionMethod = -1;
	private short optionnalParameterLength;

	public InstallForLoadCommand() {
		super(Constants.INSTALL_FOR_LOAD_COMMAND);
	}


	public void setSignature(byte[] signature) {
		this.signature = signature;
	}

	public void setCipheredKey(byte[] cipheredKey) {
		this.cipheredKey = cipheredKey;
	}

	public void setEncryptionMethod(byte encryptionMethod) {
		this.encryptionMethod = encryptionMethod;
	}

	public void setOptionnalParameterLength(short optionnalParameterLength) {
		this.optionnalParameterLength = optionnalParameterLength;
	}

	@Override
	protected byte[] createPayload() {
		/* encryption method + optional parameters = 3 byte */
		byte[] data = new byte[signature.length + (cipheredKey != null ? cipheredKey.length : 0) + 3];
		int offset = 0;

		System.arraycopy(signature, offset, data, 0, signature.length);

		offset += signature.length;

		data[offset++] = encryptionMethod;


		if (cipheredKey != null) {
			data[offset++] = (byte) (0x01);
			data[offset++] = (byte) (0x00);

			System.arraycopy(cipheredKey, 0, data, offset, cipheredKey.length);
		} else {
			data[offset++] = 0;
			data[offset] = 0;
		}

		return data;
	}
}
