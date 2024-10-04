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
package com.youtransactor.sampleapp.connexion;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.youTransactor.uCube.connexion.UCubeDevice;
import com.youtransactor.sampleapp.R;

import java.util.ArrayList;
import java.util.List;

public class DeviceAdaptor extends BaseAdapter {

    private final Context context;
    private final List<UCubeDevice> data = new ArrayList<>();
    private int imageResIs = -1;

    public DeviceAdaptor(Context context) {
        this.context = context;
    }

    public DeviceAdaptor imageResource(int imageResIs) {
        this.imageResIs = imageResIs;
        return this;
    }

    public void clear() {
        data.clear();
        notifyDataSetChanged();
    }

    public boolean contains(UCubeDevice device) {
        for (UCubeDevice item : data) {
            if (item.equals(device)) {
                return true;
            }
        }
        return false;
    }

    public void add(@Nullable UCubeDevice device) {
        if (device == null || contains(device)) {
            return;
        }

        data.add(device);
        notifyDataSetChanged();
    }

    public void addAll(List<UCubeDevice> deviceList) {
        if (deviceList == null) {
            return;
        }

        boolean changed  = false;
        for (UCubeDevice device : deviceList) {
            for (UCubeDevice item : data) {
                if (!contains(item)) {
                    changed = true;
                    add(device);
                    break;
                }
            }
        }

        if (changed) notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public UCubeDevice getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return data.get(position).getAddress().hashCode();
    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.ucube_card_layout, parent, false);
        }

        UCubeDevice item = data.get(position);

        if (imageResIs == -1){
            convertView.findViewById(R.id.imageView).setVisibility(View.GONE);
        } else {
            ((ImageView) convertView.findViewById(R.id.imageView)).setImageResource(imageResIs);
        }
        ((TextView) convertView.findViewById(R.id.textViewName)).setText(item.getName());
        ((TextView) convertView.findViewById(R.id.textViewMacAddress)).setText(item.getAddress());

        return convertView;
    }

}
