/*
 * 2006-2025 YOUTRANSACTOR ALL RIGHTS RESERVED. YouTransactor,
 * 32, rue Brancion 75015 Paris France, RCS PARIS: B 491 208 500, YOUTRANSACTOR
 * CONFIDENTIAL AND PROPRIETARY INFORMATION , CONTROLLED DISTRIBUTION ONLY,
 * THEREFORE UNDER NDA ONLY. YOUTRANSACTOR Authorized Parties and who have
 * signed NDA do not have permission to distribute this documentation further.
 * Unauthorized recipients must destroy any electronic and hard-copies
 * and without reading and notify Gregory Mardinian, CTO, YOUTRANSACTOR
 * immediately at gregory_mardinian@jabil.com.
 *
 * @package: com.youtransactor.sampleapp.transactionView
 *
 * @date: mars 28, 2025
 *
 * @author: Thomas JEANNETTE (thomas_jeannette@jabil.com)
 */
package com.youtransactor.sampleapp.transactionView.components;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jps.secureService.api.product_manager.ProductIdentifier;
import com.jps.secureService.api.product_manager.ProductManager;
import com.youTransactor.uCube.payment.PaymentUtils;
import com.youTransactor.uCube.rpc.Constants;
import com.youTransactor.uCube.rpc.EventListener;
import com.youTransactor.uCube.rpc.RPCManager;
import com.youTransactor.uCube.rpc.command.UpdateKeypad;
import com.youTransactor.uCube.rpc.command.event.kbd.EventKbd;
import com.youTransactor.uCube.rpc.command.event.ppt.EventPptResult;
import com.youtransactor.sampleapp.R;
import com.youtransactor.sampleapp.payment.Localization;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PinPromptDteFragment extends Fragment {

    private static final String PARAMS_KEY = "params";

    public record Params(String msg, int msgTag,
                         boolean keypadUpdate) implements Serializable {
    }


    private final ArrayList<Pair<Button, Integer>> kbdButtonList = new ArrayList<>();
    private TextView textViewPin;
    private TextView textViewPinMsg;
    private boolean Is_Keypad_Update;
    private Params params;
    private CloseFragmentListener closeFragmentListener;

    private final EventListener eventListener = event -> {
        switch (event.getEvent()) {
            case kbd_press:
                break;
            case kbd_release:
            case kbd_del_one_char:
            case kbd_del_all_char:
                updatePin(((EventKbd) event).getNbPressedDigit());
                break;
            case ppt_pin_ok:
            case ppt_pin_blocked:
                textViewPinMsg.setText(Localization.getMsg(((EventPptResult) event).getTag(),
                        ((EventPptResult) event).getText()));
                closeFragmentListener.onCloseFragment();
                break;
            case ppt_pin_wrong:
                textViewPinMsg.setText(Localization.getMsg(((EventPptResult) event).getTag(),
                        ((EventPptResult) event).getText()));
                break;

            default:
                break;
        }
    };

    public static PinPromptDteFragment newInstance(final Params params) {
        Bundle args = new Bundle();
        args.putSerializable(PARAMS_KEY, params);
        PinPromptDteFragment fragment = new PinPromptDteFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof CloseFragmentListener) {
            closeFragmentListener = (CloseFragmentListener) context;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        RPCManager.getInstance().registerSvppEventListener(eventListener);
    }

    @Override
    public void onPause() {
        RPCManager.getInstance().unregisterSvppEventListener(eventListener);
        super.onPause();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_pin_prompt, container, false);

        textViewPin = view.findViewById(R.id.textViewPIN);
        textViewPinMsg = view.findViewById(R.id.textViewPinMsg);

        kbdButtonList.add(new Pair<>(view.findViewById(R.id.button0), Constants.KEYPAD_BUTTON_0));
        kbdButtonList.add(new Pair<>(view.findViewById(R.id.button1), Constants.KEYPAD_BUTTON_1));
        kbdButtonList.add(new Pair<>(view.findViewById(R.id.button2), Constants.KEYPAD_BUTTON_2));
        kbdButtonList.add(new Pair<>(view.findViewById(R.id.button3), Constants.KEYPAD_BUTTON_3));
        kbdButtonList.add(new Pair<>(view.findViewById(R.id.button4), Constants.KEYPAD_BUTTON_4));
        kbdButtonList.add(new Pair<>(view.findViewById(R.id.button5), Constants.KEYPAD_BUTTON_5));
        kbdButtonList.add(new Pair<>(view.findViewById(R.id.button6), Constants.KEYPAD_BUTTON_6));
        kbdButtonList.add(new Pair<>(view.findViewById(R.id.button7), Constants.KEYPAD_BUTTON_7));
        kbdButtonList.add(new Pair<>(view.findViewById(R.id.button8), Constants.KEYPAD_BUTTON_8));
        kbdButtonList.add(new Pair<>(view.findViewById(R.id.button9), Constants.KEYPAD_BUTTON_9));
        kbdButtonList.add(new Pair<>(view.findViewById(R.id.buttonConfirm), Constants.KEYPAD_BUTTON_CONFIRM));
        kbdButtonList.add(new Pair<>(view.findViewById(R.id.buttonCancel), Constants.KEYPAD_BUTTON_ESC));
        kbdButtonList.add(new Pair<>(view.findViewById(R.id.buttonDel), Constants.KEYPAD_BUTTON_CLEAR));
        kbdButtonList.add(new Pair<>(view.findViewById(R.id.buttonClear), Constants.KEYPAD_BUTTON_CLEAR));

        if (ProductManager.id == ProductIdentifier.stick) {
            view.findViewById(R.id.pinGrid).setVisibility(View.GONE);
        } else {
            this.setupMapping();
        }

        Bundle args = getArguments();
        if (args == null) {
            return view;
        }
        params = (Params) args.getSerializable(PARAMS_KEY);
        if (params == null) {
            return view;
        }

        Is_Keypad_Update = params.keypadUpdate;
        textViewPinMsg.setText(Localization.getMsg(params.msgTag, params.msg));
        return view;
    }


    private void updatePin(byte nb_digit) {
        StringBuffer pinStr = new StringBuffer();
        for (int i = 0; i < nb_digit; i++) {
            pinStr.append('*');
        }
        this.requireActivity().runOnUiThread(() -> textViewPin.setText(pinStr));
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
                    y + ", X2: " + (x + button.getWidth()) + ", Y2: " + (y + button.getHeight()));
            KBDMapping.add(new UpdateKeypad.KBDButton(x, y,
                    x + button.getWidth(),
                    y + button.getHeight(), pair.second));
        }
        if (Is_Keypad_Update) {
            PaymentUtils.update_keypad(KBDMapping, (event, params) -> {
                switch (event) {
                    case FAILED:
                        closeFragmentListener.onCloseFragment();
                        break;
                    case SUCCESS:
                        break;
                }
            });
        }
    }
}
