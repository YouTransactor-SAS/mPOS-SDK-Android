package com.youtransactor.sampleapp.transactionView;

import static com.youTransactor.uCube.rpc.Constants.ICC_READER;
import static com.youTransactor.uCube.rpc.Constants.MSR_READER;
import static com.youTransactor.uCube.rpc.Constants.NFC_READER;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.youTransactor.uCube.rpc.command.event.EventCommand;
import com.youTransactor.uCube.rpc.command.event.dsp.EventDspUpdateClessLed;
import com.youtransactor.sampleapp.DemoActivity;
import com.youtransactor.sampleapp.R;

import java.util.Objects;

public class WaitCard extends TransactionViewBase {

    public static final String INTENT_EXTRA_WAIT_CARD_AMOUNT = "INTENT_EXTRA_WAIT_CARD_AMOUNT";
    public static final String INTENT_EXTRA_WAIT_CARD_MSG = "INTENT_EXTRA_WAIT_CARD_MSG";
    public static final String INTENT_EXTRA_WAIT_CARD_ITF = "INTENT_EXTRA_WAIT_CARD_ITF";
    private ImageView led1, led2, led3, led4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_wait_card);
        Objects.requireNonNull(getSupportActionBar()).hide();

        TextView textViewAmount = findViewById(R.id.textViewAmount);
        TextView textViewMsg = findViewById(R.id.textViewMsg);
        Button buttonCancel = findViewById(R.id.buttonCancel);
        led1 =              findViewById(R.id.led1);
        led2 =              findViewById(R.id.led2);
        led3 =              findViewById(R.id.led3);
        led4 =              findViewById(R.id.led4);
        ImageView imageViewSwipe = findViewById(R.id.imageViewSwipe);
        ImageView imageViewInsert = findViewById(R.id.imageViewInsert);
        ImageView imageViewNFC = findViewById(R.id.imageViewNFC);

        imageViewSwipe.setVisibility(View.GONE);
        imageViewInsert.setVisibility(View.GONE);
        imageViewNFC.setVisibility(View.GONE);
        led1.setVisibility(View.GONE);
        led1.setSelected(true);
        led2.setVisibility(View.GONE);
        led3.setVisibility(View.GONE);
        led4.setVisibility(View.GONE);

        buttonCancel.setOnClickListener(v -> cancel());

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getStringExtra(INTENT_EXTRA_WAIT_CARD_AMOUNT) != null) {
                textViewAmount.setText(intent.getStringExtra(INTENT_EXTRA_WAIT_CARD_AMOUNT));
            }
            if (intent.getStringExtra(INTENT_EXTRA_WAIT_CARD_MSG) != null) {
                textViewMsg.setText(intent.getStringExtra(INTENT_EXTRA_WAIT_CARD_MSG));
            }
            int[] interfaces = intent.getIntArrayExtra(INTENT_EXTRA_WAIT_CARD_ITF);
            if (interfaces != null) {
                for (int anInterface : interfaces) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onEventViewUpdate(EventCommand event) {
        switch (event.getEvent()) {
            case dsp_update_cless_LED:
                led1.setSelected(((EventDspUpdateClessLed) event).getStatusLed1() == 1);
                led2.setSelected(((EventDspUpdateClessLed) event).getStatusLed2() == 1);
                led3.setSelected(((EventDspUpdateClessLed) event).getStatusLed3() == 1);
                led4.setSelected(((EventDspUpdateClessLed) event).getStatusLed4() == 1);
                break;

            default:
                break;
        }
    }

    private void cancel() {
        Intent intent = new Intent(this, DemoActivity.class);
        startActivity(intent);
    }
}
