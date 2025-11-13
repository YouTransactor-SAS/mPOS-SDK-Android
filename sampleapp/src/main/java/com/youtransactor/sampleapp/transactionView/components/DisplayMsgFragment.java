package com.youtransactor.sampleapp.transactionView.components;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.youtransactor.sampleapp.R;
import com.youtransactor.sampleapp.payment.Localization;

import java.io.Serializable;

public class DisplayMsgFragment extends Fragment {

    private static final String PARAMS_KEY = "params";

    public static final int DISPLAY_MSG_ID_FAILED = 0;
    public static final int DISPLAY_MSG_ID_APPROVED = 1;
    public static final int DISPLAY_MSG_ID_UNSUPPORTED = 2;
    public static final int DISPLAY_MSG_ID_WAITING = 3;

    public record Params(int result, String msg, int msgTag) implements Serializable {
    }

    private Params params;

    public static DisplayMsgFragment newInstance(final Params params) {
        Bundle args = new Bundle();
        args.putSerializable(PARAMS_KEY, params);
        DisplayMsgFragment fragment = new DisplayMsgFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction_result, container, false);

        TextView textViewMsg = view.findViewById(R.id.textViewResultMsg);
        ImageView imageViewResult = view.findViewById(R.id.imageViewResult);

        Bundle args = getArguments();
        if (args != null) {
            params = (Params) args.getSerializable(PARAMS_KEY);
            if (params != null) {
                switch (params.result) {
                    case DISPLAY_MSG_ID_APPROVED:
                        imageViewResult.setImageResource(R.drawable.approved);
                        break;
                    case DISPLAY_MSG_ID_UNSUPPORTED:
                        imageViewResult.setImageResource(R.drawable.unsupported_card);
                        break;
                    case DISPLAY_MSG_ID_FAILED:
                        imageViewResult.setImageResource(R.drawable.failed);
                        break;
                    case DISPLAY_MSG_ID_WAITING:
                        imageViewResult.setImageResource(R.drawable.waiting);
                        break;
                    default:
                        imageViewResult.setVisibility(View.GONE);
                }
                textViewMsg.setText(Localization.getMsg(params.msgTag, params.msg));
            }
        }

        return view;
    }
}
