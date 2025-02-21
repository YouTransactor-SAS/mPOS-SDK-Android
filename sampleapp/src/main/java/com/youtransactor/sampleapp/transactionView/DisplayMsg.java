package com.youtransactor.sampleapp.transactionView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.youTransactor.uCube.rpc.command.event.EventCommand;
import com.youtransactor.sampleapp.R;
import com.youtransactor.sampleapp.payment.Localization;

import java.util.Objects;

public class DisplayMsg extends TransactionViewBase {

    public static final String INTENT_EXTRA_DISPLAY_MSG_RSLT = "INTENT_EXTRA_DISPLAY_MSG_RSLT";
    public static final String INTENT_EXTRA_DISPLAY_MSG_MSG = "INTENT_EXTRA_DISPLAY_MSG_MSG";
    public static final String INTENT_EXTRA_DISPLAY_MSG_TAG = "INTENT_EXTRA_DISPLAY_MSG_TAG";

    public static final int DISPLAY_MSG_ID_FAILED = 0;
    public static final int DISPLAY_MSG_ID_APPROVED = 1;
    public static final int DISPLAY_MSG_ID_UNSUPPORTED = 2;
    public static final int DISPLAY_MSG_ID_WAITING = 3;

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_transaction_result);
        Objects.requireNonNull(getSupportActionBar()).hide();

        TextView textViewMsg = findViewById(R.id.textViewResultMsg);
        ImageView imageViewResult = findViewById(R.id.imageViewResult);

        Intent intent = getIntent();
        if ((intent != null)) {
            switch (intent.getIntExtra(INTENT_EXTRA_DISPLAY_MSG_RSLT, 0)) {
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
            textViewMsg.setText(Localization.getMsg(intent.getIntExtra(INTENT_EXTRA_DISPLAY_MSG_TAG, -1),
                    intent.getStringExtra(INTENT_EXTRA_DISPLAY_MSG_MSG)));
        }
    }
}
