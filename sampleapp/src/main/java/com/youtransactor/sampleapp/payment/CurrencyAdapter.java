/*
 * Copyright (C) 2011-2021, YouTransactor. All Rights Reserved.
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

import com.youTransactor.uCube.rpc.Currency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class CurrencyAdapter extends BaseAdapter {

	private List<Currency> currencyList = new ArrayList<>();

	@Override
	public int getCount() {
		return currencyList.size();
	}

	@Override
	public Object getItem(int position) {
		return currencyList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void add(Currency currency) {
		currencyList.add(currency);
		Collections.sort(currencyList, new Currency.ByLabelComparator());
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = new TextView(parent.getContext());
			convertView.setPadding(20, 20, 20, 20);
		}

		((TextView) convertView).setText(currencyList.get(position).getLabel());

		return convertView;
	}

}
