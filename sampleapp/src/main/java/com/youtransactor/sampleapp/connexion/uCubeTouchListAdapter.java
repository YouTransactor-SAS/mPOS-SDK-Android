/*
 * Copyright (C) 2011-2020, YouTransactor. All Rights Reserved.
 * <p/>
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
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

    private List<UCubeDevice> mLeDevices = new ArrayList<>();

    private static final Comparator<UCubeDevice> SORTING_COMPARATOR = (lhs, rhs) -> Integer.compare(rhs.getRssi(), lhs.getRssi());

    private OnAdapterItemClickListener onAdapterItemClickListener;

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
