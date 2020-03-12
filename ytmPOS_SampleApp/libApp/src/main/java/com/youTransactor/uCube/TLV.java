/**
 * Copyright (C) 2011-2016, YouTransactor. All Rights Reserved.
 * <p/>
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youTransactor.uCube;

import android.util.Log;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author gbillard on 3/22/16.
 * @since v2
 */
public final class TLV {

	private TLV() {}

	public static boolean equalValue(byte[] v1, byte[] v2) {
		if (v1 == null) {
			return v2 == null;
		}

		if (v2 == null || v1.length != v2.length) {
			return false;
		}

		for (int i = 0; i < v1.length; i++) {
			if (v1[i] != v2[i]) {
				return false;
			}
		}

		return true;
	}

	public static int writeTagID(int tagID, byte[] buffer, int offset) {
		int size = 0;

		if (tagID >= 0x100) {
			buffer[offset] = (byte) ((tagID & 0xFF00) >> 8);
			size++;
		}

		buffer[offset + size] = (byte) (tagID & 0xFF);

		return ++size;
	}

	public static int writeTagLength(int length, byte[] buffer, int offset) {
		if (buffer == null || buffer.length <= offset || length > 0xFFFF) {
			return 0;
		}

		if (length < 0x7F) {
			buffer[offset] = (byte) length;
			return 1;
		}

		if (length <= 0xFF) {
			buffer[offset] = (byte) 0x81;
			buffer[offset + 1] = (byte) length;
			return 2;
		}

		buffer[offset] = (byte) 0x82;
		buffer[offset + 1] = (byte) (length >> 8);
		buffer[offset + 2] = (byte) length;
		return 3;
	}

	public static Map<Integer, byte[]> parse(byte[] buffer) {
		return parse(buffer, 0, buffer == null ? 0 : buffer.length);
	}

	public static Map<Integer, byte[]> parse(byte[] buffer, int offset, int length) {
		Map<Integer, byte[]> result = new HashMap<>();
		int end = buffer == null ? 0 : Math.min(buffer.length, offset + length);

		while (offset < end) {
			byte[] tag = readTag(buffer, offset);

			if (tag == null) {
				break;
			}

			offset += tag.length;

			byte[] tagLength = readTagLength(buffer, offset);

			if (tagLength == null) {
				break;
			}

			offset += tagLength.length;

			byte[] tagValue = readTagValue(buffer, offset, toInt(tagLength));

			if (tagValue == null) {
				break;
			}

			offset += tagValue.length;

			result.put(Integer.valueOf(toInt(tag)), tagValue);
		}

		return result;
	}

	/* TODO Must be deleteed as soon as possible (all dongles must be patched)
	 * TODO used for getting tag with YT TAR like NFC E8 where len 8C means 140, and not 12 byte of length!
	 */
	@Deprecated
	public static Map<Integer, byte[]> parseYtBerMixedLen(byte[] buffer) {
		return parseYtBerMixedLen(buffer, 0, buffer == null ? 0 : buffer.length);
	}
	/* TODO Must be deleteed as soon as possible (all dongles must be patched)
	 * TODO used for getting tag with YT TAR like NFC E8 where len 8C means 140, and not 12 byte of length!
	 */
	@Deprecated
	public static Map<Integer, byte[]> parseYtBerMixedLen(byte[] buffer, int offset, int length) {
		Map<Integer, byte[]> result = new HashMap<>();
		int end = buffer == null ? 0 : Math.min(buffer.length, offset + length);

		while (offset < end) {
			byte[] tag = readTag(buffer, offset);

			if (tag == null) {
				break;
			}

			offset += tag.length;

			byte[] tagLength = readTagLengthYTBERMixedStyle(buffer, offset, tag);

			if (tagLength == null) {
				break;
			}

			offset += tagLength.length;

			byte[] tagValue = readTagValue(buffer, offset, toInt(tagLength));

			if (tagValue == null) {
				break;
			}

			offset += tagValue.length;

			result.put(Integer.valueOf(toInt(tag)), tagValue);
		}

		return result;
	}

	public static byte[] readTag(byte[] buffer, int offset) {
		if (buffer == null || buffer.length <= offset) {
			return null;
		}

		if ((buffer[offset] & 0x1F) == 0x1F) {
			if (buffer.length < offset + 2) {
				return null;
			}

			return new byte[] {buffer[offset], buffer[offset + 1]};
		}

		return new byte[] {buffer[offset]};
	}

	/* TODO Must be deleteed as soon as possible (all dongles must be patched)
	 * For length with bit0=1 and bit1-7 > 2 consider YT style length, 8C means 140 size and not 12 byte of length
	 */
	@Deprecated
	public static byte[] readTagLengthYTBERMixedStyle(byte[] buffer, int offset, byte[] tag) {

		byte length[];

		if (buffer == null || buffer.length <= offset) {
			return null;
		}

		byte b = buffer[offset++];

		if ((b & 0x80) == 0) {
			return new byte[] {b};
		}

		switch (tag[0]) {
		case (byte)0xE8: // whne tag E8 is detected, check if len is BER or YT style
			Log.d("ReadTag", "Mixed BER-TLV and YT-TLV tag length detected!");
			switch (b) {
			case (byte) 0x81:
			case (byte) 0x82:
				// BER_TLV length style
				break;
			default:
				// YT length style
				return new byte[]{b};
			}
			break;
		}

		b = (byte) (b & 0x7F);

		if (b == 0) {
			return new byte[] {(byte) 0x80};
		}

		// DIRTY Solution, the length of the length will be used as offset at upper layer :'(
		// TODO: Needs to fix this properly
		// As if we take in accouht 81 or 82, but replaced by 0
		length = new byte[b + 1];
		length[0] = 0;

		for ( int i = 0; i < b ; i++ ) {
			length[i + 1] = buffer[offset + i];
		}

		return length;
	}

	public static byte[] readTagLength(byte[] buffer, int offset) {
		if (buffer == null || buffer.length <= offset) {
			return null;
		}

		byte b = buffer[offset++];

		if ((b & 0x80) == 0) {
			return new byte[] {b};
		}

		b = (byte) (b & 0x7F);

		if (b == 0) {
			return new byte[] {(byte) 0x80};
		}

		return Arrays.copyOfRange(buffer, offset, offset + b);
	}

	public static byte[] readTagValue(byte[] buffer, int offset, int length) {
		if (buffer == null || buffer.length < offset + length) {
			return null;
		}

		return Arrays.copyOfRange(buffer, offset, offset + length);
	}

	public static int toInt(byte[] buffer) {
		int res = 0;

		if (buffer != null && buffer.length > 0) {
			for (byte b : buffer) {
				// needed to fix crappy signed conversion for byte in JAVA
				int bb = b;
				if (bb < 0)
					bb = 256 + bb;
				res = res << 8;
				res |= bb;
			}

			// useless ?
			switch (buffer.length) {
			case 1:
				res &= 0x000000FF;
				break;

			case 2:
				res &= 0x0000FFFF;
				break;

			case 3:
				res &= 0x00FFFFFF;
			}
		}

		return res;
	}

}
