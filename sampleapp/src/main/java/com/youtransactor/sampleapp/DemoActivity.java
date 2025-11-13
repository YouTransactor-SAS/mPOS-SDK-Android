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

import static com.youTransactor.uCube.log.LogManager.LogLevel.SYSTEM;

import android.os.Bundle;
import android.widget.TextView;

import com.jps.secureService.api.product_manager.ProductManager;
import com.youTransactor.uCube.api.UCubeAPI;
import com.youTransactor.uCube.api.YTMPOSProduct;
import com.youTransactor.uCube.connexion.ConnectionListener;
import com.youTransactor.uCube.connexion.ConnectionService;
import com.youTransactor.uCube.connexion.ConnectionStatus;
import com.youtransactor.sampleapp.payment.Localization;
import com.youtransactor.sampleapp.transactionView.TransactionViewBase;
import com.youtransactor.sampleapp.transactionView.configuration.ConfigurableTransactionStepHandler;
import com.youtransactor.sampleapp.transactionView.configuration.TransactionStepHandler;

public class DemoActivity extends TransactionViewBase {

    private final TransactionStepHandler transactionStepHandler = ConfigurableTransactionStepHandler.defaultForProductAndTransactionViewBase(ProductManager.id, this).build();

    @Override
    protected void onDestroy() {
        UCubeAPI.close();
        super.onDestroy();
    }

    @Override
    protected TransactionStepHandler getTransactionStepHandler() {
        return transactionStepHandler;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setHomeFragment(new DemoFragment());

        setContentView(R.layout.activity_demo);

        final String versionName = BuildConfig.VERSION_NAME;
        final TextView versionNametv = findViewById(R.id.version_name);
        versionNametv.setText(getString(R.string.versionName, versionName));

        this.initializeYtSomCommunication();

        if (savedInstanceState == null) {
            this.backToHomeFragment();
        }
    }

    private void initializeYtSomCommunication() {
        if (UCubeAPI.getConnexionManager().isConnected()) {
            return;
        }
        UCubeAPI.setLogLevel(SYSTEM);
        UCubeAPI.setYTmPOSProduct(YTMPOSProduct.AndroidPOS);
        UCubeAPI.setConnexionManagerType(ConnectionService.ConnectionManagerType.SECURE_SERVICE);
        Localization.Localization_init(UCubeAPI.getContext());
        UCubeAPI.getConnexionManager().connect(
                10 * 1000,
                3,
                new ConnectionListener() {
                    @Override
                    public void onConnectionFailed(ConnectionStatus status, int error) {
                    }

                    @Override
                    public void onConnectionSuccess() {
                    }

                    @Override
                    public void onConnectionCancelled() {
                    }
                });
    }
}
