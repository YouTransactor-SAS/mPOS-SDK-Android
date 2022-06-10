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

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.youTransactor.uCube.mdm.BinaryUpdate;
import com.youtransactor.sampleapp.R;

import java.util.List;


public class CheckUpdateResultDialog extends DialogFragment {

	private List<BinaryUpdate> updateList;
	private View view;
	private Activity activity;
	private DialogInterface.OnClickListener onClickPositiveButton;

	public void init(@NonNull Activity activity, List<BinaryUpdate> updateList, DialogInterface.OnClickListener onClickPositiveButton) {
		this.updateList = updateList;
		this.activity = activity;
		this.onClickPositiveButton = onClickPositiveButton;
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (view != null) {
			return super.onCreateView(inflater, container, savedInstanceState);
		}

		initView();

		return view;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		initView();

		return new AlertDialog.Builder(activity)
				.setView(view)
				.setTitle(getString(R.string.list_update_dialog_title))
				.setPositiveButton(getString(R.string.ok),  onClickPositiveButton)
				.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dismiss())
				.setCancelable(false)
				.create();
	}

	private void initView() {
		if (updateList != null && updateList.size() > 0) {
			LinearLayout linearLayout = new LinearLayout(getContext());
			linearLayout.setOrientation(LinearLayout.VERTICAL);

			GridLayout gridView = new GridLayout(getContext());
			gridView.setColumnCount(2);
			gridView.setRowCount(updateList.size());

			for (BinaryUpdate update : updateList) {
				TextView txtFld = new TextView(getContext());
				txtFld.setPadding(10, 10, 10, 10);
				txtFld.setText(update.getCfg().getLabel());

				gridView.addView(txtFld);

				txtFld = new TextView(getContext());
				txtFld.setPadding(10, 10, 10, 10);
				txtFld.setText(update.getCfg().getCurrentVersion());

				if (update.isMandatory()) {
					txtFld.append(" ");
					txtFld.append(getString(R.string.mandatory));
				}

				gridView.addView(txtFld);
			}

			linearLayout.addView(gridView);

			view = linearLayout;
		}
	}


}
