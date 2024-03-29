/*
 * Copyright (C) 2011-2021, YouTransactor. All Rights Reserved.
 *
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youtransactor.sampleapp.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum Ticket {
    _7132("IDLE -> DELAY -> CONNECT -> GetInfo -> DISCONNECT \nrecommended delay : 1000 ms", 1000),
    _6928("IDLE -> DELAY -> CONNECT -> GetInfo -> InstallForLoadKey -> DISCONNECT \nrecommended delay : 0 ms", 0),
    _7300("exitSecureSession -> displayMessage -> exitSecureSession -> exitSecureSession -> enterSecureSession", 0),
    _7238("exitSecureSession -> Delay -> exitSecureSession -> enterSecureSession -> getInfo -> exitSecureSession", 100)
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

            case _7300:
                return new ArrayList<>(Arrays.asList(TestDaemon.Step.exitSecureSession, TestDaemon.Step.displayMessage,
                        TestDaemon.Step.exitSecureSession, TestDaemon.Step.exitSecureSession,
                        TestDaemon.Step.enterSecureSession));
            case _7238:
                return new ArrayList<>(Arrays.asList(TestDaemon.Step.exitSecureSession, TestDaemon.Step.delay,
                        TestDaemon.Step.exitSecureSession, TestDaemon.Step.enterSecureSession,
                        TestDaemon.Step.getCB, TestDaemon.Step.exitSecureSession));

            default:
                return null;
        }
    }
}
