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
package com.youtransactor.sampleapp.features;

import static com.youTransactor.uCube.payment.SetSdseState.*;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.youTransactor.uCube.payment.PaymentContext;
import com.youTransactor.uCube.payment.PaymentUtils;
import com.youTransactor.uCube.payment.SetSdseState;
import com.youTransactor.uCube.rpc.command.GetSecuredTagCommand;

public class SdseSession extends AsyncTask<Void, Void, Boolean> {

    public static final int SDSE_TYPE_PAN =  1;
    public static final int SDSE_TYPE_CVV =  2;
    public static final int SDSE_TYPE_DATE = 3;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Context context;

    public SdseSession(Context context){
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
                            startSetting(DISPLAY_SDSE_PAN);
                            break;
                        default:
                            break;
                    }
                });
                break;
            case DISPLAY_SDSE_PAN:
                PaymentUtils.SetSdse(SDSE_TYPE_PAN, 180, (event, params) -> {
                    switch (event) {
                        case FAILED:
                            handler.post(() -> {
                                // Code qui sera exécuté sur le thread principal (UI thread)
                                Toast.makeText(context, "Set Data failed", Toast.LENGTH_LONG).show();
                            });
                            startSetting(START_SDSE_EXIT_SECURE_SESSION);
                            break;
                        case SUCCESS:
                            startSetting(DISPLAY_SDSE_CVV);
                            break;
                    }
                });
                break;
            case DISPLAY_SDSE_CVV:
                PaymentUtils.SetSdse(SDSE_TYPE_CVV, 180, (event, params) -> {
                    switch (event) {
                        case FAILED:
                            handler.post(() -> {
                                // Code qui sera exécuté sur le thread principal (UI thread)
                                Toast.makeText(context, "Set Data failed", Toast.LENGTH_LONG).show();
                            });
                            startSetting(START_SDSE_EXIT_SECURE_SESSION);
                            break;
                        case SUCCESS:
                            startSetting(DISPLAY_SDSE_DATE);
                            break;
                    }
                });
                break;
            case DISPLAY_SDSE_DATE:
                PaymentUtils.SetSdse(SDSE_TYPE_DATE, 180, (event, params) -> {
                    switch (event) {
                        case FAILED:
                            handler.post(() -> {
                                // Code qui sera exécuté sur le thread principal (UI thread)
                                Toast.makeText(context, "Set Data failed", Toast.LENGTH_LONG).show();
                            });

                            startSetting(START_SDSE_EXIT_SECURE_SESSION);
                            break;
                        case SUCCESS:
                            startSetting(GET_SECURED_TAGS);
                            break;
                    }
                });
                break;
            case GET_SECURED_TAGS:
                new GetSecuredTagCommand(new int[]{0xDF5A}).execute((event, params) -> {
                    switch (event) {
                        case PROGRESS:
                            break;
                        case CANCELLED:
                        case FAILED:
                        case SUCCESS:
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
