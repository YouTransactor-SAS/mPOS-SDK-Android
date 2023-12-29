/*
 * ============================================================================
 *
 * Copyright (c) 2022 YouTransactor
 *
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of YouTransactor
 * ("Confidential Information"). You  shall not disclose or redistribute such
 * Confidential Information and shall use it only in accordance with the terms of
 * the license agreement you entered into with YouTransactor.
 *
 * This software is provided by YouTransactor AS IS, and YouTransactor
 * makes no representations or warranties about the suitability of the software,
 * either express or implied, including but not limited to the implied warranties
 * of merchantability, fitness for a particular purpose or non-infringement.
 * YouTransactor shall not be liable for any direct, indirect, incidental,
 * special, exemplary, or consequential damages suffered by licensee as the
 * result of using, modifying or distributing this software or its derivatives.
 *
 * ==========================================================================
 */

package com.youtransactor.sampleapp.payment;

import android.util.Log;

import com.youTransactor.uCube.AbstractService;
import com.youTransactor.uCube.TaskEvent;
import com.youTransactor.uCube.Tools;
import com.youTransactor.uCube.api.UCubeAPI;
import com.youTransactor.uCube.api.UCubeLibRpcSendListener;
import com.youTransactor.uCube.rpc.Constants;
import com.youTransactor.uCube.rpc.DisplayWithKiTagList;
import com.youTransactor.uCube.rpc.LineDescription;
import com.youTransactor.uCube.rpc.RPCCommandStatus;
import com.youTransactor.uCube.rpc.SecurityMode;
import com.youTransactor.uCube.rpc.command.DisplayMessageCommand;
import com.youTransactor.uCube.rpc.command.DisplayMessageWithKICustomCommand;
import com.youTransactor.uCube.rpc.command.EnterSecureSessionCommand;
import com.youTransactor.uCube.rpc.command.ExitSecureSessionCommand;

public class EntryTipService extends AbstractService {
    private static final String TAG = EntryTipService.class.getName();

    private double amount;
    private int tip;
    private double totalAmount;

    public double getTotalAmount() {
        return totalAmount;
    }

    /*
     * // video 1-recommended-tips
     * 0- enter secure session
     * 1- Amount \n$1.00\nAdd tip ? (yes) (no)
     * 3- Tips % (1)15% (2)18% (3)20%
     * 4- Transaction Total:\n$1.15\n Accept ? (yes) (no)
     * 5- processing (for 1second)
     * 6- exit secure session
     * */

    /*
     * // video 3-recommended-tips-terminal-mode
     * 0- enter secure session
     * 1- enter amount\n $0.00
     * 2- Amount \n$1.00\nAdd tip ? (yes) (no)
     * 3- Tips % (1)15% (2)18% (3)20% (4)Others
     * 4- Transaction Total:\n$1.15\n Accept ? (yes) (no)
     * 5- processing (for 1second)
     * 6- exit secure session
     * */
    @Override
    protected void start() {
        displayAddTips();
    }

    //* 0- enter secure session
    private void enterSecureSession() {
        new EnterSecureSessionCommand().execute((event, params) -> {
            switch (event) {
                case PROGRESS:
                    Log.d(TAG,"enter secure session progress ");
                    return;
                case FAILED:
                case CANCELLED:
                    Log.d(TAG,"enter secure session : "+ event);
                    notifyMonitor(TaskEvent.FAILED);
                    break;

                case SUCCESS:
                    Log.d(TAG,"display message : "+ event);

                    displayEnterAmountScreen();
                    break;
            }
        });
    }

    // 1- Amount \n$1.00\nAdd tip ? (yes) (no)
    private void displayAddTips() {

        DisplayMessageCommand displayMessageCommand = new DisplayMessageCommand();
        displayMessageCommand.setXPosition((byte) 0xFF);
        UCubeAPI.sendData(Constants.DISPLAY_WITHOUT_KI_COMMAND,
                Tools.hexStringToByteArray("1E07020202124C696E65203120783D464620793D20300A00FF0002124C696E65203220783D303020793D31300A00000A"),
                SecurityMode.SIGNED_NOT_CHECKED,
                SecurityMode.SIGNED,
                new UCubeLibRpcSendListener() {
                    @Override
                    public void onProgress(RPCCommandStatus rpcCommandStatus) {
                    }

                    @Override
                    public void onFinish(boolean status, byte[] response) {
                        if(status)
                            notifyMonitor(TaskEvent.SUCCESS);
                        else
                            notifyMonitor(TaskEvent.FAILED);
                    }
                });
    }

