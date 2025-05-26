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
package com.youtransactor.sampleapp.emvParamUpdate;

import static com.youTransactor.uCube.Tools.hexStringToByteArray;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.youTransactor.uCube.rpc.TransactionTerminalParameterType;
import com.youTransactor.uCube.rpc.command.BankParametersDownloads;
import com.youtransactor.sampleapp.R;

public class EmvParamEnableDisableAIDActivity extends AppCompatActivity {

    private static final String TAG = EmvParamEnableDisableAIDActivity.class.getSimpleName();
    private Spinner spinnerTerminalParameterType;
    private EditText editTextAID;
    private Button btnEnableAID;
    private Button btnDisableAID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emv_param_enable_disable_aid);
        editTextAID = findViewById(R.id.edit_text_AID);
        btnEnableAID = findViewById(R.id.buttonEnableAID);
        btnDisableAID = findViewById(R.id.buttonDisableAID);
        btnEnableAID.setOnClickListener(v -> {
            btnEnableAID.setClickable(false);
            btnDisableAID.setClickable(false);
            updateAID(true);
        });
        btnDisableAID.setOnClickListener(v -> {
            btnEnableAID.setClickable(false);
            btnDisableAID.setClickable(false);
            updateAID(false);
        });
        spinnerTerminalParameterType = findViewById(R.id.spinnerTerminalParameter);
        spinnerTerminalParameterType.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                TransactionTerminalParameterType.values()
        ));
        spinnerTerminalParameterType.setSelection(1);
    }

    private void updateAID(boolean enable) {
        byte[] AID = hexStringToByteArray(editTextAID.getText().toString());
        byte terminalParameterType = ((TransactionTerminalParameterType) spinnerTerminalParameterType.getSelectedItem()).getCode();
        boolean contact = terminalParameterType == TransactionTerminalParameterType.CONTACT.getCode() ? true : false;
        int offset = 0;
        int dataLen = 2 + 2 + (contact ? 1 : 2) + 1 + AID.length;
        byte[] payload = new byte[dataLen + 2 + 2];

        payload[offset++] = terminalParameterType;
        payload[offset++] = 0;
        payload[offset++] = (byte)(dataLen % 0x100);
        payload[offset++] = (byte)(dataLen / 0x100);
        payload[offset++] = 0x02;
        payload[offset++] = 0;
        payload[offset++] = (byte) (contact ? (enable ? 0x01 : 0x02) : (enable ? 0x11 : 0x12));
        payload[offset++] = 0;
        payload[offset++] = (byte) (contact ? 0x84 : 0x9F);
        if (!contact) payload[offset++] = 0x06;
        payload[offset++] = (byte) AID.length;
        for (int i = 0; i < AID.length; i++) {
            payload[offset++] = AID[i];
        }

        BankParametersDownloads bankParametersDownloads = new BankParametersDownloads(payload);
        bankParametersDownloads.execute((event, params) -> {
            switch (event) {
                case CANCELLED:
                case FAILED:
                    Log.e(TAG, "Enable / Disable AID error");
                    btnEnableAID.setClickable(true);
                    btnDisableAID.setClickable(true);
                    runOnUiThread(() -> Toast.makeText(this,
                            "Enable / Disable AID error",
                            Toast.LENGTH_LONG).show());

                    break;
                case SUCCESS:
                    runOnUiThread(() -> Toast.makeText(this,
                            "Enable / Disable AID success",
                            Toast.LENGTH_LONG).show());
                    btnEnableAID.setClickable(true);
                    btnDisableAID.setClickable(true);
                    break;
            }
        });
    }
}




