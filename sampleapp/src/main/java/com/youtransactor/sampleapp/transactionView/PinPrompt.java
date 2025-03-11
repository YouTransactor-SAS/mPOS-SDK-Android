package com.youtransactor.sampleapp.transactionView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;

import com.youTransactor.uCube.payment.PaymentUtils;
import com.youTransactor.uCube.rpc.command.UpdateKeypad;
import com.youTransactor.uCube.rpc.command.event.EventCommand;
import com.youTransactor.uCube.rpc.command.event.kbd.EventKbd;
import com.youTransactor.uCube.rpc.command.event.ppt.*;
import com.youtransactor.sampleapp.R;
import com.youtransactor.sampleapp.payment.Localization;
import com.youtransactor.sampleapp.product_manager.product_id;
import com.youtransactor.sampleapp.product_manager.product_manager;

import java.util.ArrayList;
import java.util.List;

public class PinPrompt extends TransactionViewBase {

    private static final String TAG = PinPrompt.class.getSimpleName();
    public static final String INTENT_EXTRA_PIN_AMOUNT = "INTENT_EXTRA_PIN_AMOUNT";
    public static final String INTENT_EXTRA_PIN_MSG = "INTENT_EXTRA_PIN_MSG";
    public static final String INTENT_EXTRA_PIN_MSG_TAG = "INTENT_EXTRA_PIN_MSG_TAG";

    private final ArrayList<Pair<Button, Integer>> kbdButtonList = new ArrayList<>();
    private TextView textViewPin;
    private TextView textViewPinMsg;

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        setContentView(R.layout.activity_pin_prompt);
        textViewPin = findViewById(R.id.textViewPIN);
        textViewPinMsg = findViewById(R.id.textViewPinMsg);

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
        kbdButtonList.add(new Pair<>(findViewById(R.id.buttonDel), 0xF3));
        kbdButtonList.add(new Pair<>(findViewById(R.id.buttonClear), 0xF3));

        if (product_manager.id == product_id.stick) {
            findViewById(R.id.pinGrid).setVisibility(View.GONE);
        }
        else{
            this.setupMapping();
        }

        textViewPinMsg.setText(Localization.getMsg(intent.getIntExtra(INTENT_EXTRA_PIN_MSG_TAG, -1),
                    intent.getStringExtra(INTENT_EXTRA_PIN_MSG)));
    }


    @Override
    protected void onEventViewUpdate(EventCommand event) {
        switch (event.getEvent()) {
            case kbd_press:
                break;
            case kbd_release:
            case kbd_del_one_char:
            case kbd_del_all_char:
                updatePin(((EventKbd) event).getPosition(), ((EventKbd) event).getValue());
                break;
            case ppt_pin_ok:
            case ppt_pin_blocked:
                textViewPinMsg.setText(Localization.getMsg(((EventPptResult)event).getTag(),
                        ((EventPptResult) event).getText()));
                PinPrompt.this.runOnUiThread(PinPrompt.this::finish);
                break;
            case ppt_pin_wrong:
                textViewPinMsg.setText(Localization.getMsg(((EventPptResult)event).getTag(),
                        ((EventPptResult) event).getText()));
            default:
                onError();
                break;
        }
    }

    private void onError() {
        finish();
    }

    private void updatePin(byte position, byte value) {
        if (value != 0xF2) {
            StringBuffer pinStr = new StringBuffer();
            for (int i = 0; i < position; i++) {
                pinStr.append('*');
            }
            runOnUiThread(() -> textViewPin.setText(pinStr));
        }
        else {
            onError();
        }
    }

    private void setupMapping() {
        final int[] buttonsProcessed = {0};

        for (Pair<Button, Integer> pair : kbdButtonList) {
            Button button = pair.first;
            button.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    button.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    buttonsProcessed[0]++;
                    if (buttonsProcessed[0] == kbdButtonList.size()) {
                        sendMapping();
                    }
                }
            });
        }
    }

    private void sendMapping() {
        List<UpdateKeypad.KBDButton> KBDMapping = new ArrayList<>();
        for (Pair<Button, Integer> pair : kbdButtonList) {
            Button button = pair.first; // Obtenir le bouton
            int[] location = new int[2];
            button.getLocationOnScreen(location);
            int x = location[0];
            int y = location[1];
            Log.d("ButtonPosition", "Button " + button.getText() + " at X1: " + x + ", Y1: " +
                    y + ", X2: " + (x +  button.getWidth())+ ", Y2: " + (y + button.getHeight()));
            KBDMapping.add(new UpdateKeypad.KBDButton(x, y,
                       x + button.getWidth(),
                       y + button.getHeight(), pair.second));
        }
        PaymentUtils.update_keypad( KBDMapping, (event, params) -> {
            switch (event) {
                case FAILED:
                    this.finish();
                    break;
                case SUCCESS:
                    break;
            }
        });
    }
}
