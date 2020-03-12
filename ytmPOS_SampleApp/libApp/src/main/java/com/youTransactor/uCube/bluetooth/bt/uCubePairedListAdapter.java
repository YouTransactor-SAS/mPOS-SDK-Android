package com.youTransactor.uCube.bluetooth.bt;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.youTransactor.uCube.R;
import java.util.List;

public class uCubePairedListAdapter extends RecyclerView.Adapter<uCubePairedListAdapter.ViewHolder> {

    private final LayoutInflater mInflater;

    private List<BluetoothDevice> data;

    private OnAdapterItemClickListener onAdapterItemClickListener;

    interface OnAdapterItemClickListener {

        void onAdapterViewClick(View view);
    }

    uCubePairedListAdapter(Activity activity, List<BluetoothDevice> data,
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

    BluetoothDevice getItemAtPosition(int childAdapterPosition) {
        return data.get(childAdapterPosition);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View itemView  = mInflater.inflate(R.layout.ucube_card_layout, parent, false);
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

            textViewName = itemView.findViewById(R.id.textViewName);
            textViewMacAddress = itemView.findViewById(R.id.textViewMacAddress);
        }
    }
}
