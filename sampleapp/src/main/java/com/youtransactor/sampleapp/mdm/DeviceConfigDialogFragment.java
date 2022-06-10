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
