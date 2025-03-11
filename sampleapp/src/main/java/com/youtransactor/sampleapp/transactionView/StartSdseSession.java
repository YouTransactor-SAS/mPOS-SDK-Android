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
package com.youtransactor.sampleapp.transactionView;


import static com.youTransactor.uCube.payment.SetSdseState.*;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.youTransactor.uCube.payment.PaymentContext;
import com.youTransactor.uCube.payment.PaymentUtils;
import com.youTransactor.uCube.payment.SetSdseState;


public class StartSdseSession extends AsyncTask<Void, Void, Boolean> {

    private byte sdse_type;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Context context;

    public StartSdseSession(Context context, byte SdseType){
        this.sdse_type = SdseType;
        this.context = context;
    }

    private void startSetting(SetSdseState state) {
        switch (state) {
            case START_SDSE_EXIT_SECURE_SESSION:
                PaymentUtils.exitSecureSession((event, params) -> {
                    switch (event) {
                        default:
                            break;
                    }
                });
                break;
            case ENTER_SDSE_SECURE_SESSION:
                PaymentContext PayContext = new PaymentContext();
                PayContext.setDataEncryptionMechanism(2);
                PaymentUtils.enterSecureSession(PayContext, (event, params) -> {
                    switch (event) {
                        case FAILED:
                            handler.post(() -> {
                                // Code qui sera exécuté sur le thread principal (UI thread)
                                Toast.makeText(context, "Enter secure session", Toast.LENGTH_LONG).show();
                            });
                            break;
                        case SUCCESS:
                            startSetting(DISPLAY_SDSE);
                            break;
                        default:
                            break;
                    }
                });
                break;
            case DISPLAY_SDSE:
                PaymentUtils.SetSdse(sdse_type, 180, (event, params) -> {
                    switch (event) {
                        case FAILED:
                            handler.post(() -> {
                                // Code qui sera exécuté sur le thread principal (UI thread)
                                Toast.makeText(context, "Set Data failed", Toast.LENGTH_LONG).show();
                            });

                            startSetting(START_SDSE_EXIT_SECURE_SESSION);
                            break;
                        case SUCCESS:
                            handler.post(() -> {
                                // Code qui sera exécuté sur le thread principal (UI thread)
                                Toast.makeText(context, "Set Data success", Toast.LENGTH_LONG).show();
                            });
                            startSetting(START_SDSE_EXIT_SECURE_SESSION);
                            break;
                    }
                });
                break;
        }

    }

    @Override
        protected Boolean doInBackground(Void... params) {
            try {
                startSetting(ENTER_SDSE_SECURE_SESSION);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }
