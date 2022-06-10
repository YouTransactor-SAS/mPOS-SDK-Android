/*
 * ============================================================================
 *
 * Copyright (c) 2022 YouTransactor
 *
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of YouTransactor
 * ("Confidential Information"). You  shall not disclose or redistribute such
 * Confidential Information and shall use it only in accordance with the terms of
 * the license agreement you entered into with YouTransactor.
 *
 * This software is provided by YouTransactor AS IS, and YouTransactor
 * makes no representations or warranties about the suitability of the software,
 * either express or implied, including but not limited to the implied warranties
 * of merchantability, fitness for a particular purpose or non-infringement.
 * YouTransactor shall not be liable for any direct, indirect, incidental,
 * special, exemplary, or consequential damages suffered by licensee as the
 * result of using, modifying or distributing this software or its derivatives.
 *
 * ==========================================================================
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

    private final List<PaymentState> stateList;

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
