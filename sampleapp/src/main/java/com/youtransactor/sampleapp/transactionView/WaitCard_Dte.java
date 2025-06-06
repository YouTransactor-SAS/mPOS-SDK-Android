package com.youtransactor.sampleapp.transactionView;

import static com.youTransactor.uCube.rpc.Constants.ICC_READER;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.youTransactor.uCube.payment.PaymentService;
import com.youTransactor.uCube.rpc.Constants;
import com.youTransactor.uCube.rpc.command.event.EventCommand;
import com.youTransactor.uCube.rpc.command.event.dsp.EventDspTxt;
import com.youTransactor.uCube.rpc.command.event.dsp.EventDspUpdateClessLed;
import com.youTransactor.uCube.rpc.command.event.dsp.EventDspWaitCard;
import com.youtransactor.sampleapp.R;
import com.youtransactor.sampleapp.payment.Localization;

import java.util.Objects;

public class WaitCard_Dte extends TransactionViewBaseDte {

    private PaymentService paymentService;
    public static final String INTENT_EXTRA_WAIT_CARD_AMOUNT = "INTENT_EXTRA_WAIT_CARD_AMOUNT";
    public static final String INTENT_EXTRA_WAIT_CARD_MSG = "INTENT_EXTRA_WAIT_CARD_MSG";
    public static final String INTENT_EXTRA_WAIT_CARD_MSG_TAG = "INTENT_EXTRA_WAIT_CARD_MSG_TAG";
    public static final String INTENT_EXTRA_ITF = "INTENT_EXTRA_ITF";


    private ImageView led1, led2, led3, led4;
    private TextView textViewMsg;
    TextView textViewAmount;
    private static WaitCard_Dte instance;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait_card);
        Objects.requireNonNull(getSupportActionBar()).hide();
        this.setHomeActivity(this.getClass());
        instance = this;
        paymentService = PaymentService.INSTANCE;
        textViewAmount = findViewById(R.id.textViewAmount);
        textViewMsg = findViewById(R.id.textViewMsg);
        Button buttonCancel = findViewById(R.id.buttonCancel);
        led1 = findViewById(R.id.led1);
        led2 = findViewById(R.id.led2);
        led3 = findViewById(R.id.led3);
        led4 = findViewById(R.id.led4);
        ImageView imageViewSwipe = findViewById(R.id.imageViewSwipe);
        ImageView imageViewInsert = findViewById(R.id.imageViewInsert);
        ImageView imageViewNFC = findViewById(R.id.imageViewNFC);

        imageViewSwipe.setVisibility(View.GONE);
        imageViewInsert.setVisibility(View.GONE);
        imageViewNFC.setVisibility(View.GONE);
        led1.setVisibility(View.GONE);
        led2.setVisibility(View.GONE);
        led3.setVisibility(View.GONE);
        led4.setVisibility(View.GONE);

        buttonCancel.setOnClickListener(v -> cancel());

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getStringExtra(INTENT_EXTRA_WAIT_CARD_AMOUNT) != null) {
                textViewAmount.setText(intent.getStringExtra(INTENT_EXTRA_WAIT_CARD_AMOUNT));
            }
            else{
                textViewAmount.setText("");
            }
            textViewMsg.setText(Localization.getMsg(intent.getIntExtra(INTENT_EXTRA_WAIT_CARD_MSG_TAG, -1),
                    intent.getStringExtra(INTENT_EXTRA_WAIT_CARD_MSG)));
            int reader = intent.getByteExtra(INTENT_EXTRA_ITF, Constants.NFC_READER);
            if(reader == Constants.NFC_READER) {
                Log.d("contactCertifIntent", "ICC_READER");
                imageViewNFC.setVisibility(View.VISIBLE);
                led1.setVisibility(View.VISIBLE);
                led2.setVisibility(View.VISIBLE);
                led3.setVisibility(View.VISIBLE);
                led4.setVisibility(View.VISIBLE);
            }
            else{
                imageViewInsert.setVisibility(View.VISIBLE);
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

            case dsp_wait_card:
                led1.setSelected(true);
                runOnUiThread(() -> textViewAmount.setText(((EventDspWaitCard) event).getAmount()));
                runOnUiThread(() -> textViewMsg.setText(((EventDspWaitCard) event).getMessage()));
                break;
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

    public static WaitCard_Dte getInstance() {
        return instance;
    }
    public void update_text(String message){
        runOnUiThread(() -> textViewMsg.setText(message));
        runOnUiThread(() -> textViewAmount.setText(""));
    }
    private void cancel() {
        paymentService.cancel(status -> {});
    }
}
