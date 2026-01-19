/*
 * ============================================================================
 *
 * Copyright (c) 2025 YouTransactor
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

import com.youTransactor.uCube.TLV;
import com.youTransactor.uCube.emv.EmvParamDOL;
import com.youTransactor.uCube.emv.EmvParamYTModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EmvParamFmt1CLCUP extends EmvParamFmt1{
    static void getEmvModelFromFmt1Input(
            JSONObject jsonD, EmvParamYTModel model) throws JSONException {
        model.setIsClCupConfigured(true);
        JSONObject clJson = jsonD.getJSONObject("contactless");
        JSONObject common = jsonD.getJSONObject("common");
        JSONObject clCommon = clJson.getJSONObject("common");
        // contact AID array
        JSONArray clAIDList = clJson.getJSONArray("aids");
        List<String> cupAppTokenL = new ArrayList<>();
        cupAppTokenL.add("UNIONPAY CREDIT");
        cupAppTokenL.add("UNIONPAY DEBIT");
        cupAppTokenL.add("UNIONPAY QUASI");
        cupAppTokenL.add("UNIONPAY US");
        int nbCupAid = EmvParamFmt1.getNbAIDProfileForKernel(
                clAIDList, cupAppTokenL);
        EmvParamDOL clCUPalltagDol = new EmvParamDOL();
        setClInfoInModel(model, common);
        for(int i = 0; i < clAIDList.length(); i++) {
            JSONObject curJsonAidLst = clAIDList.getJSONObject(i);
            EmvParamYTModel.ClessEltDsc clessAIDDsc =
                    new EmvParamYTModel.ClessEltDsc();
            String aidKrnlTok = curJsonAidLst.getString("appLabel");
            boolean isAidEltIdFound;
            for(String kernelTok : cupAppTokenL) {
                if (aidKrnlTok.contains(kernelTok)) {
                    isAidEltIdFound = true;
                    EmvParamDOL clCommonLst = EmvParamFmt1.get_tlv_dol(
                            clCommon,
                            EmvParamFmt1ToYT.get_cless_cup_tag_dict());
                    EmvParamDOL commonLst = EmvParamFmt1.get_tlv_dol(
                            common,
                            EmvParamFmt1ToYT.get_cless_cup_tag_dict());
                    clessAIDDsc.dol = EmvParamFmt1.get_tlv_dol(
                            clAIDList.getJSONObject(i),
                            EmvParamFmt1ToYT.get_cless_cup_tag_dict());
                    // AID parameters repeat common parameters
                    clessAIDDsc.dol.dol.addAll(clCommonLst.dol);
                    clessAIDDsc.dol.dol.addAll(commonLst.dol);
                    // Terminal Floor Limit
                    if (!clessAIDDsc.dol.is_tlv_present("9F1B")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "9F1B", "00000000", "B_"));
                    }
                    // Terminal Transaction Qualifiers (TTQ) (Follow Part V Contactless Integrated Circuit Card Payment Specification_v2021) according to ICS value
                    if (!clessAIDDsc.dol.is_tlv_present("9F66")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "9F66", "36804000", "B_"));
                    }
                    // Default Reader Contactless Transaction Limit
                    if (!clessAIDDsc.dol.is_tlv_present("DF00")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF00", "999999999999", "B_"));
                    }
                    // Default Reader Contactless CVM Limit
                    if (!clessAIDDsc.dol.is_tlv_present("DF01")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF01", "000000030000", "B_"));
                    }
                    // Default Reader Contactless Floor Limit
                    if (!clessAIDDsc.dol.is_tlv_present("DF02")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF02", "000000000000", "B_"));
                    }
                    // Terminal Action Code - Default
                    if (!clessAIDDsc.dol.is_tlv_present("DF8120")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF8120", "D84000A800", "B_"));
                    }
                    // Terminal Action Code - Denial
                    if (!clessAIDDsc.dol.is_tlv_present("DF8121")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF8121", "0000000000", "B_"));
                    }
                    // Terminal Action Code - Online
                    if (!clessAIDDsc.dol.is_tlv_present("DF8122")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF8122", "DC4004F800", "B_"));
                    }
                    // Default reader Risk Paramaeters Checking capabilities, see in AO-AN-Property
                    if (!clessAIDDsc.dol.is_tlv_present("DFDF22")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DFDF22", "3A00", "B_"));
                    }
                    clessAIDDsc.type = EmvParamYTModel.TypeID.clCUPKrnId.getVal();
                    clessAIDDsc.eltToUpdID = EmvParamYTModel.EltToUpdID.noId.getVal();
                    if (clCUPalltagDol.dol.isEmpty()) {
                        // add terminal parameters as first loaded element
                        clCUPalltagDol.clone_dol(clessAIDDsc.dol);
                        EmvParamYTModel.ClessEltDsc clessTermDsc =
                                new EmvParamYTModel.ClessEltDsc();
                        clessTermDsc.type = EmvParamYTModel.TypeID.clCUPKrnId.getVal();
                        clessTermDsc.eltToUpdID = EmvParamYTModel.EltToUpdID.clTermId.getVal();
                        clessTermDsc.dol = clCUPalltagDol;
                        // Transaction Currency Code
                        if (!clessTermDsc.dol.is_tlv_present("5F2A")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "5F2A", "0156", "B_"));
                        }
                        // Application Version number assigned to terminal
                        if (!clessTermDsc.dol.is_tlv_present("9F09")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F09", "0030", "B_"));
                        }
                        // Terminal Country Code
                        if (!clessTermDsc.dol.is_tlv_present("9F1A")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F1A", "0156", "B_"));
                        }
                        // Terminal Capabilities (Follow EMV Book 4 annex A2 to fill 3 bytes) according to ICS value
                        if (!clessTermDsc.dol.is_tlv_present("9F33")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F33", "E06000", "B_"));
                        }
                        // Terminal Type (Follow EMV Book 4 annex A1 to fill 1 byte) according to ICS value
                        if (!clessTermDsc.dol.is_tlv_present("9F35")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F35", "21", "B_"));
                        }
                        // Additional Terminal Capabilities (Follow EMV Book4 annex A2) according to ICS value
                        if (!clessTermDsc.dol.is_tlv_present("9F40")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F40", "600000A000", "B_"));
                        }
                        // Merchant Name and Location
                        if (!clessTermDsc.dol.is_tlv_present("9F4E")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F4E", "BankTest          01", "AN_"));
                        }
                        // Number of supported AIDs
                        clessTermDsc.dol.add_tlv(new TLV(
                                "D000", String.format(Locale.US,
                                "%02d", nbCupAid), "B_"));
                        // Online and clearing message for EMV Transaction
                        if (!clessTermDsc.dol.is_tlv_present("DFDF02")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "DFDF02", "55575A8295999A9CCF5F205F245F2A5F349F029F039F109F199F1A9F219F249F259F269F279F339F369F379F399F4E9F5D9F639F669F6E9F7C9F5BDFDF24DFDF28DFDF29DFDF27", "B_"));
                        }
                        // Socket timeout for Online Processing (in ms)
                        if (!clessTermDsc.dol.is_tlv_present("DFDF14")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "DFDF14", "0000C350", "B_"));
                        }
                        // Number of connection retries in Online Processing
                        if (!clessTermDsc.dol.is_tlv_present("DFDF15")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "DFDF15", "00000001", "B_"));
                        }
                        // Reader Features, see in AO-AN-Property
                        if (!clessTermDsc.dol.is_tlv_present("DFDF21")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "DFDF21", "2000", "B_"));
                        }
                        clessTermDsc.dol = model.filterDOLForAIDParam(
                                clessTermDsc.dol, model.clCUPTermAIDTagList);
                        // not defined in JPMC param file
                        model.add_cless_elt(clessTermDsc);
                    }
                    clessAIDDsc.dol = model.filterDOLForAIDParam(
                            clessAIDDsc.dol, model.clCUPAIDTagList);
                } else {
                    isAidEltIdFound = false;
                }
                if (isAidEltIdFound) {
                    model.add_cless_elt(clessAIDDsc);
                    model.setIsClCupConfigured(true);
                    break;
                }
            }
        }
        EmvParamFmt1.closeLoad(model.isClCupConfigured(),
                EmvParamYTModel.TypeID.clCUPKrnCloseId,
                model);
    }
}
