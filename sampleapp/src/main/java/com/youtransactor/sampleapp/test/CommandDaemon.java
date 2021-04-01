package com.youtransactor.sampleapp.test;

import android.util.Log;

import com.youTransactor.uCube.ITaskMonitor;
import com.youTransactor.uCube.TaskEvent;
import com.youTransactor.uCube.rpc.Constants;
import com.youTransactor.uCube.rpc.command.GetInfosCommand;

public class CommandDaemon implements Runnable {

    final int[] uCubeInfoTagList = {
            Constants.TAG_ATMEL_SERIAL,
            Constants.TAG_TERMINAL_PN,
            Constants.TAG_TERMINAL_SN,
            Constants.TAG_FIRMWARE_VERSION,
            Constants.TAG_EMV_ICC_CONFIG_VERSION,
            Constants.TAG_EMV_NFC_CONFIG_VERSION
    };
    int counter = 0;
    private int delayToStart;
    private boolean interrupted = false;

    public CommandDaemon(int delayToStart) {
        this.delayToStart = delayToStart;
    }

    public void stop() {
        interrupted = true;
    }

    @Override
    public void run() {
        counter = 0;

        getInfo();
    }

    private void getInfo() {

        if (interrupted)
            return;

        counter++;

        new GetInfosCommand(uCubeInfoTagList).execute(new ITaskMonitor() {
            @Override
            public void handleEvent(TaskEvent event, Object... params) {
                switch (event) {
                    case FAILED:
                    case CANCELLED:
                        Log.d(getClass().getName(), "get info number : " + counter + " failed");
                        return;

                    case SUCCESS:
                        Log.d(getClass().getName(), "get info number : " + counter + " success");

                        if (delayToStart > 0) {
                            try {
                                Thread.sleep(delayToStart);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                interrupted = true;
                            }
                        }

                        getInfo();

                        break;
                }
            }
        });
    }
}
