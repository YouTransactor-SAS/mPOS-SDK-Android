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

import com.youTransactor.uCube.TLV;
import com.youTransactor.uCube.Tools;
import com.youTransactor.uCube.payment.Currency;
import com.youTransactor.uCube.rpc.Constants;
import com.youTransactor.uCube.rpc.EMVApplicationDescriptor;
import com.youTransactor.uCube.rpc.RPCCommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author gbillard on 5/18/16.
 */
public class InitTransactionCommand extends RPCCommand {

	private double amount;
	private double cashbackAmount = 0;
	private Currency currency;
	private byte transactionType;
	private byte posEntryMode = Constants.ICC_POS_ENTRY_MODE;
	private List<String> preferredLanguageList;
	private EMVApplicationDescriptor application;
	private boolean enableTIP;
	private int[] requestedTagList;
	private Date date;

	public InitTransactionCommand(double amount, @NonNull Currency currency, byte transactionType, @NonNull EMVApplicationDescriptor application) {
		super(Constants.TRANSACTION_INIT, true);
		this.amount = amount;
		this.currency = currency;
		this.transactionType = transactionType;
		this.application = application;
	}

	public void setCashbackAmount(double cashbackAmount) {
		this.cashbackAmount = cashbackAmount;
	}

	public void setEnableTIP(boolean enableTIP) {
		this.enableTIP = enableTIP;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public void setRequestedTagList(int[] requestedTagList) {
		this.requestedTagList = requestedTagList;
	}

	public void setPreferredLanguageList(List<String> preferredLanguageList) {
		this.preferredLanguageList = new ArrayList<>();

		for (String language : preferredLanguageList) {
			if (language != null && language.length() == 2) {
				this.preferredLanguageList.add(language);
			}
		}
	}

	@Override
	protected byte[] createPayload() {
		int offset = 0;
		byte[] payload = new byte[500];

		/* AID */
		payload[offset++] = (byte) 0x84;
		payload[offset++] = (byte) application.getAid().length;
		System.arraycopy(application.getAid(), 0, payload, offset, application.getAid().length);
		offset += application.getAid().length;

		/* currency code */
		payload[offset++] = 0x5F;
		payload[offset++] = 0x2A;
		payload[offset++] = 0x02;
		System.arraycopy(Tools.toBCD(currency.getCode(), 2), 0, payload, offset, 2);
		offset += 2;

		/* currency exponent */
		payload[offset++] = 0x5F;
		payload[offset++] = 0x36;
		payload[offset++] = 0x01;
		payload[offset++] = (byte) currency.getExponent();

		/* transaction type */
		payload[offset++] = (byte) 0x9C;
		payload[offset++] = 0x01;
		payload[offset++] = transactionType;

		/* amount */
		payload[offset++] = (byte) 0x9F;
		payload[offset++] = 0x02;
		payload[offset++] = 0x06;
		System.arraycopy(Tools.toBCD_double(amount * Math.pow(10, currency.getExponent()), 6), 0, payload, offset, 6);
		offset += 6;

		if (transactionType == Constants.PURCHASE_CASHBACK) {
			payload[offset++] = (byte) 0x9F;
			payload[offset++] = 0x03;
			payload[offset++] = 0x06;
			System.arraycopy(Tools.toBCD_double((cashbackAmount * Math.pow(10, currency.getExponent())), 6), 0, payload, offset, 6);
			offset += 6;
		}

		/* POS entry mode */
		payload[offset++] = (byte) 0x9F;
		payload[offset++] = 0x39;
		payload[offset++] = 0x01;
		payload[offset++] = posEntryMode;

		if (preferredLanguageList != null && preferredLanguageList.size() > 0) {
			/* preferred language */
			payload[offset++] = (byte) 0xDF;
			payload[offset++] = 0x2D;
			payload[offset++] = (byte) (preferredLanguageList.size() * 2);

			for (String language : preferredLanguageList) {
				byte[] temp = language.getBytes();

				payload[offset++] = temp[0];
				payload[offset++] = temp[1];
			}
		}

		/* internal TIP */
		payload[offset++] = (byte) 0xDF;
		payload[offset++] = 0x50;
		payload[offset++] = 0x01;
		payload[offset++] = (byte) (enableTIP ? 0x01 : 0x00);

		if (date != null) {
			Calendar now = Calendar.getInstance();
			now.setTime(date);

			/* local date YYMMDD */
			payload[offset++] = (byte) 0x9A;
			payload[offset++] = 0x03;
			payload[offset++] = Tools.toBCD(now.get(Calendar.YEAR), 1)[0];
			payload[offset++] = Tools.toBCD(now.get(Calendar.MONTH) + 1, 1)[0];
			payload[offset++] = Tools.toBCD(now.get(Calendar.DAY_OF_MONTH), 1)[0];

			/* local time YYMMDD */
			payload[offset++] = (byte) 0x9F;
			payload[offset++] = 0x21;
			payload[offset++] = 0x03;
			payload[offset++] = Tools.toBCD(now.get(Calendar.HOUR_OF_DAY), 1)[0];
			payload[offset++] = Tools.toBCD(now.get(Calendar.MINUTE), 1)[0];
			payload[offset++] = Tools.toBCD(now.get(Calendar.SECOND), 1)[0];
		}

		if (requestedTagList != null) {

			byte[] temp = new byte[requestedTagList.length * 2];
			int lg = 0;

			for (int i = 0; i < requestedTagList.length; i++) {
				lg += TLV.writeTagID(requestedTagList[i], temp, lg);
			}

			payload[offset++] = (byte) 0xC1;
			payload[offset++] = (byte) lg;
			System.arraycopy(temp, 0, payload, offset, lg);
			offset += lg;
		}

		return Arrays.copyOfRange(payload, 0, offset);
	}

}
