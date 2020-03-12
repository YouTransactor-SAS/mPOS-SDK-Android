/*
 * Copyright (C) 2016, YouTransactor. All Rights Reserved.
 *
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */

package com.youTransactor.uCube.accounting.service;

import com.youTransactor.uCube.AbstractService;
import com.youTransactor.uCube.ITask;
import com.youTransactor.uCube.ITaskMonitor;
import com.youTransactor.uCube.TaskEvent;
import com.youTransactor.uCube.rpc.Constants;
import com.youTransactor.uCube.rpc.DeviceInfos;
import com.youTransactor.uCube.rpc.command.GetInfosCommand;

/**
 * Created by gmx on 25/07/17.
 */

public abstract class AbstractAccountingService extends AbstractService {

    protected ServiceState state;
    protected DeviceInfos deviceInfos;

    protected AbstractAccountingService() {
        state = ServiceState.IDLE;
    }

    protected AbstractAccountingService(DeviceInfos deviceInfos) {

        this();
        setDeviceInfos(deviceInfos);
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

    protected void retrieveDeviceInfos() {
        if (deviceInfos != null) {
            onDeviceInfosRetrieved();
            return;
        }

        state = ServiceState.RETRIEVE_DEVICE_INFOS;

        GetInfosCommand cmd;

        cmd = new GetInfosCommand(
                Constants.TAG_TERMINAL_PN, Constants.TAG_TERMINAL_SN, Constants.TAG_TRANSACTION_DATA);

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
                        onDeviceInfosRetrieved();
                }
            }
        });
    }

    protected void onDeviceInfosRetrieved() {
        notifyMonitor(TaskEvent.SUCCESS);
    }
}
