package com.youTransactor.uCube.bluetooth.task;

import com.youTransactor.uCube.TaskEvent;
import com.youTransactor.uCube.rpc.command.InstallForLoadKeyCommand;

public class RetrieveDeviceCertificate extends AbstractInitTask{


    @Override
    protected void start() {
        InstallForLoadKeyCommand cmd = new InstallForLoadKeyCommand();

        cmd.execute((event, params) -> {
            switch(event) {
                case FAILED:
                    notifyMonitor(TaskEvent.FAILED);
                    return;

                case SUCCESS:
                    byte[] cert = ((InstallForLoadKeyCommand) params[0]).getFullData();
                    notifyMonitor(TaskEvent.SUCCESS, (Object) cert);

                    break;
            }
        });
    }
}
