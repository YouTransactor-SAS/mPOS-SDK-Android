package com.youtransactor.sampleapp.features;

import android.util.Log;

import com.youTransactor.uCube.connexion.IConnexionManager;
import com.youTransactor.uCube.rpc.command.integrity_check.SetIntegrityCheckTimeCommand;

import java.time.LocalTime;
import java.util.function.Consumer;

public class SetIntegrityCheckTime {
    public enum FlowStatus {
        SUCCESS_REBOOT_NEEDED,
        FAILED
    }

    private final Disconnect disconnect;

    public SetIntegrityCheckTime(final IConnexionManager connexionManager) {
        this.disconnect = new Disconnect(connexionManager);
    }
    public void execute(final LocalTime timeToSet, final Consumer<FlowStatus> callback) {
        Log.i(SetIntegrityCheckTime.class.getSimpleName(), "Setting integrity check time to " + timeToSet.toString());
        SetIntegrityCheckTimeCommand command = new SetIntegrityCheckTimeCommand(timeToSet);

        command.execute((event, unused) -> {
            Log.d(SetIntegrityCheckTime.class.getSimpleName(), String.format("SetIntegrityCheckTime event: %s", event));
            switch (event) {
                case FAILED:
                case CANCELLED:
                    Log.e(SetIntegrityCheckTime.class.getSimpleName(), String.format("Setting integrity check time %s", event));
                    callback.accept(FlowStatus.FAILED);
                    break;
                case SUCCESS:
                    this.disconnect(callback);
                    break;
            }
        });
    }

    private void disconnect(final Consumer<SetIntegrityCheckTime.FlowStatus> callback) {
        Log.d(SetRtc.class.getSimpleName(), "Disconnecting after setting RTC");
        disconnect.execute((disconnectionStatus) -> callback.accept(SetIntegrityCheckTime.FlowStatus.SUCCESS_REBOOT_NEEDED));
    }
}
