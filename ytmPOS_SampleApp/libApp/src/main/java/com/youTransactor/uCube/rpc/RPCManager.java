/*
 * Copyright (C) 2011-2016, YouTransactor. All Rights Reserved.
 *
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youTransactor.uCube.rpc;

import com.youTransactor.uCube.LogManager;
import com.youTransactor.uCube.Tools;
import com.youTransactor.uCube.rpc.command.EnterSecureSessionCommand;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

/**
 * @author gbillard on 3/11/16.
 */
public class RPCManager extends Observable {
	public enum Status {
		ERROR,
		CONNECTED,
		DISCONNECTED
	}

	public class NotifyMessage {
		public Status status;

		NotifyMessage(Status status) {
			this.status = status;
		}
	}

	private IConnexionManager connexionManager;

	private SendCommandListener sendCommandListener = new SendCommandListener() {
		@Override
		public void onSuccess(byte[] responseData) {
			processReceivedData(responseData);
		}

		@Override
		public void onError(Exception e) {
			commandFailed(e);
		}
	};

	private boolean secureSession = false;

	private byte sequenceNumber = 0;

	private RPCCommand currentCommand;

	private Map<Short, IRPCMessageHandler> messageHandlerByCommandId = new HashMap<>();

	private RPCManager() {}

	private void notifyStatus(Status status) {
		setChanged();
		notifyObservers(new NotifyMessage(status));
	}

	public synchronized void send(RPCCommand command) {

		if(connexionManager == null) {
			LogManager.e("Error! connexion manager null, unable to connect to device");

			command.setState(RPCCommandStatus.CONNECT_ERROR);

			notifyStatus(Status.ERROR);

			return;
		}

		if(connexionManager.isConnected()) {

			INSTANCE.sendCommand(command);

			return;
		}

		connexionManager.connect(new ConnectionListener() {
			@Override
			public void onConnect() {

				if (secureSession) {
					new EnterSecureSessionCommand().execute(null);
				}

				notifyStatus(Status.CONNECTED);

				sendCommand(command);

				connexionManager.registerDisconnectListener(INSTANCE::stop);
			}

			@Override
			public void onError(Exception e) {
				LogManager.e("Error to connect to device", e);

				command.setState(RPCCommandStatus.CONNECT_ERROR);

				notifyStatus(Status.ERROR);
			}
		});
	}

	public void stop() {

		connexionManager.stop();

		connexionManager.unregisterDisconnectListener();

		for (IRPCMessageHandler handler : messageHandlerByCommandId.values()) {
			handler.processMessage(null);
		}

		messageHandlerByCommandId.clear();

		notifyStatus(Status.DISCONNECTED);
	}

	public void connect(ConnectionListener connectionListener) {

		if(connexionManager == null) {
			connectionListener.onError(new IllegalStateException("Error, connexion manager null, unable to connect to device"));
			return;
		}

		if(connexionManager.isConnected()) {
			connectionListener.onConnect();
			return;
		}

		connexionManager.connect(connectionListener);
	}

	public void setConnexionManager(IConnexionManager connexionManager) {
		this.connexionManager = connexionManager;
	}

	private void sendCommand(RPCCommand command) {
		LogManager.d("send command ID: 0x" + Integer.toHexString(command.getCommandId()));
		LogManager.d("send command data: 0x" + Tools.bytesToHex(command.getPayload()));

		messageHandlerByCommandId.put(command.getCommandId(), command);
		currentCommand = command;

		command.setState(RPCCommandStatus.SENDING);

		try {
			/* reset sequence number before entering in secure session */
			if(command.getCommandId() == Constants.ENTER_SECURED_SESSION) {
				sequenceNumber = 0;
			}

			if (secureSession && (command.getCommandId() != Constants.EXIT_SECURED_SESSION))
				sendSecureCommand(command);
			else
				sendInsecureCommand(command);

			command.setState(RPCCommandStatus.SENT);

		} catch (Exception e) {
			commandFailed(e);
		}
	}

