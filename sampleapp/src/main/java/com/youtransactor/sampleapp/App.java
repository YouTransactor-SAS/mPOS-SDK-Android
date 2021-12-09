/*
 * Copyright (C) 2011-2021, YouTransactor. All Rights Reserved.
 *
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youtransactor.sampleapp;

import androidx.multidex.MultiDexApplication;
import com.youTransactor.uCube.api.UCubeAPI;

public class App extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        UCubeAPI.init(getApplicationContext());

        //Setup logger : if null lib will use it own logger
        UCubeAPI.setupLogger(null);
    }
}
