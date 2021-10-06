/*
 * Copyright (C) 2011-2021, YouTransactor. All Rights Reserved.
 *
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

import java.util.List;

public class uCubeTouchPairedListAdapter extends RecyclerView.Adapter<uCubeTouchPairedListAdapter.ViewHolder> {

    private final LayoutInflater mInflater;

    private List<UCubeDevice> data;

    private OnAdapterItemClickListener onAdapterItemClickListener;

    public interface OnAdapterItemClickListener {

        void onAdapterViewClick(View view);
    }

    public uCubeTouchPairedListAdapter(Activity activity, List<UCubeDevice> data,
                                       OnAdapterItemClickListener onAdapterItemClickListener) {
        super();

        mInflater = activity.getLayoutInflater();
        this.data = data;
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

    public UCubeDevice getItemAtPosition(int childAdapterPosition) {
        return data.get(childAdapterPosition);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View itemView  = mInflater.inflate(R.layout.ucube_touch_card_layout, parent, false);
        itemView.setOnClickListener(onClickListener);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textViewMacAddress.setText(data.get(position).getAddress());
        holder.textViewName.setText(data.get(position).getName());
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
