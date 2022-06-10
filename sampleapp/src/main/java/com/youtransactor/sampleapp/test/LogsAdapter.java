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
package com.youtransactor.sampleapp.test;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.youtransactor.sampleapp.R;

import java.util.List;

public class LogsAdapter extends RecyclerView.Adapter<LogsAdapter.ViewHolder> {

    private final List<String> logs;

    public LogsAdapter(List<String> logs) {
        this.logs = logs;
    }

    @NonNull
    @Override
    public LogsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View logView = inflater.inflate(R.layout.custome_row_layout, parent, false);

        return new ViewHolder(logView);
    }

    @Override
    public void onBindViewHolder(LogsAdapter.ViewHolder holder, int position) {
        String log = logs.get(position);

        TextView textView = holder.logTextView;
        textView.setText(log);
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView logTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            logTextView = itemView.findViewById(R.id.log_message);
        }
    }

    public void clear() {
        int size = logs.size();
        logs.clear();
        notifyItemRangeRemoved(0, size);
    }
}
