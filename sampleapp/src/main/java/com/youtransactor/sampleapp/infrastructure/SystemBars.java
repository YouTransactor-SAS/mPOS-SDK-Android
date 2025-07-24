package com.youtransactor.sampleapp.infrastructure;

import android.content.Context;
import android.content.Intent;

public class SystemBars {

    private final static String SECURE_SERVICE_PACKAGE = "com.jps.secureService";
    private final static String ACTION_SHOW_SYSTEM_BARS = "com.jps.secureService.SHOW_SYSTEM_BARS";
    private final static String ACTION_HIDE_SYSTEM_BARS = "com.jps.secureService.HIDE_SYSTEM_BARS";

    private final Context context;

    public SystemBars(Context context) {
        this.context = context;
    }

    public void enable() {
        Intent showIntent = new Intent();
        showIntent.setAction(ACTION_SHOW_SYSTEM_BARS);
        showIntent.setPackage(SECURE_SERVICE_PACKAGE);
        context.startService(showIntent);
    }

    public void disable() {
        Intent hideIntent = new Intent();
        hideIntent.setAction(ACTION_HIDE_SYSTEM_BARS);
        hideIntent.setPackage(SECURE_SERVICE_PACKAGE);
        context.startService(hideIntent);
    }

}
