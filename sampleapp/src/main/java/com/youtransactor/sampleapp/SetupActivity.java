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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.youTransactor.uCube.api.UCubeAPI;

public class SetupActivity extends AppCompatActivity {

    public static final String YT_PRODUCT = "ytProduct";
    public static final String NO_DEFAULT = "no_default";
    public static final String DEFAULT_YT_PRODUCT = "default_YT_Product";
    public static final String TEST_MODE_PREF_NAME = "testMode";
    public static final String SETUP_SHARED_PREF_NAME = "setup";

    private CardView uCubeCardView;
    private CardView uCubeTouchCardView;
    private Switch defaultModelSwitch;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onDestroy() {
        UCubeAPI.close();

        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences(SETUP_SHARED_PREF_NAME, Context.MODE_PRIVATE);

        Intent intent = getIntent();
        if (intent != null && !"true".equals(intent.getStringExtra(NO_DEFAULT))) {
            try {
                YTProduct defaultModel = YTProduct.valueOf(sharedPreferences.getString(DEFAULT_YT_PRODUCT, null));
                startMainActivity(defaultModel);
            } catch (Exception ignored) {}
        }

        setContentView(R.layout.activity_setup);

        uCubeCardView = findViewById(R.id.ucube_card_view);
        uCubeTouchCardView = findViewById(R.id.ucube_touch_card_view);
        defaultModelSwitch = findViewById(R.id.defaultDongleModel);
        defaultModelSwitch.setChecked(sharedPreferences.getString(DEFAULT_YT_PRODUCT, null) != null);

        String versionName = BuildConfig.VERSION_NAME;
        TextView versionNametv = findViewById(R.id.version_name);
        versionNametv.setText(getString(R.string.versionName, versionName));

        YTProduct selectedProduct = getYtProduct();

        uCubeCardView.setOnClickListener(v -> {
            uCubeCardView.setCardBackgroundColor(ContextCompat.getColor(SetupActivity.this, android.R.color.darker_gray));
            uCubeTouchCardView.setCardBackgroundColor(ContextCompat.getColor(SetupActivity.this, android.R.color.white));

            selectProduct(YTProduct.uCube);
        });

        uCubeTouchCardView.setOnClickListener(v -> {
            uCubeCardView.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.white));
            uCubeTouchCardView.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray));

            selectProduct(YTProduct.uCubeTouch);
        });

        Switch s = findViewById(R.id.enableTest);
        s.setChecked(sharedPreferences.getBoolean(TEST_MODE_PREF_NAME, false));
        s.setOnClickListener(v -> sharedPreferences.edit().putBoolean(TEST_MODE_PREF_NAME, ((Switch) v).isChecked()).apply());
    }

    private void selectProduct(YTProduct product) {
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

    private YTProduct getYtProduct() {
        try {
            return YTProduct.valueOf(sharedPreferences.getString(YT_PRODUCT, null));
        } catch (Exception ignored) {}

        return null;
    }

}
