/*
 * Copyright (C) 2016, YouTransactor. All Rights Reserved.
 *
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */

package com.youtransactor.sampleapp.payment;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.youTransactor.uCube.payment.PaymentState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PaymentStateAdapter extends BaseAdapter {

    private List<PaymentState> stateList;

    public PaymentStateAdapter() {
        stateList = new ArrayList<>();
        stateList.addAll(Arrays.asList(PaymentState.values()));
        Collections.sort(stateList);
        stateList.add(0, null);
    }

    @Override
    public int getCount() {
        return stateList.size();
    }

    @Override
    public Object getItem(int i) {
        return stateList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = new TextView(viewGroup.getContext());
            view.setPadding(20, 20, 20, 20);
        }

        PaymentState item = stateList.get(i);

        ((TextView) view).setText(item == null ? "None" : item.name());

        return view;
    }

}
