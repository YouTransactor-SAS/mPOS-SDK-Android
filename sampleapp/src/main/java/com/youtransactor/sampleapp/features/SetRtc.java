package com.youtransactor.sampleapp.features;

import android.util.Log;

import com.youTransactor.uCube.connexion.IConnexionManager;
import com.youTransactor.uCube.rpc.command.EnterSecureSessionCommand;
import com.youTransactor.uCube.rpc.command.ExitSecureSessionCommand;
import com.youTransactor.uCube.rpc.command.RTCSetCommand;

import java.sql.Date;
import java.time.Instant;
import java.util.function.Consumer;

public class SetRtc {
    public enum FlowStatus { SUCCESS, FAILED }

    private Consumer<FlowStatus> callback;
    private Instant timeToSet;
    private FlowStatus status;

    public SetRtc() {}

    @Deprecated(forRemoval = true)
    public SetRtc(final IConnexionManager connexionManager) {
        this();
    }

    public void execute(final Instant timeToSet, final Consumer<FlowStatus> callback) {
        Log.d(SetRtc.class.getSimpleName(), "Entering secure session");
        this.timeToSet = timeToSet;
        this.callback = callback;
        enterSecureSession();
    }

    private void enterSecureSession() {
        new EnterSecureSessionCommand().execute((event, unused) -> {
            Log.d(SetRtc.class.getSimpleName(), String.format("EnterSecureSessionCommand event: %s", event));
            switch (event) {
                case PROGRESS:
                    return;
                case FAILED:
                case CANCELLED:
                    Log.e(SetRtc.class.getSimpleName(), String.format("Enter Secure Session status: %s", event));
                    callback.accept(FlowStatus.FAILED);
                    break;
                case SUCCESS:
                    setRtc(timeToSet, callback);
                    break;
            }
        });
    }

    private void setRtc(final Instant timeToSet, final Consumer<FlowStatus> callback) {
        Log.i(SetRtc.class.getSimpleName(), "Setting RTC to " + timeToSet.toString());
        new RTCSetCommand(Date.from(timeToSet)).execute((event, unused) -> {
            Log.d(SetRtc.class.getSimpleName(), String.format("RTCSetCommand event: %s", event));
            switch (event) {
                case PROGRESS:
                    return;
                case FAILED:
                case CANCELLED:
                    Log.e(SetRtc.class.getSimpleName(), String.format("Setting RTC status:%s", event));
                    status = FlowStatus.FAILED;
                    break;
                case SUCCESS:
                    status = FlowStatus.SUCCESS;
                    break;
            }
            exitSecureSession();
        });
    }

    private void exitSecureSession() {
        Log.i(SetRtc.class.getSimpleName(), "Exitting secure session");
        new ExitSecureSessionCommand().execute((event, unused) -> {
            Log.d(SetRtc.class.getSimpleName(), String.format("ExitSecureSessionCommand event: %s", event));
            switch (event) {
                case PROGRESS:
                    return;
                case FAILED:
                case CANCELLED:
                    Log.e(SetRtc.class.getSimpleName(), String.format("Exit Secure Session status: %s", event));
                    status = FlowStatus.FAILED;
                    break;
            }
            callback.accept(status);
        });
    }

}
