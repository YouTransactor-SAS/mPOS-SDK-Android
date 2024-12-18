package com.youtransactor.sampleapp.transactionView.view_factory;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.youtransactor.sampleapp.transactionView.DisplayMsg;
import com.youtransactor.sampleapp.transactionView.WaitCard;
import com.youtransactor.sampleapp.product_manager.product_id;
import com.youtransactor.sampleapp.transactionView.WaitCard_stick;

import java.util.ArrayList;
import java.util.List;

public class view_manager {
    private static final String TAG = view_manager.class.getSimpleName();

    public static void addIntentAtIndex(List<Intent> intents, View_index viewIndex, Intent intent) {
        int index = viewIndex.ordinal();
        intents.add(index, intent);
    }

    public static List<Intent> getApplicableIntents(Context context, product_id id) {
        List<Intent> intents = new ArrayList<>();
        switch (id) {
            case blade:
                addIntentAtIndex(intents, View_index.wait_card,
                        new Intent(context, WaitCard.class));
                addIntentAtIndex(intents, View_index.dsp_msg,
                        new Intent(context, DisplayMsg.class));
                break;

            case stick:
                addIntentAtIndex(intents, View_index.wait_card,
                        new Intent(context, WaitCard_stick.class));
                addIntentAtIndex(intents, View_index.dsp_msg,
                        new Intent(context, DisplayMsg.class));
                break;
           default:
               Log.e(TAG, "Worng product family");
               break;
        }

        return intents;
    }
}
