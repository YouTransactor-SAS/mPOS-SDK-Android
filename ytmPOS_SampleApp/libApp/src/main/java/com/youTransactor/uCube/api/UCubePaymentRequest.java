package com.youTransactor.uCube.api;

import com.youTransactor.uCube.payment.Currency;
import com.youTransactor.uCube.payment.TransactionType;
import com.youTransactor.uCube.payment.task.IApplicationSelectionTask;
import com.youTransactor.uCube.payment.task.IAuthorizationTask;
import com.youTransactor.uCube.payment.task.IRiskManagementTask;

import java.util.Collections;
import java.util.List;

public class UCubePaymentRequest {
    public static final Currency CURRENCY_EUR = new Currency(978, 2, "EUR");
    public static final Currency CURRENCY_USD = new Currency(840, 2, "USD");

    private static final int DEFAULT_CARD_WAIT_TIMEOUT = 30;
    private static final List<String> DEFAULT_PREFFERED_LANG_LIST = Collections.singletonList("en");
    private static final TransactionType DEFAULT_TRANSACTION_TYPE = TransactionType.PURCHASE;
    private static final double ENTER_AMOUNT_ON_UCUBE = -1;
    private static final boolean SYSTEM_FAILURE_INFO = true;
    private static final boolean SYSTEM_FAILURE_INFO_2 = true;
    private static final boolean FORCE_ONLINE_PIN = false;

    public static class Builder {

        // required
        private double amount = ENTER_AMOUNT_ON_UCUBE;
        private Currency currency = CURRENCY_USD;
        private TransactionType transactionType = DEFAULT_TRANSACTION_TYPE;
        private int cardWaitTimeout = DEFAULT_CARD_WAIT_TIMEOUT;
        private List<String> preferredLanguageList = DEFAULT_PREFFERED_LANG_LIST;
        private boolean systemFailureInfo = SYSTEM_FAILURE_INFO;
        private boolean systemFailureInfo2 = SYSTEM_FAILURE_INFO_2;
        private boolean forceOnlinePin = FORCE_ONLINE_PIN;

        private IAuthorizationTask authorizationTask;
        private IRiskManagementTask riskManagementTask;
        private IApplicationSelectionTask applicationSelectionTask;

        private int[] requestedPlainTagList; // retrieved using getPlainTag cmd at the end of NFC and MSR payment
        private int[] requestedSecuredTagList; // retrieved using getSecuredTag cmd at the end of NFC and MSR payment
        private int[] requestedAuthorizationTagList; // set at TX_Init in C1 tag when it is a SMC payment

        public Builder() {
        }

        public Builder setAmount(double amount) {
            this.amount = amount;
            return this;
        }

        public Builder setCurrency(Currency currency) {
            this.currency = currency;
            return this;
        }

        public Builder setTransactionType(TransactionType transactionType) {
            this.transactionType = transactionType;
            return this;
        }

        public Builder setCardWaitTimeout(int cardWaitTimeout) {
            this.cardWaitTimeout = cardWaitTimeout;
            return this;
        }

        public Builder setAuthorizationTask(IAuthorizationTask authorizationTask) {
            this.authorizationTask = authorizationTask;
            return this;
        }

        public Builder setRiskManagementTask(IRiskManagementTask riskManagementTask) {
            this.riskManagementTask = riskManagementTask;
            return this;
        }

        public Builder setApplicationSelectionTask(IApplicationSelectionTask applicationSelectionTask) {
            this.applicationSelectionTask = applicationSelectionTask;
            return this;
        }

        public Builder setPreferredLanguageList(List<String> preferredLanguageList) {
            this.preferredLanguageList = preferredLanguageList;
            return this;
        }

        public Builder setRequestedPlainTagList(int... requestedPlainTagList) {

            this.requestedPlainTagList = new int[requestedPlainTagList.length];
            System.arraycopy(requestedPlainTagList, 0, this.requestedPlainTagList, 0, requestedPlainTagList.length);

            return this;
        }

        public Builder setRequestedSecuredTagList(int... requestedSecuredTagList) {

            this.requestedSecuredTagList = new int[requestedSecuredTagList.length];
            System.arraycopy(requestedSecuredTagList, 0, this.requestedSecuredTagList, 0, requestedSecuredTagList.length);

            return this;
        }

        public Builder setRequestedAuthorizationTagList(int... requestedAuthorizationTagList) {

            this.requestedAuthorizationTagList = new int[requestedAuthorizationTagList.length];
            System.arraycopy(requestedAuthorizationTagList, 0, this.requestedAuthorizationTagList, 0, requestedAuthorizationTagList.length);

            return this;
        }

        public Builder setSystemFailureInfo(boolean systemFailureInfo) {
            this.systemFailureInfo = systemFailureInfo;
            return this;
        }

        public Builder setSystemFailureInfo2(boolean systemFailureInfo2) {
            this.systemFailureInfo2 = systemFailureInfo2;
            return this;
        }

        public Builder setForceOnlinePin(boolean forceOnlinePin) {
            this.forceOnlinePin = forceOnlinePin;
            return this;
        }

        public UCubePaymentRequest build() {
            // call the private constructor in the outer class
            return new UCubePaymentRequest(this);
        }
    }

    private static Builder builder;

    private UCubePaymentRequest(Builder builder) {
        UCubePaymentRequest.builder = builder;
    }

    public double getAmount() {
        return builder.amount;
    }

    public Currency getCurrency() {
        return builder.currency;
    }

    public TransactionType getTransactionType() {
        return builder.transactionType;
    }

    public int getCardWaitTimeout() {
        return builder.cardWaitTimeout;
    }

    public IAuthorizationTask getAuthorizationTask() {
        return builder.authorizationTask;
    }

    public IRiskManagementTask getRiskManagementTask() {
        return builder.riskManagementTask;
    }

    public IApplicationSelectionTask getApplicationSelectionTask() {
        return builder.applicationSelectionTask;
    }

    public List<String> getPreferredLanguageList() {
        return builder.preferredLanguageList;
    }

    public int[] getRequestedPlainTagList() {
        return builder.requestedPlainTagList;
    }

    public int[] getRequestedSecuredTagList() {
        return builder.requestedSecuredTagList;
    }

    public int[] getRequestedAuthorizationTagList() {
        return builder.requestedAuthorizationTagList;
    }

    public boolean isSystemFailureInfo() {
        return builder.systemFailureInfo;
    }

    public boolean isSystemFailureInfo2() {
        return builder.systemFailureInfo2;
    }

    public boolean isForceOnlinePin() {
        return builder.forceOnlinePin;
    }
}
