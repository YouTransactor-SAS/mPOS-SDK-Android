package com.youtransactor.sampleapp.rpc;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.youTransactor.uCube.Tools;
import com.youTransactor.uCube.rpc.DeviceInfos;
import com.youtransactor.sampleapp.R;
import com.youtransactor.sampleapp.YTProduct;

import java.lang.reflect.Field;

public class GetInfoDialog extends DialogFragment {
    private final DeviceInfos deviceInfos;
    private final YTProduct ytProduct;

    public GetInfoDialog(DeviceInfos deviceInfos, YTProduct ytProduct) {
        this.deviceInfos = deviceInfos;
        this.ytProduct = ytProduct;
    }

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder b = new AlertDialog.Builder(getActivity())
                .setTitle("uCube Information")
                .setPositiveButton("ok", (dialog, whichButton) -> dialog.dismiss());

        setCancelable(false);
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.get_info_dynamic_layout, null);
        LinearLayout linearLayout = rootView.findViewById(R.id.linear_layout);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        LinearLayout.LayoutParams tvLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        tvLp.setMargins(5, 5, 5, 5);

        //todo
        Field[] fields = DeviceInfos.class.getDeclaredFields();

        for (Field f: fields) {
            f.setAccessible(true);
            Object value;
            try {
                value = f.get(deviceInfos);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                continue;
            }

            String displayedValue = "";
            if ((value instanceof String && !((String) value).isEmpty()) ||
                    value instanceof Integer || value instanceof Boolean) {
                displayedValue = String.valueOf(value);
            } else if (value instanceof byte[]) {
                displayedValue = Tools.bytesToHex((byte[]) value);
            }

                LinearLayout layout = new LinearLayout(getActivity());
                layout.setLayoutParams(lp);
                layout.setOrientation(LinearLayout.HORIZONTAL);

                //name
                String name = f.getName();
                TextView tvName = new TextView(getActivity());
                tvName.setText(name);
                tvName.setTextSize(16);
                // tvName.setPadding(30, 10, 30, 10);
                tvName.setTextColor(Color.BLUE);
                tvName.setLayoutParams(tvLp);
                layout.addView(tvName);

                //value
                TextView tvValue = new TextView(getActivity());
                tvValue.setText(displayedValue);
                tvValue.setTextSize(16);
                //   tvValue.setPadding(30, 10, 30, 10);
                tvValue.setTextColor(Color.DKGRAY);
                tvValue.setLayoutParams(tvLp);
                layout.addView(tvValue);

                //add linear layout to scroll view
                linearLayout.addView(layout);

        }

        b.setView(rootView);
        return b.create();
    }
}


