package com.youTransactor.uCube.payment;

import com.youTransactor.uCube.rpc.Constants;

public enum TransactionType {

    PURCHASE("Purchase", Constants.PURCHASE),
    WITHDRAWAL("Withdrawal", Constants.CASH),
    REFUND("Refund", Constants.REFUND),
    PURCHASE_CASHBACK("Purchase cashback", Constants.PURCHASE_CASHBACK),
    MANUAL_CASH("Manual cash", Constants.MANUAL_CASH),
    INQUIRY("Inquiry", Constants.INQUIRY);

    private byte code;
    private String label;

    TransactionType(String label, byte code) {
        this.label = label;
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public byte getCode() {
        return code;
    }
}