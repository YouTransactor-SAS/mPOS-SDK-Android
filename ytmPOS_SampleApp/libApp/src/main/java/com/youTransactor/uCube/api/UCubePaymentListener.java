package com.youTransactor.uCube.api;

public interface UCubePaymentListener {

    void onStart(byte[] ksn);
    void onFinish(boolean status, UCubePaymentResponse uCubePaymentResponse);
}