    // *- enter amount\n $0.00
    private void displayEnterAmountScreen() {
        DisplayWithKiTagList displayWithKiTagList = new DisplayWithKiTagList((byte) 0x00,
                (byte) 0x08, 30, 60, 120,
                (byte) 0x01, (byte) 0x03);

        DisplayMessageWithKICustomCommand cmd = new DisplayMessageWithKICustomCommand();
        cmd.setLineDescription(new LineDescription("Enter Amount   $%s|______.__| 0.00"));
        cmd.setLineDescriptionSignature(Tools.hexStringToByteArray("265df6f78d17f99863826f80b361ac7b4077ac05bc4b6e4118e80818edc3069cd1128668efaa85c252af180fee65145697d271d69706ec2aecdfae650c9d038585791fe85aaec23bd2a19ed8457ff1cc7d81591baab2edc59b116bbd6f3ad871262a5c55c81e3d53cc6bded631c4216083926f98ddbc64113e0629dc04b9d26144effc446d33af562f8316760b74dde763e73a5b38d561cbf9ba931fad68945deafd63f8f66193cfdc2bc32eabe89d68da50ebcea512ed9318d4b62b78886755b0182dbe56e2a4c0ab1070097daf8acae2db18302b51e81cf7855b3e98425cda9199b0ab9826119b4f7e7340f5e45c6945694529c1c53482dfd4d7"));
        cmd.setDisplayWithKiTagList(displayWithKiTagList);

        cmd.execute((event, params) -> {
            switch (event) {
                case PROGRESS:
                    Log.d(TAG,"display progress state ");
                    return;
                case FAILED:
                case CANCELLED:
                    Log.d(TAG,"display message : "+ event);
                    notifyMonitor(TaskEvent.FAILED);
                    break;

                case SUCCESS:
                    Log.d(TAG,"display message : "+ event);
                    exitSecureSession();
                    break;
            }
        });
    }

    //* 6- exit secure session
    private void exitSecureSession() {
        new ExitSecureSessionCommand().execute((event, params) -> {
            switch (event) {
                case PROGRESS:
                    Log.d(TAG,"Exit secure session progress ");
                    return;
                case FAILED:
                case CANCELLED:
                case SUCCESS:
                    Log.d(TAG,"exit secure session : "+ event);
                    notifyMonitor(TaskEvent.SUCCESS);
                    break;

            }
        });
    }

    private void displayAddTipYesNoScreen() {
        DisplayMessageCommand displayMessageCommand = new DisplayMessageCommand("Amount \n $"+ amount + "\n Add Tip?");
        displayMessageCommand.setClearConfig((byte) 0x05);

        displayMessageCommand.execute((event1, params1) -> {
            switch (event1) {
                case PROGRESS:
                    Log.d(TAG,"display progress state "+ ((RPCCommandStatus) params1[1]).name());
                    return;
                case FAILED:
                case CANCELLED:
                    Log.d(TAG,"display message : "+ event1);
                    break;

                case SUCCESS:
                    Log.d(TAG,"display message : "+ event1);
                    break;
            }
        });
    }

    private void displayEnterTipScreen() {
        DisplayMessageCommand displayMessageCommand = new DisplayMessageCommand("Amount \n $"+ amount + "\n Add Tip?");
        displayMessageCommand.setClearConfig((byte) 0x05);

        displayMessageCommand.execute((event1, params1) -> {
            switch (event1) {
                case PROGRESS:
                    Log.d(TAG,"display progress state "+ ((RPCCommandStatus) params1[1]).name());
                    return;
                case FAILED:
                case CANCELLED:
                    Log.d(TAG,"display message : "+ event1);
                    break;

                case SUCCESS:
                    Log.d(TAG,"display message : "+ event1);
                    break;
            }
        });
    }



    private void displayTransactionTotalScreen() {
        DisplayMessageCommand displayMessageCommand = new DisplayMessageCommand("Amount \n $"+ amount + "\n Add Tip?");
        displayMessageCommand.setClearConfig((byte) 0x05);

        displayMessageCommand.execute((event1, params1) -> {
            switch (event1) {
                case PROGRESS:
                    Log.d(TAG,"display progress state "+ ((RPCCommandStatus) params1[1]).name());
                    return;
                case FAILED:
                case CANCELLED:
                    Log.d(TAG,"display message : "+ event1);
                    break;

                case SUCCESS:
                    Log.d(TAG,"display message : "+ event1);
                    break;
            }
        });
    }

    private void displayTipsPercentagesScreen() {
        DisplayMessageCommand displayMessageCommand = new DisplayMessageCommand("Amount \n $"+ amount + "\n Add Tip?");
        displayMessageCommand.setClearConfig((byte) 0x05);

        displayMessageCommand.execute((event1, params1) -> {
            switch (event1) {
                case PROGRESS:
                    Log.d(TAG,"display progress state "+ ((RPCCommandStatus) params1[1]).name());
                    return;
                case FAILED:
                case CANCELLED:
                    Log.d(TAG,"display message : "+ event1);
                    break;

                case SUCCESS:
                    Log.d(TAG,"display message : "+ event1);
                    break;
            }
        });
    }
}
