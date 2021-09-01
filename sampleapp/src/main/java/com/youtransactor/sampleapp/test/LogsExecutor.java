package com.youtransactor.sampleapp.test;

import android.content.Context;

import com.youTransactor.uCube.Tools;
import com.youTransactor.uCube.api.UCubeAPI;
import com.youTransactor.uCube.api.UCubeLibRpcSendListener;
import com.youTransactor.uCube.log.LogManager;
import com.youTransactor.uCube.rpc.RPCCommand;
import com.youTransactor.uCube.rpc.RPCCommandStatus;
import com.youTransactor.uCube.rpc.SecurityMode;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static com.youTransactor.uCube.rpc.Constants.ENTER_SECURED_SESSION;
import static com.youTransactor.uCube.rpc.Constants.EXIT_SECURED_SESSION;
import static com.youTransactor.uCube.rpc.Constants.*;
import static com.youTransactor.uCube.rpc.SecurityMode.*;

public class LogsExecutor {

    private enum RPCStatus {
        ADisconnectionAtCommand,
        commandResponseDidReceived,
        UNKNOWN,
    }

    private static final String RPC_COMMAND_LOG_IDENTIFIER = "sent message: ";
    private static final String DISCONNECTION_LOG_IDENTIFIER = "A disconnection at command";
    private static final String RPC_RECEIVED_RESPONSE_IDENTIFIER = "onCharacteristicChanged - Receiving data on TxRx...";

    private static boolean secureSession = false;

    private  static Map<RPCCommand, RPCStatus> logs;
    private  static ArrayList<RPCCommand> rpcCommands;
    private static LinkedList<RPCCommand> remainRpcCommand;

