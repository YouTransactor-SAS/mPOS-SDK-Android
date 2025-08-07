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
    private static final Dictionary<String, String>
            emvCLessJCBTagDict = new Hashtable<>();
    private static final Dictionary<String, String>
            emvCLessAMEXTagDict = new Hashtable<>();
    private static final Dictionary<String, String>
            emvCLessDISCTagDict = new Hashtable<>();
    private static final Dictionary<String, String>
            emvCLessINTERACTagDict = new Hashtable<>();
    private static final Dictionary<String, String>
            emvCLessCUPTagDict = new Hashtable<>();
    private static final Dictionary<String, String>
            emvCLessCAPKTagDict = new Hashtable<>();
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
    public static Dictionary<String, String> get_cless_amex_tag_dict(){
        return emvCLessAMEXTagDict;
    }
    public static Dictionary<String, String> get_cless_disc_tag_dict(){
        return emvCLessDISCTagDict;
    }
    public static Dictionary<String, String> get_cless_jcb_tag_dict(){
        return emvCLessJCBTagDict;
    }
    public static Dictionary<String, String> get_cless_interac_tag_dict(){
        return emvCLessINTERACTagDict;
    }
    public static Dictionary<String, String> get_cless_cup_tag_dict(){

        return emvCLessCUPTagDict;
    }
    public static Dictionary<String, String> get_cless_capk_tag_dict(){
        return emvCLessCAPKTagDict;
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

        // VISA Cless
        emvCLessVISATagDict.put("appVersion", "9F09");
        emvCLessVISATagDict.put("transType", "9C");
        emvCLessVISATagDict.put("AID", "9F06");
        emvCLessVISATagDict.put("appVersion", "9F09");
        emvCLessVISATagDict.put("termCtryCde", "9F1A");
        emvCLessVISATagDict.put("floorLim", "DF02");
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
        emvCLessVISATagDict.put("transLim", "DF00");
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

        // JCB Cless
        emvCLessJCBTagDict.put("appVersion", "9F09");
        emvCLessJCBTagDict.put("transType", "9C");
        emvCLessJCBTagDict.put("AID", "9F06");
        emvCLessJCBTagDict.put("appVersion", "9F09");
        emvCLessJCBTagDict.put("appSelInd", "E000");
        emvCLessJCBTagDict.put("termCtryCde", "9F1A");
        emvCLessJCBTagDict.put("floorLim", "9F1B");
        emvCLessJCBTagDict.put("termRisk", "9F1D");
        emvCLessJCBTagDict.put("termCap", "9F33");
        emvCLessJCBTagDict.put("termType", "9F35");
        emvCLessJCBTagDict.put("addTermCap", "9F40");
        emvCLessJCBTagDict.put("appSelOpt", "DF41");
        emvCLessJCBTagDict.put("appLabel", "DF42");
        emvCLessJCBTagDict.put("termIntProfile", "DFDF45");
        emvCLessJCBTagDict.put("enhReaderCap", "9F6E");
        emvCLessJCBTagDict.put("readerCap", "9F6D");
        emvCLessJCBTagDict.put("mobDevInd", "9F7E"); // NOT FOUND IN FMT1
        emvCLessJCBTagDict.put("dataInputCap", "DF8117");
        emvCLessJCBTagDict.put("defaultUDOL", "DF811A"); // MCL: always, value: "9F6A04"
        emvCLessJCBTagDict.put("defaultTAC", "DF8120");
        emvCLessJCBTagDict.put("denialTAC", "DF8121");
        emvCLessJCBTagDict.put("onlineTAC", "DF8122");
        emvCLessJCBTagDict.put("mclKrnlID", "DF810C"); // MCL kern ID reference: prop
        emvCLessJCBTagDict.put("CVMCapReq", "DF8118");
        emvCLessJCBTagDict.put("CVMCapNotReq", "DF8119");
        emvCLessJCBTagDict.put("kernelConfig", "DF811B");
        emvCLessJCBTagDict.put("maxLifeTimeThornRec", "DF811C"); // NOT FOUND IN FMT1 - default "0000"
        emvCLessJCBTagDict.put("maxThornRec", "DF811D"); // NOT FOUND IN FMT1 - default "00"
        emvCLessJCBTagDict.put("Mag-stripe CVM Capability - CVM Required", "DF811E"); // NOT FOUND IN FMT1 - default "20"
        emvCLessJCBTagDict.put("secCap", "DF811F");
        emvCLessJCBTagDict.put("CVMReqLim", "DF01");
        emvCLessJCBTagDict.put("transLim", "DF00");
        emvCLessJCBTagDict.put("transLimNoDev", "DF8124");
        emvCLessJCBTagDict.put("transLimDev", "DF8125");
        // emvCLessJCBTagDict.put("termClessTrxLimit", "DF00");
        emvCLessJCBTagDict.put("Mag-stripe CVM Capability - No CVM Required", "DF812C"); // NOT FOUND IN FMT1 - default "08"
        emvCLessJCBTagDict.put("Message Hold Time", "DF812D"); // NOT FOUND IN FMT1 - default value: "00000000"
        emvCLessJCBTagDict.put("onlAndClearingMsg", "DFDF02"); // alcineo prop
        emvCLessJCBTagDict.put("onlProcSockTimeout", "DFDF14"); // alcineo prop
        emvCLessJCBTagDict.put("onlProcSockNbRetry", "DFDF15"); // alcineo prop
        emvCLessJCBTagDict.put("termIntProfile", "9F53");

        // AMEX Cless
        emvCLessAMEXTagDict.put("appVersion", "9F09");
        emvCLessAMEXTagDict.put("transType", "9C");
        emvCLessAMEXTagDict.put("AID", "9F06");
        emvCLessAMEXTagDict.put("appVersion", "9F09");
        emvCLessAMEXTagDict.put("termCtryCde", "9F1A");
        emvCLessAMEXTagDict.put("floorLim", "9F1B");
        emvCLessAMEXTagDict.put("termRisk", "9F1D");
        emvCLessAMEXTagDict.put("termCap", "9F33");
        emvCLessAMEXTagDict.put("termType", "9F35");
        emvCLessAMEXTagDict.put("addTermCap", "9F40");
        emvCLessAMEXTagDict.put("appSelOpt", "DF41");
        emvCLessAMEXTagDict.put("appLabel", "DF42");
        emvCLessAMEXTagDict.put("termIntProfile", "DFDF45");
        emvCLessAMEXTagDict.put("enhReaderCap", "9F6E");
        emvCLessAMEXTagDict.put("readerCap", "9F6D");
        emvCLessAMEXTagDict.put("mobDevInd", "9F7E"); // NOT FOUND IN FMT1
        emvCLessAMEXTagDict.put("dataInputCap", "DF8117");
        emvCLessAMEXTagDict.put("defaultUDOL", "DF811A"); // MCL: always, value: "9F6A04"
        emvCLessAMEXTagDict.put("defaultTAC", "DF8120");
        emvCLessAMEXTagDict.put("denialTAC", "DF8121");
        emvCLessAMEXTagDict.put("onlineTAC", "DF8122");
        emvCLessAMEXTagDict.put("mclKrnlID", "DF810C"); // MCL kern ID reference: prop
        emvCLessAMEXTagDict.put("CVMCapReq", "DF8118");
        emvCLessAMEXTagDict.put("CVMCapNotReq", "DF8119");
        emvCLessAMEXTagDict.put("kernelConfig", "DF811B");
        emvCLessAMEXTagDict.put("maxLifeTimeThornRec", "DF811C"); // NOT FOUND IN FMT1 - default "0000"
        emvCLessAMEXTagDict.put("maxThornRec", "DF811D"); // NOT FOUND IN FMT1 - default "00"
        emvCLessAMEXTagDict.put("Mag-stripe CVM Capability - CVM Required", "DF811E"); // NOT FOUND IN FMT1 - default "20"
        emvCLessAMEXTagDict.put("secCap", "DF811F");
        emvCLessAMEXTagDict.put("CVMReqLim", "DF01");
        emvCLessAMEXTagDict.put("transLim", "DF00");
        emvCLessAMEXTagDict.put("appSelInd", "E000");
        emvCLessAMEXTagDict.put("transLimNoDev", "DF8124");
        emvCLessAMEXTagDict.put("transLimDev", "DF8125");
        // emvCLessAMEXTagDict.put("termClessTrxLimit", "DF00");
        emvCLessAMEXTagDict.put("Mag-stripe CVM Capability - No CVM Required", "DF812C"); // NOT FOUND IN FMT1 - default "08"
        emvCLessAMEXTagDict.put("Message Hold Time", "DF812D"); // NOT FOUND IN FMT1 - default value: "00000000"
        emvCLessAMEXTagDict.put("onlAndClearingMsg", "DFDF02"); // alcineo prop
        emvCLessAMEXTagDict.put("onlProcSockTimeout", "DFDF14"); // alcineo prop
        emvCLessAMEXTagDict.put("onlProcSockNbRetry", "DFDF15"); // alcineo prop

        // CUP Cless
        emvCLessCUPTagDict.put("appVersion", "9F09");
        emvCLessCUPTagDict.put("transType", "9C");
        emvCLessCUPTagDict.put("AID", "9F06");
        emvCLessCUPTagDict.put("appVersion", "9F09");
        emvCLessCUPTagDict.put("termCtryCde", "9F1A");
        emvCLessCUPTagDict.put("floorLim", "9F1B");
        emvCLessCUPTagDict.put("termRisk", "9F1D");
        emvCLessCUPTagDict.put("termCap", "9F33");
        emvCLessCUPTagDict.put("termType", "9F35");
        emvCLessCUPTagDict.put("addTermCap", "9F40");
        emvCLessCUPTagDict.put("appSelOpt", "DF41");
        emvCLessCUPTagDict.put("appLabel", "DF42");
        emvCLessCUPTagDict.put("appSelInd", "E000");
        emvCLessCUPTagDict.put("termIntProfile", "DFDF45");
        emvCLessCUPTagDict.put("enhReaderCap", "9F6E");
        emvCLessCUPTagDict.put("readerCap", "9F6D");
        emvCLessCUPTagDict.put("mobDevInd", "9F7E"); // NOT FOUND IN FMT1
        emvCLessCUPTagDict.put("TTQ", "9F66");
        emvCLessCUPTagDict.put("dataInputCap", "DF8117");
        emvCLessCUPTagDict.put("defaultUDOL", "DF811A"); // MCL: always, value: "9F6A04"
        emvCLessCUPTagDict.put("defaultTAC", "DF8120");
        emvCLessCUPTagDict.put("denialTAC", "DF8121");
        emvCLessCUPTagDict.put("onlineTAC", "DF8122");
        emvCLessCUPTagDict.put("mclKrnlID", "DF810C"); // MCL kern ID reference: prop
        emvCLessCUPTagDict.put("CVMCapReq", "DF8118");
        emvCLessCUPTagDict.put("CVMCapNotReq", "DF8119");
        emvCLessCUPTagDict.put("kernelConfig", "DF811B");
        emvCLessCUPTagDict.put("maxLifeTimeThornRec", "DF811C"); // NOT FOUND IN FMT1 - default "0000"
        emvCLessCUPTagDict.put("maxThornRec", "DF811D"); // NOT FOUND IN FMT1 - default "00"
        emvCLessCUPTagDict.put("Mag-stripe CVM Capability - CVM Required", "DF811E"); // NOT FOUND IN FMT1 - default "20"
        emvCLessCUPTagDict.put("secCap", "DF811F");
        emvCLessCUPTagDict.put("CVMReqLim", "DF01");
        emvCLessCUPTagDict.put("transLim", "DF00");
        emvCLessCUPTagDict.put("transLimNoDev", "DF8124");
        emvCLessCUPTagDict.put("transLimDev", "DF8125");
        // emvCLessCUPTagDict.put("termClessTrxLimit", "DF00");
        emvCLessCUPTagDict.put("Mag-stripe CVM Capability - No CVM Required", "DF812C"); // NOT FOUND IN FMT1 - default "08"
        emvCLessCUPTagDict.put("Message Hold Time", "DF812D"); // NOT FOUND IN FMT1 - default value: "00000000"
        emvCLessCUPTagDict.put("onlAndClearingMsg", "DFDF02"); // alcineo prop
        emvCLessCUPTagDict.put("onlProcSockTimeout", "DFDF14"); // alcineo prop
        emvCLessCUPTagDict.put("onlProcSockNbRetry", "DFDF15"); // alcineo prop

        // INTERAC Cless
        emvCLessINTERACTagDict.put("defaultTDOL", "97");
        emvCLessINTERACTagDict.put("appVersion", "9F09");
        emvCLessINTERACTagDict.put("transType", "9C");
        emvCLessINTERACTagDict.put("AID", "9F06");
        emvCLessINTERACTagDict.put("appVersion", "9F09");
        emvCLessINTERACTagDict.put("termCtryCde", "9F1A");
        emvCLessINTERACTagDict.put("floorLim", "9F1B");
        emvCLessINTERACTagDict.put("termRisk", "9F1D");
        emvCLessINTERACTagDict.put("termCap", "9F33");
        emvCLessINTERACTagDict.put("termType", "9F35");
        emvCLessINTERACTagDict.put("addTermCap", "9F40");
        emvCLessINTERACTagDict.put("appSelOpt", "DF41");
        emvCLessINTERACTagDict.put("defaultDDOL", "9F49");
        emvCLessINTERACTagDict.put("MTI", "9F58");
        emvCLessINTERACTagDict.put("TTI", "9F59");
        emvCLessINTERACTagDict.put("TOS", "9F5E");
        emvCLessINTERACTagDict.put("receiptLim", "9F5D");
        emvCLessINTERACTagDict.put("appLabel", "DF42");
        emvCLessINTERACTagDict.put("termIntProfile", "DFDF45");
        emvCLessINTERACTagDict.put("enhReaderCap", "9F6E");
        emvCLessINTERACTagDict.put("readerCap", "9F6D");
        emvCLessINTERACTagDict.put("mobDevInd", "9F7E"); // NOT FOUND IN FMT1
        emvCLessINTERACTagDict.put("appSelInd", "E000");
        emvCLessINTERACTagDict.put("dataInputCap", "DF8117");
        emvCLessINTERACTagDict.put("defaultUDOL", "DF811A"); // MCL: always, value: "9F6A04"
        emvCLessINTERACTagDict.put("defaultTAC", "DF8120");
        emvCLessINTERACTagDict.put("denialTAC", "DF8121");
        emvCLessINTERACTagDict.put("onlineTAC", "DF8122");
        emvCLessINTERACTagDict.put("mclKrnlID", "DF810C"); // MCL kern ID reference: prop
        emvCLessINTERACTagDict.put("CVMCapReq", "DF8118");
        emvCLessINTERACTagDict.put("CVMCapNotReq", "DF8119");
        emvCLessINTERACTagDict.put("kernelConfig", "DF811B");
        emvCLessINTERACTagDict.put("maxLifeTimeThornRec", "DF811C"); // NOT FOUND IN FMT1 - default "0000"
        emvCLessINTERACTagDict.put("maxThornRec", "DF811D"); // NOT FOUND IN FMT1 - default "00"
        emvCLessINTERACTagDict.put("Mag-stripe CVM Capability - CVM Required", "DF811E"); // NOT FOUND IN FMT1 - default "20"
        emvCLessINTERACTagDict.put("secCap", "DF811F");
        emvCLessINTERACTagDict.put("CVMReqLim", "DF01");
        emvCLessINTERACTagDict.put("transLim", "DF00");
        emvCLessINTERACTagDict.put("transLimNoDev", "DF8124");
        emvCLessINTERACTagDict.put("transLimDev", "DF8125");
        // emvCLessINTERACTagDict.put("termClessTrxLimit", "DF00");
        emvCLessINTERACTagDict.put("Mag-stripe CVM Capability - No CVM Required", "DF812C"); // NOT FOUND IN FMT1 - default "08"
        emvCLessINTERACTagDict.put("Message Hold Time", "DF812D"); // NOT FOUND IN FMT1 - default value: "00000000"
        emvCLessINTERACTagDict.put("onlAndClearingMsg", "DFDF02"); // alcineo prop
        emvCLessINTERACTagDict.put("onlProcSockTimeout", "DFDF14"); // alcineo prop
        emvCLessINTERACTagDict.put("onlProcSockNbRetry", "DFDF15"); // alcineo prop

        // DISC Cless
        emvCLessDISCTagDict.put("appVersion", "9F09");
        emvCLessDISCTagDict.put("transType", "9C");
        emvCLessDISCTagDict.put("AID", "9F06");
        emvCLessDISCTagDict.put("appVersion", "9F09");
        emvCLessDISCTagDict.put("termCtryCde", "9F1A");
        emvCLessDISCTagDict.put("floorLim", "9F1B");
        emvCLessDISCTagDict.put("termRisk", "9F1D");
        emvCLessDISCTagDict.put("termCap", "9F33");
        emvCLessDISCTagDict.put("termType", "9F35");
        emvCLessDISCTagDict.put("addTermCap", "9F40");
        emvCLessDISCTagDict.put("appSelOpt", "DF41");
        emvCLessDISCTagDict.put("appLabel", "DF42");
        emvCLessDISCTagDict.put("termIntProfile", "DFDF45");
        emvCLessDISCTagDict.put("enhReaderCap", "9F6E");
        emvCLessDISCTagDict.put("TTQ", "9F66");
        emvCLessDISCTagDict.put("readerCap", "9F6D");
        emvCLessDISCTagDict.put("mobDevInd", "9F7E"); // NOT FOUND IN FMT1
        emvCLessDISCTagDict.put("appSelInd", "E000");
        emvCLessDISCTagDict.put("dataInputCap", "DF8117");
        emvCLessDISCTagDict.put("defaultUDOL", "DF811A"); // MCL: always, value: "9F6A04"
        emvCLessDISCTagDict.put("defaultTAC", "DF8120");
        emvCLessDISCTagDict.put("denialTAC", "DF8121");
        emvCLessDISCTagDict.put("onlineTAC", "DF8122");
        emvCLessDISCTagDict.put("mclKrnlID", "DF810C"); // MCL kern ID reference: prop
        emvCLessDISCTagDict.put("CVMCapReq", "DF8118");
        emvCLessDISCTagDict.put("CVMCapNotReq", "DF8119");
        emvCLessDISCTagDict.put("kernelConfig", "DF811B");
        emvCLessDISCTagDict.put("maxLifeTimeThornRec", "DF811C"); // NOT FOUND IN FMT1 - default "0000"
        emvCLessDISCTagDict.put("maxThornRec", "DF811D"); // NOT FOUND IN FMT1 - default "00"
        emvCLessDISCTagDict.put("Mag-stripe CVM Capability - CVM Required", "DF811E"); // NOT FOUND IN FMT1 - default "20"
        emvCLessDISCTagDict.put("secCap", "DF811F");
        emvCLessDISCTagDict.put("CVMReqLim", "DF01");
        emvCLessDISCTagDict.put("transLim", "DF00");
        emvCLessDISCTagDict.put("transLimNoDev", "DF8124");
        emvCLessDISCTagDict.put("transLimDev", "DF8125");
        // emvCLessDISCTagDict.put("termClessTrxLimit", "DF00");
        emvCLessDISCTagDict.put("Mag-stripe CVM Capability - No CVM Required", "DF812C"); // NOT FOUND IN FMT1 - default "08"
        emvCLessDISCTagDict.put("Message Hold Time", "DF812D"); // NOT FOUND IN FMT1 - default value: "00000000"
        emvCLessDISCTagDict.put("onlAndClearingMsg", "DFDF02"); // alcineo prop
        emvCLessDISCTagDict.put("onlProcSockTimeout", "DFDF14"); // alcineo prop
        emvCLessDISCTagDict.put("onlProcSockNbRetry", "DFDF15"); // alcineo prop

        // CAPKs
        emvCLessCAPKTagDict.put("RID", "CA00");
        emvCLessCAPKTagDict.put("index", "CA01");
        emvCLessCAPKTagDict.put("mod", "CA02");
        emvCLessCAPKTagDict.put("exp", "CA03");
        emvCLessCAPKTagDict.put("checkSum", "CA04");
    }
}


