/*
 * Copyright (C) 2011-2021, YouTransactor. All Rights Reserved.
 *
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youtransactor.sampleapp.test;

import android.util.Log;

import com.youTransactor.uCube.ITaskMonitor;
import com.youTransactor.uCube.TaskEvent;
import com.youTransactor.uCube.api.UCubeAPI;
import com.youTransactor.uCube.api.UCubeLibPaymentServiceListener;
import com.youTransactor.uCube.api.UCubeLibRpcSendListener;
import com.youTransactor.uCube.api.UCubePaymentRequest;
import com.youTransactor.uCube.connexion.ConnectionListener;
import com.youTransactor.uCube.connexion.ConnectionStatus;
import com.youTransactor.uCube.rpc.CardReaderType;
import com.youTransactor.uCube.payment.PaymentContext;
import com.youTransactor.uCube.payment.PaymentState;
import com.youTransactor.uCube.rpc.TransactionType;
import com.youTransactor.uCube.rpc.Constants;
import com.youTransactor.uCube.rpc.RPCCommandStatus;
import com.youTransactor.uCube.rpc.SecurityMode;
import com.youTransactor.uCube.rpc.command.DisplayMessageCommand;
import com.youTransactor.uCube.rpc.command.EnterSecureSessionCommand;
import com.youTransactor.uCube.rpc.command.ExitSecureSessionCommand;
import com.youTransactor.uCube.rpc.command.GetInfosCommand;
import com.youTransactor.uCube.rpc.command.InstallForLoadKeyCommand;
import com.youTransactor.uCube.rpc.command.LoadCommand;
import com.youTransactor.uCube.rpc.command.PowerOffCommand;
import com.youtransactor.sampleapp.payment.AuthorizationTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.youTransactor.uCube.rpc.Constants.EMVTag.TAG_SECURE_56_TRACK_1_DATA;
import static com.youTransactor.uCube.rpc.Constants.EMVTag.TAG_SECURE_57_TRACK_2_EQUIVALENT_DATA;
import static com.youTransactor.uCube.rpc.Constants.EMVTag.TAG_SECURE_5A_APPLICATION_PRIMARY_ACCOUNT_NUMBER;
import static com.youTransactor.uCube.rpc.Constants.EMVTag.TAG_SECURE_5F24_APPLICATION_EXPIRATION_DATE;
import static com.youTransactor.uCube.rpc.Constants.INSTALL_FOR_LOAD_COMMAND;

public class Tools {
    private static final String TAG = Tools.class.getName();

    interface Listener {
        void onSent();
        void onFinish(boolean status);
    }

    public static void getInfo(Listener listener) {

        final int[] uCubeInfoTagList = {
                Constants.TAG_ATMEL_SERIAL,
                Constants.TAG_TERMINAL_PN,
                Constants.TAG_TERMINAL_SN,
                Constants.TAG_FIRMWARE_VERSION,
                Constants.TAG_EMV_ICC_CONFIG_VERSION,
                Constants.TAG_EMV_NFC_CONFIG_VERSION,
                Constants.TAG_TERMINAL_STATE,
                Constants.TAG_BATTERY_STATE,
                Constants.TAG_POWER_OFF_TIMEOUT,
                Constants.TAG_CONFIGURATION_MERCHANT_INTERFACE_LOCALE,
                Constants.TAG_SUPPORTED_LOCALE_LIST,
                Constants.TAG_EMVL1_CLESS_LIB_VERSION,
                Constants.TAG_USB_CAPABILITY,
                Constants.TAG_OS_VERSION,
                Constants.TAG_MPOS_MODULE_STATE,
                Constants.TAG_TST_LOOPBACK_VERSION,
                Constants.TAG_AGNOS_LIB_VERSION,
                Constants.TAG_ACE_LAYER_VERSION,
                Constants.TAG_GPI_VERSION,
                Constants.TAG_EMVL3_VERSION,
                Constants.TAG_PCI_PED_VERSION,
                Constants.TAG_PCI_PED_CHECKSUM,
                Constants.TAG_EMV_L1_CHECKSUM,
                Constants.TAG_BOOT_LOADER_CHECKSUM,
                Constants.TAG_EMV_L2_CHECKSUM,
                Constants.TAG_BLE_FIRMWARE_VERSION,
                Constants.TAG_RESOURCE_FILE_VERSION,
                Constants.TAG_FB_CHARGING_STATUS
        };

        new GetInfosCommand(uCubeInfoTagList).execute((event, params) -> {
            switch (event) {
                case PROGRESS:
                    Log.d(TAG,"GetInfoCommand");
                    Log.d(TAG,"progress state " + ((RPCCommandStatus) params[1]).name());
                    if(params[1] == RPCCommandStatus.SENT) {
                        listener.onSent();
                    }
                    break;

                case FAILED:
                case CANCELLED:
                    listener.onFinish(false);
                    return;

                case SUCCESS:
                    listener.onFinish(true);
                    break;
            }
        });
    }

    public static void connect(Listener listener) {

        if(UCubeAPI.getConnexionManager() == null) {
            listener.onFinish(false);
            return;
        }

        UCubeAPI.getConnexionManager().connect(new ConnectionListener() {
            @Override
            public void onConnectionFailed(ConnectionStatus status, int error) {
                listener.onFinish(false);
            }

            @Override
            public void onConnectionSuccess() {
                listener.onFinish(true);
            }

            @Override
            public void onConnectionCancelled() {
                listener.onFinish(false);
            }
        });
    }

    public static void disconnect(Listener listener) {
        if(UCubeAPI.getConnexionManager() == null) {
            listener.onFinish(false);
            return;
        }

        UCubeAPI.getConnexionManager().disconnect(status -> {
            if (!status) {
                listener.onFinish(false);
            } else {
                listener.onFinish(true);
            }
        });
    }

    public static void installForLoadKey(Listener listener) {
        new InstallForLoadKeyCommand().execute((event1, params1) -> {
            switch (event1) {
                case PROGRESS:
                    Log.d(TAG,"InstallForLoadKeyCommand");
                    Log.d(TAG,"progress state " + ((RPCCommandStatus) params1[1]).name());
                    if(params1[1] == RPCCommandStatus.SENT) {
                        listener.onSent();
                    }

                    break;
                case FAILED:
                case CANCELLED:
                    listener.onFinish(false);
                    return;
                case SUCCESS:
                    listener.onFinish(true);
                    break;

            }
        });
    }

    public static void installForLoad(Listener listener) {
        byte[] installForLoad = com.youTransactor.uCube.Tools.hexStringToByteArray("00015DCE55E5D02254C479F42C5C7B47273C89FB7B8518AA1BF10F4D28006A01D50F86A5273ABFFCBCC4FB1BA919E31FFDF41CE090A7B652DB50FE5D6EA47236124A2F734E727FFF3A147795C225C30720AE7FB8BCF8B5C48A2DDABAADCC3DE247FEEC92BBB95A17627FD8FDB1BBADA31F9253CA0B708AF3B59BC115CE41CD0038B32C78192556525E9B39B178419DD952D2B1B0F87A1966DAC77FBCD186E564AB58D1BF3B2DCB2EADEBDEFD4301E8CACEAC48B95E027703C814DE3B4A1D19632D1E5E91A9B65143B2CE25819056B3265ED747824D394B60F97060A4575D79314C424A0E812201D2D0DB794849E9EA6B7F925C060A080F1409F7FB5D0F2E8EF22CA305010000001E0315010000BE2D0000BE2D0000020000");

        UCubeAPI.sendData(INSTALL_FOR_LOAD_COMMAND, installForLoad, SecurityMode.SIGNED_NOT_CHECKED, SecurityMode.SIGNED, new UCubeLibRpcSendListener() {
            @Override
            public void onProgress(RPCCommandStatus rpcCommandStatus) {
                Log.d(TAG,"InstallForLoadCommand");
                Log.d(TAG,"progress state " + rpcCommandStatus.name());
                if(rpcCommandStatus == RPCCommandStatus.SENT) {
                    listener.onSent();
                }
            }

            @Override
            public void onFinish(boolean status, byte[] response) {
                listener.onFinish(status);
            }
        });
    }

    public static void load(Listener listener) {
        byte[] loadBuffer = com.youTransactor.uCube.Tools.hexStringToByteArray("84000000DB000000A204000000000000A2040000A20400000001090401020A04FFFFFFFF08000000160000001600000030000000B0000000100200001A9F0000020000007003000002000000339F000003000000720300000300000000D000000100000075030000010000001781DF000100000076030000010000001A81DF0003000000770300000300000002DFDF00000000007A0300008000000014DFDF0004000000FA0300000400000015DFDF0004000000FE03000004000000069F0000070000000204000010000000099F00000200000012040000020000001D9F0000060000001404000006000000359F0000010000001A04000001000000409F0000050000001B040000050000007E9F00000100000020040000010000000C81DF000100000021040000010000001881DF000100000022040000010000001981DF000100000023040000010000001B81DF000100000024040000010000001C81DF000200000025040000020000001D81DF000100000027040000010000001E81DF000100000028040000010000001F81DF000100000029040000010000002081DF00050000002A040000050000002181DF00050000002F040000050000002281DF000500000034040000050000002381DF000600000039040000060000002481DF00060000003F040000060000002581DF000600000045040000060000002681DF00060000004B040000060000002C81DF00010000005104000001000000069F0000070000005204000010000000099F00000200000062040000020000001D9F0000060000006404000006000000359F0000010000006A04000001000000409F0000050000006B040000050000007E9F00000100000070040000010000000C81DF000100000071040000010000001881DF000100000072040000010000001981DF000100000073040000010000001B81DF000100000074040000010000001C81DF000200000075040000020000001D81DF000100000077040000010000001E81DF000100000078040000010000001F81DF000100000079040000010000002081DF00050000007A040000050000002181DF00050000007F040000050000002281DF000500000084040000050000002381DF000600000089040000060000002481DF00060000008F040000060000002581DF000600000095040000060000002681DF00060000009B040000060000002C81DF0001000000A104000001000000078820B84802209F6A04000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000003A9800000001A000000004101000000000000000000000020CF08000000022600080300101022008B000000020C8F45080800C0000000000F45080800C00000000000000009999999900009999999900000000500008A000000004306000000000000000000000020CF08000000022600080300101022008B000000020C8F45080800C0000000000F45080800C000000000000000099999999000099999999000000005000088500000056000000A002000000000000A0020000A00200000001090401020A04FFFFFFFF1400000001000000010000003000000070010000800100001A9F0000020000009001000002000000339F0000030000009201000003000000359F00000100000095010000010000004E9F0000000000009601000032000000669F000004000000C80100000400000000D0000001000000CC0100000100000001D0000001000000CD0100000100000000DF000006000000CE0100000600000001DF000006000000D40100000600000002DF000006000000DA010000060000002081DF0005000000E0010000050000002181DF0005000000E5010000050000002281DF0005000000EA0100000500000002DFDF0000000000EF0100008000000014DFDF00040000006F0200000400000015DFDF0004000000730200000400000016DFDF0004000000770200000400000021DFDF00020000007B0200000200000022DFDF00020000007D0200000200000023DFDF00010000007F02000001000000069F0000070000008002000010000000069F0000070000009002000010000000078820B848220000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000320040000200000099999999000000005000000000000000DC4000A8000010000000DC4004F800000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000003A98000000010000008083B0390005A0000000031010000000000000000000A000000003201000000000000000000081000000");

        List<byte[]> listLoad = new ArrayList<>();
        listLoad.add(loadBuffer);

        new LoadCommand(listLoad).execute((event, params) -> {
            switch (event) {
                case PROGRESS:
                    Log.d(TAG,"LoadCommand");
                    Log.d(TAG,"progress state " + ((RPCCommandStatus) params[1]).name());
                    if(params[1] == RPCCommandStatus.SENT) {
                        listener.onSent();
                    }
                    break;

                case FAILED:
                case CANCELLED:
                case SUCCESS:
                    listener.onFinish(true);
                    break;
            }
        });
    }

    public static void transaction(Listener listener) {
        UCubeAPI.pay(preparePaymentRequest(), new UCubeLibPaymentServiceListener() {
            @Override
            public void onProgress(PaymentState state, PaymentContext context) {
                Log.d(TAG,"payment progress : "+ state);
            }

            @Override
            public void onFinish(PaymentContext context) {
                Log.d(TAG,"payment finish : "+ context.paymentStatus);
                listener.onFinish(true);
            }
        });

    }

    public static void powerOff(Listener listener) {
        new PowerOffCommand().execute(new ITaskMonitor() {
            @Override
            public void handleEvent(TaskEvent event, Object... params) {
                switch (event) {
                    case PROGRESS:
                        Log.d(TAG,"Power off command");
                        Log.d(TAG,"progress state " + ((RPCCommandStatus) params[1]).name());
                        if(params[1] == RPCCommandStatus.SENT) {
                            listener.onSent();
                        }
                        break;

                    case FAILED:
                    case CANCELLED:
                        listener.onFinish(false);
                        break;

                    case SUCCESS:
                        Log.d(getClass().getName(), "power off " + event.name());
                        listener.onFinish(true);
                        break;
                }
            }
        });
    }

    private static UCubePaymentRequest preparePaymentRequest() {
        List<CardReaderType> readerList = new ArrayList<>();

        readerList.add(CardReaderType.ICC);
        readerList.add(CardReaderType.NFC);

        UCubePaymentRequest uCubePaymentRequest = new UCubePaymentRequest(100, UCubePaymentRequest.CURRENCY_EUR, TransactionType.PURCHASE,
                readerList, new AuthorizationTask(null), Collections.singletonList("en"));

        //Add optional variables
        uCubePaymentRequest
                .setForceOnlinePin(false)
                .setTransactionDate(new Date())
                .setForceAuthorisation(false)
                .setCardWaitTimeout(30)
                .setSystemFailureInfo2(false)
                .setForceDebug(false)
                .setSkipCardRemoval(true)

                .setAuthorizationPlainTags(
                        0x4F, 0x50, 0x5F34, 0x82, 0x84, 0x8E, 0x8F, 0x95, 0x9A, 0x9C, 0x9F06, 0x9F08, 0x9F10, 0x9F12,
                        0x9F1A, 0x9F26, 0x9F27, 0x9F33, 0x9F34, 0x9F36, 0x9F37, 0x9F39, 0x9F41, 0x9F66, 0x9F6E, 0x9F71,
                        0x9F7C, 0xD3, 0xD4, 0xDF02, 0xDF81, 0x29, 0xDFC302
                )

                .setAuthorizationSecuredTags(
                        TAG_SECURE_5A_APPLICATION_PRIMARY_ACCOUNT_NUMBER,
                        TAG_SECURE_57_TRACK_2_EQUIVALENT_DATA,
                        TAG_SECURE_56_TRACK_1_DATA,
                        TAG_SECURE_5F24_APPLICATION_EXPIRATION_DATE,
                        0x99,
                        0x9F02,
                        0x9F03,
                        0x5F2A
                );

        return uCubePaymentRequest;
    }

    public static void exitSecureSession(Listener listener) {
        new ExitSecureSessionCommand().execute((event, params) -> {
            switch (event) {
                case PROGRESS:
                    Log.d(TAG,"exitSecureSession command");
                    Log.d(TAG,"progress state " + ((RPCCommandStatus) params[1]).name());
                    if(params[1] == RPCCommandStatus.SENT) {
                        listener.onSent();
                    }
                    break;

                case FAILED:
                case CANCELLED:
                case SUCCESS:
                    listener.onFinish(true);
                    break;
            }
        });
    }

    public static void enterSecureSession(Listener listener) {
        new EnterSecureSessionCommand().execute((event, params) -> {
            switch (event) {
                case PROGRESS:
                    Log.d(TAG,"enterSecureSession command");
                    Log.d(TAG,"progress state " + ((RPCCommandStatus) params[1]).name());
                    if(params[1] == RPCCommandStatus.SENT) {
                        listener.onSent();
                    }
                    break;

                case FAILED:
                case CANCELLED:
                    Log.e(TAG,"Error! Enter secure session Failed");
                    break;

                case SUCCESS:
                    if(((EnterSecureSessionCommand) params[0]).getKsn() == null) {
                        Log.e(TAG,"Error! KSN IS NULL");
                    }

                    listener.onFinish(true);
                    break;
            }
        });
    }

    public static void displayMessage(Listener listener) {
        //02000A15 5040 00 00 05 01 00 01 00 00 00 00
        //5040 00 00 05 01 00 01 00 00 00 00
      //  5040 00 00 05 01 00 01 00 00 00   640D03

        DisplayMessageCommand cmd = new DisplayMessageCommand("");
        cmd.setTimeout(0);
        cmd.setAbortKey((byte) 0x00);
        cmd.setClearConfig((byte) 0x05);

        cmd.execute((event, params) -> {
            switch (event) {
                case PROGRESS:
                    Log.d(TAG,"display command");
                    Log.d(TAG,"progress state " + ((RPCCommandStatus) params[1]).name());
                    if(params[1] == RPCCommandStatus.SENT) {
                        listener.onSent();
                    }
                    break;

                case FAILED:
                case CANCELLED:
                    listener.onFinish(false);
                    break;

                case SUCCESS:
                    listener.onFinish(true);
                    break;
            }
        });
    }

    public static void getCB(Listener listener) {

        final int[] uCubeInfoTagList = {0xCB};

        new GetInfosCommand(uCubeInfoTagList).execute((event, params) -> {
            switch (event) {
                case PROGRESS:
                    Log.d(TAG,"GetInfoCommand");
                    Log.d(TAG,"progress state " + ((RPCCommandStatus) params[1]).name());
                    if(params[1] == RPCCommandStatus.SENT) {
                        listener.onSent();
                    }
                    break;

                case FAILED:
                case CANCELLED:
                    listener.onFinish(false);
                    return;

                case SUCCESS:
                    listener.onFinish(true);
                    break;
            }
        });
    }
}
