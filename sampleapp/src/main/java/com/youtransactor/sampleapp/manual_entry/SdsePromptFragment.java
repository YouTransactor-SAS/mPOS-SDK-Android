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
package com.youtransactor.sampleapp.manual_entry;

import android.app.Activity;
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
import android.widget.Toast;

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
import com.youTransactor.uCube.rpc.command.event.ppt.EventPptSdse;
import com.youtransactor.sampleapp.R;
import com.youtransactor.sampleapp.features.SdseSession;
import com.youtransactor.sampleapp.infrastructure.SystemBars;
import com.youtransactor.sampleapp.transactionView.components.CloseFragmentListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SdsePromptFragment extends Fragment {

    private static final String TAG = SdsePromptFragment.class.getSimpleName();
    private static final String PARAMS_KEY = "params";

    public record SdsePromptParams(String msg, int type) implements Serializable {
    }

    private TextView textViewSdseMsg;
    private TextView textViewSdse;

    private ArrayList<Pair<Button, Integer>> kbdButtonList;
    private List<UpdateKeypad.KBDButton> KBDMapping = new ArrayList<>();

    private SystemBars systemBars;

    private SdsePromptParams params;

    private int sdsePromptType;
    private CloseFragmentListener closeFragmentListener;


    private final EventListener eventListener = event -> {
        switch (event.getEvent()) {
            case kbd_release:
                break;
            case kbd_press:
            case kbd_del_one_char:
            case kbd_del_all_char:
                updateSdse(((EventKbd) event).getNbPressedDigit());
                break;
            case ppt_sdse:
                final EventPptSdse sdseEvent = (EventPptSdse) event;
                final int type = sdseEvent.getSdse_type();
                Log.d(TAG, "SDSE Event type received: " + type);
                if (type != sdsePromptType) {
                    SdsePromptParams params = switch (((EventPptSdse) event).getSdse_type()) {
                        case SdseSession.SDSE_TYPE_PAN -> new SdsePromptParams(
                                getString(R.string.set_pan),
                                ((EventPptSdse) event).getSdse_type()
                        );
                        case SdseSession.SDSE_TYPE_CVV -> new SdsePromptParams(
                                getString(R.string.set_cvv),
                                ((EventPptSdse) event).getSdse_type()
                        );
                        case SdseSession.SDSE_TYPE_DATE -> new SdsePromptParams(
                                getString(R.string.set_exp_date),
                                ((EventPptSdse) event).getSdse_type()
                        );
                        default -> null;
                    };
                    final SdsePromptFragment fragment = SdsePromptFragment.newInstance(params);
                    this.requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .commit();
                }
                break;
        }
    };

    public static SdsePromptFragment newInstance(final SdsePromptParams params) {
        Bundle args = new Bundle();
        args.putSerializable(PARAMS_KEY, params);
        SdsePromptFragment fragment = new SdsePromptFragment();
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final Context context = requireContext();
        this.systemBars = new SystemBars(context);

        View view = inflater.inflate(R.layout.fragment_pan_prompt, container, false);

        textViewSdseMsg = view.findViewById(R.id.textViewSdseMsg);
        textViewSdse = view.findViewById(R.id.textViewPan);
        kbdButtonList = new ArrayList<>();
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

        Bundle args = getArguments();
        if (args == null) {
            return view;
        }
        params = (SdsePromptParams) args.getSerializable(PARAMS_KEY);
        if (params == null) {
            return view;
        }
        sdsePromptType = params.type;
        textViewSdseMsg.setText(params.msg);
        new Thread(() -> {
            try {
                starThread();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

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
        super.onPause();
        RPCManager.getInstance().unregisterSvppEventListener(eventListener);

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
                            (kbdButtonList.size() != 0)) {
                        if (ProductManager.id == ProductIdentifier.blade) {
                            sendMapping();
                        }
                    }
                }
            });
        }
    }

    private void sendMapping() {
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
                    final Activity activity = this.requireActivity();
                    activity.runOnUiThread(() -> Toast.makeText(activity, "Update Keypad fail", Toast.LENGTH_LONG).show());
                    closeFragmentListener.onCloseFragment();
                    break;
                case SUCCESS:
                    break;
                default:
                    break;
            }
        });
    }

    private void updateSdse(byte nb_digit) {
        StringBuffer pinStr = new StringBuffer();
        for (int i = 0; i < nb_digit; i++) {
            pinStr.append('*');
        }
        this.requireActivity().runOnUiThread(() -> textViewSdse.setText(pinStr));
    }
}