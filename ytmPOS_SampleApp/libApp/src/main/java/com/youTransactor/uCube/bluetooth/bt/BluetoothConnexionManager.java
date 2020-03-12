/*
 * Copyright (C) 2016, YouTransactor. All Rights Reserved.
 *
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youTransactor.uCube.bluetooth.bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.youTransactor.uCube.LogManager;
import com.youTransactor.uCube.Tools;
import com.youTransactor.uCube.ContextDataManager;
import com.youTransactor.uCube.rpc.ConnectionListener;
import com.youTransactor.uCube.rpc.DisconnectListener;
import com.youTransactor.uCube.rpc.IConnexionManager;
import com.youTransactor.uCube.rpc.RPCManager;
import com.youTransactor.uCube.rpc.SendCommandListener;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;

import static com.youTransactor.uCube.rpc.RPCManager.MAX_RPC_PACKET_SIZE;

/**
 * @author gbillard on 5/25/16.
 */
public class BluetoothConnexionManager implements IConnexionManager {

    private String deviceAddr;
    private BluetoothSocket socket;
    private AcceptThread acceptThread;

    private OutputStream out;

    private SendCommandListener sendCommandListener;

    private DisconnectListener disconnectListener = null;

    private static final UUID MY_UUID = UUID.fromString("596f7554-7261-6e73-6163-746f72b0ec8f"); //YoutransactorUUID
    private static final String MY_NAME = "no_bt_name_specified";//BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddr).getName();

    private class AcceptThread extends Thread {
        private BluetoothServerSocket bluetoothServerSocket;
        private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        AcceptThread() {
            BluetoothServerSocket tmp;

            try {
                //tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(name, MY_UUID);
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(MY_NAME, MY_UUID);
            } catch (IOException e) {
                Log.e("BT Server", "listen() failed", e);
                return;
            }
            bluetoothServerSocket = tmp;
        }

        public void run() {
            while (true) {
                try {
                    Log.i("BT Server", "waiting for new bt client");
                    socket = bluetoothServerSocket.accept();
                    Log.i("BT Server", "New BT client detected.");
                } catch (IOException e) {
                    Log.i("BT Server", "fail to get a bt client.");
                    break;
                }
                if (socket != null) {
                    try {
                        if (bluetoothServerSocket != null)
                            bluetoothServerSocket.close();
                    } catch (IOException ignored) {
                    }
                    bluetoothServerSocket = null;

                    try {
                        out =  socket.getOutputStream();

                        DAEMON = new RPCDaemon(socket.getInputStream());

                        new Thread(DAEMON).start();

                        //BluetoothConnexionManager.communicationManagerBluetoothListener.onCommunicationManagerInterfaceBluetoothEvent(CommunicationManagerBluetoothEvent.BT_DEVICE_CONNECTED_FROM_EXT);
                    } catch (Exception e) {
                        Log.d(BluetoothConnexionManager.class.getName(), "connect device error", e);
                        if (socket != null && socket.isConnected()) {
                            try {
                                socket.close();
                            } catch (IOException ignored) {
                            }
                        }
                        //BluetoothConnexionManager.communicationManagerBluetoothListener.onCommunicationManagerInterfaceBluetoothEvent(CommunicationManagerBluetoothEvent.BT_DEVICE_CONNECTION_ERROR);
                    }
                    break;
                }
            }
        }

        void cancel() {
            try {
                if (bluetoothServerSocket != null)
                    bluetoothServerSocket.close();
                bluetoothServerSocket = null;
            } catch (IOException ignored) {
            }
        }
    }

    private BluetoothConnexionManager() {
    }

    public void accept() {
        if (acceptThread != null)
            cancelAccept();
        acceptThread = new AcceptThread();
        acceptThread.start();
    }

    public void cancelAccept() {
        if (acceptThread == null)
            return;

        acceptThread.cancel();
        acceptThread.interrupt();
        acceptThread = null;
    }

