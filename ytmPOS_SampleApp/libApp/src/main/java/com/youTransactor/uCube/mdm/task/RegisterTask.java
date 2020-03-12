/*
 * Copyright (C) 2011-2020, YouTransactor. All Rights Reserved.
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
import com.youTransactor.uCube.mdm.MDMManager;
import com.youTransactor.uCube.rpc.DeviceInfos;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.security.KeyStore;

/**
 * @author gbillard on 4/25/16.
 */
public class RegisterTask extends AbstractMDMTask {

	private byte[] dongleCert;
	private KeyStore pkcs12Store;

	public RegisterTask(DeviceInfos deviceInfos, byte[] dongleCert) {
		super(deviceInfos);

		this.dongleCert = dongleCert;
	}

	public void setDongleCert(byte[] dongleCert) {
		this.dongleCert = dongleCert;
	}

	public KeyStore getPkcs12Store() {
		return pkcs12Store;
	}

	@Override
	protected void start() {
		HttpURLConnection urlConnection = null;

		try {
			String url =  WS_URL + deviceInfos.getSerial() + '/' + deviceInfos.getPartNumber();
			urlConnection = MDMManager.getInstance().initRequest(url, MDMManager.POST_METHOD);

			urlConnection.setRequestProperty("Content-Type", "application/octet-stream");

			OutputStream output = urlConnection.getOutputStream();
			output.write(dongleCert);

			HTTPResponseCode = urlConnection.getResponseCode();

			if (HTTPResponseCode == 200) {
				pkcs12Store = KeyStore.getInstance("PKCS12");
				pkcs12Store.load(urlConnection.getInputStream(), "gmxsas".toCharArray());

				if (pkcs12Store.isKeyEntry("MDM-client")) {
					notifyMonitor(TaskEvent.SUCCESS, pkcs12Store);

				} else {
					notifyMonitor(TaskEvent.FAILED);
				}
			} else {
				LogManager.debug(RegisterTask.class.getSimpleName(), "register WS error: " + HTTPResponseCode);

				notifyMonitor(TaskEvent.FAILED, this);
			}

		} catch(Exception e) {
			LogManager.error(RegisterTask.class.getSimpleName(), "register WS error", e);

			notifyMonitor(TaskEvent.FAILED, this);

		} finally {
			if(urlConnection != null) {
				urlConnection.disconnect();
			}
		}
	}

	private static final String WS_URL = "/public/v1/dongle/register/";
}
