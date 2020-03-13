/*
 * Copyright (C) 2020, YouTransactor. All Rights Reserved.
 *
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youtransactor.sampleapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.crashlytics.android.Crashlytics;
import com.youTransactor.uCube.api.UCubeAPI;
import com.youTransactor.uCube.api.UCubeAPIListener;
import com.youTransactor.uCube.api.UCubeAPIState;
import com.youTransactor.uCube.api.YTMPOSProduct;

import io.fabric.sdk.android.Fabric;

public class SetupActivity extends AppCompatActivity {

    private CardView uCubeCardView;
    private CardView uCubeTouchCardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_setup);

        UCubeAPI.initManagers(getApplicationContext());

        uCubeCardView = findViewById(R.id.ucube_card_view);
        uCubeTouchCardView = findViewById(R.id.ucube_touch_card_view);


        String versionName = BuildConfig.VERSION_NAME;
        TextView versionNametv = findViewById(R.id.version_name);
        versionNametv.setText(getString(R.string.versionName, versionName));

        YTMPOSProduct ytmposProduct;

        try {
            ytmposProduct  = UCubeAPI.getYTMPOSProduct();

            if (ytmposProduct != null) {

                switch (ytmposProduct) {
                    case uCube:
                        uCubeTouchCardView.setCardBackgroundColor(ContextCompat.getColor(SetupActivity.this, android.R.color.white));
                        uCubeCardView.setCardBackgroundColor(ContextCompat.getColor(SetupActivity.this,android.R.color.darker_gray));
                        break;

                    case uCube_touch:
                        uCubeCardView.setCardBackgroundColor(ContextCompat.getColor(SetupActivity.this, android.R.color.white));
                        uCubeTouchCardView.setCardBackgroundColor(ContextCompat.getColor(SetupActivity.this, android.R.color.darker_gray));
                        break;
                }

            } else {
                uCubeCardView.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.white));
                uCubeTouchCardView.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.white));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        uCubeCardView.setOnClickListener(v -> {
            final ProgressDialog progressDlg = UIUtils.showProgress(this, getString(R.string.setup_progress), false);

            try {
                UCubeAPI.setup(getApplicationContext(), this, YTMPOSProduct.uCube, new UCubeAPIListener() {
                    @Override
                    public void onProgress(UCubeAPIState uCubeAPIState) {
                    }

                    @Override
                    public void onFinish(boolean status) {
                        progressDlg.dismiss();

                        if(status) {
                            uCubeCardView.setCardBackgroundColor(ContextCompat.getColor(SetupActivity.this, android.R.color.darker_gray));
                            uCubeTouchCardView.setCardBackgroundColor(ContextCompat.getColor(SetupActivity.this, android.R.color.white));

                            Intent btCnxActivityIntent = new Intent(SetupActivity.this, MainActivity.class);
                            startActivity(btCnxActivityIntent);
                        } else {
                            Toast.makeText(SetupActivity.this, getString(R.string.error_setup), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();

                progressDlg.dismiss();

                Toast.makeText(SetupActivity.this, getString(R.string.error_setup), Toast.LENGTH_LONG).show();
            }

        });

        uCubeTouchCardView.setOnClickListener(v -> {
            final ProgressDialog progressDlg = UIUtils.showProgress(this, getString(R.string.setup_progress), false);

            try {
                UCubeAPI.setup(getApplicationContext(), this, YTMPOSProduct.uCube_touch, new UCubeAPIListener() {
                    @Override
                    public void onProgress(UCubeAPIState uCubeAPIState) {
                    }

                    @Override
                    public void onFinish(boolean status) {
                        progressDlg.dismiss();

                        if(status) {
                            uCubeCardView.setCardBackgroundColor(ContextCompat.getColor(SetupActivity.this, android.R.color.white));
                            uCubeTouchCardView.setCardBackgroundColor(ContextCompat.getColor(SetupActivity.this, android.R.color.darker_gray));

                            Intent btCnxActivityIntent = new Intent(SetupActivity.this, MainActivity.class);
                            startActivity(btCnxActivityIntent);
                        } else {
                            Toast.makeText(SetupActivity.this, getString(R.string.error_setup), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();

                progressDlg.dismiss();

                Toast.makeText(SetupActivity.this, getString(R.string.error_setup), Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        UCubeAPI.close();
    }
}
