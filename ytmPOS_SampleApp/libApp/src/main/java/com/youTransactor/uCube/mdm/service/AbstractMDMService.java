/*
 * Copyright (C) 2011-2020, YouTransactor. All Rights Reserved.
 * <p/>
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youTransactor.uCube.mdm.service;

import com.youTransactor.uCube.AbstractService;
import com.youTransactor.uCube.ITask;
import com.youTransactor.uCube.ITaskMonitor;
import com.youTransactor.uCube.TaskEvent;
import com.youTransactor.uCube.mdm.MDMManager;
import com.youTransactor.uCube.rpc.Constants;
import com.youTransactor.uCube.rpc.DeviceInfos;
import com.youTransactor.uCube.rpc.command.GetInfosCommand;

/**
 * @author gbillard on 4/5/16.
 */
abstract public class AbstractMDMService extends AbstractService {

	protected ServiceState state;
	protected DeviceInfos deviceInfos;

	protected AbstractMDMService() {
		this.state = ServiceState.IDLE;
		this.deviceInfos = MDMManager.getInstance().getDeviceInfos();
	}

	public DeviceInfos getDeviceInfos() {
		return deviceInfos;
	}

	public void setDeviceInfos(DeviceInfos deviceInfos) {
		this.deviceInfos = deviceInfos;
	}

	@Override
	protected void start() {
		retrieveDeviceInfos();
	}

	protected void setState(ServiceState state, Object... params) {
		this.state = state;

		Object[] args = new Object[params.length + 1];
		args[0] = state;
		System.arraycopy(params, 0, args, 1, params.length);

		notifyMonitor(TaskEvent.PROGRESS, args);
	}

	public ServiceState getState() {
		return state;
	}

	private void retrieveDeviceInfos() {
		if (deviceInfos != null) {
			onDeviceInfosRetrieved();
			return;
		}

		state = ServiceState.RETRIEVE_DEVICE_INFOS;

		GetInfosCommand cmd = new GetInfosCommand(
					Constants.TAG_TERMINAL_PN, Constants.TAG_TERMINAL_SN, Constants.TAG_FIRMWARE_VERSION,
					Constants.TAG_EMV_ICC_CONFIG_VERSION, Constants.TAG_EMV_NFC_CONFIG_VERSION, Constants.TAG_MPOS_MODULE_STATE);

		cmd.execute(new ITaskMonitor() {
			@Override
			public void handleEvent(TaskEvent event, Object... params) {
				switch (event) {
				case FAILED:
					failedTask = (ITask) params[0];
					notifyMonitor(event, this);
					return;

				case SUCCESS:
					deviceInfos = new DeviceInfos(((GetInfosCommand) params[0]).getResponseData());

					if (deviceInfos.getNfcModuleState() != 0) {
						retrieveDeviceNFCInfos(deviceInfos.getTlv());
					} else {
						onDeviceInfosRetrieved();
					}
				}
			}
		});
	}

	private void retrieveDeviceNFCInfos(final byte[] primaryData) {
		GetInfosCommand cmd = new GetInfosCommand(Constants.TAG_EMV_NFC_CONFIG_VERSION, Constants.TAG_NFC_INFOS);

		cmd.execute((event, params) -> {
            switch (event) {
            case PROGRESS:
                return;

            case FAILED:
                initDeviceInfos(primaryData, null);
                break;

            case SUCCESS:
                initDeviceInfos(primaryData, ((GetInfosCommand) params[0]).getResponseData());
                break;
            }
        });
	}

	protected void onDeviceInfosRetrieved() {
		notifyMonitor(TaskEvent.SUCCESS);
	}

	private void initDeviceInfos(byte[] primaryData, byte[] additionalData) {
		byte[] data;

		if (additionalData == null) {
			data = primaryData;
		} else {
			data = new byte[primaryData.length + additionalData.length];

			System.arraycopy(primaryData, 0, data, 0, primaryData.length);
			System.arraycopy(additionalData, 0, data, primaryData.length, additionalData.length);
		}

		deviceInfos = new DeviceInfos(data);

		onDeviceInfosRetrieved();
	}

}
