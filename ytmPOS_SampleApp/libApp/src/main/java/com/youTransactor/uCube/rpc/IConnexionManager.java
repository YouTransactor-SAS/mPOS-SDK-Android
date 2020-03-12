/*
 * Copyright (C) 2016, YouTransactor. All Rights Reserved.
 *
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youTransactor.uCube.rpc;

public interface IConnexionManager {

	boolean isConnected();

	void connect(ConnectionListener connectionListener);

	void send(byte[] input, SendCommandListener sendCommandListener);

	void stop();

	void registerDisconnectListener(DisconnectListener disconnectListener);

	void unregisterDisconnectListener();

}
