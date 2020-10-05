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
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.crashlytics.android.Crashlytics;
import com.youTransactor.uCube.api.UCubeAPI;

import io.fabric.sdk.android.Fabric;

public class SetupActivity extends AppCompatActivity {

    private CardView uCubeCardView;
    private CardView uCubeTouchCardView;

    private SharedPreferences sharedPreferences;

    private static final String SHAREDPREF_NAME = "setup";
    public static final String YT_PRODUCT = "ytProduct";

    @Override
    protected void onDestroy() {
        UCubeAPI.close();

        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setup);

        uCubeCardView = findViewById(R.id.ucube_card_view);
        uCubeTouchCardView = findViewById(R.id.ucube_touch_card_view);

        sharedPreferences = getSharedPreferences(SHAREDPREF_NAME, Context.MODE_PRIVATE);

        String versionName = BuildConfig.VERSION_NAME;
        TextView versionNametv = findViewById(R.id.version_name);
        versionNametv.setText(getString(R.string.versionName, versionName));

        if(getYtProduct() == null) {
            uCubeCardView.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.white));
            uCubeTouchCardView.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.white));
        } else {

            switch (getYtProduct()) {
                case uCube:
                    uCubeTouchCardView.setCardBackgroundColor(ContextCompat.getColor(SetupActivity.this, android.R.color.white));
                    uCubeCardView.setCardBackgroundColor(ContextCompat.getColor(SetupActivity.this, android.R.color.darker_gray));
                    break;

                case uCubeTouch:
                    uCubeCardView.setCardBackgroundColor(ContextCompat.getColor(SetupActivity.this, android.R.color.white));
                    uCubeTouchCardView.setCardBackgroundColor(ContextCompat.getColor(SetupActivity.this, android.R.color.darker_gray));
                    break;
            }
        }


        uCubeCardView.setOnClickListener(v -> {

            setYTProduct(YTProduct.uCube);

            uCubeCardView.setCardBackgroundColor(ContextCompat.getColor(SetupActivity.this, android.R.color.darker_gray));
            uCubeTouchCardView.setCardBackgroundColor(ContextCompat.getColor(SetupActivity.this, android.R.color.white));

            Intent intent = new Intent(SetupActivity.this, MainActivity.class);
            intent.putExtra(YT_PRODUCT, YTProduct.uCube.name());
            startActivity(intent);

        });

        uCubeTouchCardView.setOnClickListener(v -> {

            setYTProduct(YTProduct.uCubeTouch);

            uCubeCardView.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.white));
            uCubeTouchCardView.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray));

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(YT_PRODUCT, YTProduct.uCubeTouch.name());
            startActivity(intent);
        });

    }

    private void setYTProduct(YTProduct ytProduct) {
        sharedPreferences.edit().putString(YT_PRODUCT, ytProduct.name()).apply();
    }

    private YTProduct getYtProduct() {
        String ytProductName = sharedPreferences.getString(YT_PRODUCT, null);
        if(ytProductName == null)
            return null;

        return YTProduct.valueOf(ytProductName);
    }
}
