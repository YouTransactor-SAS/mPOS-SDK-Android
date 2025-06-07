/*
 * ============================================================================
 *
 * Copyright (c) 2024 YouTransactor
 *
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of YouTransactor
 * ("Confidential Information"). You  shall not disclose or redistribute such
 * Confidential Information and shall use it only in accordance with the terms of
 * the license agreement you entered into with YouTransactor.
 *
 * This software is provided by YouTransactor AS IS, and YouTransactor
 * makes no representations or warranties about the suitability of the software,
 * either express or implied, including but not limited to the implied warranties
 * of merchantability, fitness for a particular purpose or non-infringement.
 * YouTransactor shall not be liable for any direct, indirect, incidental,
 * special, exemplary, or consequential damages suffered by licensee as the
 * result of using, modifying or distributing this software or its derivatives.
 *
 * ==========================================================================
 */
package com.youtransactor.sampleapp.emvParamUpdate;

import java.util.Dictionary;
import java.util.Hashtable;

public final class EmvParamFmt1ToYT {
    private static final Dictionary<String, String>
            emvContactTagDict = new Hashtable<>();
    private static final Dictionary<String, String>
            emvCLessVISATagDict = new Hashtable<>();

    private static final Dictionary<String, String>
            emvCLessMCLTagDict = new Hashtable<>();
    private EmvParamFmt1ToYT() {}

    public static Dictionary<String, String> get_contact_tag_dict(){
        return emvContactTagDict;
    }

    public static Dictionary<String, String> get_cless_visa_tag_dict(){
        return emvCLessVISATagDict;
    }

    public static Dictionary<String, String> get_cless_mcl_tag_dict(){
        return emvCLessMCLTagDict;
    }
    public static String get_mcl_tag_from_id(String id_str){
        return emvCLessMCLTagDict.get(id_str);
    }

