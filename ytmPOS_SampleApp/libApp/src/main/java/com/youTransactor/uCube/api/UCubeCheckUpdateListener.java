package com.youTransactor.uCube.api;

import com.youTransactor.uCube.mdm.Config;
import com.youTransactor.uCube.mdm.service.BinaryUpdate;

import java.util.List;

public interface UCubeCheckUpdateListener {
    void onProgress(UCubeAPIState state);

    void onFinish(boolean status, List<BinaryUpdate> updateList, List<Config> cfgList);
}
