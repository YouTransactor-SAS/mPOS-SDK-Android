package com.youtransactor.sampleapp.transactionView;

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
    public static final String INTENT_EXTRA_DISPLAY_LIST_MSG = "INTENT_EXTRA_DISPLAY_LIST_MSG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_list);
        listView = findViewById(R.id.list_view);

        Intent intent = getIntent();
        ArrayList<String> itemList = null;
        if ((intent != null)) {
            if (intent.getStringArrayListExtra(INTENT_EXTRA_DISPLAY_LIST_MSG) != null) {
                Log.e(TAG, "getStringArrayListExtra Not NULL");
                itemList = getIntent().getStringArrayListExtra("INTENT_EXTRA_DISPLAY_LIST_MSG");
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.list_item, R.id.text_item, itemList);
        Log.e(TAG, String.format("Item: " + itemList.size()));
        for (String item : itemList) {
            Log.e(TAG, "Item: " + item);
        }
        listView.setAdapter(adapter);
        Log.e(TAG, "onCreate");
        // Set an OnItemClickListener to detect item clicks
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String clickedItem = (String) parent.getItemAtPosition(position);
                Toast.makeText(DisplayList.this, "Clicked: " + clickedItem, Toast.LENGTH_SHORT).show();
                PaymentUtils.evtSelectedItem(EVT_APP_SELECT_LANG, position, (event, params) -> {
                    switch (event) {
                        case FAILED:
                            break;
                        case SUCCESS:
                            break;
                    }
                });
                DisplayList.this.finish();
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "onStop");
        //TODO unbind secure service
    }

    @Override
    protected void onDestroy() {
        //TODO unregister secure service listener
        super.onDestroy();
    }


}