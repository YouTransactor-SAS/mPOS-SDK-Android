package com.youTransactor.uCube.bluetooth.service;

public enum InitServiceState {
    IDLE,
    CANCEL_ALL,
    PAIR_DEVICE,
    RETRIEVE_DEVICE_INFO,
    RETRIEVE_DEVICE_CERTIFICATE,
    REGISTER_DEVICE
}
