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

import android.util.Log;

import java.util.Date;

public class PaymentMeasure implements MeasuresStatesListener {
    public static final String TAG = PaymentMeasure.class.getName();

    Date start;
    Date waitingCard;
    Date userPresentCard;
    Date crEnterPin;
    Date uEnterPin;
    Date authorisationCall;
    Date authorisationResponse;
    Date finish;

    long calculateMeasureStartToWaitingCard() {
        return diff(start, waitingCard);
    }

    long calculateMeasureUPresentCardToCREnterPin() {
        return diff(userPresentCard, crEnterPin);
    }

    long calculateMeasureUEnterPinToAuthorization() {
        return diff(uEnterPin, authorisationCall);
    }

    long calculateMeasureUPresentCardToAuthorization() {
        return diff(userPresentCard, authorisationCall);
    }

    long calculateMeasureAuthorisationRpToFinish() {
        return diff(authorisationResponse, finish);
    }

    private long diff(Date startDate, Date endDate) {
        if(startDate == null || endDate == null)
            return 0;

        //milliseconds
        return endDate.getTime() - startDate.getTime();
    }

    @Override
    public void onStart() {
        Log.d(TAG, "on start");
        start = new Date();
    }

    @Override
    public void onWaitingCard() {
        Log.d(TAG, "on waiting card");
        waitingCard = new Date();
    }

    @Override
    public void onUserPresentCard() {
        Log.d(TAG, "on user present card");
        userPresentCard = new Date();
    }

    @Override
    public void onCREnterPin() {
        Log.d(TAG, "on card reader display enter pin");
        crEnterPin = new Date();
    }

    @Override
    public void onUEnterPin() {
        Log.d(TAG, "on user enter pin");
        uEnterPin = new Date();
    }

    @Override
    public void onAuthorizationCalled() {
        Log.d(TAG, "on authorization called");
        authorisationCall = new Date();
    }

    @Override
    public void onAuthorizationResponse() {
        Log.d(TAG, "on authorization response returned");
        authorisationResponse = new Date();
    }

    @Override
    public void onFinish() {
        Log.d(TAG, "on finish");
        finish = new Date();
    }
}
