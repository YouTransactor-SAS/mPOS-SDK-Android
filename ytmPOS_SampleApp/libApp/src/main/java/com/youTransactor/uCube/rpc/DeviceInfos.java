/**
 * Copyright (C) 2011-2016, YouTransactor. All Rights Reserved.
 * <p/>
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youTransactor.uCube.rpc;

import com.youTransactor.uCube.TLV;
import com.youTransactor.uCube.Tools;

import java.util.Map;
import java.util.Objects;

import static com.youTransactor.uCube.rpc.Constants.*;
import static com.youTransactor.uCube.rpc.DukptIksnInfos.*;

/**
 * @author gbillard on 4/5/16.
 */
public class DeviceInfos {

    private final String KSL_ID_PATTERN_LEGACY = "550911E31DB7";
    private final String KSL_ID_PATTERN_PROD_1 = "54A8E843A954";
    private final String KSL_ID_PATTERN_DEV = "BEA704B46856";
    private final String KSL_ID_LABEL_LEGACY = "LEGACY KEY";
    private final String KSL_ID_LABEL_PROD_1 = "PROD 1 KEY";
    private final String KSL_ID_LABEL_DEV = "DEV KEY";
    private final String KSL_ID_LABEL_UNKNOWN = "ERROR";
    private byte[] tlv;
    private String serial;
    private String partNumber;
    private String svppFirmware;
    private String emvL1Version;
    private String emvL2Version;
    private String iccEmvConfigVersion;
    private String nfcFirmware;
    private String nfcEmvL1Version;
    private String nfcEmvL2Version;
    private String nfcEmvConfigVersion;
    private byte nfcModuleState;
    private String svppChecksum;
    private String kslId;
    private String kslIdLabel;
    private String ksnFormats;
    private DukptIksnInfos ksnInfos;
    private String transactionData;
    private String transactionDataConfig;
    private byte batteryState;
    private byte autoPowerOffTimeout;

    public DeviceInfos() {
    }

    public DeviceInfos(String serial, String partNUmber) {
        this.serial = serial;
        this.partNumber = partNUmber;
    }

    public DeviceInfos(String serial, String partNUmber,
                       String svppFirmware, String nfcFirmware,
                       String iccEmvConfigVersion, String nfcEmvConfigVersion) {
        this.serial = serial;
        this.partNumber = partNUmber;
        this.svppFirmware = svppFirmware;
        this.nfcFirmware = nfcFirmware;
        this.iccEmvConfigVersion = iccEmvConfigVersion;
        this.nfcEmvConfigVersion = nfcEmvConfigVersion;
    }

    public DeviceInfos(byte[] tlv) {
        init(tlv);
    }

    public byte[] getTlv() {
        return tlv;
    }

