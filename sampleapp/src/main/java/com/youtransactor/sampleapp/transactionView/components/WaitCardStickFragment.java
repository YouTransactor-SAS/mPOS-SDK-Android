package com.youtransactor.sampleapp.transactionView.components;

import static com.youTransactor.uCube.rpc.Constants.ICC_READER;
import static com.youTransactor.uCube.rpc.Constants.MSR_READER;
import static com.youTransactor.uCube.rpc.Constants.NFC_READER;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.youTransactor.uCube.payment.PaymentService;
import com.youTransactor.uCube.rpc.EventListener;
import com.youTransactor.uCube.rpc.RPCManager;
import com.youTransactor.uCube.rpc.command.event.dsp.EventDspUpdateClessLed;
import com.youtransactor.sampleapp.R;

import java.io.Serializable;

public class WaitCardStickFragment extends Fragment {

    private static final String TAG = WaitCardStickFragment.class.getSimpleName();

    private static final String PARAMS_KEY = "params";

    public record Params(String amount, String msg, int[] itf) implements Serializable {
    }

    private PaymentService paymentService;
    private ImageView led1, led2, led3, led4;
    private Params params;

    private final EventListener eventListener = event -> {
        switch (event.getEvent()) {
            case dsp_update_cless_LED:
                this.requireActivity().runOnUiThread(() -> {
                    led1.setSelected(((EventDspUpdateClessLed) event).getStatusLed1() == 1);
                    led2.setSelected(((EventDspUpdateClessLed) event).getStatusLed2() == 1);
                    led3.setSelected(((EventDspUpdateClessLed) event).getStatusLed3() == 1);
                    led4.setSelected(((EventDspUpdateClessLed) event).getStatusLed4() == 1);
                });
                break;
            default:
                break;
        }
    };

    public static WaitCardStickFragment newInstance(final Params params) {
        Bundle args = new Bundle();
        args.putSerializable(PARAMS_KEY, params);
        WaitCardStickFragment fragment = new WaitCardStickFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wait_card_stick, container, false);

        paymentService = PaymentService.INSTANCE;

        TextView textViewAmount = view.findViewById(R.id.textViewAmount);
        TextView textViewMsg = view.findViewById(R.id.textViewMsg);
        Button buttonCancel = view.findViewById(R.id.buttonCancel);
        led1 = view.findViewById(R.id.led1);
        led2 = view.findViewById(R.id.led2);
        led3 = view.findViewById(R.id.led3);
        led4 = view.findViewById(R.id.led4);
        ImageView imageViewSwipe = view.findViewById(R.id.imageViewSwipe);
        ImageView imageViewInsert = view.findViewById(R.id.imageViewInsert);
        ImageView imageViewNFC = view.findViewById(R.id.imageViewNFC);

        imageViewSwipe.setVisibility(View.GONE);
        imageViewInsert.setVisibility(View.GONE);
        imageViewNFC.setVisibility(View.GONE);
        led1.setVisibility(View.GONE);
        led1.setSelected(true);
        led2.setVisibility(View.GONE);
        led3.setVisibility(View.GONE);
        led4.setVisibility(View.GONE);

        buttonCancel.setOnClickListener(v -> cancel());

        Bundle args = getArguments();
        if (args != null) {
            params = (Params) args.getSerializable(PARAMS_KEY);
            if (params != null) {
                if (params.amount != null) {
                    textViewAmount.setText(params.amount);
                }
                if (params.msg != null) {
                    textViewMsg.setText(params.msg);
                }
                if (params.itf != null) {
                    for (int anInterface : params.itf) {
                        switch (anInterface) {
                            case ICC_READER:
                                imageViewInsert.setVisibility(View.VISIBLE);
                                break;
                            case NFC_READER:
                                imageViewNFC.setVisibility(View.VISIBLE);
                                led1.setVisibility(View.VISIBLE);
                                led2.setVisibility(View.VISIBLE);
                                led3.setVisibility(View.VISIBLE);
                                led4.setVisibility(View.VISIBLE);
                                break;
                            case MSR_READER:
                                imageViewSwipe.setVisibility(View.VISIBLE);
                                break;
                        }
                    }
                }
            }
        }

        return view;
    }

    private void cancel() {
        paymentService.cancel(status -> {
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: registerSvppEventListener");
        RPCManager.getInstance().registerSvppEventListener(eventListener);
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause: unregisterSvppEventListener");
        RPCManager.getInstance().unregisterSvppEventListener(eventListener);
        super.onPause();
    }
}