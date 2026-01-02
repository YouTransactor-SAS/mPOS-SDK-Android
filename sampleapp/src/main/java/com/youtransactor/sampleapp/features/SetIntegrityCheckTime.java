package com.youtransactor.sampleapp.features;

import android.util.Log;

import com.youTransactor.uCube.TaskEvent;
import com.youTransactor.uCube.connexion.IConnexionManager;
import com.youTransactor.uCube.rpc.command.integrity_check.SetIntegrityCheckTimeCommand;

import java.time.LocalTime;
import java.util.function.Consumer;

public class SetIntegrityCheckTime {

    public enum FlowStatus { SUCCESS, FAILED }

    public SetIntegrityCheckTime() {}

    @Deprecated(forRemoval = true)
    public SetIntegrityCheckTime(final IConnexionManager connexionManager) {
        this();
    }

    public void execute(final LocalTime timeToSet, final Consumer<FlowStatus> callback) {
        Log.i(SetIntegrityCheckTime.class.getSimpleName(), "Setting integrity check time to " + timeToSet.toString());
        new SetIntegrityCheckTimeCommand(timeToSet).execute((event, unused) -> {
            Log.d(SetIntegrityCheckTime.class.getSimpleName(), String.format("SetIntegrityCheckTime event: %s", event));
            if (event == TaskEvent.PROGRESS) return;
            callback.accept(event == TaskEvent.SUCCESS ? FlowStatus.SUCCESS : FlowStatus.FAILED);
        });
    }

}
