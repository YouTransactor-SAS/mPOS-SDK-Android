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
import com.youTransactor.uCube.Tools;
import com.youTransactor.uCube.payment.Currency;
import com.youTransactor.uCube.rpc.Constants;
import com.youTransactor.uCube.rpc.RPCCommand;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author gbillard on 5/31/16.
 */
public class StartNFCTransactionCommand extends RPCCommand {

	private byte[] enabledReaders;
	private byte activatedReader;
	private double amount = -1;
	private double cashbackAmount = 0;
	private double balanceBeforeGAC = 0;
	private double balanceAfterGAC = 0;
	private int timeout = 60;
	private Currency currency;
	private byte transactionType;
	private Date date;
	private short merchantCategoryCode = -1;
	private String merchantID;
	private List<byte[]> requestedTagList;
	private char transactionCategoryCode = ' ';
	private boolean forceAuthorization;
	private byte[] merchantProprietaryData;
	private Map<Integer, byte[]> responseTLV;
	private byte[] NFCOutcome;
	private boolean noAmount;
	private byte[] proprietaryTLVStream;

	public StartNFCTransactionCommand(byte[] enabledReaders, Currency currency) {
		super(Constants.START_NFC_TRANSACTION);

		this.enabledReaders = enabledReaders;
		this.currency = currency;
		this.proprietaryTLVStream = null;
	}

	public void setNoAmount(boolean noAmount) {
		this.noAmount = noAmount;
	}

	public byte getActivatedReader() {
		return activatedReader;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		if (amount < 0) {
			this.amount = 0;
			noAmount = true;
		} else {
			this.amount = amount;
		}
	}

	public void setCashbackAmount(double cashbackAmount) {
		this.cashbackAmount = cashbackAmount;
	}

	public void setBalanceBeforeGAC(double balanceBeforeGAC) {
		this.balanceBeforeGAC = balanceBeforeGAC;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void setBalanceAfterGAC(double balanceAfterGAC) {
		this.balanceAfterGAC = balanceAfterGAC;
	}

	public void setTransactionType(byte transactionType) {
		this.transactionType = transactionType;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public void setMerchantCategoryCode(short merchantCategoryCode) {
		this.merchantCategoryCode = merchantCategoryCode;
	}

	public void setMerchantID(String merchantID) {
		this.merchantID = merchantID;
	}

	public void setRequestedTagList(List<byte[]> requestedTagList) {
		this.requestedTagList = requestedTagList;
	}

	public void setTransactionCategoryCode(char transactionCategoryCode) {
		this.transactionCategoryCode = transactionCategoryCode;
	}

	public void setForceAuthorization(boolean forceAuthorization) {
		this.forceAuthorization = forceAuthorization;
	}

	public void setMerchantProprietaryData(byte[] merchantProprietaryData) {
		this.merchantProprietaryData = merchantProprietaryData;
	}

	public void setInputProprietaryTLVStream(byte[] proprietaryTLVStream) {
		this.proprietaryTLVStream = proprietaryTLVStream;
	}

	public byte[] getNFCOutcome() {
		return NFCOutcome;
	}

	@Override
	protected byte[] createPayload() {
		byte[] payload = new byte[254];
		int offset = 0;

		// DF70, DF71, DF52 and 9C if present, MUST be placed first in the following order

		payload[offset++] = (byte) 0xDF;
		payload[offset++] = 0x70;
		payload[offset++] = (byte) enabledReaders.length;
		System.arraycopy(enabledReaders, 0, payload, offset, enabledReaders.length);
		offset += enabledReaders.length;

		/* card wait timeout */
		payload[offset++] = (byte) 0xDF;
		payload[offset++] = 0x71;
		payload[offset++] = 0x01;
		payload[offset++] = (byte) timeout;

		/* enter amount on uCube ? */
		payload[offset++] = (byte) 0xDF;
		payload[offset++] = 0x52;
		payload[offset++] = 0x01;
		payload[offset++] = (byte) (noAmount ? 0x01 : 0x00);

		/* transaction type */
		payload[offset++] = (byte) 0x9C;
		payload[offset++] = 0x01;
		payload[offset++] = transactionType;

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

		/* amount */
		payload[offset++] = (byte) 0x9F;
		payload[offset++] = 0x02;
		payload[offset++] = 0x06;
		System.arraycopy(Tools.toBCD((int) (amount * Math.pow(10, currency.getExponent())), 6), 0, payload, offset, 6);
		offset += 6;

		if (transactionType == Constants.PURCHASE_CASHBACK) {
			payload[offset++] = (byte) 0x9F;
			payload[offset++] = 0x03;
			payload[offset++] = 0x06;
			System.arraycopy(Tools.toBCD((int) (cashbackAmount * Math.pow(10, currency.getExponent())), 6), 0, payload, offset, 6);
			offset += 6;
		}

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

		if (merchantCategoryCode != -1) {
			payload[offset++] = (byte) 0x9F;
			payload[offset++] = 0x15;
			payload[offset++] = 0x02;
			System.arraycopy(Tools.toBCD(merchantCategoryCode, 2), 0, payload, offset, 2);
			offset += 2;
		}

		if (merchantID != null) {
			payload[offset++] = (byte) 0x9F;
			payload[offset++] = 0x15;
			payload[offset++] = 0x0F;
			byte[] id = merchantID.getBytes();
			System.arraycopy(id, 0, payload, offset, Math.min(id.length, 15));
			offset += Math.min(id.length, 15);
		}

		if (transactionCategoryCode != ' ') {
			payload[offset++] = (byte) 0x9F;
			payload[offset++] = 0x53;
			payload[offset++] = 0x01;
			payload[offset++] = (byte) transactionCategoryCode;
		}

		if (merchantProprietaryData != null) {
			payload[offset++] = (byte) 0x9F;
			payload[offset++] = 0x7C;
			payload[offset++] = (byte) Math.min(merchantProprietaryData.length, 20);
			System.arraycopy(merchantProprietaryData, 0, payload, offset, Math.min(merchantProprietaryData.length, 20));
			offset += Math.min(merchantProprietaryData.length, 20);
		}

		if (balanceBeforeGAC != 0) {
			payload[offset++] = (byte) 0xDF;
			payload[offset++] = 0x04;
			payload[offset++] = 0x06;
			System.arraycopy(Tools.toBCD((int) balanceBeforeGAC, 6), 0, payload, offset, 6);
			offset += 6;
		}

		if (balanceAfterGAC != 0) {
			payload[offset++] = (byte) 0xDF;
			payload[offset++] = 0x05;
			payload[offset++] = 0x06;
			System.arraycopy(Tools.toBCD((int) balanceAfterGAC, 6), 0, payload, offset, 6);
			offset += 6;
		}

		payload[offset++] = (byte) 0xDF;
		payload[offset++] = 0x7F;
		payload[offset++] = 0x01;
		payload[offset++] = (byte) (forceAuthorization ? 0x01 : 0x00);

		if(proprietaryTLVStream != null){
			System.arraycopy(proprietaryTLVStream, 0, payload, offset, proprietaryTLVStream.length);
			offset += proprietaryTLVStream.length;
		}

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

		activatedReader = responseTLV.get(Integer.valueOf(0xDF70))[0];

		if (noAmount) {
			byte[] buf = responseTLV.get(Integer.valueOf(0x9F02));

			if (buf != null) {
				amount = Tools.fromBCD_double(buf);

				amount = amount / Math.pow(10, currency.getExponent());
			}
		}

		NFCOutcome = responseTLV.get(Integer.valueOf(0xDF72));

		return true;
	}

}
