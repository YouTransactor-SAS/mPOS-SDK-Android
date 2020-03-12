package com.youTransactor.uCube;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.youTransactor.uCube.api.YTMPOSProduct;
import com.youTransactor.uCube.rpc.DeviceInfos;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import static com.youTransactor.uCube.api.YTMPOSProduct.*;

public final class ContextDataManager {

    private static final String SHAREDPREF_NAME = "ucube_lib_context_data";

    public static final char[] KEYSTORE_PWD = new char[] {'g', 'm', 'x', 's', 'a', 's'};
    private static final String KEYSTORE_CLIENT_FILENAME = "keystore_client.jks";
    private static final String MDM_CLIENT_CERT_ALIAS = "MDM-client";
    private static final String KEYSTORE_TYPE = "PKCS12";

    private static final String YT_MPOS_PRODUCT = "ytMposProduct";
    private static final String UCUBE_MAC_ADDR = "uCubeMacAddr";
    private static final String UCUBE_NAME = "uCubeName";
    private static final String UCUBE_SERIAL = "uCubeSerial";
    private static final String UCUBE_PART_NUMBER = "uCubePartNumber";
    private static final String UCUBE_FIRMWARE_VERSION = "uCubeFirmwareVersion";
    private static final String UCUBE_FIRMWARE_ST_VERSION = "uCubeFirmwareStVersion";
    private static final String UCUBE_ICC_CONFIG_VERSION = "uCubeIccConfigVersion";
    private static final String UCUBE_NFC_CONFIG_VERSION = "uCubeNfcConfigVersion";
    private static final String UCUBE_NFC_SUPPORTED = "uCubeNfcSupported";

    @SuppressLint("StaticFieldLeak")
    private static final ContextDataManager INSTANCE = new ContextDataManager();

    public static ContextDataManager getInstance() {
        return INSTANCE;
    }

    @SuppressLint("StaticFieldLeak")
    static private Context context;

    private SharedPreferences sharedPreferences;

    public void init(@NonNull Context context) {
        ContextDataManager.context = context.getApplicationContext();
        sharedPreferences = context.getApplicationContext().getSharedPreferences(SHAREDPREF_NAME, Context.MODE_PRIVATE);
    }

    public static Context getContext() {
        return context;
    }

    private List<ContextDataChangeListener> contextDataChangeListenerList = new ArrayList<>();

   // private SharedPreferenceChangeListener sharedPreferenceChangeListener = new SharedPreferenceChangeListener();

    public interface ContextDataChangeListener {
        void onChanged();
    }

    public void registerListener(ContextDataChangeListener contextDataChangeListener) {
        if(contextDataChangeListenerList.contains(contextDataChangeListener))
            return;

        this.contextDataChangeListenerList.add(contextDataChangeListener);
    }

    public void unregisterListener(ContextDataChangeListener contextDataChangeListener) {
        if(!contextDataChangeListenerList.contains(contextDataChangeListener))
            return;

        this.contextDataChangeListenerList.remove(contextDataChangeListener);
    }

    public void saveDevice(DeviceInfos deviceInfos) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor
                .putString(UCUBE_SERIAL, deviceInfos.getSerial())
                .putString(UCUBE_PART_NUMBER, deviceInfos.getPartNumber())
                .putString(UCUBE_FIRMWARE_VERSION, deviceInfos.getSvppFirmware())
                .putString(UCUBE_FIRMWARE_ST_VERSION, deviceInfos.getNfcFirmware())
                .putString(UCUBE_ICC_CONFIG_VERSION, deviceInfos.getIccEmvConfigVersion())
                .putString(UCUBE_NFC_CONFIG_VERSION, deviceInfos.getNfcEmvConfigVersion())
                .apply();

        if(getYTProduct() == uCube_touch)
            editor.putBoolean(UCUBE_NFC_SUPPORTED, true).apply();
        else
            editor.putBoolean(UCUBE_NFC_SUPPORTED, deviceInfos.getNfcModuleState() != 0).apply();

