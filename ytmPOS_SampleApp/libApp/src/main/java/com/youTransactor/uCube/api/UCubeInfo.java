package com.youTransactor.uCube.api;

public class UCubeInfo {

    public YTMPOSProduct ytmposProduct;
    public String address;
    public String name;
    public String serialNum;
    public String partNum;
    public String firmwareVersion;
    public String firmwareSTVersion;
    public String iccConfig;
    public String nfcConfig;
    public boolean supportNFC;

    public UCubeInfo(YTMPOSProduct ytmposProduct, String address, String name, String serialNum,
                     String partNum, String firmwareVersion, String firmwareSTVersion,
                     String iccConfig, String nfcConfig, boolean supportNFC) {
        this.ytmposProduct = ytmposProduct;
        this.address = address;
        this.name = name;
        this.serialNum = serialNum;
        this.partNum = partNum;
        this.firmwareVersion = firmwareVersion;
        this.firmwareSTVersion = firmwareSTVersion;
        this.iccConfig = iccConfig;
        this.nfcConfig = nfcConfig;
        this.supportNFC = supportNFC;
    }
}
