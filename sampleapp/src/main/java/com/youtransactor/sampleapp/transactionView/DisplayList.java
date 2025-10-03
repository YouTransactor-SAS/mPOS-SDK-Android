package com.youtransactor.sampleapp.transactionView;

import static com.youTransactor.uCube.rpc.Constants.DISPLAY_LIST_NO_ITEM_SELECTED;
import static com.youTransactor.uCube.rpc.Constants.EVT_APP_SELECT_LANG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.youTransactor.uCube.payment.PaymentUtils;
import com.youtransactor.sampleapp.R;

import java.util.ArrayList;

public class DisplayList extends TransactionViewBase {
    private static final String TAG = DisplayList.class.getSimpleName();
    private ListView listView;
    private int list_type;
    public static final String INTENT_EXTRA_DISPLAY_LIST_MSG = "INTENT_EXTRA_DISPLAY_LIST_MSG";
    public static final String INTENT_EXTRA_DISPLAY_LIST_TYPE = "INTENT_EXTRA_DISPLAY_LIST_TYPE";
    public static final int INTENT_EXTRA_DISPLAY_LIST_LANGUAGE = 0;
    public static final int INTENT_EXTRA_DISPLAY_LIST_AID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_list);
        listView = findViewById(R.id.list_view);

        Intent intent = getIntent();
        ArrayList<String> itemList = null;
        if ((intent != null)) {
            if (intent.getStringArrayListExtra(INTENT_EXTRA_DISPLAY_LIST_MSG) != null) {
                itemList = getIntent().getStringArrayListExtra("INTENT_EXTRA_DISPLAY_LIST_MSG");
            }
            list_type = getIntent().getIntExtra("INTENT_EXTRA_DISPLAY_LIST_TYPE",
                    INTENT_EXTRA_DISPLAY_LIST_LANGUAGE);
        }
        if((itemList.isEmpty()) && (list_type == INTENT_EXTRA_DISPLAY_LIST_LANGUAGE)){
            PaymentUtils.evtSelectedItem(EVT_APP_SELECT_LANG, DISPLAY_LIST_NO_ITEM_SELECTED,
                    (event, params) -> {
                switch (event) {
                    case FAILED, SUCCESS:
                        break;
                }
            });
        }
        else {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.list_item, R.id.text_item, itemList);
            Log.e(TAG, String.format("Item: " + itemList.size()));
            for (String item : itemList) {
                Log.e(TAG, "Item: " + item);
            }
            listView.setAdapter(adapter);
            // Set an OnItemClickListener to detect item clicks
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String clickedItem = (String) parent.getItemAtPosition(position);
                    Toast.makeText(DisplayList.this, "Clicked: " + clickedItem, Toast.LENGTH_SHORT).show();
                    if (list_type == 0) {
                        PaymentUtils.evtSelectedItem(EVT_APP_SELECT_LANG, position, (event, params) -> {
                            switch (event) {
                                case FAILED, SUCCESS:
                                    break;
                            }
                        });
                    } else {
                        byte[] tlv = new byte[0];
                        PaymentUtils.send_event_filter_cless_aid(position, tlv, (event, params) -> {
                            switch (event) {
                                case FAILED, SUCCESS:
                                    break;
                            }
                        });
                    }
                    DisplayList.this.finish();
                }
            });
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    protected void onStop() {
        super.onStop();
        //TODO unbind secure service
    }

    @Override
    protected void onDestroy() {
        //TODO unregister secure service listener
        super.onDestroy();
    }


}