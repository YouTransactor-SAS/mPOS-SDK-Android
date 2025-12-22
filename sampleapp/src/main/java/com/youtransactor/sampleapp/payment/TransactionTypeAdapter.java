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

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.youTransactor.uCube.rpc.TransactionType;

public class TransactionTypeAdapter extends ArrayAdapter<TransactionType> {

    public TransactionTypeAdapter(Context context) {
        super(context, android.R.layout.simple_spinner_item, TransactionType.values());
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = super.getView(position, convertView, parent);
        }
		((TextView) convertView).setText(getItem(position).getLabel());
		return convertView;
	}
}
