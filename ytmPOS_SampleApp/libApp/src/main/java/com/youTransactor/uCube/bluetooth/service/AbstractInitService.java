package com.youTransactor.uCube.bluetooth.service;


import com.youTransactor.uCube.AbstractService;
import com.youTransactor.uCube.TaskEvent;

public abstract class AbstractInitService extends AbstractService {

    protected InitServiceState state;

    AbstractInitService() {
        state = InitServiceState.IDLE;
    }

    protected void setState(InitServiceState state, Object... params) {
        this.state = state;

        Object[] args = new Object[params.length + 1];
        args[0] = state;
        System.arraycopy(params, 0, args, 1, params.length);

        notifyMonitor(TaskEvent.PROGRESS, args);
    }

    public InitServiceState getState() {
        return state;
    }
}
