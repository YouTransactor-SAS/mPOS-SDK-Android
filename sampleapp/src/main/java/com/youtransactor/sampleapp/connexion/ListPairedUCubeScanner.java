/*
 * ============================================================================
 *
 * Copyright (c) 2024 JABIL Payment Solution
 *
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of JABIL Payment Solution
 * ("Confidential Information"). You  shall not disclose or redistribute such
 * Confidential Information and shall use it only in accordance with the terms of
 * the license agreement you entered into with JABIL Payment Solution.
 *
 * This software is provided by JABIL Payment Solution AS IS, and JABIL Payment Solution
 * makes no representations or warranties about the suitability of the software,
 * either express or implied, including but not limited to the implied warranties
 * of merchantability, fitness for a particular purpose or non-infringement.
 * JABIL Payment Solution shall not be liable for any direct, indirect, incidental,
 * special, exemplary, or consequential damages suffered by licensee as the
 * result of using, modifying or distributing this software or its derivatives.
 *
 * ==========================================================================
 */
package com.youtransactor.sampleapp.connexion;

import com.youTransactor.uCube.api.UCubeAPI;
import com.youTransactor.uCube.connexion.ScanListener;
import com.youTransactor.uCube.connexion.UCubeDevice;
import com.youtransactor.sampleapp.R;

import java.util.List;

public class ListPairedUCubeScanner implements IDeviceScanner {

    private static final String DEFAULT_FILTER = "uCube";

    @Override
    public int getDeviceImageResourceId() {
        return R.drawable.ucube;
    }

    @Override
    public String getDefaultFilter() {
        return DEFAULT_FILTER;
    }

    @Override
    public void scan(String filter, ScanListener listener) {
        List<UCubeDevice> deviceList = UCubeAPI.getConnexionManager().getPairedUCubes(filter);

//        for (UCubeDevice device : deviceList) {
//            listener.onDeviceDiscovered(device);
//        }

        listener.onScanComplete(deviceList);
    }

    @Override
    public void stop() {
    }

}
