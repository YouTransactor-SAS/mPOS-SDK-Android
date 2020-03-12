/**
 * Copyright (C) 2011-2016, YouTransactor. All Rights Reserved.
 * <p/>
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youTransactor.uCube.mdm.service;

import com.youTransactor.uCube.ITask;
import com.youTransactor.uCube.ITaskMonitor;
import com.youTransactor.uCube.TaskEvent;
import com.youTransactor.uCube.mdm.Config;
import com.youTransactor.uCube.mdm.MDMManager;
import com.youTransactor.uCube.mdm.task.GetConfigTask;

import java.util.List;

/**
 * @author gbillard on 4/25/16.
 */
public class GetConfigService extends AbstractMDMService {

	private List<Config> cfgList;

	public GetConfigService() {
		super();
	}

	@Override
	protected void onDeviceInfosRetrieved() {
		getDeviceConfig();
	}

	public List<Config> getCfgList() {
		return cfgList;
	}

	private void getDeviceConfig() {
		setState(ServiceState.RETRIEVE_DEVICE_CONFIG);

		new GetConfigTask(deviceInfos).execute(new ITaskMonitor() {
			@Override
			public void handleEvent(TaskEvent event, Object... params) {
				switch (event) {
				case FAILED:
					failedTask = (ITask) params[0];
					notifyMonitor(TaskEvent.FAILED, this);
					break;

				case SUCCESS:
					cfgList = (List<Config>) params[0];
					notifyMonitor(TaskEvent.SUCCESS, cfgList);
				}
			}
		});
	}

}
