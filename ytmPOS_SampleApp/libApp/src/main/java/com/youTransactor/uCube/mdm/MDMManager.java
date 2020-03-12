/*
 * Copyright (C) 2011-2016, YouTransactor. All Rights Reserved.
 * <p>
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youTransactor.uCube.mdm;

import com.youTransactor.uCube.BuildConfig;
import com.youTransactor.uCube.LogManager;
import com.youTransactor.uCube.rpc.DeviceInfos;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import static com.youTransactor.uCube.ContextDataManager.*;

/**
 * @author gbillard on 4/3/16.
 */
public class MDMManager {

    private static final String WS_URL_PREFIX = "/MDM/jaxrs";
    private static final String DEFAULT_URL = "mdm-dev.youtransactor.com";
    private static final String PROTOCOL = "https://";

    private String serverURL;
    private SSLContext sslContext;
    private boolean ready;
    private DeviceInfos deviceInfos;

    private MDMManager() {
    }

    public String getServerURL() {
        return serverURL;
    }

    public HttpURLConnection initRequest(String service, String method) throws IOException {
        URL url = new URL(PROTOCOL + serverURL + WS_URL_PREFIX + service);

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        if (urlConnection instanceof HttpsURLConnection) {
            ((HttpsURLConnection) urlConnection).setSSLSocketFactory(sslContext.getSocketFactory());
        }

        urlConnection.setRequestMethod(method);
        urlConnection.setConnectTimeout(20000);
        urlConnection.setReadTimeout(30000);

        LogManager.debug(MDMManager.class.getSimpleName(), "connect request: " + serverURL + url.getPath() + " (" + method + ")");

        return urlConnection;
    }

    public boolean isReady() {
        return ready;
    }

    public DeviceInfos getDeviceInfos() {
        return deviceInfos;
    }

    public void setDeviceInfos(DeviceInfos deviceInfos) {
        this.deviceInfos = deviceInfos;
    }

    public void initialize(KeyStore uCubeCert) {

        if (!BuildConfig.PropertyPairs.contains("server_url")) {
            serverURL = DEFAULT_URL;
        } else {
            serverURL = (String) BuildConfig.PropertyPairs.get("server_url");
        }

        try {
            ready = uCubeCert != null;

            if (ready) {
                KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");
                kmf.init(uCubeCert, KEYSTORE_PWD);

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

    public void stop() {
        deviceInfos = null;
        ready = false;
        sslContext = null;
    }

    public static MDMManager getInstance() {
        return INSTANCE;
    }

    public static final String GET_METHOD = "GET";
    public static final String POST_METHOD = "POST";

    private static final MDMManager INSTANCE = new MDMManager();
}
