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
package com.youtransactor.sampleapp.test;

import static com.youTransactor.uCube.payment.TestPinState.START_DISPLAY_PIN;
import static com.youTransactor.uCube.payment.TestPinState.START_ENTER_SECURE_SESSION;
import static com.youTransactor.uCube.payment.TestPinState.START_EXIT_SECURE_SESSION;
import static com.youTransactor.uCube.payment.TestPinState.START_UPDATE_KEY_PAD;
import static com.youTransactor.uCube.rpc.OnlinePinBlockFormatType.FORMAT_4;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.jps.secureService.api.product_manager.ProductIdentifier;
import com.jps.secureService.api.product_manager.ProductManager;
import com.youTransactor.uCube.api.UCubePaymentRequest;
import com.youTransactor.uCube.payment.PaymentContext;
import com.youTransactor.uCube.payment.PaymentUtils;
import com.youTransactor.uCube.payment.TestPinState;
import com.youTransactor.uCube.rpc.Constants;
import com.youTransactor.uCube.rpc.OnlinePinBlockFormatType;
import com.youTransactor.uCube.rpc.command.SimplifiedOnlinePINCommand;
import com.youTransactor.uCube.rpc.command.UpdateKeypad;

import java.util.ArrayList;
import java.util.List;

public class TestPinSession extends AsyncTask<Void, Void, Boolean> {

    private Handler handler = new Handler(Looper.getMainLooper());
    private Context context;
    private PaymentContext pay_context = new PaymentContext();
    private OnlinePinBlockFormatType onlinePinBlockFormatType;
    private final byte keySlot;
    private StopTestInterface stopTestInterface;

    public interface StopTestInterface {
        void stopTestSession();
    }
    public TestPinSession(Context context,
                          OnlinePinBlockFormatType onlinePinBlockFormatType,
                          byte keySlot){
        this.context = context;
        this.onlinePinBlockFormatType = onlinePinBlockFormatType;
        this.keySlot = keySlot;
    }

    public void setStopTestInterface(StopTestInterface stopTestInterface) {
        this.stopTestInterface = stopTestInterface;
    }

    private void  sendMapping() {
        List<UpdateKeypad.KBDButton> KBDMapping = new ArrayList<>();
        KBDMapping = new ArrayList<>();
        //utton 0 at X1: 312, Y1: 838, X2: 488, Y2: 934
        KBDMapping.add(new UpdateKeypad.KBDButton(312, 838,
                    488, 934, 0));
        //Button 1 at X1: 104, Y1: 454, X2: 280, Y2: 550
        KBDMapping.add(new UpdateKeypad.KBDButton(104, 454,
                280, 550, 1));
        //Button 2 at X1: 312, Y1: 454, X2: 488, Y2: 550
        KBDMapping.add(new UpdateKeypad.KBDButton(312, 454,
                488, 550, 2));
        //Button 3 at X1: 520, Y1: 454, X2: 696, Y2: 550
        KBDMapping.add(new UpdateKeypad.KBDButton(520, 454,
                696, 550, 3));
        //Button 4 at X1: 104, Y1: 582, X2: 280, Y2: 678
        KBDMapping.add(new UpdateKeypad.KBDButton(104, 582,
                280, 678, 4));
        //Button 5 at X1: 312, Y1: 582, X2: 488, Y2: 678
        KBDMapping.add(new UpdateKeypad.KBDButton(312, 582,
                488, 678, 5));
        //Button 6 at X1: 520, Y1: 582, X2: 696, Y2: 678
        KBDMapping.add(new UpdateKeypad.KBDButton(520, 582,
                696, 678, 6));
        //Button 7 at X1: 104, Y1: 710, X2: 280, Y2: 806
        KBDMapping.add(new UpdateKeypad.KBDButton(104, 710,
                280, 806, 7));
        //Button 8 at X1: 312, Y1: 710, X2: 488, Y2: 806
        KBDMapping.add(new UpdateKeypad.KBDButton(312, 710,
                488, 806, 8));
        //Button 9 at X1: 520, Y1: 710, X2: 696, Y2: 806
        KBDMapping.add(new UpdateKeypad.KBDButton(520, 710,
                696, 806, 9));
        //Button Confirm at X1: 544, Y1: 1046, X2: 768, Y2: 1152
        KBDMapping.add(new UpdateKeypad.KBDButton(544, 1046,
                768, 1152, 0xF1));
        //Button Cancel at X1: 32, Y1: 1046, X2: 256, Y2: 1152
        KBDMapping.add(new UpdateKeypad.KBDButton(32, 1046,
                256, 1152, 0xF2));
        //Button - at X1: 520, Y1: 838, X2: 696, Y2: 934
        KBDMapping.add(new UpdateKeypad.KBDButton(520, 838,
                696, 934, 0xF3));
        //Button Clear at X1: 288, Y1: 1046, X2: 512, Y2: 1152
        KBDMapping.add(new UpdateKeypad.KBDButton(288, 1046,
                512, 1152, 0xF3));
        PaymentUtils.update_keypad(KBDMapping, (event, params) -> {
            switch (event) {
                case FAILED:
                    if (this.stopTestInterface != null) stopTestInterface.stopTestSession();
                    handler.post(() -> {
                        Toast.makeText(context, "Update Keyboard failure", Toast.LENGTH_LONG).show();
                    });
                    break;
                case SUCCESS:
                    startSetting(START_DISPLAY_PIN);
                    break;
                default:
                    break;
            }
        });
    }

