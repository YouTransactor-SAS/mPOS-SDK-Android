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

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.youTransactor.uCube.rpc.OnlinePinBlockFormatType;
import com.youtransactor.sampleapp.test.TestPinSession;
import com.youtransactor.sampleapp.transactionView.TransactionViewBasePinTest;

public class OnlinePinTestActivity extends TransactionViewBasePinTest {

    private Button startBtn;
    private boolean testOngoing = false;
    private boolean testStarted = false;
    private static int TIME_BETWEEN_ONLINE_PIN = 3000;
    private Spinner onlinePinBlockFormatChoice;
    private TestPinSession testPinSessionInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setHomeActivity(this.getClass());
        setContentView(R.layout.activity_online_pin_test);
        startBtn = findViewById(R.id.startOnlinePinTestButton);
        startBtn.setOnClickListener(v -> {
            startTest();
            testStarted = true;
        });
        onlinePinBlockFormatChoice = findViewById(R.id.onlinePinBlockFormatTestChoice);
        onlinePinBlockFormatChoice.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                OnlinePinBlockFormatType.values()
        ));
        onlinePinBlockFormatChoice.setSelection(1);
        // Todo: remove when all format are supported
        onlinePinBlockFormatChoice.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        testStarted = false;
        startBtn.setClickable(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (testOngoing && !testStarted) startTest();
            }
        }, TIME_BETWEEN_ONLINE_PIN);
    }

    private void startTest() {
        startBtn.setClickable(false);
        testOngoing = true;
        startBtn.setOnClickListener(v -> stopTest());
        runOnUiThread(() -> startBtn.setText(R.string.stop));
        testPinSessionInstance = new TestPinSession(this, (OnlinePinBlockFormatType) onlinePinBlockFormatChoice.getSelectedItem());
        testPinSessionInstance.setStopTestInterface(new TestPinSession.StopTestInterface() {
            @Override
            public void stopTestSession() {
                stopTest();
            }
        });
        testPinSessionInstance.execute();
    }

    private void stopTest() {
        testStarted = false;
        testOngoing = false;
        startBtn.setOnClickListener(v -> startTest());
        runOnUiThread(() -> startBtn.setText(R.string.start));
    }
}




