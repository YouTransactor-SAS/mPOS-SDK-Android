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
package com.youtransactor.sampleapp.transactionView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.youTransactor.uCube.api.UCubeAPI;
import com.youTransactor.uCube.payment.PaymentUtils;
import com.youTransactor.uCube.rpc.Constants;
import com.youtransactor.sampleapp.R;
import com.youTransactor.uCube.rpc.command.UpdateKeypad;
import com.youTransactor.uCube.rpc.command.event.EventCommand;
import com.youTransactor.uCube.rpc.command.event.kbd.EventKbd;

import com.jps.secureService.api.product_manager.ProductIdentifier;
import com.jps.secureService.api.product_manager.ProductManager;
import com.jps.secureService.api.ISecureLogicServiceConnectCallback;
import com.jps.secureService.api.SecureLogicServiceManager;
import com.youtransactor.sampleapp.payment.Localization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class TestPinPrompt extends TransactionViewBasePinTest {

    private static final String TAG = TestPinPrompt.class.getSimpleName();
    public static final String INTENT_EXTRA_PIN_AMOUNT = "INTENT_EXTRA_PIN_AMOUNT";
    public static final String INTENT_EXTRA_PIN_MSG = "INTENT_EXTRA_PIN_MSG";
    public static final String INTENT_EXTRA_PIN_MSG_TAG = "INTENT_EXTRA_PIN_MSG_TAG";
    public static final String INTENT_EXTRA_UPDATE_KEYPAD_TAG = "INTENT_EXTRA_UPDATE_KEYPAD_TAG";
    private static final Logger log = LoggerFactory.getLogger(TestPinPrompt.class);

    private TextView textViewPin;
    private TextView textViewPinMsg;
    private boolean Is_Keypad_Update;

    private ArrayList<Pair<Button, Integer>> kbdButtonList;
    private List<UpdateKeypad.KBDButton> KBDMapping = new ArrayList<>();

    private SecureLogicServiceManager mSecureLogicSvcMgr = SecureLogicServiceManager.getInstance();
    private ISecureLogicServiceConnectCallback mSecureLogicConnectionCallback =
            new ISecureLogicServiceConnectCallback() {
                @Override
                public void onServiceConnected() {
                    mSecureLogicSvcMgr.disableSystemBars(true);
                }
                @Override
                public void onServiceDisconnected() {
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        setContentView(R.layout.activity_pin_prompt);
        textViewPin = findViewById(R.id.textViewPIN);
        textViewPinMsg = findViewById(R.id.textViewPinMsg);
        kbdButtonList = new ArrayList<>();
        kbdButtonList.add(new Pair<>(findViewById(R.id.button0), Constants.KEYPAD_BUTTON_0));
        kbdButtonList.add(new Pair<>(findViewById(R.id.button1), Constants.KEYPAD_BUTTON_1));
        kbdButtonList.add(new Pair<>(findViewById(R.id.button2), Constants.KEYPAD_BUTTON_2));
        kbdButtonList.add(new Pair<>(findViewById(R.id.button3), Constants.KEYPAD_BUTTON_3));
        kbdButtonList.add(new Pair<>(findViewById(R.id.button4), Constants.KEYPAD_BUTTON_4));
        kbdButtonList.add(new Pair<>(findViewById(R.id.button5), Constants.KEYPAD_BUTTON_5));
        kbdButtonList.add(new Pair<>(findViewById(R.id.button6), Constants.KEYPAD_BUTTON_6));
        kbdButtonList.add(new Pair<>(findViewById(R.id.button7), Constants.KEYPAD_BUTTON_7));
        kbdButtonList.add(new Pair<>(findViewById(R.id.button8), Constants.KEYPAD_BUTTON_8));
        kbdButtonList.add(new Pair<>(findViewById(R.id.button9), Constants.KEYPAD_BUTTON_9));
        kbdButtonList.add(new Pair<>(findViewById(R.id.buttonConfirm), Constants.KEYPAD_BUTTON_CONFIRM));
        kbdButtonList.add(new Pair<>(findViewById(R.id.buttonCancel), Constants.KEYPAD_BUTTON_ESC));
        kbdButtonList.add(new Pair<>(findViewById(R.id.buttonDel), Constants.KEYPAD_BUTTON_CLEAR));
        kbdButtonList.add(new Pair<>(findViewById(R.id.buttonClear), Constants.KEYPAD_BUTTON_CLEAR));

        if (ProductManager.id == ProductIdentifier.stick) {
            findViewById(R.id.pinGrid).setVisibility(View.GONE);
        }

        Is_Keypad_Update = intent.getBooleanExtra(INTENT_EXTRA_UPDATE_KEYPAD_TAG, true);
        textViewPinMsg.setText(Localization.getMsg(intent.getIntExtra(INTENT_EXTRA_PIN_MSG_TAG, -1),
                intent.getStringExtra(INTENT_EXTRA_PIN_MSG)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSecureLogicSvcMgr.setServiceConnectCallback(mSecureLogicConnectionCallback);
        if (mSecureLogicSvcMgr.isServiceDisconnected()) {
            mSecureLogicSvcMgr.bindService(getApplicationContext());
        }
    }

    @Override
    protected void onPause() {
        mSecureLogicSvcMgr.disableSystemBars(false);
        mSecureLogicSvcMgr.setServiceConnectCallback(null);
        super.onPause();
    }

    @Override
    protected void onStop() {
        mSecureLogicSvcMgr.disableSystemBars(false);
        mSecureLogicSvcMgr.setServiceConnectCallback(null);
        super.onStop();
    }

    protected void onDestroy() {
        mSecureLogicSvcMgr.disableSystemBars(false);
        mSecureLogicSvcMgr.setServiceConnectCallback(null);
        super.onDestroy();
    }

    private void starThread() {
        final int[] buttonsProcessed = {0};

        for (Pair<Button, Integer> pair : kbdButtonList) {
            Button button = pair.first;
            button.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    button.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    buttonsProcessed[0]++;
                    if ((buttonsProcessed[0] == kbdButtonList.size()) &&
                            (kbdButtonList.size() != 0)){
                        if(ProductManager.id == ProductIdentifier.blade) {
                            sendMapping();
                        }
                    }
                }
            });
        }
    }

    private void  sendMapping() {
        KBDMapping = new ArrayList<>();
        for (Pair<Button, Integer> pair : kbdButtonList) {
            Button button = pair.first;
            int[] location = new int[2];
            button.getLocationOnScreen(location);
            int x = location[0];
            int y = location[1];
            Log.d("ButtonPosition", "Button " + button.getText() + " at X1: " + x + ", Y1: " +
                    y + ", X2: " + (x + button.getWidth()) + ", Y2: " + (y + button.getHeight()));
            KBDMapping.add(new UpdateKeypad.KBDButton(x, y,
                    x + button.getWidth(),
                    y + button.getHeight(), pair.second));
        }
        PaymentUtils.update_keypad(KBDMapping, (event, params) -> {
            switch (event) {
                case FAILED:
                    runOnUiThread(() -> Toast.makeText(this, "Update Keypad fail", Toast.LENGTH_LONG).show());
                    this.finish();
                    break;
                case SUCCESS:
                    runOnUiThread(() -> Toast.makeText(this, "Update Keypad success", Toast.LENGTH_LONG).show());
                    break;
                default:
                    break;
            }
        });
    }

    @Override
    protected void onEventViewUpdate(EventCommand event) {
        switch (event.getEvent()) {
            case kbd_press:
                break;
            case kbd_release:
            case kbd_del_one_char:
            case kbd_del_all_char:
                updatePin(((EventKbd) event).getNbPressedDigit(), ((EventKbd) event).getValue());
                break;
            default:
                break;
        }
    }
    private void updatePin(byte nb_digit, byte value) {
        StringBuffer pinStr = new StringBuffer();
        for (int i = 0; i < nb_digit; i++) {
            pinStr.append('*');
        }
        runOnUiThread(() -> textViewPin.setText(pinStr));
    }
}