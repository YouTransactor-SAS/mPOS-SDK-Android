package com.youTransactor.uCube.api;

public interface UCubeAPIListener {

    void onProgress(UCubeAPIState uCubeAPIState);

    void onFinish(boolean status);
}
