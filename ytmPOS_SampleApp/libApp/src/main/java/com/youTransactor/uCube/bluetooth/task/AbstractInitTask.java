/*
 * Copyright (C) 2011-2020, YouTransactor. All Rights Reserved.
 * <p/>
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youTransactor.uCube.bluetooth.task;

import com.youTransactor.uCube.AbstractTask;
import com.youTransactor.uCube.rpc.DeviceInfos;

abstract public class AbstractInitTask extends AbstractTask {

	protected DeviceInfos deviceInfos;

	public AbstractInitTask() {}

	public AbstractInitTask(DeviceInfos deviceInfos) {
		setDeviceInfos(deviceInfos);
	}

	public void setDeviceInfos(DeviceInfos deviceInfos) {
		this.deviceInfos = deviceInfos;
	}

}
