/*
 * Copyright (C) 2011-2019, YouTransactor. All Rights Reserved.
 * <p/>
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youTransactor.uCube.bluetooth.ble;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.scan.ScanResult;
import com.youTransactor.uCube.R;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ScanResultsAdapter extends RecyclerView.Adapter<ScanResultsAdapter.ViewHolder> {

	private final LayoutInflater mInflater;

	private List<ScanResult> data = new ArrayList<>();

	private static final Comparator<ScanResult> SORTING_COMPARATOR = (lhs, rhs) -> Integer.compare(rhs.getRssi(), lhs.getRssi());

	private OnAdapterItemClickListener onAdapterItemClickListener;

	interface OnAdapterItemClickListener {

		void onAdapterViewClick(View view);
	}

	ScanResultsAdapter(Activity activity, OnAdapterItemClickListener onAdapterItemClickListener) {
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

	void addScanResult(ScanResult bleScanResult) {

		if(StringUtils.isEmpty(bleScanResult.getBleDevice().getName()))
			return;

		for (int i = 0; i < data.size(); i++) {
			if (data.get(i).getBleDevice().getMacAddress().equals(bleScanResult.getBleDevice().getMacAddress()))
				return;
		}

		data.add(bleScanResult);
		notifyItemInserted(data.size() -1);

		Collections.sort(data, SORTING_COMPARATOR);
		notifyDataSetChanged();
	}

	void clearScanResults() {
		data.clear();
		notifyDataSetChanged();
	}

	ScanResult getItemAtPosition(int childAdapterPosition) {
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

		final ScanResult rxBleScanResult = data.get(position);
		final RxBleDevice bleDevice = rxBleScanResult.getBleDevice();

		holder.textViewMacAddress.setText(bleDevice.getMacAddress());
		holder.textViewName.setText(bleDevice.getName());
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