	private void sendSecureCommand(RPCCommand command) throws IOException {
		byte[] payload = command.getPayload();

		if (payload.length == 0) {
			sendInsecureCommand(command);
			return;
		}

        byte[] message = new byte[payload.length + Constants.RPC_SECURED_HEADER_LEN + Constants.RPC_SRED_MAC_SIZE];
		int securedLen = payload.length + Constants.RPC_SECURED_HEADER_CRYPTO_RND_LEN + Constants.RPC_SRED_MAC_SIZE;

        int offset = 0;

        message[offset++] = (byte) (securedLen / 0x100);
        message[offset++] = (byte) (securedLen % 0x100);
        message[offset++] = sequenceNumber++;
        message[offset++] = (byte) (command.getCommandId() / 0x100);
        message[offset++] = (byte) (command.getCommandId() % 0x100);

        message[offset++] = (byte) 0x7F; // TODO should be random

        System.arraycopy(payload, 0, message, offset, payload.length);
        offset += payload.length;

		/* Padding the last 4 bytes with 0x00 (SRED OPT) */
		for (int i = 0; i < Constants.RPC_SRED_MAC_SIZE; i++){
            message[offset++] = 0x00;
		}

        int crc = computeChecksumCRC16(message);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
		outputStream.write( Constants.STX );
		outputStream.write( message );
		outputStream.write( (byte) (crc / 0x100) );
		outputStream.write( (byte) (crc % 0x100) );
		outputStream.write( Constants.ETX );

		byte[] data = outputStream.toByteArray();

		connexionManager.send(data, sendCommandListener);

		LogManager.d("sent message: " + Tools.bytesToHex(data));
	}

	private void sendInsecureCommand(RPCCommand command) throws IOException {
		byte[] payload = command.getPayload();

		byte[] message = new byte[payload.length + Constants.RPC_HEADER_LEN];
		int offset = 0;

		message[offset++] = (byte) (payload.length / 0x100);
		message[offset++] = (byte) (payload.length % 0x100);
		message[offset++] = sequenceNumber++;
		message[offset++] = (byte) (command.getCommandId() / 0x100);
		message[offset++] = (byte) (command.getCommandId() % 0x100);

		System.arraycopy(payload, 0, message, offset, payload.length);

		int crc = computeChecksumCRC16(message);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
		outputStream.write( Constants.STX );
		outputStream.write( message );
		outputStream.write( (byte) (crc / 0x100) );
		outputStream.write( (byte) (crc % 0x100) );
		outputStream.write( Constants.ETX );

		byte[] data = outputStream.toByteArray();

		connexionManager.send(data, sendCommandListener);

		LogManager.d("sent message: " + Tools.bytesToHex(data));

	}

	private void processReceivedData(byte[] bufferToDeliver) {

		short expected_length;
		int bufferToDeliverOffset = bufferToDeliver.length;

		// check header
		// first byte must be STX
		if (bufferToDeliver[0] != Constants.STX) {
			commandFailed(new IllegalStateException("First byte is not STX! (" + bufferToDeliver[0] + ")"));
			return;
		}


		// compute size if at least 3 bytes in buff
		if (bufferToDeliverOffset < 3) {
			commandFailed(new IllegalStateException("buffer size < 3! (" + bufferToDeliverOffset + ")"));
			return;
		}

		expected_length = Tools.makeShort(bufferToDeliver[1], bufferToDeliver[2]);
		expected_length += 1 + 2 + 1 + 2 + 3; //ETX,CRC,STX,CMDID,LENGTH

		if (expected_length > MAX_RPC_PACKET_SIZE) {
			commandFailed(new IllegalStateException("expected_length(" + expected_length + ") > MAX_RPC_PACKET_SIZE(" + MAX_RPC_PACKET_SIZE + ")"));
			return;
		}

		// size must be at least expected one
		if (bufferToDeliverOffset < expected_length) {
			commandFailed(new IllegalStateException("expected_length" + expected_length + " but input length : " + bufferToDeliverOffset));
			return;
		}

		// last byte is etx
		if (bufferToDeliver[expected_length - 1] != Constants.ETX) {
			commandFailed(new IllegalStateException(
					"Last byte is not ETX! (" + bufferToDeliver[expected_length - 1] + ") - " + Tools.bytesToHex(bufferToDeliver)));
			return;
		}

		// checksum concur
		int len = expected_length - 1 - 2; // STX CRC ETX
		byte[] b = Arrays.copyOfRange(bufferToDeliver, 1, len);
		int crc = computeChecksumCRC16(b);
		int check = Tools.makeInt(bufferToDeliver[expected_length - 3], bufferToDeliver[expected_length - 2]);
		if (check != crc) {
			commandFailed(new IllegalStateException("Checksum is failed! " + crc + " expected: " + check));
			return;
		}


		byte[] data = new byte[expected_length];
		System.arraycopy(bufferToDeliver, 0, data, 0, expected_length);

		LogManager.d("received: " + Tools.bytesToHex(data));

		RPCMessage response = new RPCMessage(data, INSTANCE.secureSession);

		INSTANCE.processMessage(response);
	}

