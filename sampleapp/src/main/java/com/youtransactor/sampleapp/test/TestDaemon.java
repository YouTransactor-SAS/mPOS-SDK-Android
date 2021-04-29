package com.youtransactor.sampleapp.test;

import android.util.Log;

import androidx.annotation.NonNull;

import com.youTransactor.uCube.log.LogManager;

import java.util.LinkedList;
import java.util.List;

import static com.youtransactor.sampleapp.test.Tools.*;

public class TestDaemon implements Runnable {
    public static final String TAG = TestDaemon.class.getName();

    public enum Step{
        IDLE,
        delay,
        connect,
        getInfo,
        installForLoadKay,
        installForLoad,
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

    private Listener finishListener = new Listener() {
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

    private Listener sentListener = new Listener() {
        @Override
        public void onSent() {
            loop();
        }

        @Override
        public void onFinish(boolean status) {//ignore
        }
    };

    public TestDaemon(@NonNull Ticket ticket, int delayToStart, int numberOfRuns) {
        this.delayToStart = delayToStart;
        this.numberOfRuns = numberOfRuns;
        this.sequence = ticket.getSequence();
    }

    public void end() {
        LogManager.d("Test daemon end");
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
            LogManager.e("error step is null");
            return;
        }

        LogManager.d("Test daemon Step : " + currentStep);

        switch (currentStep) {
            case IDLE:
                if(counter == numberOfRuns) {
                    end();
                    return;
                }

                counter++;
                LogManager.d("Test number "+ counter);

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
        }
    }

}
