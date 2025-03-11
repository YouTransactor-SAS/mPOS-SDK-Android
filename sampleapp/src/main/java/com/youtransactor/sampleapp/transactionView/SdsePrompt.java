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

import com.youTransactor.uCube.payment.PaymentUtils;
import com.youtransactor.sampleapp.MainActivity;
import com.youtransactor.sampleapp.R;
import com.youTransactor.uCube.rpc.command.UpdateKeypad;
import com.youTransactor.uCube.rpc.command.event.EventCommand;
import com.youTransactor.uCube.rpc.command.event.kbd.EventKbd;
import com.youtransactor.sampleapp.product_manager.product_id;
import com.youtransactor.sampleapp.product_manager.product_manager;

import java.util.ArrayList;
import java.util.List;

public class SdsePrompt extends TransactionViewBase {

    private static final String TAG = SdsePrompt.class.getSimpleName();
    public static final String INTENT_EXTRA_SDSE_PROMPT_MSG = "INTENT_EXTRA_SDSE_PROMPT_MSG";
    public static final String INTENT_EXTRA_SDSE_PROMPT_TYPE = "INTENT_EXTRA_SDSE_PROMPT_TYPE";
    private TextView textViewSdseMsg;
    private TextView textViewSdse;

    private int Sdse_type;
    private ArrayList<Pair<Button, Integer>> kbdButtonList;
    private List<UpdateKeypad.KBDButton> KBDMapping = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        setContentView(R.layout.activity_pan_prompt);
        textViewSdseMsg = findViewById(R.id.textViewSdseMsg);
        textViewSdse= findViewById(R.id.textViewPan);
        kbdButtonList = new ArrayList<>();
        kbdButtonList.add(new Pair<>(findViewById(R.id.button0), 0x00));
        kbdButtonList.add(new Pair<>(findViewById(R.id.button1), 0x01));
        kbdButtonList.add(new Pair<>(findViewById(R.id.button2), 0x02));
        kbdButtonList.add(new Pair<>(findViewById(R.id.button3), 0x03));
        kbdButtonList.add(new Pair<>(findViewById(R.id.button4), 0x04));
        kbdButtonList.add(new Pair<>(findViewById(R.id.button5), 0x05));
        kbdButtonList.add(new Pair<>(findViewById(R.id.button6), 0x06));
        kbdButtonList.add(new Pair<>(findViewById(R.id.button7), 0x07));
        kbdButtonList.add(new Pair<>(findViewById(R.id.button8), 0x08));
        kbdButtonList.add(new Pair<>(findViewById(R.id.button9), 0x09));
        kbdButtonList.add(new Pair<>(findViewById(R.id.buttonConfirm), 0xF1));
        kbdButtonList.add(new Pair<>(findViewById(R.id.buttonCancel), 0xF2));
        kbdButtonList.add(new Pair<>(findViewById(R.id.buttonClear), 0xF3));

        if (product_manager.id == product_id.stick) {
             findViewById(R.id.pinGrid).setVisibility(View.GONE);
        }

        textViewSdseMsg.setText(intent.getStringExtra(INTENT_EXTRA_SDSE_PROMPT_MSG));
        Sdse_type = (byte) intent.getIntExtra(INTENT_EXTRA_SDSE_PROMPT_TYPE, -1);
        new Thread(() -> {
            try {
                starThread();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
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
                        if(product_manager.id == product_id.blade) {
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

    private void onError() {
        finish();
    }

    @Override
    protected void onEventViewUpdate(EventCommand event) {
        switch (event.getEvent()) {
            case kbd_release:
                break;
            case kbd_press:
            case kbd_del_one_char:
            case kbd_del_all_char:
                updateSdse(((EventKbd) event).getPosition(), ((EventKbd) event).getValue());
                break;
            default:
                onError();
                break;
        }
    }
    private void updateSdse(byte position, byte value) {
        if (value != 0xF2) {
            StringBuffer pinStr = new StringBuffer();
            for (int i = 0; i < position; i++) {
                pinStr.append('*');
            }
            runOnUiThread(() -> textViewSdse.setText(pinStr));
        }
        else {
            onError();
        }
    }

}