    @Override
    public boolean isConnected() {
        return out != null && socket != null && socket.isConnected();
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

        LogManager.d("connect to " + deviceAddr);

        if (socket != null && socket.isConnected()) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }

        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddr);

        try {
//			socket = device.createRfcommSocketToServiceRecord(BT_UUID);
            Method m = device.getClass().getMethod("createInsecureRfcommSocket", int.class);
            socket = (BluetoothSocket) m.invoke(device, 1);

            if (socket == null) {
                connectionListener.onError(new IllegalStateException("Error when trying to create socket"));
                return;
            }

            socket.connect();

            this.out =  socket.getOutputStream();

            DAEMON = new RPCDaemon(socket.getInputStream());

            new Thread(DAEMON).start();

            connectionListener.onConnect();

        } catch (Exception e) {
            LogManager.e("connect device error", e);

            if (socket != null && socket.isConnected()) {
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            }

            connectionListener.onError(e);
        }
    }

    @Override
    public void send(byte[] input, SendCommandListener sendCommandListener) {
        INSTANCE.sendCommandListener = sendCommandListener;

        try {
            out.write(input);

            out.flush();

        } catch (IOException e) {

            e.printStackTrace();

            sendCommandListener.onError(e);
        }
    }

    @Override
    public void stop() {
        if (DAEMON != null) {
            DAEMON.stop();
        }

        out = null;

        if (socket != null && socket.isConnected()) {
            try {
                socket.close();
                socket = null;
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public void registerDisconnectListener(DisconnectListener disconnectListener) {
        this.disconnectListener = disconnectListener;
    }

    @Override
    public void unregisterDisconnectListener() {
        disconnectListener = null;
    }

    private boolean isInitialized() {
        return StringUtils.isNoneBlank(deviceAddr);
    }

    public void initialize() {
        this.deviceAddr = ContextDataManager.getInstance().getUCubeAddress();
    }

    public void setDeviceAddr(String deviceAddr) {
        this.deviceAddr = deviceAddr;
    }

    public String getDeviceAddr() {
        return deviceAddr;
    }

    public static BluetoothConnexionManager getInstance() {
        return INSTANCE;
    }

    private static final BluetoothConnexionManager INSTANCE = new BluetoothConnexionManager();


    /**
     * this function to calculate the checksum 16bit
     *
     * @param bytes the payload data
     * @return the calculated CRC16
     */
    private static int computeChecksumCRC16(byte[] bytes) {
        int crc = 0x0000;
        int temp;
        int crc_byte;

        for (byte aByte : bytes) {

            crc_byte = aByte;

            if (crc_byte < 0)
                crc_byte += 256;

            for (int bit_index = 0; bit_index < 8; bit_index++) {

                temp = (crc >> 15) ^ (crc_byte >> 7);

                crc <<= 1;
                crc &= 0xFFFF;

                if (temp > 0) {
                    crc ^= 0x1021;
                    crc &= 0xFFFF;
                }

                crc_byte <<= 1;
                crc_byte &= 0xFF;

            }
        }

        return crc;
    }

    private RPCDaemon DAEMON;

    private class RPCDaemon implements Runnable {

        private InputStream in;
        private boolean interrupted = false;

        private RPCDaemon(InputStream in) {
            this.in = in;
        }

        private void stop() {
            interrupted = true;
        }

        @Override
        public void run() {
            byte[] bufferFromStream = new byte[MAX_RPC_PACKET_SIZE];
            byte[] bufferToDeliver = new byte[MAX_RPC_PACKET_SIZE];

            short expected_length = 0;
            short bufferToDeliverOffset = 0;

            boolean checkSize = true;
            boolean checkComplete = true;
            boolean checkChecksum = true;

            boolean skipOneRead = false;

            while (!interrupted) {
                try {
                    if (skipOneRead)
                        skipOneRead = false;
                    else {
                        int nb_bytes = in.read(bufferFromStream);

                        // copy data to buffer
                        System.arraycopy(bufferFromStream, 0, bufferToDeliver, bufferToDeliverOffset, nb_bytes);
                        bufferToDeliverOffset += nb_bytes;
                    }

                    if (checkSize) {
                        // compute size if at least 3 bytes in buff
                        if (bufferToDeliverOffset < 3)
                            continue;
                        expected_length = Tools.makeShort(bufferToDeliver[1], bufferToDeliver[2]);
                        expected_length += 1 + 2 + 1 + 2 + 3; //ETX,CRC,STX,CMDID,LENGTH

                        if (expected_length > MAX_RPC_PACKET_SIZE) {
                            sendCommandListener.onError(new IllegalStateException("expected_length'" + expected_length + ") > MAX_RPC_PACKET_SIZE("+MAX_RPC_PACKET_SIZE+")"));
                            continue;
                        }

                        checkSize = false;
                    }

                    if (checkComplete) {
                        // size must be at least expected one
                        if (bufferToDeliverOffset < expected_length)
                            continue;

                        checkComplete = false;
                    }

                    if (checkChecksum) {
                        // checksum concur
                        int len = expected_length - 1 - 2; // STX CRC ETX
                        byte[] b = Arrays.copyOfRange(bufferToDeliver, 1, len);
                        int crc = computeChecksumCRC16(b);
                        int check = Tools.makeInt(bufferToDeliver[expected_length - 3], bufferToDeliver[expected_length - 2]);
                        if (check != crc) {
                            sendCommandListener.onError(new IllegalStateException("Checksum is failed! " + crc + " expected: " + check));
                            continue;
                        }
                        checkChecksum = false;
                    }

                    byte[] data = new byte[expected_length];
                    System.arraycopy(bufferToDeliver, 0, data, 0, expected_length);

                    LogManager.debug(RPCManager.class.getSimpleName(), "received: " + Tools.bytesToHex(data));


                    sendCommandListener.onSuccess(data);


                    // packet comlete with no error remove expected length from buffer, reset values and check booleans
                    checkSize = checkComplete = checkChecksum = true;

                    // remove the first expected_length bytes from bufferToDeliver
                    if (bufferToDeliverOffset > expected_length) {
                        System.arraycopy(bufferToDeliver, expected_length, bufferToDeliver, 0, bufferToDeliverOffset - expected_length);
                        skipOneRead = true;
                    }
                    bufferToDeliverOffset -= expected_length;

                } catch (Exception e) {
                    if (!interrupted) {
                        if (e instanceof IOException) {
                            LogManager.e("socket closed");
                        } else {
                            LogManager.e("RPC socket read  error", e);
                        }

                        if(disconnectListener != null)
                            disconnectListener.onDisconnect();

                        INSTANCE.stop();
                    }
                }
            }

            DAEMON = null;

        }
    }
}
