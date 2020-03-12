/*
 * Copyright (C) 2016, YouTransactor. All Rights Reserved.
 *
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youTransactor.uCube.rpc;

import java.util.Arrays;

/**
 * @author gbillard on 5/18/16.
 */
public class EMVApplicationDescriptor {

	private byte[] aid;
	private String label;
	private int priority;
	private int issuerCodeIndex;
	private byte selectionOptions;
	private boolean blocked;

	public byte[] getAid() {
		return aid;
	}

	public String getLabel() {
		return label;
	}

	public int getPriority() {
		return priority;
	}

	public int getIssuerCodeIndex() {
		return issuerCodeIndex;
	}

	public byte getSelectionOptions() {
		return selectionOptions;
	}

	public boolean isBlocked() {
		return blocked;
	}

	public static EMVApplicationDescriptor parse(byte[] buffer, int offset) {
		/* 0x84 header */
		if (buffer[offset++] != (byte) 0x84) {
			return null;
		}

		EMVApplicationDescriptor app = new EMVApplicationDescriptor();

		int lg = buffer[offset++];
		app.aid = Arrays.copyOfRange(buffer, offset, offset + lg);
		offset += 16;

		offset += buffer[offset] == 0x50 ? 2 : 3; /* 0x50 0x01 or 0x9F 0x12 0x02  */
		lg = 0;
		while (buffer[offset + lg] != 0x00) {
			lg++;
		}
		app.label = new String(buffer, offset, lg);
		offset += 16;

		offset += 2; /* 0x87 0x01 */
		app.priority = (buffer[offset++] & 0x0F);

		offset += 3; /* 0x9F 0x11 0x01 */
		app.issuerCodeIndex = (buffer[offset++] & 0x0F);

		offset += 3; /* 0xDF 0x22 0x01 */
		app.selectionOptions = (byte) (buffer[offset++] & 0xF0);

		offset += 3; /* 0xDF 0x04 0x01 */
		app.blocked = buffer[offset] == 0x01;

		return app;
	}

}
