/**
 * Copyright (C) 2011-2016, YouTransactor. All Rights Reserved.
 *
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youTransactor.uCube.mdm.task;

import com.youTransactor.uCube.LogManager;
import com.youTransactor.uCube.TaskEvent;
import com.youTransactor.uCube.mdm.Config;
import com.youTransactor.uCube.mdm.MDMManager;
import com.youTransactor.uCube.rpc.DeviceInfos;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.List;

/**
 * @author gbillard on 4/5/16.
 */
public class GetConfigTask extends AbstractMDMTask {

	private List<Config> cfgList;

	public GetConfigTask() {}

	public GetConfigTask(DeviceInfos deviceInfos) {
		super(deviceInfos);
	}

	@Override
	protected void start() {
		HttpURLConnection urlConnection = null;

		try {
			urlConnection = MDMManager.getInstance().initRequest(GET_CONFIG_WS
					+ deviceInfos.getSerial() + '/' + deviceInfos.getPartNumber(),
					MDMManager.GET_METHOD);

			HTTPResponseCode = urlConnection.getResponseCode();

			if (HTTPResponseCode == 200) {
				String result = IOUtils.toString(urlConnection.getInputStream());

				LogManager.debug(GetConfigTask.class.getSimpleName(), "result config server: " + result);

				cfgList = Config.fromJson(new JSONObject(result));

			} else {
				LogManager.debug(GetConfigTask.class.getSimpleName(), "config WS error: " + HTTPResponseCode);
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

		notifyMonitor(TaskEvent.SUCCESS, (Object) cfgList);
	}

	private static final String GET_CONFIG_WS = "/v2/dongle/config/";

}
