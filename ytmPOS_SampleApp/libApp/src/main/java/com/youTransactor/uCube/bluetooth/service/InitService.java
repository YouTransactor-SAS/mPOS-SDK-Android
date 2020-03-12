package com.youTransactor.uCube.bluetooth.service;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;

import com.youTransactor.uCube.ContextDataManager;
import com.youTransactor.uCube.ITask;
import com.youTransactor.uCube.ITaskMonitor;
import com.youTransactor.uCube.LogManager;
import com.youTransactor.uCube.TaskEvent;
import com.youTransactor.uCube.api.YTMPOSProduct;
import com.youTransactor.uCube.bluetooth.ActivityConnexionListener;
import com.youTransactor.uCube.bluetooth.ble.BleConnexionActivity;
import com.youTransactor.uCube.bluetooth.bt.BtConnexionActivity;
import com.youTransactor.uCube.bluetooth.task.GetDeviceInfoTask;
import com.youTransactor.uCube.bluetooth.task.RetrieveDeviceCertificate;
import com.youTransactor.uCube.mdm.MDMManager;
import com.youTransactor.uCube.mdm.task.RegisterTask;
import com.youTransactor.uCube.mdm.task.SendStateTask;
import com.youTransactor.uCube.rpc.DeviceInfos;

import java.security.KeyStore;

public class InitService extends AbstractInitService {

    private static final int BT_ACTIVITY_REQUEST = 1;

    private YTMPOSProduct ytmposProduct;
    private Activity activity;
    private DeviceInfos deviceInfos;
    private byte[] cert;
    private KeyStore sslKey;

    private ActivityConnexionListener pairDeviceListener = new ActivityConnexionListener() {
        @Override
        public void onConnected(BluetoothDevice device) {
            LogManager.d("Connected to paired device");

            activity.finishActivity(BT_ACTIVITY_REQUEST);

            onDevicePaired();
        }

        @Override
        public void onError() {
            LogManager.d("Error to pair device");

            activity.finishActivity(BT_ACTIVITY_REQUEST);

            notifyMonitor(TaskEvent.FAILED);
        }

        @Override
        public void onCancelled() {
            LogManager.d("Pairing device cancelled");

            activity.finishActivity(BT_ACTIVITY_REQUEST);

            notifyMonitor(TaskEvent.CANCELLED);
        }
    };

    public InitService(Activity activity, YTMPOSProduct ytmposProduct) {
        this.activity = activity;
        this.ytmposProduct = ytmposProduct;
    }

    @Override
    protected void start() {
        LogManager.d("Start init service");

        if(ContextDataManager.getInstance().isuCubePaired()) {

            setState(InitServiceState.RETRIEVE_DEVICE_INFO);

            retrieveDeviceInfo();
        } else {

            setState(InitServiceState.PAIR_DEVICE);

            pairDevice();
        }
    }

    private void pairDevice() {

        switch (ytmposProduct) {
            case uCube:
                BtConnexionActivity.setActivityConnexionListener(pairDeviceListener);

                Intent btCnxActivityIntent = new Intent(activity, BtConnexionActivity.class);
                activity.startActivityForResult(btCnxActivityIntent, BT_ACTIVITY_REQUEST);
                break;

            case uCube_touch:
                BleConnexionActivity.setActivityConnexionListener(pairDeviceListener);

                Intent bleCnxActivityIntent = new Intent(activity, BleConnexionActivity.class);
                activity.startActivityForResult(bleCnxActivityIntent, BT_ACTIVITY_REQUEST);
                break;
        }
    }

    private void onDevicePaired() {
        LogManager.d("device paired");

        setState(InitServiceState.RETRIEVE_DEVICE_INFO);

        retrieveDeviceInfo();
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

        if(ContextDataManager.getInstance().isuCubePaired())

            notifyMonitor(TaskEvent.SUCCESS, deviceInfos);

        else {
            setState(InitServiceState.RETRIEVE_DEVICE_CERTIFICATE);

            retrieveDeviceCertificate();
        }
    }

    private void retrieveDeviceCertificate() {
        LogManager.d("Retrieve device certificate");

        final RetrieveDeviceCertificate task = new RetrieveDeviceCertificate();
        task.execute(new ITaskMonitor() {
            @Override
            public void handleEvent(TaskEvent event, Object... params) {
                switch (event) {
                    case FAILED:
                        failedTask = task;
                        notifyMonitor(TaskEvent.FAILED, this);
                        break;

                    case SUCCESS:
                        cert = (byte[]) params[0];

                        onDeviceCertRetrieved();
                        break;
                }
            }
        });
    }

    private void onDeviceCertRetrieved()  {
        LogManager.d("device certificate retrieved");

        setState(InitServiceState.REGISTER_DEVICE);

        registerDevice();
    }

    private void registerDevice() {
        LogManager.d("Register device");

        RegisterTask task = new RegisterTask(deviceInfos, cert);

        task.execute(new ITaskMonitor() {
            @Override
            public void handleEvent(TaskEvent event, Object... params) {
                switch(event) {
                    case FAILED:
                        failedTask = (ITask) params[0];
                        notifyMonitor(TaskEvent.FAILED, this);
                        return;

                    case SUCCESS:
                        sslKey = (KeyStore) params[0];

                        if(ContextDataManager.getInstance().setUCubeSslCertificate(sslKey)) {

                            MDMManager.getInstance().initialize(sslKey);

                            sendState();
                        }else
                            notifyMonitor(TaskEvent.FAILED, this);
                        break;
                }
            }
        });
    }

    private void sendState() {
        LogManager.d("Send state device");

        new SendStateTask(deviceInfos).execute((event, params) -> notifyMonitor(TaskEvent.SUCCESS, deviceInfos));
    }
}
