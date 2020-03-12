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
import java.util.List;
import java.util.Map;

/**
 * @author gbillard on 6/1/16.
 */
public class CompleteNFCTransactionCommand extends RPCCommand {

	private byte[] authResponse;
	private List<byte[]> requestedTagList;
	private byte[] NFCOutcome;
	private Map<Integer, byte[]> responseTLV;

	public CompleteNFCTransactionCommand(byte[] authResponse) {
		super(Constants.COMPLETE_NFC_TRANSACTION);

		if (authResponse == null || authResponse.length != 2) {
			authResponse = new byte[] {0x60, 0x60}; /* technical issue */
		} else {
			this.authResponse = authResponse;
		}
	}

	public void setRequestedTagList(List<byte[]> requestedTagList) {
		this.requestedTagList = requestedTagList;
	}

	public byte[] getNFCOutcome() {
		return NFCOutcome;
	}

	public Map<Integer, byte[]> getResponseTLV() {
		return responseTLV;
	}

	@Override
	protected byte[] createPayload() {
		byte[] payload = new byte[255];
		int offset = 0;

		payload[offset++] = (byte) 0xDF;
		payload[offset++] = 0x73;
		payload[offset++] = 0x02;
		System.arraycopy(authResponse, 0, payload, offset, 2);
		offset += 2;

		if (requestedTagList != null && requestedTagList.size() > 0) {
			byte[] temp = new byte[requestedTagList.size() * 2];
			int lg = 0;

			for (byte[] tag : requestedTagList) {
				System.arraycopy(tag, 0, temp, lg, Math.min(tag.length, 2));
				lg += Math.min(tag.length, 2);
			}

			payload[offset++] = (byte) 0xC1;
			payload[offset++] = (byte) lg;
			System.arraycopy(temp, 0, payload, offset, lg);
			offset += lg;
		}

		return Arrays.copyOfRange(payload, 0, offset);
	}

	@Override
	protected boolean parseResponse() {
		responseTLV = TLV.parse(response.getData());

		NFCOutcome = responseTLV.get(Integer.valueOf(0xDF72));

		return true;
	}

}
