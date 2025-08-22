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

public class EmvParamFmt1CLInterac extends EmvParamFmt1{
    static void getEmvModelFromFmt1Input(
            JSONObject jsonD, EmvParamYTModel model) throws JSONException {
        model.setIsClInteracConfigured(true);
        JSONObject clJson = jsonD.getJSONObject("contactless");
        JSONObject common = jsonD.getJSONObject("common");
        JSONObject clCommon = clJson.getJSONObject("common");
        // contact AID array
        JSONArray clAIDList = clJson.getJSONArray("aids");
        List<String> interacAppTokenL = new ArrayList<>();
        interacAppTokenL.add("INTERAC");
        int nbInteracAid = EmvParamFmt1.getNbAIDProfileForKernel(
                clAIDList, interacAppTokenL);
        EmvParamDOL clINTERACalltagDol = new EmvParamDOL();
        model.setClParamID(EmvParamFmt1.getClParamID(common));
        for(int i = 0; i < clAIDList.length(); i++) {
            JSONObject curJsonAidLst = clAIDList.getJSONObject(i);
            EmvParamYTModel.ClessEltDsc clessAIDDsc =
                    new EmvParamYTModel.ClessEltDsc();
            String aidKrnlTok = curJsonAidLst.getString("appLabel");
            boolean isAidEltIdFound;
            for(String kernelTok : interacAppTokenL) {
                if (aidKrnlTok.contains(kernelTok)) {
                    isAidEltIdFound = true;
                    EmvParamDOL clCommonLst = EmvParamFmt1.get_tlv_dol(
                            clCommon,
                            EmvParamFmt1ToYT.get_cless_interac_tag_dict());
                    clessAIDDsc.dol = EmvParamFmt1.get_tlv_dol(
                            clAIDList.getJSONObject(i),
                            EmvParamFmt1ToYT.get_cless_interac_tag_dict());
                    // AID parameters repeat common parameters
                    clessAIDDsc.dol.dol.addAll(clCommonLst.dol);
                    // Default TDOL
                    if (!clessAIDDsc.dol.is_tlv_present("97")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "97", "5F2403", "B_"));
                    }
                    // Acquirer Identifier
                    if (!clessAIDDsc.dol.is_tlv_present("9F01")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "9F01", "000000000001", "B_"));
                    }
                    // Application Version Number
                    if (!clessAIDDsc.dol.is_tlv_present("9F09")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "9F09", "0002", "B_"));
                    }
                    // Terminal Floor Limit according to ICS value
                    if (!clessAIDDsc.dol.is_tlv_present("9F1B")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "9F1B", "00000000", "B_"));
                    }
                    // Default Dynamic Data Authentication Data Object List (DDOL)
                    if (!clessAIDDsc.dol.is_tlv_present("9F49")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "9F49", "9F3704", "B_"));
                    }
                    // Terminal Contactless Floor Limit
                    if (!clessAIDDsc.dol.is_tlv_present("9F5F")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "9F5F", "000000000000", "B_"));
                    }
                    // Application Selection Indicator (ASI)
                    if (!clessAIDDsc.dol.is_tlv_present("E001")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "E001", "01", "B_"));
                    }
                    // Terminal Action Code - Default
                    if (!clessAIDDsc.dol.is_tlv_present("DF8120")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF8120", "FC50F8A8F0", "B_"));
                    }
                    // Terminal Action Code - Denial
                    if (!clessAIDDsc.dol.is_tlv_present("DF8121")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF8121", "1010580000", "B_"));
                    }
                    // Terminal Action Code - Online
                    if (!clessAIDDsc.dol.is_tlv_present("DF8122")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF8122", "FCF8E4B870", "B_"));
                    }
                    // Online message for EMV transaction
                    if (!clessAIDDsc.dol.is_tlv_present("DFDF05")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DFDF05", "9A829F369F1E9F109F5B9F339F35959F015F245A5F348A9F159F169F399F1A9F1C579F025F2A9F199F219C9F249F25", "B_"));
                    }
                    // Threshold Value for Biased Random Selection according to ICS value or is up to implementer
                    if (!clessAIDDsc.dol.is_tlv_present("DFDF10")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DFDF10", "000000000500", "B_"));
                    }
                    // Target Percentage to be Used for Biased Random Selection according to ICS value or is up to implementer
                    if (!clessAIDDsc.dol.is_tlv_present("DFDF11")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DFDF11", "00", "B_"));
                    }
                    // Maximum Target Percentage to be Used for Biased Random Selection according to ICS value or is up to implementer
                    if (!clessAIDDsc.dol.is_tlv_present("DFDF12")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DFDF12", "00", "B_"));
                    }
                    clessAIDDsc.type = EmvParamYTModel.TypeID.clINTERACKrnId.getVal();
                    clessAIDDsc.eltToUpdID = EmvParamYTModel.EltToUpdID.noId.getVal();
                    if (clINTERACalltagDol.dol.isEmpty()) {
                        // add terminal parameters as first loaded element
                        clINTERACalltagDol.clone_dol(clessAIDDsc.dol);
                        EmvParamYTModel.ClessEltDsc clessTermDsc =
                                new EmvParamYTModel.ClessEltDsc();
                        clessTermDsc.type = EmvParamYTModel.TypeID.clINTERACKrnId.getVal();
                        clessTermDsc.eltToUpdID = EmvParamYTModel.EltToUpdID.clTermId.getVal();
                        clessTermDsc.dol = clINTERACalltagDol;
                        // Merchant Category Code
                        if (!clessTermDsc.dol.is_tlv_present("9F15")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F15", "1234", "B_"));
                        }
                        // Terminal Country Code
                        if (!clessTermDsc.dol.is_tlv_present("9F1A")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F1A", "0124", "B_"));
                        }
                        // Terminal Identification
                        if (!clessTermDsc.dol.is_tlv_present("9F1C")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F1C", "3131323233333434", "B_"));
                        }
                        // Terminal Identification
                        if (!clessTermDsc.dol.is_tlv_present("9F1E")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F1E", "3132333435363738", "B_"));
                        }
                        // Terminal Capabilities (Follow EMV Book 4 annex A2 to fill 3 bytes) according to ICS value
                        if (!clessTermDsc.dol.is_tlv_present("9F33")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F33", "E05808", "B_"));
                        }
                        // Terminal Type (Follow EMV Book 4 annex A1 to fill 1 byte) according to ICS value
                        if (!clessTermDsc.dol.is_tlv_present("9F35")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F35", "22", "B_"));
                        }
                        // Additional Terminal Capabilities (Follow EMV Book4 annex A2) according to ICS value
                        if (!clessTermDsc.dol.is_tlv_present("9F40")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F40", "600000F000", "B_"));
                        }
                        // Merchant Name and Location
                        if (!clessTermDsc.dol.is_tlv_present("9F4E")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F4E", "ALCINEO - Test Merchant", "AN_"));
                        }
                        // Merchant Type Indicator (Follow Dual Interface Reader/Terminal Specification for Interac Flash and NFC Based Transactions annex B15) according to ICS value
                        if (!clessTermDsc.dol.is_tlv_present("9F58")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F58", "01", "B_"));
                        }
                        // Terminal Transaction Information (TTI) according to ICS value
                        if (!clessTermDsc.dol.is_tlv_present("9F59")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F59", "DC8700", "B_"));
                        }
                        // Terminal Transaction Type (TTT)
                        if (!clessTermDsc.dol.is_tlv_present("9F5A")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F5A", "00", "B_"));
                        }
                        // Terminal Contactless Receipt Required Limit
                        if (!clessTermDsc.dol.is_tlv_present("9F5D")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F5D", "000000005000", "B_"));
                        }
                        // Terminal Option Status
                        if (!clessTermDsc.dol.is_tlv_present("9F5E")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F5E", "E000", "B_"));
                        }
                        clessTermDsc.dol.add_tlv(new TLV(
                                "D000", String.format(Locale.US,
                                "%02d", nbInteracAid), "B_"));
                        // Online message for EMV transaction
                        if (!clessTermDsc.dol.is_tlv_present("DFDF02")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "DFDF02", "24575A5B82898A8E95999A9B9C5F255F345F2A9F019F029F039F069F079F0D9F0E9F0F9F109F169F199F219F249F259F1A9F1E9F269F279F339F349F369F379F399F409F459F4C9F4E9F5FDFDF00DFDF28DFDF29", "B_"));
                        }
                        // Tags participating in online response
                        if (!clessTermDsc.dol.is_tlv_present("DFDF06")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "DFDF06", "8A91", "B_"));
                        }
                        // Socket timeout for Online Processing (in ms)
                        if (!clessTermDsc.dol.is_tlv_present("DFDF14")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "DFDF14", "00000BB8", "B_"));
                        }
                        // Number of connection retries in Online Processing
                        if (!clessTermDsc.dol.is_tlv_present("DFDF15")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "DFDF15", "00000003", "B_"));
                        }
                        // Terminal Features, see AO-AN-Property according to ICS value
                        if (!clessTermDsc.dol.is_tlv_present("DFDF22")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "DFDF22", "0100", "B_"));
                        }
                        // Online Authorization server IP address
                        if (!clessTermDsc.dol.is_tlv_present("DFDF23")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "DFDF23", "3139322E3136382E322E31313900", "B_"));
                        }
                        // Online Authorization server port
                        if (!clessTermDsc.dol.is_tlv_present("DFDF24")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "DFDF24", "000005EA", "B_"));
                        }
                        // Terminal Supported languages
                        if (!clessTermDsc.dol.is_tlv_present("DFDF2D")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "DFDF2D", "656E", "B_"));
                        }
                        // Interac Retry Limit
                        if (!clessTermDsc.dol.is_tlv_present("DFDF30")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "DFDF30", "00", "B_"));
                        }
                        clessTermDsc.dol = model.filterDOLForAIDParam(
                                clessTermDsc.dol, model.clINTERACTermAIDTagList);
                        // not defined in JPMC param file
                        model.add_cless_elt(clessTermDsc);
                    }
                    clessAIDDsc.dol = model.filterDOLForAIDParam(
                            clessAIDDsc.dol, model.clINTERACAIDTagList);
                } else {
                    isAidEltIdFound = false;
                }
                if (isAidEltIdFound) {
                    model.add_cless_elt(clessAIDDsc);
                    model.setIsClInteracConfigured(true);
                    break;
                }
            }
        }
    }
}