        notifyChangeListener();
    }

    public void setYtProduct(YTMPOSProduct ytProduct) {
        sharedPreferences.edit().putString(YT_MPOS_PRODUCT, ytProduct.name()).apply();
    }

    public void setUCubeName(String name) {
        sharedPreferences.edit().putString(UCUBE_NAME, name).apply();
    }

    public void setUCubeAddress(String address) {
        sharedPreferences.edit().putString(UCUBE_MAC_ADDR, address).apply();
    }

    public void setUCubeSerialNum(String serialNum) {
        sharedPreferences.edit().putString(UCUBE_SERIAL, serialNum).apply();
    }

    public void setUCubePartNum(String partNum) {
        sharedPreferences.edit().putString(UCUBE_PART_NUMBER, partNum).apply();
    }

    public void setUCubeFirmwareVersion(String svppVersion) {
        sharedPreferences.edit().putString(UCUBE_FIRMWARE_VERSION, svppVersion).apply();
    }

    public void setUCubeFirmwareSTVersion(String firmwareStVersion) {
        sharedPreferences.edit().putString(UCUBE_FIRMWARE_ST_VERSION, firmwareStVersion).apply();
    }

    public void setUCubeIicConfigVersion(String iicConfigVersion) {
        sharedPreferences.edit().putString(UCUBE_ICC_CONFIG_VERSION, iicConfigVersion).apply();
    }

    public void setUCubeNfcConfigVersion(String nfcConfigVersion) {
        sharedPreferences.edit().putString(UCUBE_NFC_CONFIG_VERSION, nfcConfigVersion).apply();
    }

    public void setNFCSupported(boolean nfcSupported) {
        sharedPreferences.edit().putBoolean(UCUBE_NFC_SUPPORTED, nfcSupported).apply();
    }

    public boolean setUCubeSslCertificate(KeyStore sslKeystore) {
        try {
            FileOutputStream out = context.openFileOutput(KEYSTORE_CLIENT_FILENAME, Context.MODE_PRIVATE);
            sslKeystore.store(out, KEYSTORE_PWD);
            out.close();

            notifyChangeListener();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isUCubeSslCertificateExist() {
        try {
            KeyStore keystoreClient = null;

            File file = context.getFileStreamPath(KEYSTORE_CLIENT_FILENAME);

            if (file.exists()) {
                keystoreClient = KeyStore.getInstance(KEYSTORE_TYPE);
                InputStream in = new FileInputStream(file);
                keystoreClient.load(in, KEYSTORE_PWD);
            }

            return keystoreClient != null && keystoreClient.getKey(MDM_CLIENT_CERT_ALIAS, KEYSTORE_PWD) != null;

        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public KeyStore getUCubeSslCertificate() {
        try {
            KeyStore keystoreClient = null;

            File file = context.getFileStreamPath(KEYSTORE_CLIENT_FILENAME);

            if (file.exists()) {
                keystoreClient = KeyStore.getInstance(KEYSTORE_TYPE);
                InputStream in = new FileInputStream(file);
                keystoreClient.load(in, KEYSTORE_PWD);
            }

            if(keystoreClient != null && keystoreClient.getKey(MDM_CLIENT_CERT_ALIAS, KEYSTORE_PWD) != null)
                return keystoreClient;

            return null;

        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void deleteUCubeSslCertificate() {
        File file = context.getFileStreamPath(KEYSTORE_CLIENT_FILENAME);

        if (file.exists()) {
            boolean res = file.delete();

            if(res)
                notifyChangeListener();
        }
    }

    public void removeDevice() {
        sharedPreferences.edit()
                .remove(UCUBE_MAC_ADDR)
                .remove(UCUBE_NAME)
                .remove(UCUBE_SERIAL)
                .remove(UCUBE_PART_NUMBER)
                .remove(UCUBE_FIRMWARE_VERSION)
                .remove(UCUBE_FIRMWARE_ST_VERSION)
                .remove(UCUBE_ICC_CONFIG_VERSION)
                .remove(UCUBE_NFC_CONFIG_VERSION)
                .remove(UCUBE_NFC_SUPPORTED)
                .apply();

        deleteUCubeSslCertificate();
    }

    public YTMPOSProduct getYTProduct() {
        String productName = sharedPreferences.getString(YT_MPOS_PRODUCT, null);

        if(productName == null)
            return null;

        if(uCube.name().equals(productName))
            return uCube;

        if(uCube_touch.name().equals(productName))
            return uCube_touch;

        return null;
    }

    public String getUCubeName() {
        return sharedPreferences.getString(UCUBE_NAME, null);
    }

    public String getUCubeAddress() {
        return sharedPreferences.getString(UCUBE_MAC_ADDR, null);
    }

    public String getUCubeSerial() {
        return sharedPreferences.getString(UCUBE_SERIAL, null);
    }

    public String getUCubePartNumber() {
        return sharedPreferences.getString(UCUBE_PART_NUMBER, null);
    }

    public String getUCubeFirmwareVersion() {
        return sharedPreferences.getString(UCUBE_FIRMWARE_VERSION, null);
    }

    public String getUCubeFirmwareSTVersion() {
        return sharedPreferences.getString(UCUBE_FIRMWARE_ST_VERSION, null);
    }

    public String getUCubeIICConfigVersion() {
        return sharedPreferences.getString(UCUBE_ICC_CONFIG_VERSION, null);
    }

    public String getUCubeNFCConfigVersion() {
        return sharedPreferences.getString(UCUBE_NFC_CONFIG_VERSION, null);
    }

    public boolean getUCubeNfcAvailable() {
        return sharedPreferences.getBoolean(UCUBE_NFC_SUPPORTED, false);
    }

    public boolean isuCubePaired() {
        return getYTProduct() != null && getUCubeAddress() != null && isUCubeSslCertificateExist();
    }

    public DeviceInfos getDeviceInfos() {
       return new DeviceInfos(
               getUCubeSerial(),
               getUCubePartNumber(),
               getUCubeFirmwareVersion(),
               getUCubeFirmwareSTVersion(),
               getUCubeIICConfigVersion(),
               getUCubeNFCConfigVersion());
    }

  /*  private class SharedPreferenceChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            notifyChangeListener();
        }
    }*/

    //listener
    private void notifyChangeListener() {
        for (ContextDataChangeListener contextDataChangeListener : contextDataChangeListenerList) {
            contextDataChangeListener.onChanged();
        }
    }
}
