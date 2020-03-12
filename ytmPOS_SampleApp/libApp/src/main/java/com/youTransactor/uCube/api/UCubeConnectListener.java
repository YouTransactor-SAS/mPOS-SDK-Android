package com.youTransactor.uCube.api;

public interface UCubeConnectListener {

    void onProgress(UCubeAPIState uCubeAPIState);

    void onFinish(boolean status, UCubeInfo uCubeInfo);
}
