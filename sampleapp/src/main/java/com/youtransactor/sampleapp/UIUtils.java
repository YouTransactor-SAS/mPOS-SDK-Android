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
package com.youtransactor.sampleapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Objects;

public class UIUtils {

	private static AlertDialog progressDialog;
	private static TextView progressMessageView;

	private UIUtils() {}

	public static AlertDialog showItemsDialog(Context context, String title, CharSequence[] items, DialogInterface.OnClickListener onClickListener) {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
		dialogBuilder.setTitle(title);
		dialogBuilder.setCancelable(false);
		dialogBuilder.setItems(items, onClickListener);
		AlertDialog dialog = dialogBuilder.create();
		dialog.show();
		return dialog;
	}

	public static AlertDialog showOptionDialog(Context context, String text,
										String yesLabel, String noLabel,
										final DialogInterface.OnClickListener listener) {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);

		dialogBuilder.setMessage(text);
		dialogBuilder.setCancelable(false);

		dialogBuilder.setPositiveButton(yesLabel, (dialog, which) -> {
			dialog.dismiss();

			if (listener != null) {
				listener.onClick(dialog, which);
			}
		});

		dialogBuilder.setNegativeButton(noLabel, (dialog, which) -> {
			dialog.dismiss();

			if (listener != null) {
				listener.onClick(dialog, which);
			}
		});

		AlertDialog dialog = dialogBuilder.create();

		dialog.show();

		return dialog;
	}

	public static AlertDialog showProgressDialog(Context context) {
		return showProgress(context, null);
	}

	public static AlertDialog showProgress(Context context, String message) {
		return showProgress(context, message, false);
	}

	public static AlertDialog showProgress(Context context, String message, boolean cancellable) {
		return showProgress(context, message, cancellable, null);
	}

	public static AlertDialog showProgress(Context context, String message, boolean cancellable, DialogInterface.OnCancelListener onCancel) {
		// Create custom layout for progress dialog
		View progressView = createProgressDialogView(context, message);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setView(progressView);
		builder.setCancelable(cancellable);
		
		if (onCancel != null) {
			builder.setOnCancelListener(onCancel);
		}
		
		hideProgressDialog();
		
		AlertDialog dlg = builder.create();
		progressDialog = dlg;
		
		dlg.show();
		
		return dlg;
	}

	public static void setProgressMessage(String msg) {
		if (progressMessageView != null) {
			progressMessageView.setText(msg);
		}
	}

	public static void hideProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
			progressMessageView = null;
		}
	}
	
	private static View createProgressDialogView(Context context, String message) {
		// Create a linear layout to hold progress bar and message
		android.widget.LinearLayout layout = new android.widget.LinearLayout(context);
		layout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
		layout.setPadding(50, 50, 50, 50);
		layout.setGravity(android.view.Gravity.CENTER_VERTICAL);
		
		// Create progress bar
		ProgressBar progressBar = new ProgressBar(context);
		android.widget.LinearLayout.LayoutParams progressParams = 
			new android.widget.LinearLayout.LayoutParams(
				android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
				android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
		progressParams.setMargins(0, 0, 30, 0);
		progressBar.setLayoutParams(progressParams);
		
		// Create message text view
		progressMessageView = new TextView(context);
		progressMessageView.setText(message != null ? message : "Loading...");
		progressMessageView.setTextSize(16);
		android.widget.LinearLayout.LayoutParams textParams = 
			new android.widget.LinearLayout.LayoutParams(
				android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
				android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
		progressMessageView.setLayoutParams(textParams);
		
		// Add views to layout
		layout.addView(progressBar);
		layout.addView(progressMessageView);
		
		return layout;
	}

	public static void showMessageDialog(final Context context, String msg) {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);

		dialogBuilder.setMessage(msg);
		dialogBuilder.setCancelable(false);

		dialogBuilder.setPositiveButton("Ok", (dialog, which) -> dialog.dismiss());

		AlertDialog dialog = dialogBuilder.create();

		dialog.show();

	}

	public static void showUserInputDialog(final Context context, String prompt, final DialogInterface.OnClickListener okListener) {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);

		final EditText userInput = new EditText(context);
		userInput.setId(USER_INPUT_FIELD_ID);

		dialogBuilder.setView(userInput);

		dialogBuilder.setTitle(prompt);

		dialogBuilder.setCancelable(false);

		dialogBuilder.setPositiveButton("OK", (dialog, which) -> {
			InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
			Objects.requireNonNull(imm).hideSoftInputFromWindow(userInput.getWindowToken(), 0);

			if (okListener != null) {
				okListener.onClick(dialog, which);
			}

			dialog.dismiss();
		});

		dialogBuilder.setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss());

		AlertDialog alertDialog = dialogBuilder.create();

		alertDialog.show();

		userInput.requestFocus();

		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		Objects.requireNonNull(imm).showSoftInput(userInput, InputMethodManager.SHOW_IMPLICIT);
	}

	private static final int USER_INPUT_FIELD_ID = 0;

}
