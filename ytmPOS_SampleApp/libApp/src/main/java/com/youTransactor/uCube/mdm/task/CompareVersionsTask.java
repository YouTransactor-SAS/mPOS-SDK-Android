/*
 * Copyright (C) 2016, YouTransactor. All Rights Reserved.
 *
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youTransactor.uCube.mdm.task;

import androidx.annotation.NonNull;

import com.youTransactor.uCube.AbstractTask;
import com.youTransactor.uCube.LogManager;
import com.youTransactor.uCube.TaskEvent;
import com.youTransactor.uCube.mdm.Config;
import com.youTransactor.uCube.mdm.service.BinaryUpdate;
import com.youTransactor.uCube.rpc.DeviceInfos;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.youTransactor.uCube.mdm.Constants.*;

/**
 * NB : PCI Require that update same version not allowed and the down grade neither
 */
public class CompareVersionsTask extends AbstractTask {

    private DeviceInfos deviceInfos;
    private List<Config> configList;
    private List<BinaryUpdate> updateList;
    private boolean checkOnlyFirmwareVersion;
    private boolean forceUpdate;

    public CompareVersionsTask(@NonNull DeviceInfos deviceInfos, @NonNull List<Config> configList) {
        init(deviceInfos, configList);
    }

    public void setCheckOnlyFirmwareVersion(boolean checkOnlyFirmwareVersion) {
        this.checkOnlyFirmwareVersion = checkOnlyFirmwareVersion;
    }

    public void setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }

    @Override
    protected void start() {
        notifyMonitor(TaskEvent.SUCCESS, getUpdateList());
    }

    public void init(@NonNull DeviceInfos deviceInfos, @NonNull List<Config> configList) {
        this.deviceInfos = deviceInfos;
        this.configList = configList;
    }

    public List<BinaryUpdate> getUpdateList() {
        if (updateList == null) {
            updateList = new ArrayList<>();

            if (configList != null && deviceInfos != null) {
                for (Config cfg : configList) {
                    compareVersion(cfg);
                }
            }
        }

        return updateList;
    }

    private void compareVersion(Config cfg) {
        if (checkOnlyFirmwareVersion) {
            if (cfg.getType() != SVPP_FIRMWARE_TYPE && cfg.getType() != ST_FIRMWARE_TYPE)
                return;
        }

        int[] current = getComparableVersion(cfg.getType());

        if (current == null) {
            updateList.add(new BinaryUpdate(cfg, true));
            return;
        }

        int[] expected = convertToComparable(cfg.getCurrentVersion());

        int res = compare(current, expected);

        if (res > 0 || (res == 0 && !forceUpdate)) {
            return;
        }

        expected = convertToComparable(cfg.getMinVersion());

        updateList.add(new BinaryUpdate(cfg, compare(current, expected) < 0));
    }

    private int compare(int[] current, int[] expected) {
        for (int i = 0; i < 4; i++) {
            if (current[i] < expected[i]) {
                return -1;
            }

            if (current[i] > expected[i]) {
                return 1;
            }
        }

        return 0;
    }

    private int[] getComparableVersion(int type) {
        switch (type) {
            case SVPP_FIRMWARE_TYPE:
                return convertToComparable(deviceInfos.getSvppFirmware());

            case ST_FIRMWARE_TYPE:
                return convertToComparable(deviceInfos.getNfcFirmware());

            case ICC_CONFIG_TYPE:
                return convertToComparable(deviceInfos.getIccEmvConfigVersion());

            case NFC_CONFIG_TYPE:
                return convertToComparable(deviceInfos.getNfcEmvConfigVersion());

            default:
                return null;
        }
    }

    private int[] convertToComparable(String version) {
        if (!StringUtils.isBlank(version)) {
            try {
                int index = 0;
                int[] res = new int[4];

                for (String v : version.split("\\.")) {
                    res[index++] = Integer.valueOf(v);
                }

                return res;

            } catch (Exception e) {
                LogManager.e("invalid binary version: '" + version + "'", e);
            }
        }

        return null;
    }

}
