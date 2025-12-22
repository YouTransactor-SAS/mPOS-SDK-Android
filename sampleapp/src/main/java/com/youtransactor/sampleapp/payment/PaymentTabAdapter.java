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
package com.youtransactor.sampleapp.payment;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.youtransactor.sampleapp.R;

public class PaymentTabAdapter extends RecyclerView.Adapter<PaymentTabAdapter.TabViewHolder> {
    
    private final PaymentFragment paymentFragment;
    
    public PaymentTabAdapter(PaymentFragment paymentFragment) {
        this.paymentFragment = paymentFragment;
    }
    
    @NonNull
    @Override
    public TabViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;
        
        if (viewType == 0) {
            // Payment tab
            view = inflater.inflate(R.layout.tab_payment, parent, false);
        } else {
            // Settings tab
            view = inflater.inflate(R.layout.tab_settings, parent, false);
        }
        
        return new TabViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull TabViewHolder holder, int position) {
        if (position == 0) {
            // Initialize Payment tab views
            paymentFragment.initPaymentTabViews(holder.itemView);
        } else if (position == 1) {
            // Initialize Settings tab views
            paymentFragment.initSettingsTabViews(holder.itemView);
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Payment and Settings tabs
    }
    
    @Override
    public int getItemViewType(int position) {
        return position; // 0 for Payment, 1 for Settings
    }
    
    public static class TabViewHolder extends RecyclerView.ViewHolder {
        private final int tabType;
        
        public TabViewHolder(@NonNull View itemView, int tabType) {
            super(itemView);
            this.tabType = tabType;
        }
        
        public int getTabType() {
            return tabType;
        }
    }
}
