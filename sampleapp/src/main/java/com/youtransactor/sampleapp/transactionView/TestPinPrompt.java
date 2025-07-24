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
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jps.secureService.api.product_manager.ProductIdentifier;
import com.jps.secureService.api.product_manager.ProductManager;
import com.youTransactor.uCube.rpc.Constants;
import com.youTransactor.uCube.rpc.command.event.EventCommand;
import com.youTransactor.uCube.rpc.command.event.kbd.EventKbd;
import com.youtransactor.sampleapp.R;
import com.youtransactor.sampleapp.infrastructure.SystemBars;
import com.youtransactor.sampleapp.payment.Localization;

import java.util.ArrayList;

public class TestPinPrompt extends TransactionViewBasePinTest {

    public static final String INTENT_EXTRA_PIN_MSG = "INTENT_EXTRA_PIN_MSG";
    public static final String INTENT_EXTRA_PIN_MSG_TAG = "INTENT_EXTRA_PIN_MSG_TAG";

    private TextView textViewPin;
    private TextView textViewPinMsg;

    private ArrayList<Pair<Button, Integer>> kbdButtonList;

    private SystemBars systemBars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.systemBars = new SystemBars(this);
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

        textViewPinMsg.setText(Localization.getMsg(intent.getIntExtra(INTENT_EXTRA_PIN_MSG_TAG, -1),
                intent.getStringExtra(INTENT_EXTRA_PIN_MSG)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.systemBars.disable();
    }

    @Override
    protected void onPause() {
        this.systemBars.enable();
        super.onPause();
    }

    @Override
    protected void onStop() {
        this.systemBars.enable();
        super.onStop();
    }

    protected void onDestroy() {
        this.systemBars.enable();
        super.onDestroy();
    }

    @Override
    protected void onEventViewUpdate(EventCommand event) {
        switch (event.getEvent()) {
            case kbd_press:
                break;
            case kbd_release:
            case kbd_del_one_char:
            case kbd_del_all_char:
                updatePin(((EventKbd) event).getNbPressedDigit());
                break;
            default:
                break;
        }
    }

    private void updatePin(byte nb_digit) {
        StringBuffer pinStr = new StringBuffer();
        for (int i = 0; i < nb_digit; i++) {
            pinStr.append('*');
        }
        runOnUiThread(() -> textViewPin.setText(pinStr));
    }
}