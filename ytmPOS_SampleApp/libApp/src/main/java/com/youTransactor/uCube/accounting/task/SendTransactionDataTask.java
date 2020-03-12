/*
 * Copyright (C) 2016, YouTransactor. All Rights Reserved.
 *
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */

package com.youTransactor.uCube.accounting.task;

import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.youTransactor.uCube.LogManager;
import com.youTransactor.uCube.TaskEvent;
import com.youTransactor.uCube.accounting.AccountingManager;
import com.youTransactor.uCube.accounting.service.AbstractAccountingService;
import com.youTransactor.uCube.rpc.DeviceInfos;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by gmx on 25/07/17.
 */

public class SendTransactionDataTask extends AbstractAccountingTask {

    private static final String SEND_TRANSACTION_DATA_WS = "/v1/transaction/";

    private Bundle accountingBundle;
    private String merchantContract;
    private String transactionID;
    private int transactionStatus;

    public SendTransactionDataTask(DeviceInfos deviceInfos, String merchantContract, String transactionID, int transactionStatus) {

        super(deviceInfos);

        this.merchantContract  = merchantContract;
        this.transactionID     = transactionID;
        this.transactionStatus = transactionStatus;
        accountingBundle       = new Bundle();
    }

    @Override
    protected void start() {
        HttpURLConnection urlConnection = null;

        try {
            String jsonData;
            String url    = SEND_TRANSACTION_DATA_WS + deviceInfos.getPartNumber() + '/' + deviceInfos.getSerial();
            urlConnection = AccountingManager.getInstance().initRequest(url, AccountingManager.POST_METHOD);

            urlConnection.setRequestProperty("Content-Type", "application/json");

            if (deviceInfos.getTransactionData().length() > 0) {

                accountingBundle.putInt("transactionStatus", transactionStatus);
                accountingBundle.putString("transactionID", transactionID);
                accountingBundle.putString("payboxID", merchantContract);

                // Print bundle content
                for (String key: accountingBundle.keySet())
                {
                    Log.v("returnPayment", key + " : " + accountingBundle.get(key).toString());
                }
                jsonData = formatTransactionDataToJson(accountingBundle);

                OutputStream output = urlConnection.getOutputStream();
                output.write(jsonData.getBytes());

                HTTPResponseCode = urlConnection.getResponseCode();

                if (HTTPResponseCode == 200) {

                    Log.e("ACCOUNTING", "HTTPResponseCode " + HTTPResponseCode);
                }
                else {

                    Log.e("ACCOUNTING", "Error send transaction data: " + HTTPResponseCode);
                    LogManager.debug(SendTransactionDataTask.class.getSimpleName(), "transaction WS error: " + HTTPResponseCode);
                    notifyMonitor(TaskEvent.FAILED, this);
                }
            }
            else {

                Log.e("ACCOUNTING", "No transaction data error");
                LogManager.debug(SendTransactionDataTask.class.getSimpleName(), "No transaction data error");
                notifyMonitor(TaskEvent.FAILED, this);
            }

        } catch(Exception e) {

            LogManager.error(SendTransactionDataTask.class.getSimpleName(), "transaction WS error", e);
            notifyMonitor(TaskEvent.FAILED, this);

        } finally {

            if(urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private String formatTransactionDataToJson(Bundle bundle) {

        String jsonData = null;

        if (bundle.containsKey("transactionStatus")
                && bundle.containsKey("transactionID")
                && bundle.containsKey("payboxID")
                && bundle.containsKey("terminalData")) {

            if (bundle.getString("terminalData").length() > 0) {

                JSONObject record = new JSONObject();
                try {
                    record.put("transactionStatus", bundle.get("transactionStatus"));
                    record.put("transactionID", bundle.get("transactionID"));
                    record.put("payboxID", bundle.get("payboxID"));
                    record.put("terminalData", bundle.get("terminalData"));
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }

                JSONArray array = new JSONArray();
                array.put(record);

                JSONObject records = new JSONObject();
                try {
                    records.put("records", array);
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }

                jsonData = records.toString();

                Log.v("JSON", jsonData);
            }
        }

        return jsonData;
    }
}
