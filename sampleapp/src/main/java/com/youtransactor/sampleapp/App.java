package com.youtransactor.sampleapp;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.youTransactor.uCube.api.UCubeAPI;

import io.fabric.sdk.android.Fabric;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Fabric.with(this, new Crashlytics());

        UCubeAPI.init(getApplicationContext());

        //Setup logger : if null lib will use it own logger
        UCubeAPI.setupLogger(this.getApplicationContext(), null);
    }
}
