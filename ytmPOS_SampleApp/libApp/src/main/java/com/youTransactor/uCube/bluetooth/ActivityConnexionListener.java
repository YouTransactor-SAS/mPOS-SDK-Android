package com.youTransactor.uCube.bluetooth;


import android.bluetooth.BluetoothDevice;

public interface ActivityConnexionListener {
    void onConnected(BluetoothDevice device);
    void onError();
    void onCancelled();
}
