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
package com.youtransactor.sampleapp;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDexApplication;

import com.youTransactor.uCube.api.UCubeAPI;

public class App extends MultiDexApplication {

    private Activity currentActivity = null;

    @Override
    public void onCreate() {
        super.onCreate();

        UCubeAPI.init(getApplicationContext());

        //Setup logger : if null lib will use it own logger
        UCubeAPI.setupLogger(null);
        registerActivityLifecycleCallbacks(this.activityLifecycleCallbacks());
    }

    private ActivityLifecycleCallbacks activityLifecycleCallbacks() {
        return new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityResumed(@NonNull final Activity activity) {
                currentActivity = activity;
            }

            @Override
            public void onActivityPaused(@NonNull final Activity activity) {
                if (currentActivity == activity) currentActivity = null;
            }

            @Override
            public void onActivityCreated(@NonNull final Activity activity, final Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(@NonNull final Activity activity) {
            }

            @Override
            public void onActivityStopped(@NonNull final Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull final Activity activity, @NonNull final Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(@NonNull final Activity activity) {
            }
        };
    }

}
