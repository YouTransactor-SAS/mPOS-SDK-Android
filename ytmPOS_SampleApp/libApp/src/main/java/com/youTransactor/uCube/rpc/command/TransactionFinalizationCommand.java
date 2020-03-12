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
import com.youTransactor.uCube.rpc.RPCMessage;

import java.util.Arrays;
import java.util.Map;

/**
 * @author gbillard on 5/23/16.
 */
public class TransactionFinalizationCommand extends RPCCommand {

	/**
		0x00 => no forcing
		0x01 => forced after authorization
		0x59 ('Y') => voice referral accepted
		0x5A ('Z') => voice referral declined
	*/
	private byte forceFlag = 0x00;

	/**
		0x00 => unable to go online
		0x01 => approved
		0x02 => declined
	*/
	private byte authorizationStatus = 0x02;
	private byte[] authResponseCode;
	private byte[] issuerDataAuth;
	private byte[] issuerScript1;
	private byte[] issuerScript2;
	private byte[] requestedTags;
	private byte[] transactionData;

	public TransactionFinalizationCommand() {
		super(Constants.TRANSACTION_FINAL);
	}

	public void setAuthResponse(byte[] response) {
		setAuthResponse(TLV.parse(response));
	}

	public void setAuthResponse(Map<Integer, byte[]> response) {
		setAuthResponseCode(response.get(0x8A));
		setIssuerDataAuth(response.get(0x91));
		setIssuerScript1(response.get(0x71));
		setIssuerScript2(response.get(0x72));
	}

	public byte[] getTransactionData() {
		return transactionData;
	}

	public void setForceFlag(byte forceFlag) {
		this.forceFlag = forceFlag;
	}

	public void setAuthResponseCode(byte[] authResponseCode) {
		this.authResponseCode = authResponseCode;

		if (authResponseCode instanceof byte[] && authResponseCode.length == 2) {
			if (authResponseCode[0] == 0x39) {
				authorizationStatus = 0x00;

			} else if (authResponseCode[0] == 0x30 && authResponseCode[1] == 0x30) {
				authorizationStatus = 0x01;

			} else {
				authorizationStatus = 0x02;
			}
		}
	}

	public void setAuthorizationStatus(byte authorizationStatus) {
		this.authorizationStatus = authorizationStatus;
	}

	public void setIssuerDataAuth(byte[] issuerDataAuth) {
		this.issuerDataAuth = issuerDataAuth;
	}

	public void setIssuerScript1(byte[] issuerScript1) {
		this.issuerScript1 = issuerScript1;
	}

	public void setIssuerScript2(byte[] issuerScript2) {
		this.issuerScript2 = issuerScript2;
	}

	public void setRequestedTags(byte[] requestedTags) {
		this.requestedTags = requestedTags;
	}

	@Override
	protected byte[] createPayload() {
		byte[] payload = new byte[1024];
		int offset = 0;

		payload[offset++] = (byte) 0xDF;
		payload[offset++] = 0x15;
		payload[offset++] = 0x01;
		payload[offset++] = (byte) forceFlag;

		payload[offset++] = (byte) 0xDF;
		payload[offset++] = 0x16;
		payload[offset++] = 0x01;
		payload[offset++] = (byte) authorizationStatus;

		if (authResponseCode != null) {
			payload[offset++] = (byte) 0x8A;
			payload[offset++] = 0x02;
			payload[offset++] = authResponseCode[1];
			payload[offset++] = authResponseCode[1];
		}

		if (issuerDataAuth != null) {
			payload[offset++] = (byte) 0x91;
			payload[offset++] = (byte) issuerDataAuth.length;
			System.arraycopy(issuerDataAuth, 0, payload, offset, issuerDataAuth.length);
			offset += issuerDataAuth.length;
		}

		if (issuerScript1 != null) {
			payload[offset++] = (byte) 0x71;
			payload[offset++] = (byte) 0x81;
			payload[offset++] = (byte) issuerScript1.length;
			System.arraycopy(issuerScript1, 0, payload, offset, issuerScript1.length);
			offset += issuerScript1.length;
		}

		if (issuerScript2 != null) {
			payload[offset++] = (byte) 0x71;
			payload[offset++] = (byte) 0x81;
			payload[offset++] = (byte) issuerScript2.length;
			System.arraycopy(issuerScript2, 0, payload, offset, issuerScript2.length);
			offset += issuerScript2.length;
		}

		if (requestedTags != null) {
			payload[offset++] = (byte) 0xC1;
			payload[offset++] = (byte) requestedTags.length;
			System.arraycopy(requestedTags, 0, payload, offset, requestedTags.length);
			offset += requestedTags.length;
		}

		return Arrays.copyOfRange(payload, 0, offset);
	}

	@Override
	protected boolean isValidResponse() {
		return response.getStatus() == 0x07 || response.getStatus() == 0x08;
	}

}
