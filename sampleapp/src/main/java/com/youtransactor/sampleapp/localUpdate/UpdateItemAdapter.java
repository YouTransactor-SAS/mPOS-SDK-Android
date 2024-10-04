/*
 * ============================================================================
 *
 * Copyright (c) 2024 JABIL Payment Solution
 *
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of JABIL Payment Solution
 * ("Confidential Information"). You  shall not disclose or redistribute such
 * Confidential Information and shall use it only in accordance with the terms of
 * the license agreement you entered into with JABIL Payment Solution.
 *
 * This software is provided by JABIL Payment Solution AS IS, and JABIL Payment Solution
 * makes no representations or warranties about the suitability of the software,
 * either express or implied, including but not limited to the implied warranties
 * of merchantability, fitness for a particular purpose or non-infringement.
 * JABIL Payment Solution shall not be liable for any direct, indirect, incidental,
 * special, exemplary, or consequential damages suffered by licensee as the
 * result of using, modifying or distributing this software or its derivatives.
 *
 * ==========================================================================
 */
package com.youtransactor.sampleapp.localUpdate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.youTransactor.uCube.mdm.UpdateItem;
import com.youtransactor.sampleapp.R;

public class UpdateItemAdapter extends ArrayAdapter<UpdateItem> {

    private UpdateItem selected;

    public UpdateItemAdapter(Context context, int viewId, int viewItemId) {
       super(context, viewId, viewItemId);
    }

    public boolean contains(@NonNull String label) {
        for (int i = 0; i < getCount(); i++) {
            if (label.equals(getItem(i).label())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void remove(@Nullable UpdateItem object) {
        if (object == selected) {
            selected = null;
        }
        super.remove(object);
    }

    public UpdateItem getSelected() {
        return selected;
    }

    public void setSelected(UpdateItem selected) {
        this.selected = selected;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.update_item_layout, parent, false);
        }

        UpdateItem item = getItem(position);

        TextView fld = convertView.findViewById(R.id.item_label_fld);
        fld.setText(item.label());

        fld = convertView.findViewById(R.id.item_type_fld);
        fld.setText(item.type().name());

        fld = convertView.findViewById(R.id.item_version_fld);
        fld.setText(item.version());

        return convertView;
    }

}
