package com.youtransactor.sampleapp.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum Ticket {
    _7132;

    public List<TestDaemon.Step> getSequence() {
        switch (this) {
            case _7132:
                return new ArrayList<>(Arrays.asList(TestDaemon.Step.IDLE, TestDaemon.Step.connect, TestDaemon.Step.getInfo, TestDaemon.Step.disconnect));

            default:
                return null;
        }
    }
}
