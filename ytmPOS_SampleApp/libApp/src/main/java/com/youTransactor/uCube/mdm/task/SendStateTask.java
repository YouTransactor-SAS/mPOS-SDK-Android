/**
 * Copyright (C) 2011-2016, YouTransactor. All Rights Reserved.
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

import java.net.HttpURLConnection;

/**
 * @author gbillard on 4/26/16.
 */
public class SendStateTask extends AbstractMDMTask {

	public SendStateTask(DeviceInfos deviceInfos) {
		super(deviceInfos);
	}

	@Override
	protected void start() {
		HttpURLConnection urlConnection = null;

		try {
			urlConnection = MDMManager.getInstance().initRequest(
					WS_URL + deviceInfos.getSerial() + '/' + deviceInfos.getPartNumber(),
					MDMManager.POST_METHOD);

			urlConnection.setRequestProperty("Content-Type", "application/octet-stream");

			urlConnection.getOutputStream().write(deviceInfos.getTlv());

			HTTPResponseCode = urlConnection.getResponseCode();

			if (HTTPResponseCode == 200) {
				notifyMonitor(TaskEvent.SUCCESS);

			} else {
				LogManager.debug(GetConfigTask.class.getSimpleName(), "config WS error: " + HTTPResponseCode);

				notifyMonitor(TaskEvent.FAILED);
			}

		} catch(Exception e) {
			LogManager.error(GetConfigTask.class.getSimpleName(), "config WS error", e);

			notifyMonitor(TaskEvent.FAILED);

			return;

		} finally {
			if(urlConnection != null) {
				urlConnection.disconnect();
			}
		}
	}

	private static final String WS_URL = "/v2/dongle/infos/";

}
