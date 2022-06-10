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
package com.youtransactor.sampleapp.connexion;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.youTransactor.uCube.connexion.UCubeDevice;
import com.youtransactor.sampleapp.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class uCubeTouchListAdapter extends RecyclerView.Adapter<uCubeTouchListAdapter.ViewHolder> {

    private final LayoutInflater mInflater;

    private final List<UCubeDevice> mLeDevices = new ArrayList<>();

    private static final Comparator<UCubeDevice> SORTING_COMPARATOR = (lhs, rhs) -> Integer.compare(rhs.getRssi(), lhs.getRssi());

    private final OnAdapterItemClickListener onAdapterItemClickListener;

    public interface OnAdapterItemClickListener {

        void onAdapterViewClick(View view);
    }

    public uCubeTouchListAdapter(Activity activity, OnAdapterItemClickListener onAdapterItemClickListener) {
        super();

        mInflater = activity.getLayoutInflater();
        this.onAdapterItemClickListener = onAdapterItemClickListener;
    }

    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (onAdapterItemClickListener != null) {
                onAdapterItemClickListener.onAdapterViewClick(v);
            }
        }
    };

    public void addDevice(UCubeDevice device) {
        if(device.getName() == null)
            return;

        for (int i = 0; i < mLeDevices.size(); i++) {
            if (mLeDevices.get(i).getAddress().equals(device.getAddress()))
                return;
        }

        mLeDevices.add(device);
        notifyItemInserted(mLeDevices.size() - 1);

        Collections.sort(mLeDevices, SORTING_COMPARATOR);
        notifyDataSetChanged();
    }

    public UCubeDevice getDevice(int position) {
        return mLeDevices.get(position);
    }

    public void clearScanResults() {
        mLeDevices.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mLeDevices.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        final View itemView = mInflater.inflate(R.layout.ucube_touch_card_layout, parent, false);

        itemView.setOnClickListener(onClickListener);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final UCubeDevice ucubeDevice = mLeDevices.get(position);

        holder.textViewMacAddress.setText(ucubeDevice.getAddress());
        holder.textViewName.setText(ucubeDevice.getName());
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName;
        TextView textViewMacAddress;

        ViewHolder(View itemView) {
            super(itemView);

            textViewName = itemView.findViewById(R.id.device_name);
            textViewMacAddress = itemView.findViewById(R.id.device_address);
        }
    }
}