    public String getSerial() {
        return serial;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public String getSvppFirmware() {
        return svppFirmware;
    }

    public String getEmvL1Version() {
        return emvL1Version;
    }

    public String getEmvL2Version() {
        return emvL2Version;
    }

    public String getIccEmvConfigVersion() {
        return iccEmvConfigVersion;
    }

    public String getNfcFirmware() {
        return nfcFirmware;
    }

    public String getNfcEmvL1Version() {
        return nfcEmvL1Version;
    }

    public String getNfcEmvL2Version() {
        return nfcEmvL2Version;
    }

    public String getNfcEmvConfigVersion() {
        return nfcEmvConfigVersion;
    }

    public byte getNfcModuleState() {
        return nfcModuleState;
    }

    public String getSvppChecksum() {
        return svppChecksum;
    }

    public String getKslId() {
        return kslId;
    }

    public String getKslIdLabel() {
        return kslIdLabel;
    }

    public String getKsnFormats() {
        return ksnFormats;
    }

    public String getKsiPin() {
        return ksnInfos.getKsi(IDX_IPEK);
    }

    public String getKsiData() {
        return ksnInfos.getKsi(IDX_IDEK);
    }

    public String getTransactionDataConfig() {
        return transactionDataConfig;
    }

    public String getTransactionData() {
        return transactionData;
    }

    public byte getBatteryState() {return batteryState;}

    public byte getAutoPowerOffTimeout() {return autoPowerOffTimeout;}

    private void init(byte[] tlv) {
        this.tlv = tlv;

        if (tlv == null || tlv.length == 0) {
            return;
        }

        Map<Integer, byte[]> valueByTag = TLV.parseYtBerMixedLen(tlv);


        serial = Tools.parseSerial(valueByTag.get(TAG_TERMINAL_SN));
        partNumber = Tools.parsePartNumber(valueByTag.get(TAG_TERMINAL_PN));

        //todo remove  this when the bug of inverse SN and PN will be fixed in ucube
        //todo do this only ucube model not for ucube touch

        if(!partNumber.contains("UCUBE")) {
            String tmp = serial;
            serial = partNumber;
            partNumber = tmp;
        }

        svppFirmware = Tools.parseVersion(valueByTag.get(TAG_FIRMWARE_VERSION));

        svppChecksum = Tools.bytesToHex(valueByTag.get(TAG_SVPP_CHECKSUM));

        emvL1Version = Tools.parseVersion(valueByTag.get(TAG_EMV_L1_VERSION));

        emvL2Version = Tools.parseVersion(valueByTag.get(TAG_EMV_L2_VERSION));

        iccEmvConfigVersion = Tools.parseVersion(valueByTag.get(TAG_EMV_ICC_CONFIG_VERSION));

        kslId = Tools.bytesToHex(valueByTag.get(TAG_KSL_ID));

        if (!"".equals(kslId)) {
            setKslIDLabel();
        }

        ksnFormats = Tools.bytesToHex(valueByTag.get(TAG_KSN_FORMATS));

        if (!"".equals(ksnFormats)) {
            this.ksnInfos = new DukptIksnInfos(valueByTag.get(TAG_KSN_FORMATS), ksnFormats);
        }

        // NOTE: following code exit if NFC datas are not in the payload
        if (valueByTag.containsKey(TAG_MPOS_MODULE_STATE)) {
            nfcModuleState = valueByTag.get(TAG_MPOS_MODULE_STATE)[0];
        }

        Map<Integer, byte[]> nfcInfos = TLV.parseYtBerMixedLen(valueByTag.get(TAG_NFC_INFOS));

        if (nfcInfos != null) {

            nfcFirmware = Tools.parseVersion(nfcInfos.get(TAG_FIRMWARE_VERSION));

            byte[] v = nfcInfos.get(TAG_EMV_L1_NFC_VERSION);
            if (v != null && v.length > 4) {
                byte[] v2 = new byte[4];
                System.arraycopy(v, 0, v2, 0, 4);

                nfcEmvL1Version = Tools.parseVersion(v2);
            }

            nfcEmvL2Version = Tools.parseVersion(nfcInfos.get(TAG_EMV_L2_NFC_VERSION));

            nfcEmvConfigVersion = Tools.parseVersion(valueByTag.get(TAG_EMV_NFC_CONFIG_VERSION));

        }
        
        transactionData = Tools.bytesToHex(valueByTag.get(TAG_TRANSACTION_DATA));

        transactionDataConfig = Tools.bytesToHex(valueByTag.get(TAG_TRANSACTION_CONFIG));

        if (valueByTag.containsKey(TAG_BATTERY_STATE)) {
            batteryState = valueByTag.get(TAG_BATTERY_STATE)[0];
        }

        if (valueByTag.containsKey(TAG_POWER_OFF_TIMEOUT)) {
            autoPowerOffTimeout = valueByTag.get(TAG_POWER_OFF_TIMEOUT)[0];
        }

    }

    private void setKslIDLabel() {

        if (kslId.contains(KSL_ID_PATTERN_LEGACY)) {
            kslIdLabel = KSL_ID_LABEL_LEGACY;
        } else if (kslId.contains(KSL_ID_PATTERN_PROD_1)) {
            kslIdLabel = KSL_ID_LABEL_PROD_1;
        } else if (kslId.contains(KSL_ID_PATTERN_DEV)) {
            kslIdLabel = KSL_ID_LABEL_DEV;
        } else {
            kslIdLabel = KSL_ID_LABEL_UNKNOWN;
        }
    }

}
