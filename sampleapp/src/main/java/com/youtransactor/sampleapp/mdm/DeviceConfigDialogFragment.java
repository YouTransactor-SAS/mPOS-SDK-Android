/*
 * Copyright (C) 2011-2021, YouTransactor. All Rights Reserved.
 *
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youtransactor.sampleapp.mdm;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.youTransactor.uCube.mdm.Config;

import java.util.List;

public class DeviceConfigDialogFragment extends DialogFragment {

	private List<Config> configList;
	private View view;

	public void init(List<Config> configList) {
		this.configList = configList;
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (view != null) {
			return super.onCreateView(inflater, container, savedInstanceState);
		}

		initView(inflater, container);

		return view;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		initView(LayoutInflater.from(getContext()), null);

		return new AlertDialog.Builder(getActivity())
				.setView(view)
				.setTitle("UCubeDevice configuration")
				.setPositiveButton("Ok", null)
				.create();
	}

	private void initView(LayoutInflater inflater, ViewGroup container) {
		if (configList != null && configList.size() > 0) {
			ExpandableListView listView = new ExpandableListView(getContext());
			listView.setAdapter(new DeviceConfigAdapter(configList));

			view = listView;

		} else {
			TextView txtView = new TextView(getContext());
			txtView.setText("No config found");

			view = txtView;
		}
	}

}
