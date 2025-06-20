package com.youtransactor.sampleapp.features;

import com.youTransactor.uCube.connexion.IConnexionManager;

import java.util.function.Consumer;

public class Disconnect {

    public enum DisconnectionStatus {
        CONNECTED,
        DISCONNECTED,
    }

    private final IConnexionManager connexionManager;

    public Disconnect(IConnexionManager connexionManager) {
        this.connexionManager = connexionManager;
    }

    public void execute(Consumer<DisconnectionStatus> callback) {
        if (!connexionManager.isConnected()) {
            callback.accept(DisconnectionStatus.DISCONNECTED);
            return;
        }
        connexionManager.disconnect((success) -> {
            callback.accept(success ? DisconnectionStatus.DISCONNECTED : DisconnectionStatus.CONNECTED);
        });
    }
}
