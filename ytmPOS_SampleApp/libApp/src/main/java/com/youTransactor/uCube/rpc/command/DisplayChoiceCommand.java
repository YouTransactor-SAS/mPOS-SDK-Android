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
import com.youTransactor.uCube.rpc.RPCCommandStatus;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * @author gbillard on 4/11/16.
 */
public class DisplayChoiceCommand extends RPCCommand {

	private List<String> choiceList;
	private byte timeout = 60;
	private int selectedIndex = -1;
	private boolean cancelled = false;

	public DisplayChoiceCommand() {
		super(Constants.DISPLAY_LISTBOX_WITHOUT_KI_COMMAND);
	}

	public DisplayChoiceCommand(List<String> choiceList) {
		super(Constants.DISPLAY_LISTBOX_WITHOUT_KI_COMMAND);
		setChoiceList(choiceList);
	}

	public void setChoiceList(List<String> choiceList) {
		this.choiceList = choiceList;
	}

	public void setTimeout(int timeout) {
		this.timeout = (byte) timeout;
	}

	public int getSelectedIndex() {
		return selectedIndex;
	}

	public String getSelectedItem() {
		return (choiceList != null && selectedIndex >= 0 && selectedIndex < choiceList.size()) ? choiceList.get(selectedIndex) : null;
	}

	@Override
	protected byte[] createPayload() {
		int length = 0;

		byte[] choiceBuffer = new byte[200];

		for (String choice : choiceList) {
			try {
				byte[] buffer = choice.replaceAll("[^\\p{ASCII}]", "").getBytes("ASCII");
				System.arraycopy(buffer, 0, choiceBuffer, length, buffer.length);
				length += buffer.length;
				choiceBuffer[length++] = '\0';
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		int offset = 0;

		byte[] data = new byte[length + 8];

		data[offset++] = timeout;

		/* Title */
		data[offset++] = 0x00; /* Title font by default RFU */
		data[offset++] = 0x01; /* Title Length RFU */
		data[offset++] = '\0'; /* Title set to '\0' RFU */

		/* Scroll key Label */
		data[offset++] = 0x00; /* Scroll key Label font by default RFU */
		data[offset++] = '\0'; /* Scroll key Label set to '\0' RFU */

		/* Font */
		data[offset++] = 0x00; /* Text font by default RFU */

		/* List Length */
		data[offset++] = (byte) length;

		System.arraycopy(choiceBuffer, 0, data, offset, length);

		return data;
	}

	@Override
	protected boolean isValidResponse() {
		switch (response.getStatus()) {
			case Constants.SUCCESS_STATUS:
			case -114://todo use constants for ACMABORTED
				return true;

			default:
				return false;
		}
	}

	@Override
	protected boolean parseResponse() {
		try {
			byte[] data = response.getData();

			selectedIndex = data[0] - 1;
		} catch (Exception ignored) {
			selectedIndex = -1;
			cancelled = true;
		}
		return true;
	}

	@Override
	protected RPCCommandStatus getCommandStatusValue() {
		if (cancelled)
			return RPCCommandStatus.CANCELED;
		return RPCCommandStatus.SUCCESS;
	}
}
