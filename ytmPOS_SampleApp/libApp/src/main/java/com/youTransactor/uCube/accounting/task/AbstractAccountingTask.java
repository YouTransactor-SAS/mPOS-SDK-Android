/*
 * Copyright (C) 2016, YouTransactor. All Rights Reserved.
 *
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */

package com.youTransactor.uCube.accounting.task;

import com.youTransactor.uCube.AbstractTask;
import com.youTransactor.uCube.rpc.DeviceInfos;

/**
 * Created by gmx on 25/07/17.
 */

public abstract class AbstractAccountingTask extends AbstractTask {

    protected DeviceInfos deviceInfos;
    protected int HTTPResponseCode;

    public AbstractAccountingTask(DeviceInfos deviceInfos) {

        setDeviceInfos(deviceInfos);
    }

    public void setDeviceInfos(DeviceInfos deviceInfos) {

        this.deviceInfos = deviceInfos;
    }

    public int getHTTPResponseCode() {

        return HTTPResponseCode;
    }
}