    private void startSetting(TestPinState state) {
        switch (state) {
            case START_EXIT_SECURE_SESSION:
                PaymentUtils.exitSecureSession((event, params) -> {});
                break;
            case START_ENTER_SECURE_SESSION:
                PaymentContext PayContext = new PaymentContext();
                PayContext.setDataEncryptionMechanism(2);
                PaymentUtils.enterSecureSession(PayContext, (event, params) -> {
                    switch (event) {
                        case FAILED:
                            if (this.stopTestInterface != null) stopTestInterface.stopTestSession();
                            handler.post(() -> {
                                Toast.makeText(context, "Enter secure session failure", Toast.LENGTH_LONG).show();
                            });
                            break;
                        case SUCCESS:
                            if(ProductManager.id == ProductIdentifier.blade) {
                                startSetting(START_UPDATE_KEY_PAD);
                            }
                            else{
                                startSetting(START_DISPLAY_PIN);
                            }
                            break;
                        default:
                            break;
                    }
                });
                break;
            case START_UPDATE_KEY_PAD:
                sendMapping();
                break;

            case START_DISPLAY_PIN:
                pay_context.currency = UCubePaymentRequest.CURRENCY_USD;
                pay_context.amount = 1;
                pay_context.setOnlinePinBlockFormat(onlinePinBlockFormatType);
                pay_context.dukpt_key_slot = this.keySlot;
                PaymentUtils.doSimplifiedOnlinePin(pay_context, (event, params) -> {
                    switch (event) {
                        case FAILED:
                        case CANCELLED:
                            if (this.stopTestInterface != null) stopTestInterface.stopTestSession();
                            handler.post(() -> {
                                Toast.makeText(context, "Failed PIN", Toast.LENGTH_LONG).show();
                            });
                            startSetting(START_EXIT_SECURE_SESSION);
                            break;
                        case SUCCESS:
                            handler.post(() -> {
                                Toast.makeText(context, "PIN Success", Toast.LENGTH_LONG).show();
                            });
                            pay_context.pinKsn =  ((SimplifiedOnlinePINCommand) params[0]).getPinKSN();
                            pay_context.onlinePinBlock = ((SimplifiedOnlinePINCommand) params[0]).getEncryptedPinBlock();
                            startSetting(START_EXIT_SECURE_SESSION);
                            break;
                    }
                });
                break;
        }

    }

    @Override
        protected Boolean doInBackground(Void... params) {
            try {
                startSetting(START_ENTER_SECURE_SESSION);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }
