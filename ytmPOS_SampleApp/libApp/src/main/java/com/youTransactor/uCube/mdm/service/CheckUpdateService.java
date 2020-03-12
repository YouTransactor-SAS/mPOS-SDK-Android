/*
 * Copyright (C) 2011-2020, YouTransactor. All Rights Reserved.
 *
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youTransactor.uCube.mdm.service;

import com.youTransactor.uCube.ITaskMonitor;
import com.youTransactor.uCube.TaskEvent;
import com.youTransactor.uCube.mdm.Config;
import com.youTransactor.uCube.mdm.task.CompareVersionsTask;
import com.youTransactor.uCube.mdm.task.GetConfigTask;

import java.util.List;

/**
 * @author gbillard on 4/5/16.
 */
public class CheckUpdateService extends AbstractMDMService {

	private List<Config> cfgList;
	private List<BinaryUpdate> updateList;
	private boolean forceUpdate;
	private boolean checkOnlyFirmwareVersion;

	public CheckUpdateService() {
		super();
	}

	public List<BinaryUpdate> getUpdateList() {
		return updateList;
	}

	public List<Config> getCfgList() {
		return cfgList;
	}

	public CheckUpdateService setCheckOnlyFirmwareVersion(boolean checkOnlyFirmwareVersion) {
		this.checkOnlyFirmwareVersion = checkOnlyFirmwareVersion;
		return this;
	}

	public CheckUpdateService setForceUpdate(boolean forceUpdate) {
		this.forceUpdate = forceUpdate;
		return this;
	}

	@Override
	protected void onDeviceInfosRetrieved() {
		retrieveDeviceConfig();
	}

	private void retrieveDeviceConfig() {
		setState(ServiceState.RETRIEVE_DEVICE_CONFIG);

		final GetConfigTask task = new GetConfigTask(deviceInfos);

		task.execute(new ITaskMonitor() {
			@Override
			public void handleEvent(TaskEvent event, Object... params) {
				if (event == TaskEvent.PROGRESS) {
					return;
				}

				if (event == TaskEvent.FAILED) {
					failedTask = task;
					notifyMonitor(TaskEvent.FAILED, this);
					return;
				}

				cfgList = (List<Config>) params[0];

				compareVersion();
			}
		});
	}

	private void compareVersion() {
		setState(ServiceState.CHECK_UPDATE);

		CompareVersionsTask task = new CompareVersionsTask(deviceInfos, cfgList);
		task.setCheckOnlyFirmwareVersion(checkOnlyFirmwareVersion);
		task.setForceUpdate(forceUpdate);


		updateList = task.getUpdateList();

		/* hard coded dependency of NFC config on NFC firmware */
		/* Misc parameters are never mandatory ( because we have no standard way to compare versions) */
		int nfcCfgFound = -1;
		int nfcFirmwareFound = -1;

		for (int i = 0; i < updateList.size(); i++) {
			BinaryUpdate bin = updateList.get(i);

			switch (bin.getCfg().getType()) {
			case 3:
				nfcFirmwareFound = i;
				break;

			case 5:
				nfcCfgFound = i;
				break;

			case 6:
				bin.setMandatory(false);
				break;
			}
		}

		if (nfcFirmwareFound != -1) {
			if (nfcCfgFound == -1) {
				for (Config cfg : cfgList) {
					if (cfg.getType() == 5) {
						nfcCfgFound = updateList.size();
						updateList.add(new BinaryUpdate(cfg, updateList.get(nfcFirmwareFound).isMandatory()));
						break;
					}
				}
			}

		}

		/* ensure NFC config will be updated after NFC firmware */
		if (nfcFirmwareFound > nfcCfgFound) {
			BinaryUpdate bin = updateList.get(nfcFirmwareFound);

			updateList.set(nfcFirmwareFound, updateList.get(nfcCfgFound));
			updateList.set(nfcCfgFound, bin);
		}

		notifyMonitor(TaskEvent.SUCCESS, (Object) updateList);
	}

}
