package com.youTransactor.uCube.bluetooth.ble;

/*
 * Copyright (C) 2019, YouTransactor. All Rights Reserved.
 *
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.google.common.primitives.Bytes;
import com.polidea.rxandroidble2.LogConstants;
import com.polidea.rxandroidble2.LogOptions;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.Timeout;
import com.polidea.rxandroidble2.exceptions.BleException;
import com.youTransactor.uCube.LogManager;
import com.youTransactor.uCube.Tools;
import com.youTransactor.uCube.ContextDataManager;
import com.youTransactor.uCube.rpc.ConnectionListener;
import com.youTransactor.uCube.rpc.DisconnectListener;
import com.youTransactor.uCube.rpc.IConnexionManager;
import com.youTransactor.uCube.rpc.SendCommandListener;

import org.apache.commons.lang3.StringUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

import static android.bluetooth.BluetoothGatt.CONNECTION_PRIORITY_HIGH;


public class BleConnexionManager implements IConnexionManager {

    public static BleConnexionManager getInstance() {
        return INSTANCE;
    }

    private static final BleConnexionManager INSTANCE = new BleConnexionManager();

    enum ConnectionSteps {
        connected,
        powerOff,
        hStateNotificationSetup,
        powerOn,
        dataNotificationSetup
    }

    private final static String UCUBE_TXRX = "d1f4c2d0-f8c2-4565-9349-e0312effd1ee";
    private final static String UCUBE_CONNECTED_PASSWORD = "4e6efab4-7028-49d4-8458-95e69a93607d";
    private final static String UCUBE_HSTATE = "d329a8cb-b872-4c13-971b-72bca85ef0cd";

    private final static UUID UUID_UCUBE_TXRX = UUID.fromString(UCUBE_TXRX);
    private final static UUID UUID_UCUBE_CONNECTED_PASSWORD = UUID.fromString(UCUBE_CONNECTED_PASSWORD);
    private final static UUID UUID_UCUBE_HSTATE = UUID.fromString(UCUBE_HSTATE);

    private final static Timeout CONNECTION_TIMEOUT = new Timeout(15, TimeUnit.SECONDS);

    private RxBleConnection rxBleConnection;
    private RxBleDevice bleDevice;
    private String bleDeviceName;

    private byte[] response = new byte[]{};

    private final CompositeDisposable sendDisposable = new CompositeDisposable();

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    //listeners
    private ConnectionListener connectionListener;
    private DisconnectListener disconnectListener;
    private SendCommandListener sendCommandListener;

    private RxBleClient setupRxBle(Context context) {
        RxBleClient rxBleClient = RxBleClient.create(context);
        RxBleClient.updateLogOptions(new LogOptions.Builder()
                .setLogLevel(LogConstants.WARN)
                .setMacAddressLogSetting(LogConstants.MAC_ADDRESS_FULL)
                .setUuidsLogSetting(LogConstants.UUIDS_FULL)
                .setShouldLogAttributeValues(true)
                .build()
        );

        RxJavaPlugins.setErrorHandler(throwable -> {
            if (throwable instanceof UndeliverableException && throwable.getCause() instanceof BleException) {
                LogManager.e("Suppressed UndeliverableException: " + throwable.toString());
                return; // ignore BleExceptions as they were surely delivered at least once
            }
            // add other custom handlers if needed
            throw new RuntimeException("Unexpected Throwable in RxJavaPlugins error handler", throwable);
        });

        return rxBleClient;
    }

    public void initialize(Context context) {

        String macAddress = ContextDataManager.getInstance().getUCubeAddress();
        String name = ContextDataManager.getInstance().getUCubeName();

        if (macAddress == null || name == null)
            return;

        RxBleClient rxBleClient = setupRxBle(context);

        bleDevice = rxBleClient.getBleDevice(macAddress);
        bleDeviceName = name;
    }

    public void setDevice(Context context, BluetoothDevice bluetoothDevice) {
        String macAddress = bluetoothDevice.getAddress();
        String name = bluetoothDevice.getName();

        if (macAddress == null || name == null)
            return;

        RxBleClient rxBleClient = setupRxBle(context);

        bleDevice = rxBleClient.getBleDevice(macAddress);
        bleDeviceName = name;
    }

    private boolean isInitialized() {
        return bleDevice != null && StringUtils.isNoneBlank(bleDeviceName);
    }

    @Override
    public void connect(ConnectionListener connectionListener) {

        if(connectionListener == null) {
            LogManager.e("connexion listener cannot be null");
            return;
        }

        if (!isInitialized()) {
            connectionListener.onError(new IllegalStateException("connexion manager is not initialized"));
            return;
        }

        if (isConnected()) {
            LogManager.d("Already connect");
            connectionListener.onConnect();
            return;
        }

        this.connectionListener = connectionListener;

        final Disposable disposable = bleDevice.establishConnection(false, CONNECTION_TIMEOUT)
                .observeOn(Schedulers.io())
                .subscribe(this::setRxBleConnection, this::onConnectionFailure);

        final Disposable stateDisposable = bleDevice.observeConnectionStateChanges()
                .observeOn(Schedulers.io())
                .subscribe(this::onConnectionStateChange);

        compositeDisposable.add(stateDisposable);
        compositeDisposable.add(disposable);
    }

    @Override
    public void send(byte[] input, SendCommandListener sendCommandListener) {

        if (!isConnected()) {
            sendCommandListener.onError(new IllegalStateException("Cannot send command device not connected"));
            return;
        }

        if(sendCommandListener == null) {
            LogManager.e("cannot accept null listener");
            return;
        }

        this.sendCommandListener = sendCommandListener;

        this.response = new byte[]{};

        write(input);
    }

    @Override
    public void stop() {

        rxBleConnection = null;

        connectionListener = null;

        sendCommandListener = null;

        compositeDisposable.clear();
    }

    @Override
    public void registerDisconnectListener(DisconnectListener disconnectListener) {
        this.disconnectListener = disconnectListener;
    }

    @Override
    public void unregisterDisconnectListener() {
        disconnectListener = null;
    }

    @Override
    public boolean isConnected() {
        if (bleDevice == null)
            return false;

        if (bleDevice.getConnectionState() == null)
            return false;

        return bleDevice.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED;
    }

    private void onConnectionStateChange(RxBleConnection.RxBleConnectionState newState) {
        LogManager.d("Connection state change: " + newState.name());

        switch (newState) {
            case CONNECTED:
                //todo
                break;

            case DISCONNECTED:
                if (disconnectListener != null)
                    disconnectListener.onDisconnect();
                break;
        }
    }

    private byte[] getConnectionPassword(String deviceName) {
        byte[] pw = new byte[2];
        int crc = 0x0000;
        int polynomial = 0x1021;

        for (byte b : deviceName.getBytes()) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit)
                    crc ^= polynomial;
            }
        }
        pw[0] = (byte) (crc & 0xFF);
        pw[1] = (byte) ((crc & 0xFF00) >> 8);

        return pw;
    }

    private void setRxBleConnection(RxBleConnection rxBleConnection) {

        this.rxBleConnection = rxBleConnection;

        final Disposable requestConnectionPriorityDisposable = rxBleConnection.requestConnectionPriority(CONNECTION_PRIORITY_HIGH, 5, TimeUnit.SECONDS)
                .observeOn(Schedulers.io())
                .subscribe(() -> connectionLoop(ConnectionSteps.connected),
                        this::onConnectionFailure);

        compositeDisposable.add(requestConnectionPriorityDisposable);

    }

    private void setupHStateNotification() {

        if (isConnected()) {

            final Disposable disposable = rxBleConnection.setupNotification(UUID_UCUBE_HSTATE)
                    .doOnNext(notificationObservable -> {
                        LogManager.d("HState notification has been setup!");

                        connectionLoop(ConnectionSteps.hStateNotificationSetup);
                    })
                    .flatMap(notificationObservable -> notificationObservable) // <-- Notification has been set up, now observe value changes.
                    .subscribe(
                            bytes -> {
                                LogManager.d("HState notification has been changed : " + Tools.bytesToHex(bytes));
                                if (bytes[0] == (byte) 0x34)
                                    connectionLoop(ConnectionSteps.powerOn);
                            },
                            this::onConnectionFailure
                    );

            compositeDisposable.add(disposable);
        }
    }

    private void writeConnectionPassword() {

        if (isConnected()) {
            final Disposable disposable = rxBleConnection.writeCharacteristic(UUID_UCUBE_CONNECTED_PASSWORD, getConnectionPassword(bleDeviceName))
                    .subscribe(
                            characteristicValue -> {
                                // Characteristic value confirmed.
                                LogManager.d("write connection password has been done : " + Tools.bytesToHex(characteristicValue));
                            },
                            this::onConnectionFailure
                    );

            compositeDisposable.add(disposable);
        }
    }

    private void setupTxRxNotification() {

        if (isConnected()) {
            final Disposable disposable = rxBleConnection.setupNotification(UUID_UCUBE_TXRX)
                    .doOnNext(notificationObservable -> {
                        LogManager.d("TXRX notification has been setup!");

                        connectionLoop(ConnectionSteps.dataNotificationSetup);
                    })
                    .flatMap(notificationObservable -> notificationObservable) // <-- Notification has been set up, now observe value changes.
                    .subscribe(
                            this::txRxReceived,
                            this::onConnectionFailure // todo I don't know if I should use onSendError hear or this callback
                    );

            compositeDisposable.add(disposable);
        }
    }

    private void write(byte[] input) {

        final Disposable disposable = rxBleConnection.createNewLongWriteBuilder()
                .setCharacteristicUuid(UUID_UCUBE_TXRX)
                .setBytes(Tools.bleCode(input))
                .build()
                .doFinally(sendDisposable::clear)
                .subscribe(
                        (writtenData) -> LogManager.d("message sent : " + Tools.bytesToHex(writtenData)),
                        this::onSendError
                );

        sendDisposable.add(disposable);
    }

    private void readPowerOn() {

        if (isConnected()) {

            final Disposable disposable = rxBleConnection.readCharacteristic(UUID_UCUBE_HSTATE)
                    .subscribe(
                            characteristicValue -> {
                                LogManager.d("HState value: " + Tools.bytesToHex(characteristicValue));

                                if (characteristicValue[0] == (byte) 0x34)
                                    connectionLoop(ConnectionSteps.powerOn);
                                else
                                    connectionLoop(ConnectionSteps.powerOff);
                            },
                            this::onConnectionFailure
                    );

            compositeDisposable.add(disposable);
        }
    }

    private void onConnectionSuccess() {
        if (connectionListener != null) {
            connectionListener.onConnect();
            connectionListener = null;
        }
    }

    private void onConnectionFailure(Throwable throwable) {
        if (connectionListener != null) {
            connectionListener.onError(new Exception(throwable));
            connectionListener = null;
        }
    }

    private synchronized void onSendSuccess() {
        LogManager.d("send success");

        if (sendCommandListener != null)
            sendCommandListener.onSuccess(Tools.bleDecode(response));
    }

    private synchronized void onSendError(Throwable e) {
        LogManager.e("send error : ", new Exception(e));

        if (sendCommandListener != null)
            sendCommandListener.onError(new Exception(e));
    }

    private void connectionLoop(ConnectionSteps connectionSteps) {
        switch (connectionSteps) {
            case connected:
                readPowerOn();
                break;

            case powerOff:
                setupHStateNotification();

                writeConnectionPassword();
                break;

            case hStateNotificationSetup:
                break;

            case powerOn:
                setupTxRxNotification();
                break;

            case dataNotificationSetup:
                onConnectionSuccess();
                break;
        }
    }

    private void txRxReceived(byte[] data) {
        LogManager.d("data : " + Tools.bytesToHex(data));

        response = Bytes.concat(response, data);

        if (checkEndOfMessage(response))
            onSendSuccess();
    }

    private boolean checkEndOfMessage(byte[] message) {
        return (message[message.length - 2] == 0x0E) &&
                (message[message.length - 1] == 0x0E || message[message.length - 1] == 0x0F);
    }

}