    public static void runLogs(Context context) {
        FileInputStream fis;

        try {
            fis = context.openFileInput("logs.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            LogManager.e("getting logs file exception ", e);
            return;
        }

        logs = new HashMap<>();
        rpcCommands = new ArrayList<>();
        try {
            LogsExecutor.parseLogs(fis);
        } catch (Exception e) {
            e.printStackTrace();
            LogManager.e("exception at logs parsing ", e);
        }

        if(logs.isEmpty() || rpcCommands.isEmpty()) {
            LogManager.e("parsing result is an empty list of commands");
            return;
        }

        remainRpcCommand = new LinkedList<>(rpcCommands);
        executeRpc(context);

    }

    private static void executeRpc(Context context) {

        final RPCCommand cmd = remainRpcCommand.poll();
        if(cmd == null) {
            LogManager.e("Error! Command null");
            return;
        }

        UCubeAPI.sendData(cmd.getCommandId(), cmd.getPayload(), cmd.getInputSecurityMode(), cmd.getOutputSecurityMode(), new UCubeLibRpcSendListener() {
            @Override
            public void onProgress(RPCCommandStatus rpcCommandStatus) {
                if (rpcCommandStatus == RPCCommandStatus.SENT) {
                    RPCStatus status = logs.get(cmd);
                    if (status == null)
                        return;

                    if (status == RPCStatus.ADisconnectionAtCommand) {
                        LogManager.e("call Disconnect");
                        UCubeAPI.getConnexionManager().disconnect(status1 -> {
                            UCubeAPI.init(context.getApplicationContext());
                            UCubeAPI.setupLogger(null);
                        });
                    }
                }
            }

            @Override
            public void onFinish(boolean status, byte[] response) {
                LogManager.d("RPC Command status " + status);
                if (!remainRpcCommand.isEmpty()) {
                    executeRpc(context);
                }else {
                    LogManager.d("END OF FILE !");
                }
            }
        });

    }

    private static void parseLogs(FileInputStream fis) throws Exception {

        RPCCommand cmd;
        RPCStatus status;

        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
        String line = reader.readLine();
        while(line != null){
            if(line.contains(RPC_COMMAND_LOG_IDENTIFIER)) {
                String message = line.substring(line.lastIndexOf(RPC_COMMAND_LOG_IDENTIFIER) + RPC_COMMAND_LOG_IDENTIFIER.length()).replaceAll(" ", "");
                LogManager.d( "message to parse " + message);

                byte[] buffer = Tools.hexStringToByteArray(message);
                cmd  = getRPCCommand(buffer);
                if(cmd == null)
                    throw new Exception("Error to parse this line " + line);

                rpcCommands.add(cmd);
                LogManager.d("command ID: 0x" + Integer.toHexString(cmd.getCommandId()));
                LogManager.d("command data: 0x" + Tools.bytesToHex(cmd.getPayload()));

                // get command status
                line = reader.readLine();
                while (line != null) {
                    if(line.contains(RPC_COMMAND_LOG_IDENTIFIER)) {
                        LogManager.d("command status : UNKNOWN");
                        status = RPCStatus.UNKNOWN;
                        logs.put(cmd, status);
                        break;
                    }
                    if(line.contains(DISCONNECTION_LOG_IDENTIFIER)) {
                        LogManager.d("command status : disconnect");
                        status = RPCStatus.ADisconnectionAtCommand;
                        secureSession = false;
                        logs.put(cmd, status);
                        break;
                    } else if(line.contains(RPC_RECEIVED_RESPONSE_IDENTIFIER)) {
                        LogManager.d("command status : response received");
                        status = RPCStatus.commandResponseDidReceived;
                        logs.put(cmd, status);
                        break;
                    }
                    line = reader.readLine();
                }
            } else
                line = reader.readLine();
        }
    }

    private static RPCCommand getRPCCommand(byte[] buffer) {
        if(buffer == null)
            return null;

        if(buffer.length < 4 + 3 + 2) {
            LogManager.e("buffer size < RPC_HEADER_LEN + RPC_FOOTER_LEN + RPC_CMD_ID_LEN");
            return null;
        }

        byte[] data = new byte[0];

        /* offset start after STX */
        int offset = 1;

        int payloadLength = Tools.makeShort(buffer[offset++], buffer[offset++]);

        offset++; // sequence number

        short commandId = Tools.makeShort(buffer[offset++], buffer[offset++]);

        if (commandId == ENTER_SECURED_SESSION) {
            secureSession = true;
        } else if (commandId == EXIT_SECURED_SESSION) {
            secureSession = false;
        }

        if(payloadLength > 0) {
            if(!secureSession) {
                data = Arrays.copyOfRange(buffer, offset, offset + payloadLength);
            } else {
                offset++; //crypto header

                if (payloadLength > RPC_SRED_MAC_SIZE + RPC_SECURED_HEADER_CRYPTO_RND_LEN) {
                    data = Arrays.copyOfRange(buffer, offset, offset + (payloadLength - RPC_SRED_MAC_SIZE - RPC_SECURED_HEADER_CRYPTO_RND_LEN));
                }
            }
        }

        SecurityMode inputSecurityMode = getInputSecurityMode(commandId);
        SecurityMode outputSecurityMode = getOuputSecurityMode(commandId);

        RPCCommand cmd = new RPCCommand(commandId, inputSecurityMode, outputSecurityMode);
        cmd.setPayload(data);

        return cmd;

    }

    private static SecurityMode getInputSecurityMode(short rpcCommandId) {

        switch (rpcCommandId) {
            case CANCEL_COMMAND:
            case ENTER_SECURED_SESSION:
            case EXIT_SECURED_SESSION:
            case SET_INFO_FIELD_COMMAND:
            case CARD_WAIT_INSERTION_COMMAND:
            case CARD_WAIT_REMOVAL_COMMAND:
            case INSTALL_FOR_LOAD_KEY_COMMAND:
            case POWER_OFF:
                return NONE;

            case INSTALL_FOR_LOAD_COMMAND:
            case LOAD_COMMAND:
            case SIMPLIFIED_ONLINE_PIN:
            case BUILD_CANDIDATE_LIST:
            case GET_PLAIN_TAG_VALUE_COMMAND:
            case DISPLAY_WITHOUT_KI_COMMAND:
            case DISPLAY_LISTBOX_WITHOUT_KI_COMMAND:
            case TRANSACTION_PROCESS:
            case TRANSACTION_FINAL:
            case START_NFC_TRANSACTION:
            case COMPLETE_NFC_TRANSACTION:
            case GET_INFO_COMMAND:
            case TRANSACTION_INIT:
            case GET_SECURED_TAG_VALUE_COMMAND:
                return SIGNED_NOT_CHECKED;

            case BANK_PARAMETERS_DOWNLOADS:
                return SIGNED;
        }

        return NONE;

    }

    private static SecurityMode getOuputSecurityMode(short rpcCommandId) {
        switch (rpcCommandId) {
            case CANCEL_COMMAND:
            case ENTER_SECURED_SESSION:
            case EXIT_SECURED_SESSION:
            case SET_INFO_FIELD_COMMAND:
            case CARD_WAIT_INSERTION_COMMAND:
            case CARD_WAIT_REMOVAL_COMMAND:
            case INSTALL_FOR_LOAD_KEY_COMMAND:
            case POWER_OFF:
                return NONE;

            case GET_INFO_COMMAND:
            case TRANSACTION_INIT:
            case GET_SECURED_TAG_VALUE_COMMAND:
                return SIGNED_CIPHERED;

            case INSTALL_FOR_LOAD_COMMAND:
            case LOAD_COMMAND:
            case SIMPLIFIED_ONLINE_PIN:
            case BUILD_CANDIDATE_LIST:
            case GET_PLAIN_TAG_VALUE_COMMAND:
            case DISPLAY_WITHOUT_KI_COMMAND:
            case DISPLAY_LISTBOX_WITHOUT_KI_COMMAND:
            case TRANSACTION_PROCESS:
            case TRANSACTION_FINAL:
            case START_NFC_TRANSACTION:
            case COMPLETE_NFC_TRANSACTION:
            case BANK_PARAMETERS_DOWNLOADS:
                return SIGNED;
        }

        return NONE;
    }
}
