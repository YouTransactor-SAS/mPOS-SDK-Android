/**
 * 2006-2025 YOUTRANSACTOR ALL RIGHTS RESERVED. YouTransactor,
 * 32, rue Brancion 75015 Paris France, RCS PARIS: B 491 208 500, YOUTRANSACTOR
 * CONFIDENTIAL AND PROPRIETARY INFORMATION , CONTROLLED DISTRIBUTION ONLY,
 * THEREFORE UNDER NDA ONLY. YOUTRANSACTOR Authorized Parties and who have
 * signed NDA do not have permission to distribute this documentation further.
 * Unauthorized recipients must destroy any electronic and hard-copies
 * and without reading and notify Gregory Mardinian, CTO, YOUTRANSACTOR
 * immediately at gregory_mardinian@jabil.com.
 *
 * @date: oct. 07, 2025
 * @author: Emmanuel COLAS (emmanuel_colas@jabil.com)
 */

package com.youtransactor.sampleapp.transactionView.components;

import android.content.Context;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jps.secureService.api.product_manager.ProductIdentifier;
import com.jps.secureService.api.product_manager.ProductManager;
import com.youTransactor.uCube.rpc.Constants;
import com.youTransactor.uCube.rpc.EventListener;
import com.youTransactor.uCube.rpc.RPCManager;
import com.youTransactor.uCube.rpc.command.event.kbd.EventKbd;
import com.youtransactor.sampleapp.R;
import com.youtransactor.sampleapp.infrastructure.SystemBars;

import java.util.ArrayList;

public class TestPinPromptFragment extends Fragment {

    private SystemBars systemBars;
    private TextView textViewPin;
    private final ArrayList<Pair<Button, Integer>> kbdButtonList = new ArrayList<>();

    private final EventListener eventListener = event -> {
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
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final Context context = requireContext();
        this.systemBars = new SystemBars(context);
        View view = inflater.inflate(R.layout.fragment_pin_prompt, container, false);

        textViewPin = view.findViewById(R.id.textViewPIN);

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
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        this.systemBars.disable();
        RPCManager.getInstance().registerSvppEventListener(eventListener);
    }

    @Override
    public void onPause() {
        this.systemBars.enable();
        RPCManager.getInstance().unregisterSvppEventListener(eventListener);
        super.onPause();
    }


    private void updatePin(byte nb_digit) {
        StringBuffer pinStr = new StringBuffer();
        for (int i = 0; i < nb_digit; i++) {
            pinStr.append('*');
        }
        this.requireActivity().runOnUiThread(() -> textViewPin.setText(pinStr));
    }
}