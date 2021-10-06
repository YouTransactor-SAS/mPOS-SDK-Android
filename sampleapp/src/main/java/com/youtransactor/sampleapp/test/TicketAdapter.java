/*
 * Copyright (C) 2011-2021, YouTransactor. All Rights Reserved.
 *
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youtransactor.sampleapp.test;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TicketAdapter extends BaseAdapter {

    private List<Ticket> ticketList;

    public TicketAdapter() {
        ticketList = new ArrayList<>();
        ticketList.addAll(Arrays.asList(Ticket.values()));
        Collections.sort(ticketList);
    }

    @Override
    public int getCount() {
        return ticketList.size();
    }

    @Override
    public Object getItem(int i) {
        return ticketList.get(i);
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

        Ticket item = ticketList.get(i);

        ((TextView) view).setText(item == null ? "None" : item.name());

        return view;
    }

}
