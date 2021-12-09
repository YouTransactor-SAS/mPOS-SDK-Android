package com.youtransactor.sampleapp.payment;

interface MeasureStatesListener {
    void onStart();
    void onWaitingCard();
    void onUserPresentCard();
    void onCREnterPin();
    void onUEnterPin();
    void onAuthorizationCalled();
    void onAuthorizationResponse();
    void onFinish();
}
