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

import static com.youTransactor.uCube.connexion.ConnectionService.ConnectionManagerType.BT;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.youTransactor.uCube.api.UCubeAPI;
import com.youTransactor.uCube.connexion.ConnectionService;
import com.youTransactor.uCube.log.LogManager;
import com.youTransactor.uCube.mdm.MDMServices;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SetupActivity extends AppCompatActivity {

    public static final String YT_PRODUCT = "ytProduct";
    public static final String NO_DEFAULT = "no_default";
    public static final String DEFAULT_YT_PRODUCT = "default_YT_Product";
    public static final String TEST_MODE_PREF_NAME = "testMode";
    public static final String MEASURES_MODE_PREF_NAME = "measuresMode";
    public static final String RECOVERY_MODE_PERF_NAME = "recoveryMode";
    public static final String ENABLE_SDK_LOGS_PREF_NAME = "enableSDKLogs";
    public static final String COMMUNICATION_TYPE_PREF_NAME = "communicationType";
    public static final String SDK_LOGS_LEVEL_PREF_NAME = "SDKLogLevel";
    public static final String MDM_URL_PREF_NAME = "mdmUrl";
    public static final String SETUP_SHARED_PREF_NAME = "setup";

    private CardView ytkeyCardView;
    private CardView uCubeCardView;
    private CardView uCubeTouchCardView;
    private CardView androidPOSCardView;
    private CardView ytSOMCardView;
    private SwitchMaterial defaultModelSwitch;
    private EditText mdmUrlEditText;
    private SharedPreferences sharedPreferences;
    private  View.OnClickListener productSelectionListener = (v) -> {
        ytkeyCardView.setCardBackgroundColor(ContextCompat.getColor(SetupActivity.this, android.R.color.white));
        uCubeCardView.setCardBackgroundColor(ContextCompat.getColor(SetupActivity.this, android.R.color.white));
        uCubeTouchCardView.setCardBackgroundColor(ContextCompat.getColor(SetupActivity.this, android.R.color.white));
        androidPOSCardView.setCardBackgroundColor(ContextCompat.getColor(SetupActivity.this, android.R.color.white));
        ytSOMCardView.setCardBackgroundColor(ContextCompat.getColor(SetupActivity.this, android.R.color.white));

        ((CardView) v).setCardBackgroundColor(ContextCompat.getColor(SetupActivity.this, android.R.color.darker_gray));

        switch (v.getId()) {
            case R.id.ytkey_card_view:
                selectProduct(YTProduct.YT_Key);
                break;
            case R.id.ucube_card_view:
                selectProduct(YTProduct.uCube);
                break;
            case R.id.ucube_touch_card_view:
                selectProduct(YTProduct.uCubeTouch);
                break;
            case R.id.androidPOS_view:
                selectProduct(YTProduct.AndroidPOS);
                break;
            case R.id.yt_som_card_view:
                selectProduct(YTProduct.YT_SOM);
                break;
        }
    };

    @Override
    protected void onDestroy() {
        UCubeAPI.close();

        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences(SETUP_SHARED_PREF_NAME, Context.MODE_PRIVATE);

        setContentView(R.layout.activity_setup);

        ytkeyCardView = findViewById(R.id.ytkey_card_view);
        uCubeCardView = findViewById(R.id.ucube_card_view);
        uCubeTouchCardView = findViewById(R.id.ucube_touch_card_view);
        androidPOSCardView = findViewById(R.id.androidPOS_view);
        ytSOMCardView = findViewById(R.id.yt_som_card_view);

        ArrayAdapter<ConnectionService.ConnectionManagerType> connectionTypeAdapater = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                ConnectionService.ConnectionManagerType.values()
        );

        Spinner connectionTypeSpinner = findViewById(R.id.connectionTypeSpinner);
        connectionTypeSpinner.setAdapter(connectionTypeAdapater);

        try {
            connectionTypeSpinner.setSelection(connectionTypeAdapater.getPosition(ConnectionService.ConnectionManagerType.valueOf(sharedPreferences.getString(SetupActivity.COMMUNICATION_TYPE_PREF_NAME, BT.name()))));
        } catch (Exception ignored) {}


        connectionTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                sharedPreferences.edit().putString(COMMUNICATION_TYPE_PREF_NAME, ((ConnectionService.ConnectionManagerType) connectionTypeSpinner.getSelectedItem()).name()).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        defaultModelSwitch = findViewById(R.id.defaultDongleModel);
        defaultModelSwitch.setChecked(sharedPreferences.getString(DEFAULT_YT_PRODUCT, null) != null);

        String versionName = BuildConfig.VERSION_NAME;
        TextView versionNametv = findViewById(R.id.version_name);
        versionNametv.setText(getString(R.string.versionName, versionName));

        ytkeyCardView.setOnClickListener(productSelectionListener);
        uCubeCardView.setOnClickListener(productSelectionListener);
        uCubeTouchCardView.setOnClickListener(productSelectionListener);
        androidPOSCardView.setOnClickListener(productSelectionListener);
        ytSOMCardView.setOnClickListener(productSelectionListener);

        SwitchMaterial s = findViewById(R.id.enableTest);
        s.setChecked(sharedPreferences.getBoolean(TEST_MODE_PREF_NAME, false));
        s.setOnCheckedChangeListener((compoundButton, b) ->
                sharedPreferences.edit().putBoolean(TEST_MODE_PREF_NAME, compoundButton.isChecked()).apply());

        SwitchMaterial e = findViewById(R.id.enableMeasures);
        e.setChecked(sharedPreferences.getBoolean(MEASURES_MODE_PREF_NAME, false));
        e.setOnCheckedChangeListener((compoundButton, b) ->
                sharedPreferences.edit().putBoolean(MEASURES_MODE_PREF_NAME, compoundButton.isChecked()).apply());

        SwitchMaterial d = findViewById(R.id.enableRecoveryMechanism);
        d.setOnCheckedChangeListener((compoundButton, b) -> {
            boolean enable = compoundButton.isChecked();
            sharedPreferences
                    .edit()
                    .putBoolean(RECOVERY_MODE_PERF_NAME, enable)
                    .apply();
            UCubeAPI.enableRecoveryMechanism(enable);
        });

        SwitchMaterial enableLogSwitch = findViewById(R.id.enableSDKLog);
        enableLogSwitch.setOnCheckedChangeListener((compoundButton, b) ->  {
            boolean enable = compoundButton.isChecked();
            sharedPreferences.edit().putBoolean(ENABLE_SDK_LOGS_PREF_NAME, enable).apply();
            UCubeAPI.enableLogs(enable);
            findViewById(R.id.logLevelSection).setVisibility(enable? View.VISIBLE : View.GONE);
        });

        Spinner logLevelSpinner = findViewById(R.id.logLevelSpinner);
        logLevelSpinner.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                LogManager.LogLevel.values()
        ));

        logLevelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                sharedPreferences.edit().putInt(SDK_LOGS_LEVEL_PREF_NAME, ((LogManager.LogLevel) logLevelSpinner.getSelectedItem()).getCode()).apply();
                UCubeAPI.setLogLevel((LogManager.LogLevel) logLevelSpinner.getSelectedItem());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        boolean logsEnabled = sharedPreferences.getBoolean(ENABLE_SDK_LOGS_PREF_NAME, false);
        int level =  sharedPreferences.getInt(SDK_LOGS_LEVEL_PREF_NAME, LogManager.LogLevel.API.getCode());
        LogManager.LogLevel logLevel = LogManager.LogLevel.valueOf(level);
        List<LogManager.LogLevel> values = new ArrayList<>(Arrays.asList(LogManager.LogLevel.values()));
        findViewById(R.id.logLevelSection).setVisibility(logsEnabled ? View.VISIBLE : View.GONE);
        enableLogSwitch.setChecked(logsEnabled);
        logLevelSpinner.setSelection(values.indexOf(logLevel));
        UCubeAPI.setLogLevel(logLevel);
        d.setChecked(sharedPreferences.getBoolean(RECOVERY_MODE_PERF_NAME, false));


        String url = sharedPreferences.getString(MDM_URL_PREF_NAME, MDMServices.DEFAULT_URL);
        mdmUrlEditText = findViewById(R.id.mdm_url_edit_text);
        mdmUrlEditText.setText(url);

        UCubeAPI.mdmSetup(this.getApplicationContext());
        MDMServices.changeServerUrl(this.getApplicationContext(), url);

        Intent intent = getIntent();
        if (intent != null && !"true".equals(intent.getStringExtra(NO_DEFAULT))) {
            try {
                YTProduct defaultModel = YTProduct.valueOf(sharedPreferences.getString(DEFAULT_YT_PRODUCT, null));
                startMainActivity(defaultModel);
            } catch (Exception ignored) {}
        }
    }

    private void selectProduct(YTProduct product) {
        String url = mdmUrlEditText.getText().toString();
        MDMServices.changeServerUrl(this.getApplicationContext(), url);
        sharedPreferences.edit().putString(MDM_URL_PREF_NAME, url).apply();

        sharedPreferences.edit().putString(YT_PRODUCT, product.name()).apply();

        if (defaultModelSwitch != null) {
            if (defaultModelSwitch.isChecked()) {
                sharedPreferences.edit().putString(DEFAULT_YT_PRODUCT, product.name()).apply();
            } else {
                sharedPreferences.edit().remove(DEFAULT_YT_PRODUCT).apply();
            }
        }
        startMainActivity(product);
    }

    private void startMainActivity(YTProduct product) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(YT_PRODUCT, product.name());

        startActivity(intent);
    }
}
