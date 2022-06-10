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

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.LinkedList;
import java.util.List;

import static com.youtransactor.sampleapp.test.Tools.*;
import static com.youtransactor.sampleapp.test.Tools.installForLoad;
import static com.youtransactor.sampleapp.test.Tools.installForLoadKey;

public class TestDaemon implements Runnable {
    public static final String TAG = TestDaemon.class.getName();

    public enum Step{
        IDLE,
        delay,
        connect,
        getInfo,
        getCB,
        installForLoadKay,
        installForLoad,
        load,
        transaction,
        powerOff,
        disconnect,
        exitSecureSession,
        enterSecureSession,
        displayMessage,
    }

    int counter = 0;
    private final int delayToStart;
    private final int numberOfRuns;
    private boolean interrupted = false;
    private final List<Step> sequence;
    private LinkedList<Step> remainSequence;

    private final Listener finishListener = new Listener() {
        @Override
        public void onSent() {//ignore
        }

        @Override
        public void onFinish(boolean status) {
            if(status)
                loop();
            else
                end();
        }
    };

    private final Listener sentListener = new Listener() {
        @Override
        public void onSent() {
            loop();
        }

        @Override
        public void onFinish(boolean status) {//ignore
        }
    };

    public TestDaemon(@NonNull Test test, int delayToStart, int numberOfRuns) {
        this.delayToStart = delayToStart;
        this.numberOfRuns = numberOfRuns;
        this.sequence = test.getSequence();
    }

    public void end() {
        Log.d(TAG,"Test daemon end");
        interrupted = true;
    }

    @Override
    public void run() {
        counter = 0;

        if(sequence == null || sequence.isEmpty())
            return;

        loop();
    }

    private void loop() {
        if (interrupted)
            return;

        if(remainSequence == null || remainSequence.isEmpty()) {
            //pass to next run
            remainSequence = new LinkedList<>(sequence);
            loop();
            return;
        }

        Step currentStep = remainSequence.poll();
        if(currentStep == null) {
            Log.e(TAG,"error step is null");
            return;
        }

        Log.d(TAG,"Test daemon Step : " + currentStep);

        switch (currentStep) {
            case IDLE:
                if(counter == numberOfRuns) {
                    end();
                    return;
                }

                counter++;
                Log.d(TAG,"Test number "+ counter);

                loop();
                break;

            case delay:
                if (delayToStart > 0) {
                    try {
                        Thread.sleep(delayToStart);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        end();
                        return;
                    }
                }

                loop();
                break;

            case connect:
                connect(finishListener);
                break;

            case getInfo:
                getInfo(finishListener);
                break;

            case installForLoadKay:
                installForLoadKey(sentListener);
                break;

            case installForLoad:
                installForLoad(finishListener);
                break;

            case load:
                load(finishListener);
                break;

            case powerOff:
                powerOff(finishListener);
                break;

            case transaction:
                transaction(finishListener);
                break;

            case disconnect:
                disconnect(finishListener);
                break;

            case exitSecureSession:
                exitSecureSession(finishListener);
                break;

            case enterSecureSession:
                enterSecureSession(finishListener);
                break;

            case displayMessage:
                displayMessage(finishListener);
                break;

            case getCB:
                getCB(finishListener);
                break;
        }
    }

}
