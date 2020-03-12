/*
 * Copyright (C) 2011-2016, YouTransactor. All Rights Reserved.
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
import com.youTransactor.uCube.mdm.task.RegisterTask;
import com.youTransactor.uCube.mdm.task.SendStateTask;
import com.youTransactor.uCube.rpc.command.InstallForLoadKeyCommand;

import java.security.KeyStore;

/**
 * @author gbillard on 4/25/16.
 */
public class RegisterService extends AbstractMDMService {

	private byte[] cert;
	private KeyStore sslKey;

	public RegisterService() {
		super();
	}

	@Override
	protected void onDeviceInfosRetrieved() {
		if (sslKey == null) {
			retrieveDeviceCertificat();

		} else {
			sendState();
		}
	}

	private void retrieveDeviceCertificat() {
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
					cert = ((InstallForLoadKeyCommand) params[0]).getFullData();
					registerDevice();
				}
			}
		});
	}

	private void registerDevice() {
		setState(ServiceState.REGISTER_DEVICE);

		RegisterTask task = new RegisterTask(deviceInfos, cert);

		task.execute(new ITaskMonitor() {
			@Override
			public void handleEvent(TaskEvent event, Object... params) {
				switch(event) {
				case FAILED:
					LogManager.e("Register failed");
					failedTask = (ITask) params[0];
					notifyMonitor(TaskEvent.FAILED, this);
					return;

				case SUCCESS:
					LogManager.d("Register success");
					sslKey = (KeyStore) params[0];
					 if(ContextDataManager.getInstance().setUCubeSslCertificate(sslKey))
					 	sendState();
					 else
						 notifyMonitor(TaskEvent.FAILED, this);
				}
			}
		});
	}

	private void sendState() {
		new SendStateTask(deviceInfos).execute((event, params) -> notifyMonitor(TaskEvent.SUCCESS, sslKey));
	}

}
