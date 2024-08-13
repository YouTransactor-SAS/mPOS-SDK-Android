/*
 * ============================================================================
 *
 * Copyright (c) 2022 YouTransactor
 *
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of YouTransactor
 * ("Confidential Information"). You  shall not disclose or redistribute such
 * Confidential Information and shall use it only in accordance with the terms of
 * the license agreement you entered into with YouTransactor.
 *
 * This software is provided by YouTransactor AS IS, and YouTransactor
 * makes no representations or warranties about the suitability of the software,
 * either express or implied, including but not limited to the implied warranties
 * of merchantability, fitness for a particular purpose or non-infringement.
 * YouTransactor shall not be liable for any direct, indirect, incidental,
 * special, exemplary, or consequential damages suffered by licensee as the
 * result of using, modifying or distributing this software or its derivatives.
 *
 * ==========================================================================
 */
package com.youtransactor.sampleapp.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum Test {
    _payment("IDLE -> Transaction ", 0),
    ;

    private final String description;
    private final int delayInMilleseconds;

    public String getDescription() {
        return description;
    }

    public int getDelayInMilleseconds() {
        return delayInMilleseconds;
    }

    Test(String description, int delayInMilleseconds) {
        this.description = description;
        this.delayInMilleseconds = delayInMilleseconds;
    }

    public List<TestDaemon.Step> getSequence() {
        switch (this) {
            case _payment:
                return new ArrayList<>(Arrays.asList(TestDaemon.Step.IDLE, TestDaemon.Step.transaction));

            default:
                return null;
        }
    }
}
