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

import androidx.annotation.NonNull;

import com.youTransactor.uCube.payment.Currency;
import com.youTransactor.uCube.rpc.Constants;
import com.youTransactor.uCube.rpc.RPCCommand;

import java.util.Arrays;

/**
 * @author gbillard on 5/12/16.
 */
public class SimplifiedOnlinePINCommand extends RPCCommand {

	private byte pinFormat = Constants.PIN_BLOCK_ISO9564_FORMAT_0;
	private double amount;
	private Currency currency;
	private String PINRequestLabel = "PIN ?";
	private String waitLabel = "...";
	private int minKey = 4;
	private int maxKey = 12;
	private int firstDigitTimeout = 24;
	private int interDigitTimeout = 24;
	private int globalTimeout = 60;
	private byte correctionKeyAction = Constants.ERASE_LAST_DIGIT_PIN_INPUT_CORRECTION_KEY_MODE;
	private int inputDigitPerLine = 6;

	public SimplifiedOnlinePINCommand() {
		super(Constants.SIMPLIFIED_ONLINE_PIN);
	}

	public SimplifiedOnlinePINCommand(double amount, @NonNull Currency currency, byte pinFormat) {
		this();

		this.amount = amount;
		this.currency = currency;
		this.pinFormat = pinFormat;
	}

	public void setPinFormat(byte pinFormat) {
		this.pinFormat = pinFormat;
	}

	public void setCurrency(@NonNull Currency currency) {
		this.currency = currency;
	}

	public void setAmount(float amount) {
		this.amount = amount;
	}

	public void setPINRequestLabel(@NonNull String PINRequestLabel) {
		this.PINRequestLabel = PINRequestLabel;
	}

	public void setWaitLabel(@NonNull String waitLabel) {
		this.waitLabel = waitLabel;
	}

	public void setMinKey(int minKey) {
		if (minKey >= 0 && minKey < 256) {
			this.minKey = minKey;
		}
	}

	public void setMaxKey(int maxKey) {
		if (maxKey >= 0 && maxKey < 256) {
			this.maxKey = maxKey;
		}
	}

	public void setFirstDigitTimeout(int firstDigitTimeout) {
		if (firstDigitTimeout > 0 && firstDigitTimeout <= 60) {
			this.firstDigitTimeout = firstDigitTimeout;
		}
	}

	public void setInterDigitTimeout(int interDigitTimeout) {
		if (interDigitTimeout > 0 && interDigitTimeout <= 60) {
			this.interDigitTimeout = interDigitTimeout;
		}
	}

	public void setGlobalTimeout(int globalTimeout) {
		if (globalTimeout > 0 && globalTimeout <= 60) {
			this.globalTimeout = globalTimeout;
		}
	}

	public void setCorrectionKeyAction(byte correctionKeyAction) {
		this.correctionKeyAction = correctionKeyAction;
	}

	public void setInputDigitPerLine(int inputDigitPerLine) {
		this.inputDigitPerLine = inputDigitPerLine;
	}

	@Override
	protected byte[] createPayload() {
		byte[] payload = new byte[200];
		int off = 0;

		/* amount string zéro terminated */
		byte[] temp = String.format("%,.2f", amount).getBytes();
		payload[off++] = (byte) (temp.length + 1); /* Length */
		System.arraycopy(temp, 0, payload, off, temp.length);
		off += temp.length;
		payload[off++] = 0x00;

		/* Currency label zero terminated */
		temp = currency.getLabel().getBytes();
		payload[off++] = (byte) (temp.length + 1); /* Length */
		System.arraycopy(temp, 0, payload, off, temp.length);
		off += temp.length;
		payload[off++] = 0x00;

		/* Pin Block type */
		payload[off++] = pinFormat;

		/* Online Pin Text structure*/
		payload[off++] = 0x00; /* Font */

		temp = PINRequestLabel.getBytes();
		payload[off++] = (byte) (temp.length + 1); /* Length */
		System.arraycopy(temp, 0, payload, off, temp.length);
		off += temp.length;
		payload[off++] = 0x00;

		payload[off++] = 0x00; /* x-coord */
		payload[off++] = 0x00; /* y-coord */

		/* Waiting  Text structure*/
		payload[off++] = 0x00; /* Font */

		temp = waitLabel.getBytes();
		payload[off++] = (byte) (temp.length + 1); /* Length */
		System.arraycopy(temp, 0, payload, off, temp.length);
		off += temp.length;
		payload[off++] = 0x00;

		payload[off++] = 0x00; /* x-coord */
		payload[off++] = 0x00; /* y-coord */


	    /* Mandatory Tags list */
		payload[off++] = (byte) 0xDF;
		payload[off++] = 0x30;
		payload[off++] = 0x01;
		payload[off++] = (byte) minKey;

		payload[off++] = (byte) 0xDF;
		payload[off++] = 0x31;
		payload[off++] = 0x01;
		payload[off++] = (byte) maxKey;

		payload[off++] = (byte) 0xDF;
		payload[off++] = 0x32;
		payload[off++] = 0x02;
		payload[off++] = 0x00;
		payload[off++] = (byte) firstDigitTimeout;

		payload[off++] = (byte) 0xDF;
		payload[off++] = 0x33;
		payload[off++] = 0x02;
		payload[off++] = 0x00;
		payload[off++] = (byte) interDigitTimeout;

		payload[off++] = (byte) 0xDF;
		payload[off++] = 0x34;
		payload[off++] = 0x02;
		payload[off++] = 0x00;
		payload[off++] = (byte) globalTimeout;

		payload[off++] = (byte) 0xDF;
		payload[off++] = 0x35;
		payload[off++] = 0x01;
		payload[off++] = correctionKeyAction;

		payload[off++] = (byte) 0xDF;
		payload[off++] = 0x36;
		payload[off++] = 0x01;
		payload[off++] = (byte) inputDigitPerLine;

		payload = Arrays.copyOfRange(payload, 0, off);

		return payload;
	}

//	@Override
//	protected boolean parseResponse() {
//		return response.getData().length == Constants.ONLINE_PIN_BLOCK_LENGTH;
//	}

}
