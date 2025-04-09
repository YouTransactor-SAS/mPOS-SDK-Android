package com.youtransactor.sampleapp.transactionView;

import static com.youtransactor.sampleapp.transactionView.view_factory.View_index.list;
import static com.youtransactor.sampleapp.transactionView.view_factory.View_index.txt;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.youTransactor.uCube.rpc.EventListener;
import com.youTransactor.uCube.rpc.RPCManager;
import com.youTransactor.uCube.rpc.command.event.EventCommand;
import com.youTransactor.uCube.rpc.command.event.dsp.EventDspTxt;
import com.youtransactor.sampleapp.R;

public class DisplayTxt extends TransactionViewBase {

    public static final String INTENT_EXTRA_DISPLAY_TXT_MSG = "INTENT_EXTRA_DISPLAY_TXT_MSG";
    private TextView textViewMsg;
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_display_txt);

        textViewMsg = findViewById(R.id.textViewMsg);

        Intent intent = getIntent();
        if ((intent != null)) {
            if (intent.getStringExtra(INTENT_EXTRA_DISPLAY_TXT_MSG) != null) {
                textViewMsg.setText(intent.getStringExtra(INTENT_EXTRA_DISPLAY_TXT_MSG));
            }
        }
    }

}
