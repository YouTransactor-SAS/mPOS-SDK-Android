/**
 * Copyright (C) 2011-2016, YouTransactor. All Rights Reserved.
 * <p/>
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youTransactor.uCube.rpc;

import com.youTransactor.uCube.Tools;

import java.util.Arrays;

/**
 * @author gbillard on 3/22/16.
 */
public class RPCMessage {

	private byte sequence_number;
	private short commandId;
	private byte crypto_header;
	private short status;
	private byte data[];
	private byte data_mac[];

	public RPCMessage(byte[] buffer, boolean secureSession) {
		/* offset start after STX */
		int offset = 1;

		/* payload_length includes state */
		short payload_length = (short) (Tools.makeShort(buffer[offset++], buffer[offset++]) - Constants.RPC_STATUS_SIZE);
		sequence_number = buffer[offset++];
		commandId = Tools.makeShort(buffer[offset++], buffer[offset++]);

		if(!secureSession || commandId == Constants.EXIT_SECURED_SESSION) {

			// Set crypto to 0 if unused
			this.crypto_header = (byte) 0;
			this.status = Tools.makeShort(buffer[offset++], buffer[offset++]);

			if (payload_length > 0) {
				data = new byte[payload_length];
				data = Arrays.copyOfRange(buffer, offset, offset + payload_length);
				// Set response_data_mac to null if unused
				data_mac = null;
			}

		} else {
			this.crypto_header = buffer[offset++];
			this.status = Tools.makeShort(buffer[offset++], buffer[offset++]);

			if (payload_length > Constants.RPC_SRED_MAC_SIZE) {
				data = new byte[payload_length - Constants.RPC_SRED_MAC_SIZE];
				data = Arrays.copyOfRange(buffer, offset, offset + (payload_length - Constants.RPC_SRED_MAC_SIZE));
				offset += payload_length - Constants.RPC_SRED_MAC_SIZE + 1;
				data_mac = new byte[Constants.RPC_SRED_MAC_SIZE];
				data_mac = Arrays.copyOfRange(buffer, offset, offset + Constants.RPC_SRED_MAC_SIZE);
			}

		}

	}

	public short getCommandId() {
		return commandId;
	}

	public byte[] getData() {
		return data;
	}

	public short getStatus() {
		return status;
	}

	public byte getSequence_number() {
		return sequence_number;
	}

	public byte[] getData_mac() {
		return data_mac;
	}

	public byte getCrypto_header() {
		return crypto_header;
	}
}
