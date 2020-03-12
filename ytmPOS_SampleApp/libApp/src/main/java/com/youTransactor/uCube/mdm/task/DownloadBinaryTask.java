/*
 * Copyright (C) 2020- YouTransactor. All Rights Reserved.
 * <p/>
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youTransactor.uCube.mdm.task;

import com.youTransactor.uCube.LogManager;
import com.youTransactor.uCube.TaskEvent;
import com.youTransactor.uCube.Tools;
import com.youTransactor.uCube.mdm.MDMManager;
import com.youTransactor.uCube.rpc.DeviceInfos;
import com.youTransactor.uCube.mdm.service.BinaryUpdate;

import org.apache.commons.io.IOUtils;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * @author gbillard on 4/5/16.
 */
public class DownloadBinaryTask extends AbstractMDMTask {

	private byte[] certificate;
	private BinaryUpdate binaryUpdate;

	public DownloadBinaryTask(DeviceInfos deviceInfos, BinaryUpdate binaryUpdate, byte[] certificate) {
		super(deviceInfos);

		setBinaryUpdate(binaryUpdate);
		setCertificate(certificate);
	}

	@Override
	protected void start() {
		HttpURLConnection urlConnection = null;

		try {
			String url = GET_BINARY_WS + String.valueOf(binaryUpdate.getCfg().getType()) + '/'
					+ deviceInfos.getSerial() + '/' + deviceInfos.getPartNumber();;
			urlConnection = MDMManager.getInstance().initRequest(url, MDMManager.POST_METHOD);

			urlConnection.setRequestProperty("Content-Type", "application/octet-stream");

			urlConnection.getOutputStream().write(certificate);

			HTTPResponseCode = urlConnection.getResponseCode();

			if (HTTPResponseCode == 200) {
				byte[] response = IOUtils.toByteArray(urlConnection.getInputStream());

				if (parseResponse(response)) {
					notifyMonitor(TaskEvent.SUCCESS, binaryUpdate);

				} else {
					notifyMonitor(TaskEvent.FAILED);
				}
			} else {
				LogManager.debug(MDMManager.class.getSimpleName(), "config WS error: " + HTTPResponseCode);

				notifyMonitor(TaskEvent.FAILED);
			}

		} catch(Exception e) {
			LogManager.error(MDMManager.class.getSimpleName(), "config WS error", e);

			notifyMonitor(TaskEvent.FAILED);

		} finally {
			if(urlConnection != null) {
				urlConnection.disconnect();
			}
		}
	}

	public BinaryUpdate getBinaryUpdate() {
		return binaryUpdate;
	}

	public void setBinaryUpdate(BinaryUpdate binaryUpdate) {
		this.binaryUpdate = binaryUpdate;
	}

	public void setCertificate(byte[] certificate) {
		this.certificate = certificate;
	}

	private boolean parseResponse(byte[] response) {
		if (response.length < 2) {
			return false;
		}

		int offset = 0;
		short length = Tools.makeShort(response[offset++], response[offset++]);

		if (response.length < offset + length) {
			return false;
		}

		byte[] buffer = new byte[length];

		System.arraycopy(response, offset, buffer, 0, length);

		binaryUpdate.setSignature(buffer);
		offset += length;

		// If binary is ciphered, retrieve the "ciphered block" containing the session key"
		if (binaryUpdate.getCfg().isCiphered()) {

			length = Tools.makeShort(response[offset++], response[offset++]);

			if (response.length < offset + length) {
				return false;
			}

			buffer = new byte[length];

			System.arraycopy(response, offset, buffer, 0, length);

			binaryUpdate.setKey(buffer);
			offset += length;

		}

		List<byte[]> binaryBlockList = new ArrayList<>();

		while (offset < response.length) {
			if (response.length < offset + 2) {
				return false;
			}

			length = Tools.makeShort(response[offset++], response[offset++]);

            // We are expecting 2 "0" bytes at the end of the stream
            if( length == 0 ){
                break;
            }

			if (response.length < offset + length) {
				return false;
			}

			byte[] block = new byte[length];

			System.arraycopy(response, offset, block, 0, length);

			binaryBlockList.add(block);

			offset += length;
		}

		binaryUpdate.setBinaryBlock(binaryBlockList);

		return true;
	}

	private static final String GET_BINARY_WS = "/v2/dongle/binary/";

}
