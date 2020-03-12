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

import com.youTransactor.uCube.rpc.Constants;
import com.youTransactor.uCube.rpc.RPCCommand;
import com.youTransactor.uCube.Tools;

import java.io.UnsupportedEncodingException;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Created by gbillard on 3/22/16.
 */
public class DisplayMessageCommand extends RPCCommand {

	private byte timeout = 0x00;
	private byte abortKey = 0x00;
	private byte clearConfig = 0x00;
	private byte centered = 0x00;
	private byte yPosition = 0x00;
	private byte font = 0x00;
	private byte textByteArray[];
	private String defaultCharset = "ISO-8859-1";

	public DisplayMessageCommand() {
		super(Constants.DISPLAY_WITHOUT_KI_COMMAND);
	}

	public DisplayMessageCommand(String message) {
		this();

		try {
			textByteArray = message.getBytes(defaultCharset);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public DisplayMessageCommand(String message, String charsetToUse) {
		this();

		try {
			textByteArray = message.getBytes(charsetToUse);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public DisplayMessageCommand(String byteArray, boolean isHexaString) {
		this();

		if(isHexaString) {
			// TODO should check even numbers of digits
			textByteArray = Tools.hexStringToByteArray(byteArray);
		} else {
			try {
				textByteArray = byteArray.getBytes(defaultCharset);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}

	public void setFont(byte font) {
		this.font = font;
	}

	public void setCentered(byte centered) {
		this.centered = centered;
	}

	public void setYPosition(byte yposition) {
		this.yPosition = yposition;
	}

	public void setClearConfig(byte clearConfig) {
		this.clearConfig = clearConfig;
	}

	public void setAbortKey(byte abortKey) {
		this.abortKey = abortKey;
	}

	public void setTimeout(byte timeout) {
		this.timeout = timeout;
	}

	@Override
	protected byte[] createPayload() {

		byte textLen;

		/* text max length is 254 */
		textLen = (byte) Math.min(textByteArray.length, 254);

		/**
		 * (textLen + 9) = timeout + abort_key + clear configuration  + number of lines + Font + Length
		 * 		+ x-Coordinate + y-Coordinate + lasting (last byt in the text ) 0x00
		 */
		byte[] buffer = new byte[textLen + 9];

		int offset = 0;

		buffer[offset++] = timeout;

		buffer[offset++] = abortKey;

		buffer[offset++] = clearConfig;

		/* Number of lines */
		buffer[offset++] = 0x01;

		/* Line description: Font */
		buffer[offset++] = font;

		buffer[offset++] = (byte) (textLen + 1);

		System.arraycopy(textByteArray, 0, buffer, offset, textLen);
		offset += textLen;

		/* Lasting 0x00 */
		buffer[offset++] = 0;

		/* Coordinates */
		buffer[offset++] = centered;
		buffer[offset++] = yPosition;

		return buffer;
	}

}
