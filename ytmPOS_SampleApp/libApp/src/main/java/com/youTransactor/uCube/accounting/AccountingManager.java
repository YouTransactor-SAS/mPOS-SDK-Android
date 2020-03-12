/*
 * Copyright (C) 2016, YouTransactor. All Rights Reserved.
 *
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */

package com.youTransactor.uCube.accounting;

import android.util.Log;

import com.youTransactor.uCube.BuildConfig;
import com.youTransactor.uCube.ContextDataManager;
import com.youTransactor.uCube.LogManager;
import com.youTransactor.uCube.rpc.DeviceInfos;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import static com.youTransactor.uCube.ContextDataManager.KEYSTORE_PWD;

/**
 * Created by gmx on 25/07/17.
 */

public class AccountingManager implements ContextDataManager.ContextDataChangeListener {

    private static final AccountingManager ourInstance = new AccountingManager();

    public static AccountingManager getInstance() {
        return ourInstance;
    }

    public static final String GET_METHOD                                 = "GET";
    public static final String POST_METHOD                                = "POST";
    public static final String DEFAULT_URL                                = "https://mdm-dev.youtransactor.com";

    private static final String WS_URL_PREFIX_DEV                         = "/Accounting-DEV";
    private static final String WS_URL_PREFIX_PREPROD                     = "/Accounting-QLF";
    private static final String WS_URL_PREFIX_PROD                        = "/Accounting";

    public static final String ACCOUNTING_DEVICE_SERIAL_SETTINGS_KEY      = "Accounting.deviceSerial";
    public static final String ACCOUNTING_DEVICE_PART_NUMBER_SETTINGS_KEY = "Accounting.devicePartNUmber";
    public static final String ACCOUNTING_SERVER_URL_SETTINGS_KEY         = "Accounting.serverUrl";

    private String serverURL = DEFAULT_URL;
    private SSLContext sslContext;
    private boolean ready;
    private DeviceInfos deviceinfos;

    private AccountingManager() {
    }

    public boolean isReady() {
        return ready;
    }

    public DeviceInfos getDeviceinfos() {
        return deviceinfos;
    }

    public void setDeviceinfos(DeviceInfos deviceinfos) {
        this.deviceinfos = deviceinfos;
    }

    public void initialize() {
        if (!BuildConfig.PropertyPairs.contains("server_url")) {
            serverURL = DEFAULT_URL;
        } else {
            serverURL = (String) BuildConfig.PropertyPairs.get("server_url");
        }

        ContextDataManager.getInstance().registerListener(this);

        try {
            ready = ContextDataManager.getInstance().isUCubeSslCertificateExist();

            if (ready) {
                KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");
                kmf.init(ContextDataManager.getInstance().getUCubeSslCertificate(), KEYSTORE_PWD);

                sslContext = SSLContext.getInstance("TLS");
                sslContext.init(kmf.getKeyManagers(), null, null);
            } else {
                sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, null, null);
            }

        } catch (Exception e) {
            LogManager.e("load keystore error", e);
            sslContext = null;
        }
    }

    public HttpURLConnection initRequest(String service, String method) throws IOException {
        URL url = new URL(serverURL + WS_URL_PREFIX_DEV + service);

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        if(urlConnection instanceof HttpsURLConnection) {
            ((HttpsURLConnection)urlConnection).setSSLSocketFactory(sslContext.getSocketFactory());
        }

        urlConnection.setRequestMethod(method);
        urlConnection.setConnectTimeout(20000);
        urlConnection.setReadTimeout(30000);

        Log.e(AccountingManager.class.getSimpleName(), "connect request: " + url.getPath() + " (" + method + ")");

        return urlConnection;
    }

    public void stop() {
        ContextDataManager.getInstance().unregisterListener(this);
    }

    @Override
    public void onChanged() {
        String serial = ContextDataManager.getInstance().getUCubeSerial();
        String pn = ContextDataManager.getInstance().getUCubePartNumber();

        if (StringUtils.isNotBlank(serial) && StringUtils.isNotBlank(pn)) {
            deviceinfos = new DeviceInfos(serial, pn);
        }

        initialize();
    }
}
