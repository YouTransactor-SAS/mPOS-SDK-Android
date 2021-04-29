package com.youtransactor.sampleapp.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum Ticket {
    _7132("IDLE -> DELAY -> CONNECT -> GetInfo -> DISCONNECT \nrecommended delay : 1000 ms", 1000),
    _6928("IDLE -> DELAY -> CONNECT -> GetInfo -> InstallForLoadKey -> DISCONNECT \nrecommended delay : 0 ms", 0),
    ;

    private String description;
    private int delay;

    public String getDescription() {
        return description;
    }

    public int getDelay() {
        return delay;
    }

    Ticket(String description, int delay) {
        this.description = description;
        this.delay = delay;
    }

    public List<TestDaemon.Step> getSequence() {
        switch (this) {
            case _7132:
                return new ArrayList<>(Arrays.asList(TestDaemon.Step.IDLE, TestDaemon.Step.delay, TestDaemon.Step.connect, TestDaemon.Step.getInfo, TestDaemon.Step.disconnect));

            case _6928:
                return new ArrayList<>(Arrays.asList(TestDaemon.Step.IDLE, TestDaemon.Step.delay, TestDaemon.Step.connect, TestDaemon.Step.getInfo, TestDaemon.Step.installForLoadKay, TestDaemon.Step.disconnect));

            default:
                return null;
        }
    }
}
