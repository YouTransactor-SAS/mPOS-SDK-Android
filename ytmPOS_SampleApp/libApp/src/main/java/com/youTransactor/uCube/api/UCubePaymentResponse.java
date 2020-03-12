package com.youTransactor.uCube.api;

import com.youTransactor.uCube.payment.PaymentContext;

public class UCubePaymentResponse{

    public String cardLabel;
    public PaymentContext paymentContext;
    public UCube uCube;

    public UCubePaymentResponse(String cardLabel,
                                PaymentContext paymentContext, String ucubeName,
                                String ucubeAddress, String ucubePartNumber,
                                String ucubeSerialNumber) {
        this.cardLabel = cardLabel;
        this.paymentContext = paymentContext;
        this.uCube = new UCube();
        this.uCube.ucubeName = ucubeName;
        this.uCube.ucubeAddress = ucubeAddress;
        this.uCube.ucubePartNumber = ucubePartNumber;
        this.uCube.ucubeSerialNumber = ucubeSerialNumber;

    }

    public class UCube {
        public String ucubeName;
        public String ucubeAddress;
        public String ucubePartNumber;
        public String ucubeSerialNumber;
    }
}
