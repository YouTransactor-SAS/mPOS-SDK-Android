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
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.youTransactor.uCube.rpc.OnlinePinBlockFormatType;
import com.youtransactor.sampleapp.test.TestPinSession;
import com.youtransactor.sampleapp.transactionView.TransactionViewBase;
import com.youtransactor.sampleapp.transactionView.components.TestPinPromptFragment;
import com.youtransactor.sampleapp.transactionView.configuration.TransactionStepHandler;
import com.youtransactor.sampleapp.transactionView.steps.display_integrity_check_warning.DefaultDisplayIntegrityCheckWarningStep;

public class OnlinePinTestActivity extends TransactionViewBase {

    private static final String TAG = OnlinePinTestActivity.class.getSimpleName();

    private Button startBtn;
    private boolean testOngoing = false;
    private boolean testStarted = false;
    private static final int TIME_BETWEEN_ONLINE_PIN = 3000;
    private Spinner onlinePinBlockFormatChoice;
    private TestPinSession testPinSessionInstance;
    private Spinner onlinePINTestSpinner;
    private final String[] keySlotList = {"0x40", "0x41", "0x42", "0x43", "0x44"};

    private final TestPinPromptFragment testPinPromptFragment = new TestPinPromptFragment();

    private final TransactionStepHandler transactionStepHandler = (eventCmd) -> {
        Log.d(TAG, "Event received: " + eventCmd.getEvent().name());
        switch (eventCmd.getEvent()) {
            case ppt_pin:
                this.switchToPinPromptView();
                break;
            case dsp_idle:
                this.switchToTestMenuView();
                break;
            case dsp_integ_check_24h_warning:
                new DefaultDisplayIntegrityCheckWarningStep(this).execute(eventCmd);
                break;
            default:
                break;
        }
    };

    @Override
    protected TransactionStepHandler getTransactionStepHandler() {
        return transactionStepHandler;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        ArrayAdapter<String> keySlotListAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                this.keySlotList
        );
        this.onlinePINTestSpinner = findViewById(R.id.onlinePINTestSpinner);
        keySlotListAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        this.onlinePINTestSpinner.setAdapter(keySlotListAdapter);


    }

    private void switchToPinPromptView() {
        this.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.pin_prompt_container, testPinPromptFragment)
                .commit();
        runOnUiThread(() -> {
            this.findViewById(R.id.test_menu_container).setVisibility(View.GONE);
        });
    }

    private void switchToTestMenuView() {
        this.getSupportFragmentManager()
                .beginTransaction()
                .remove(testPinPromptFragment)
                .commit();
        runOnUiThread(() -> {
            this.findViewById(R.id.test_menu_container).setVisibility(View.VISIBLE);
            this.launchNextTestIfNeeded();
        });
    }

    private void launchNextTestIfNeeded() {
        startBtn.setClickable(true);
        testStarted = false;
        new Handler().postDelayed(() -> {
            if (testOngoing && !testStarted) startTest();
        }, TIME_BETWEEN_ONLINE_PIN);
    }

    private byte hexStringToByte(String hexString) {
        // substring 2: remove 0x
        return (byte) Integer.parseInt(hexString.substring(2), 16);
    }

    private void startTest() {
        startBtn.setClickable(false);
        testOngoing = true;
        startBtn.setOnClickListener(v -> stopTest());
        runOnUiThread(() -> startBtn.setText(R.string.stop));
        testPinSessionInstance = new TestPinSession(this,
                (OnlinePinBlockFormatType) onlinePinBlockFormatChoice.getSelectedItem(),
                hexStringToByte((String) onlinePINTestSpinner.getSelectedItem()));
        testPinSessionInstance.setStopTestInterface(this::stopTest);
        testPinSessionInstance.execute();
    }

    private void stopTest() {
        testStarted = false;
        testOngoing = false;
        startBtn.setOnClickListener(v -> startTest());
        runOnUiThread(() -> startBtn.setText(R.string.start));
    }

    @Override
    protected void onStop() {
        stopTest();
        super.onStop();
    }
}




