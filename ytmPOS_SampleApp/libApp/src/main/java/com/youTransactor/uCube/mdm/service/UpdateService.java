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

import com.youTransactor.uCube.ContextDataManager;
import com.youTransactor.uCube.ITask;
import com.youTransactor.uCube.ITaskMonitor;
import com.youTransactor.uCube.LogManager;
import com.youTransactor.uCube.TaskEvent;
import com.youTransactor.uCube.bluetooth.service.InitServiceState;
import com.youTransactor.uCube.bluetooth.task.GetDeviceInfoTask;
import com.youTransactor.uCube.mdm.MDMManager;
import com.youTransactor.uCube.mdm.task.DownloadBinaryTask;
import com.youTransactor.uCube.rpc.ConnectionListener;
import com.youTransactor.uCube.rpc.Constants;
import com.youTransactor.uCube.rpc.DeviceInfos;
import com.youTransactor.uCube.rpc.RPCManager;
import com.youTransactor.uCube.rpc.command.GetInfosCommand;
import com.youTransactor.uCube.rpc.command.InstallForLoadCommand;
import com.youTransactor.uCube.rpc.command.InstallForLoadKeyCommand;
import com.youTransactor.uCube.rpc.command.LoadCommand;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author gbillard on 4/25/16.
 */
public class UpdateService extends AbstractMDMService {

	private List<BinaryUpdate> updateList;
	private byte[] dongleCert;
	private LinkedList<BinaryUpdate> remainBinaryUpdates;

	public UpdateService(List<BinaryUpdate> updateList) {
		super();
		this.updateList = updateList;
	}

	@Override
	protected void start() {
		setState(ServiceState.RETRIEVE_DEVICE_CERTIFICAT);

		InstallForLoadKeyCommand cmd = new InstallForLoadKeyCommand();

		cmd.execute(new ITaskMonitor() {
			@Override
			public void handleEvent(TaskEvent event, Object... params) {
				switch(event) {
				case FAILED:
					failedTask = (ITask) params[0];
					notifyMonitor(TaskEvent.FAILED, this);
					return;

				case SUCCESS:
					dongleCert = ((InstallForLoadKeyCommand) params[0]).getFullData();
					downloadBinaryFromServer();
				}
			}
		});

	}

	private void downloadBinaryFromServer() {
		remainBinaryUpdates = new LinkedList<>(updateList);

		downloadRemainingBinary();
	}

	private void downloadRemainingBinary() {
		final BinaryUpdate binaryUpdate = remainBinaryUpdates.poll();

		LogManager.debug(UpdateService.class.getSimpleName(), "download " + binaryUpdate.getCfg().getLabel());

		setState(ServiceState.DOWNLOAD_BINARY, binaryUpdate.getCfg(), remainBinaryUpdates.size());

		new DownloadBinaryTask(deviceInfos, binaryUpdate, dongleCert).execute(new ITaskMonitor() {
			@Override
			public void handleEvent(TaskEvent event, Object... params) {
				switch(event) {
				case FAILED:
					failedTask = (ITask) params[0];
					notifyMonitor(TaskEvent.FAILED, this);
					return;

				case SUCCESS:
					if (remainBinaryUpdates.isEmpty()) {
						uploadBinaryToDevice();
					} else {
						downloadRemainingBinary();
					}
					break;
				}
			}
		});
	}

	private void uploadBinaryToDevice() {
		remainBinaryUpdates = new LinkedList<>(updateList);

		uploadRemainingBinaryToDevice();
	}

	private void uploadRemainingBinaryToDevice() {
		if (remainBinaryUpdates.isEmpty()) {
			retrieveDeviceInfo();
			return;
		}

		final BinaryUpdate binaryUpdate = remainBinaryUpdates.poll();

		setState(ServiceState.UPDATE_DEVICE, binaryUpdate.getCfg(), remainBinaryUpdates.size());

		InstallForLoadCommand cmd = new InstallForLoadCommand();
		cmd.setSignature(binaryUpdate.getSignature());

		if (binaryUpdate.getCfg().isCiphered()) {
			cmd.setEncryptionMethod(Constants.DYNAMIC_ENCRIPTION_METHOD);
			cmd.setCipheredKey(binaryUpdate.getKey());
		} else {
			cmd.setEncryptionMethod(Constants.PLAIN_DATA_ENCRIPTION_METHOD);
		}

		cmd.execute(new ITaskMonitor() {
			@Override
			public void handleEvent(TaskEvent event, Object... params) {
				switch (event) {
				case FAILED:
					failedTask = (ITask) params[0];
					notifyMonitor(TaskEvent.FAILED, this);
					break;

				case SUCCESS:
					uploadBinary(binaryUpdate);
					break;
				}
			}
		});
	}

	private void uploadBinary(final BinaryUpdate binaryUpdate) {
		LogManager.debug(UpdateService.class.getSimpleName(), "upload " + binaryUpdate.getCfg().getLabel());

		final LoadCommand cmd = new LoadCommand(binaryUpdate.getBinaryBlock());

		cmd.execute(new ITaskMonitor() {
			@Override
			public void handleEvent(TaskEvent event, Object... params) {
				switch (event) {
				case FAILED:
					failedTask = (ITask) params[0];
					notifyMonitor(TaskEvent.FAILED, this);
					break;

				case SUCCESS:
					updateList.remove(binaryUpdate);

					if (binaryUpdate.getCfg().getType() == 0) {
							//&& remainBinaryUpdates.size()> 0) {
						waitReboot();
					} else {
						uploadRemainingBinaryToDevice();
					}
					break;
				}
			}
		});
	}

	private void waitReboot() {
		LogManager.debug(UpdateService.class.getSimpleName(), "wait for uCube reboot");

		setState(ServiceState.RECONNECT);

		RPCManager.getInstance().stop();

		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, 1);

		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {

				if (System.currentTimeMillis() > cal.getTimeInMillis()) {
					LogManager.e("uCube reboot not detected after one minute");

					cancel();

					notifyMonitor(TaskEvent.FAILED);
					return;
				}

				RPCManager.getInstance().connect(new ConnectionListener() {
					@Override
					public void onConnect() {
						LogManager.d("connect to ucube");

						cancel();

						uploadRemainingBinaryToDevice();
					}

					@Override
					public void onError(Exception e) {
						LogManager.e("error to connect ucube");
					}
				});

				LogManager.debug(UpdateService.class.getSimpleName(), "wait for uCube reboot");
			}
		}, 5000, 5000);
	}


	private void retrieveDeviceInfo() {
		LogManager.d("Retrieve device infos");

		final GetDeviceInfoTask task = new GetDeviceInfoTask();
		task.execute(new ITaskMonitor() {
			@Override
			public void handleEvent(TaskEvent event, Object... params) {
				switch (event) {
					case FAILED:
						failedTask = task;
						notifyMonitor(TaskEvent.FAILED, this);
						break;

					case SUCCESS:
						deviceInfos = (DeviceInfos) params[0];

						onDeviceInfoRetrieved();
						break;
				}
			}
		});
	}

	private void onDeviceInfoRetrieved() {
		LogManager.d("Device Info retrieved : " +
				deviceInfos.getSerial() + " - " +
				deviceInfos.getPartNumber());

		ContextDataManager.getInstance().saveDevice(deviceInfos);

		MDMManager.getInstance().setDeviceInfos(deviceInfos);

		notifyMonitor(TaskEvent.SUCCESS);
	}
}