    static {
        // Contact AID parameters
        // Terminal param
        emvContactTagDict.put("termType", "9F35");
        emvContactTagDict.put("addTermCap", "9F40");
        emvContactTagDict.put("termId", "9F1C");
        emvContactTagDict.put("termCtryCde", "9F1A");
        emvContactTagDict.put("mercId", "9F16");
        emvContactTagDict.put("mercCatCde", "9F15");
        emvContactTagDict.put("acqId", "9F01");
        emvContactTagDict.put("transCurrCde", "5F2A");
        emvContactTagDict.put("transCurrExp", "5F36");
        emvContactTagDict.put("transRefCurrCde", "9F3C");
        emvContactTagDict.put("transRefCurrExp", "9F3D");
        // TODO: to manage at some point here or somewhere else
//        emvContactTagDict.put("settingsVersion", "N"");/A
//        emvContactTagDict.put("settingsDate", "N"");/A
//        emvContactTagDict.put("CAPKVersion", "N"");/A
//        emvContactTagDict.put("CAPKDate", "N"");/A

        // AID Param
        emvContactTagDict.put("termCap", "9F33");
        emvContactTagDict.put("AID", "84");
        emvContactTagDict.put("defaultDDOL", "D6");
        emvContactTagDict.put("defaultTDOL", "D7");
        emvContactTagDict.put("defaultTAC", "D8");
        emvContactTagDict.put("denialTAC", "D9");
        emvContactTagDict.put("onlineTAC", "DA");
        emvContactTagDict.put("appSelInd", "DC");
        emvContactTagDict.put("appVersion", "9F09");
        emvContactTagDict.put("threshVal", "DF0E");
        emvContactTagDict.put("targPerc", "DF0D");
        emvContactTagDict.put("maxTargPerc", "DF0F");
        emvContactTagDict.put("floorLim", "9F1B");
        emvContactTagDict.put("transType", "9C");
        emvContactTagDict.put("appSelOpt", "DF41");
        emvContactTagDict.put("appLabel", "DF42");

        emvCLessVISATagDict.put("appVersion", "9F09");
        // VISA Cless
        emvCLessVISATagDict.put("transType", "9C");
        emvCLessVISATagDict.put("AID", "9F06");
        emvCLessVISATagDict.put("appVersion", "9F09");
        emvCLessVISATagDict.put("termCtryCde", "9F1A");
        emvCLessVISATagDict.put("floorLim", "9F1B");
        emvCLessVISATagDict.put("termRisk", "9F1D");
        emvCLessVISATagDict.put("termCap", "9F33");
        emvCLessVISATagDict.put("termType", "9F35");
        emvCLessVISATagDict.put("addTermCap", "9F40");
        emvCLessVISATagDict.put("appSelOpt", "DF41");
        emvCLessVISATagDict.put("appLabel", "DF42");
        emvCLessVISATagDict.put("termIntProfile", "9F53");
        emvCLessVISATagDict.put("TTQ", "9F66");
        emvCLessVISATagDict.put("enhReaderCap", "9F6E");
        emvCLessVISATagDict.put("readerCap", "9F6D");
        emvCLessVISATagDict.put("mobDevInd", "9F7E"); // NOT FOUND IN FMT1
        emvCLessVISATagDict.put("dataInputCap", "DF8117");
        emvCLessVISATagDict.put("defaultUDOL", "DF811A"); // MCL: always, value: "9F6A04"
        emvCLessVISATagDict.put("defaultTAC", "DF8120");
        emvCLessVISATagDict.put("denialTAC", "DF8121");
        emvCLessVISATagDict.put("onlineTAC", "DF8122");
        emvCLessVISATagDict.put("mclKrnlID", "DF810C"); // MCL kern ID reference: prop
        emvCLessVISATagDict.put("CVMCapReq", "DF8118");
        emvCLessVISATagDict.put("CVMCapNotReq", "DF8119");
        emvCLessVISATagDict.put("kernelConfig", "DF811B");
        emvCLessVISATagDict.put("maxLifeTimeThornRec", "DF811C"); // NOT FOUND IN FMT1 - default "0000"
        emvCLessVISATagDict.put("maxThornRec", "DF811D"); // NOT FOUND IN FMT1 - default "00"
        emvCLessVISATagDict.put("Mag-stripe CVM Capability - CVM Required", "DF811E"); // NOT FOUND IN FMT1 - default "20"
        emvCLessVISATagDict.put("secCap", "DF811F");
        emvCLessVISATagDict.put("CVMReqLim", "DF01");
        emvCLessVISATagDict.put("transLim", "DF02");
        emvCLessVISATagDict.put("transLimNoDev", "DF8124");
        emvCLessVISATagDict.put("transLimDev", "DF8125");
        // emvCLessVISATagDict.put("termClessTrxLimit", "DF00");
        emvCLessVISATagDict.put("Mag-stripe CVM Capability - No CVM Required", "DF812C"); // NOT FOUND IN FMT1 - default "08"
        emvCLessVISATagDict.put("Message Hold Time", "DF812D"); // NOT FOUND IN FMT1 - default value: "00000000"
        emvCLessVISATagDict.put("onlAndClearingMsg", "DFDF02"); // alcineo prop
        emvCLessVISATagDict.put("onlProcSockTimeout", "DFDF14"); // alcineo prop
        emvCLessVISATagDict.put("onlProcSockNbRetry", "DFDF15"); // alcineo prop

        // MCL Cless
        emvCLessMCLTagDict.put("transType", "9C");
        emvCLessMCLTagDict.put("AID", "9F06");
        emvCLessMCLTagDict.put("appVersion", "9F09");
        emvCLessMCLTagDict.put("mercCatCde", "9F15");
        emvCLessMCLTagDict.put("mercId", "9F16");
        emvCLessMCLTagDict.put("appVersion", "9F09");
        emvCLessMCLTagDict.put("termCtryCde", "9F1A");
        emvCLessMCLTagDict.put("termId", "9F1C");
        emvCLessMCLTagDict.put("termRisk", "9F1D");
        emvCLessMCLTagDict.put("termCap", "9F33");
        emvCLessMCLTagDict.put("termType", "9F35");
        emvCLessMCLTagDict.put("addTermCap", "9F40");
        emvCLessMCLTagDict.put("appSelOpt", "DF41");
        emvCLessMCLTagDict.put("appLabel", "DF42");
        emvCLessMCLTagDict.put("termIntProfile", "9F53");
        emvCLessMCLTagDict.put("TTQ", "9F66");
        emvCLessMCLTagDict.put("enhReaderCap", "9F6E");
        emvCLessMCLTagDict.put("readerCap", "9F6D");
        emvCLessMCLTagDict.put("mobDevInd", "9F7E"); // NOT FOUND IN FMT1
        emvCLessMCLTagDict.put("dataInputCap", "DF8117");
        emvCLessMCLTagDict.put("defaultUDOL", "DF811A"); // MCL: always, value: "9F6A04"
        emvCLessMCLTagDict.put("defaultTAC", "DF8120");
        emvCLessMCLTagDict.put("denialTAC", "DF8121");
        emvCLessMCLTagDict.put("onlineTAC", "DF8122");
        emvCLessMCLTagDict.put("mclKrnlID", "DF810C"); // MCL kern ID reference: prop
        emvCLessMCLTagDict.put("CVMCapReq", "DF8118");
        emvCLessMCLTagDict.put("CVMCapNotReq", "DF8119");
        emvCLessMCLTagDict.put("kernelConfig", "DF811B");
        emvCLessMCLTagDict.put("maxLifeTimeThornRec", "DF811C"); // NOT FOUND IN FMT1 - default "0000"
        emvCLessMCLTagDict.put("maxThornRec", "DF811D"); // NOT FOUND IN FMT1 - default "00"
        emvCLessMCLTagDict.put("Mag-stripe CVM Capability - CVM Required", "DF811E"); // NOT FOUND IN FMT1 - default "20"
        emvCLessMCLTagDict.put("secCap", "DF811F");
        emvCLessMCLTagDict.put("floorLim", "DF8123");
        emvCLessMCLTagDict.put("transLim", "DF8123");
        emvCLessMCLTagDict.put("transLimNoDev", "DF8124");
        emvCLessMCLTagDict.put("transLimDev", "DF8125");
        emvCLessMCLTagDict.put("CVMReqLim", "DF8126");
        emvCLessMCLTagDict.put("Mag-stripe CVM Capability - No CVM Required", "DF812C"); // NOT FOUND IN FMT1 - default "08"
        emvCLessMCLTagDict.put("Message Hold Time", "DF812D"); // NOT FOUND IN FMT1 - default value: "00000000"
        emvCLessMCLTagDict.put("onlAndClearingMsg", "DFDF02"); // alcineo prop
        emvCLessMCLTagDict.put("onlProcSockTimeout", "DFDF14"); // alcineo prop
        emvCLessMCLTagDict.put("onlProcSockNbRetry", "DFDF15"); // alcineo prop
    }
}


