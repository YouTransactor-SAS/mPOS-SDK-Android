package com.youtransactor.sampleapp.test;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.LinkedList;
import java.util.List;

import static com.youtransactor.sampleapp.test.Tools.*;

public class TestDaemon implements Runnable {
    public static final String TAG = TestDaemon.class.getName();

    public enum Step{
        IDLE,
        connect,
        getInfo,
        load,
        transaction,
        powerOff,
        disconnect
    }

    int counter = 0;
    private int delayToStart;
    private int numberOfRuns;
    private boolean interrupted = false;
    private List<Step> sequence;
    private LinkedList<Step> remainSequence;

    public TestDaemon(@NonNull Ticket ticket, int delayToStart, int numberOfRuns) {
        this.delayToStart = delayToStart;
        this.numberOfRuns = numberOfRuns;
        this.sequence = ticket.getSequence();
    }

    public void end() {
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
            Log.e(TAG, "error step is null");
            return;
        }

        switch (currentStep) {
            case IDLE:

                if(counter == numberOfRuns) {
                    end();
                    return;
                }

                counter++;

                Log.d(TAG, "Test number "+ counter);

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
                connect(status -> {
                    if(status)
                        loop();
                    else
                        end();
                });
                break;

            case load:
                load(status -> {
                    if(status)
                        loop();
                    else
                        end();
                });
                break;

            case getInfo:
                getInfo(status -> {
                    if(status)
                        loop();
                    else
                        end();
                });
                break;

            case powerOff:
                powerOff(status -> {
                    if(status)
                        loop();
                    else
                        end();
                });
                break;

            case transaction:
                transaction(status -> {
                    if(status)
                        loop();
                    else
                        end();
                });
                break;

            case disconnect:
                disconnect(status -> {
                    if(status)
                        loop();
                    else
                        end();
                });
                break;
        }
    }

}