	private void processMessage(RPCMessage message) {
		LogManager.d("received command ID: 0x" + Integer.toHexString(message.getCommandId()));
		LogManager.d("received command Status: 0x" + Integer.toHexString(message.getStatus()));
		LogManager.d("received command data: 0x" + Tools.bytesToHex(message.getData()));

		if (message.getStatus() == Constants.SUCCESS_STATUS) {
			if (message.getCommandId() == Constants.ENTER_SECURED_SESSION) {
                /* Set Secure Session State*/
				secureSession = true;
			} else if (message.getCommandId() == Constants.EXIT_SECURED_SESSION) {
				secureSession = false;
			}
		}

		IRPCMessageHandler handler = messageHandlerByCommandId.remove(message.getCommandId());

		if (handler == null && messageHandlerByCommandId.size() == 1) {
			/* command ID is 0x8000 in case of error => unable to recognize command */
			handler = messageHandlerByCommandId.values().iterator().next();
			messageHandlerByCommandId.clear();
		}

		if (handler != null) {
			try {
				handler.processMessage(message);
			} catch (Exception e) {
				commandFailed(e);
			}
		}
	}

	private void commandFailed(Exception e) {
		LogManager.e("Error! to send command : " + Integer.toHexString(currentCommand.getCommandId()), e);

		messageHandlerByCommandId.remove(currentCommand.getCommandId());

		currentCommand.setState(RPCCommandStatus.FAILED);

		notifyStatus(Status.ERROR);
	}

	public static RPCManager getInstance() {
		return INSTANCE;
	}

	/**
	 * this function to calculate the checksum 16bit
	 *
	 * @param bytes the payload data
	 * @return the calculated CRC16
	 */
	private static int computeChecksumCRC16(byte[] bytes) {
		int crc = 0x0000;
		int temp;
		int crc_byte;

		for (byte aByte : bytes) {

			crc_byte = aByte;

			if (crc_byte < 0)
				crc_byte += 256;

			for (int bit_index = 0; bit_index < 8; bit_index++) {

				temp = (crc >> 15) ^ (crc_byte >> 7);

				crc <<= 1;
				crc &= 0xFFFF;

				if (temp > 0) {
					crc ^= 0x1021;
					crc &= 0xFFFF;
				}

				crc_byte <<= 1;
				crc_byte &= 0xFF;

			}
		}

		return crc;
	}

	/**
	 * The size of the biggest packet is the LOAD command and its 2052
	 * 2040(block) + 2(length) + 1(isLastBlock) + 2(command_ID) + 7(RPC Headers ETX,STX,SEQ,CRC,PLL) = 2052
	 */
	public static final int MAX_RPC_PACKET_SIZE = 2068;

	private static final RPCManager INSTANCE = new RPCManager();

}
