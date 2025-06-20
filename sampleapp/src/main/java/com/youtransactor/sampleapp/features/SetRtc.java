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
    public enum FlowStatus {
        SUCCESS_REBOOT_NEEDED,
        FAILED,
    }

    private final Disconnect disconnect;

    public SetRtc(final IConnexionManager connexionManager) {
        this.disconnect = new Disconnect(connexionManager);
    }

    public void execute(final Instant timeToSet, final Consumer<FlowStatus> callback) {
        Log.d(SetRtc.class.getSimpleName(), "Entering secure session");
        final EnterSecureSessionCommand command = new EnterSecureSessionCommand();

        command.execute((event, unused) -> {
            Log.d(SetRtc.class.getSimpleName(), String.format("EnterSecureSessionCommand event: %s", event));
            switch (event) {
                case PROGRESS:
                    return;
                case FAILED:
                case CANCELLED:
                    Log.e(SetRtc.class.getSimpleName(), String.format("Enter Secure Session %s", event));
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
        RTCSetCommand rtcSetCommand = new RTCSetCommand(Date.from(timeToSet));

        rtcSetCommand.execute((event, unused) -> {
            Log.d(SetRtc.class.getSimpleName(), String.format("RTCSetCommand event: %s", event));
            switch (event) {
                case FAILED:
                case CANCELLED:
                    Log.e(SetRtc.class.getSimpleName(), String.format("Setting RTC %s", event));
                    callback.accept(FlowStatus.FAILED);
                    break;
                case SUCCESS:
                    exitSecureSession(callback);
                    break;
            }
        });
    }

    private void exitSecureSession(final Consumer<FlowStatus> callback) {
        Log.d(SetRtc.class.getSimpleName(), "Exiting secure session");
        final ExitSecureSessionCommand command = new ExitSecureSessionCommand();
        command.execute((event, unused) -> {
            Log.d(SetRtc.class.getSimpleName(), String.format("ExitSecureSessionCommand event: %s", event));
            switch (event) {
                case FAILED:
                case CANCELLED:
                    Log.e(SetRtc.class.getSimpleName(), String.format("Exit Secure Session %s", event));
                    callback.accept(FlowStatus.FAILED);
                    break;
                case SUCCESS:
                    this.disconnect(callback);
                    break;
            }
        });
    }

    private void disconnect(final Consumer<FlowStatus> callback) {
        Log.d(SetRtc.class.getSimpleName(), "Disconnecting after setting RTC");
        disconnect.execute((disconnectionStatus) -> callback.accept(FlowStatus.SUCCESS_REBOOT_NEEDED));
    }
}
