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
import com.youTransactor.uCube.rpc.Constants;
import com.youTransactor.uCube.rpc.RPCCommand;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author gbillard on 5/20/16.
 */
public class TransactionProcessCommand extends RPCCommand {

	private byte[] tvr;
	private byte[] tacDefault;
	private byte[] tacDenial;
	private byte[] tacOnline;
	private byte[] ddol;
	private byte[] tdol;
	private Date date;
	private int sequenceCounter = -1;
	private int applicationVersion = -1;
	private int PINFloorLimit = -1;
	private int SIGFloorLimit = -1;
	private int PINSIGFloorLimit = -1;
	private int targetPercent = -1;
	private int threshOldValue = -1;
	private int maxTargetPercent = -1;
	private List<byte[]> requestedTagList;
	private Map<Integer, byte[]> responseTags;

	public TransactionProcessCommand(byte[] tvr) {
		super(Constants.TRANSACTION_PROCESS);

		setTvr(tvr);
	}

	public void setTvr(byte[] tvr) {
		this.tvr = tvr;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public void setTacDefault(byte[] tacDefault) {
		this.tacDefault = tacDefault;
	}

	public void setDdol(byte[] ddol) {
		this.ddol = ddol;
	}

	public void setTdol(byte[] tdol) {
		this.tdol = tdol;
	}

	public void setApplicationVersion(int applicationVersion) {
		this.applicationVersion = applicationVersion;
	}

	public void setSequenceCounter(int sequenceCounter) {
		this.sequenceCounter = sequenceCounter;
	}

	public void setTacDenial(byte[] tacDenial) {
		this.tacDenial = tacDenial;
	}

	public void setTacOnline(byte[] tacOnline) {
		this.tacOnline = tacOnline;
	}

	public void setPINFloorLimit(int PINFloorLimit) {
		this.PINFloorLimit = PINFloorLimit;
	}

	public void setSIGFloorLimit(int SIGFloorLimit) {
		this.SIGFloorLimit = SIGFloorLimit;
	}

	public void setPINSIGFloorLimit(int PINSIGFloorLimit) {
		this.PINSIGFloorLimit = PINSIGFloorLimit;
	}

	public void setTargetPercent(int targetPercent) {
		this.targetPercent = targetPercent;
	}

	public void setThreshOldValue(int threshOldValue) {
		this.threshOldValue = threshOldValue;
	}

	public void setMaxTargetPercent(int maxTargetPercent) {
		this.maxTargetPercent = maxTargetPercent;
	}

	public void setRequestedTagList(List<byte[]> requestedTagList) {
		this.requestedTagList = requestedTagList;
	}

	@Override
	protected byte[] createPayload() {
		byte[] payload = new byte[500];
		int offset = 0;

		payload[offset++] = (byte) 0x95;
		payload[offset++] = 0x05;
		System.arraycopy(tvr, 0, payload, offset, tvr.length);
		offset += 5;

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

		if (sequenceCounter >= 0) {
			payload[offset++] = (byte) 0x9F;
			payload[offset++] = 0x41;

			byte[] temp = Tools.toBCD(sequenceCounter, 4);
			System.arraycopy(temp, 0, payload, offset, temp.length);
			offset += temp.length;
		}

		if (applicationVersion > 0) {
			payload[offset++] = (byte) 0x9F;
			payload[offset++] = 0x09;
			payload[offset++] = 0x02;
			System.arraycopy(Tools.toBCD(applicationVersion, 2), 0, payload, offset, 2);
			offset += 2;
		}

		if (ddol instanceof byte[]) {
			payload[offset++] = (byte) 0xD6;
			offset += TLV.writeTagLength(ddol.length, payload, offset);
			System.arraycopy(ddol, 0, payload, offset, ddol.length);
			offset += ddol.length;
		}

		if (tdol instanceof byte[]) {
			payload[offset++] = (byte) 0xD7;
			offset += TLV.writeTagLength(tdol.length, payload, offset);
			System.arraycopy(tdol, 0, payload, offset, tdol.length);
			offset += tdol.length;
		}

		if (tacDefault instanceof byte[] && tacDefault.length == 5) {
			payload[offset++] = (byte) 0xD8;
			payload[offset++] = 0x05;
			System.arraycopy(tacDefault, 0, payload, offset, 5);
			offset += 5;
		}

		if (tacDenial instanceof byte[] && tacDenial.length == 5) {
			payload[offset++] = (byte) 0xD9;
			payload[offset++] = 0x05;
			System.arraycopy(tacDenial, 0, payload, offset, 5);
			offset += 5;
		}

		if (tacOnline instanceof byte[] && tacDefault.length == 5) {
			payload[offset++] = (byte) 0xDA;
			payload[offset++] = 0x05;
			System.arraycopy(tacOnline, 0, payload, offset, 5);
			offset += 5;
		}

		if (PINFloorLimit >= 0) {
			payload[offset++] = (byte) 0xDF;
			payload[offset++] = 0x12;
			payload[offset++] = 0x04;
			System.arraycopy(Tools.intToByteArray(PINFloorLimit, 4), 0, payload, offset, 4);
			offset += 4;
		}

		if (SIGFloorLimit >= 0) {
			payload[offset++] = (byte) 0xDF;
			payload[offset++] = 0x13;
			payload[offset++] = 0x04;
			System.arraycopy(Tools.intToByteArray(SIGFloorLimit, 4), 0, payload, offset, 4);
			offset += 4;
		}

		if (PINSIGFloorLimit >= 0) {
			payload[offset++] = (byte) 0xDF;
			payload[offset++] = 0x14;
			payload[offset++] = 0x04;
			System.arraycopy(Tools.intToByteArray(PINSIGFloorLimit, 4), 0, payload, offset, 4);
			offset += 4;
		}

		if (targetPercent >= 0) {
			payload[offset++] = (byte) 0xDF;
			payload[offset++] = 0x0D;
			payload[offset++] = 0x01;
			payload[offset++] = (byte) targetPercent;
		}

		if (threshOldValue >= 0) {
			payload[offset++] = (byte) 0xDF;
			payload[offset++] = 0x0E;
			payload[offset++] = 0x04;
			System.arraycopy(Tools.intToByteArray(threshOldValue, 4), 0, payload, offset, 4);
			offset += 4;
		}

		if (maxTargetPercent >= 0) {
			payload[offset++] = (byte) 0xDF;
			payload[offset++] = 0x0F;
			payload[offset++] = 0x01;
			payload[offset++] = (byte) maxTargetPercent;
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
		responseTags = TLV.parse(response.getData());

		//todo set NEW TVR IN PAYMENT CONTEXT
		return true;
	}

}
