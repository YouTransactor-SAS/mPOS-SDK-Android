/*
 * Copyright (C) 2011-2020, YouTransactor. All Rights Reserved.
 * <p/>
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youtransactor.sampleapp;

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

import com.youTransactor.uCube.mdm.service.BinaryUpdate;

import java.util.List;


public class CheckUpdateResultDialog extends DialogFragment {

	private List<BinaryUpdate> updateList;
	private View view;
	private Activity activity;
	private DialogInterface.OnClickListener onClickPositiveButton;

	void init(@NonNull Activity activity, List<BinaryUpdate> updateList, DialogInterface.OnClickListener onClickPositiveButton) {
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
