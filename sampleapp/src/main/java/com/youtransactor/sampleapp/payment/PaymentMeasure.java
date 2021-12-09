package com.youtransactor.sampleapp.payment;

import android.util.Log;

import java.util.Date;

public class PaymentMeasure implements MeasureStatesListener {
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
