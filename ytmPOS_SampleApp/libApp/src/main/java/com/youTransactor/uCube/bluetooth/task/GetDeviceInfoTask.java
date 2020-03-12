package com.youTransactor.uCube.bluetooth.task;

import com.youTransactor.uCube.LogManager;
import com.youTransactor.uCube.TaskEvent;
import com.youTransactor.uCube.rpc.Constants;
import com.youTransactor.uCube.rpc.DeviceInfos;
import com.youTransactor.uCube.rpc.command.GetInfosCommand;

public class GetDeviceInfoTask extends AbstractInitTask {

    private void retrieveDeviceInfo() {
        LogManager.d("Retrieve device infos");

        GetInfosCommand cmd = new GetInfosCommand(
                Constants.TAG_TERMINAL_PN,
                Constants.TAG_TERMINAL_SN,
                Constants.TAG_FIRMWARE_VERSION,
                Constants.TAG_EMV_ICC_CONFIG_VERSION,
                Constants.TAG_MPOS_MODULE_STATE);

        cmd.execute((event, params) -> {
            switch (event) {
                case FAILED:
                    notifyMonitor(TaskEvent.FAILED);
                    return;

                case SUCCESS:
                    deviceInfos = new DeviceInfos(((GetInfosCommand) params[0]).getResponseData());

                    if (deviceInfos.getNfcModuleState() != 0) {
                        retrieveDeviceNFCInfos(deviceInfos.getTlv());
                    } else {
                        onDeviceInfoRetrieved();
                    }
                    break;
            }
        });
    }

    private void retrieveDeviceNFCInfos(final byte[] primaryData) {
        LogManager.d("Retrieve NFC device infos");

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

        onDeviceInfoRetrieved();
    }

    private void onDeviceInfoRetrieved() {
        LogManager.d("Device Info retrieved : " +
                deviceInfos.getSerial() + " - " +
                deviceInfos.getPartNumber());

        notifyMonitor(TaskEvent.SUCCESS, deviceInfos);

    }

    @Override
    protected void start() {
        retrieveDeviceInfo();
    }
}
