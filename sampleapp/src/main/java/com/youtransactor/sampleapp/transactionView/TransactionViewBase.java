/*
 * 2006-2025 YOUTRANSACTOR ALL RIGHTS RESERVED. YouTransactor,
 * 32, rue Brancion 75015 Paris France, RCS PARIS: B 491 208 500, YOUTRANSACTOR
 * CONFIDENTIAL AND PROPRIETARY INFORMATION , CONTROLLED DISTRIBUTION ONLY,
 * THEREFORE UNDER NDA ONLY. YOUTRANSACTOR Authorized Parties and who have
 * signed NDA do not have permission to distribute this documentation further.
 * Unauthorized recipients must destroy any electronic and hard-copies
 * and without reading and notify Gregory Mardinian, CTO, YOUTRANSACTOR
 * immediately at gregory_mardinian@jabil.com.
 *
 * @package: com.youtransactor.sampleapp.transactionView
 *
 * @date: août 08, 2024
 *
 * @author: Thomas JEANNETTE (thomas_jeannette@jabil.com)
 */
package com.youtransactor.sampleapp.transactionView;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.youTransactor.uCube.rpc.EventListener;
import com.youTransactor.uCube.rpc.RPCManager;
import com.youtransactor.sampleapp.R;
import com.youtransactor.sampleapp.transactionView.components.CloseFragmentListener;
import com.youtransactor.sampleapp.transactionView.configuration.TransactionStepHandler;

public abstract class TransactionViewBase extends AppCompatActivity implements CloseFragmentListener {

    private Fragment homeFragment;

    private final EventListener eventListener = event -> {
        this.getTransactionStepHandler().handle(event);
    };

    public void setHomeFragment(Fragment homeFragment) {
        this.homeFragment = homeFragment;
    }

    protected abstract TransactionStepHandler getTransactionStepHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
    }

    @Override
    protected void onResume() {
        super.onResume();
        RPCManager.getInstance().registerSvppEventListener(eventListener);
    }

    @Override
    protected void onDestroy() {
        RPCManager.getInstance().unregisterSvppEventListener(eventListener);
        super.onDestroy();
    }

    @Override
    public void onCloseFragment() {
        this.backToHomeFragment();
    }

    public void backToHomeFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, homeFragment)
                .commitAllowingStateLoss();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}


