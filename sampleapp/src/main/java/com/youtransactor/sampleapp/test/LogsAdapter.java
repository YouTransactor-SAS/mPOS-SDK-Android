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

    private List<String> logs;

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
}
