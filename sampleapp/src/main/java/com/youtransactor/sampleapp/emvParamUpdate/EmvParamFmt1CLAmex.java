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

public class EmvParamFmt1CLAmex extends EmvParamFmt1{
    static void getEmvModelFromFmt1Input(
            JSONObject jsonD, EmvParamYTModel model) throws JSONException {
        model.setIsClAmexConfigured(true);
        JSONObject clJson = jsonD.getJSONObject("contactless");
        JSONObject common = jsonD.getJSONObject("common");
        JSONObject clCommon = clJson.getJSONObject("common");
        // contact AID array
        JSONArray clAIDList = clJson.getJSONArray("aids");
        List<String> amexAppTokenL = new ArrayList<>();
        amexAppTokenL.add("AMERICAN EXPRESS");
        amexAppTokenL.add("AMEX US DEBIT");
        int nbAmexAid = EmvParamFmt1.getNbAIDProfileForKernel(
                clAIDList, amexAppTokenL);
        EmvParamDOL clAMEXalltagDol = new EmvParamDOL();
        model.setClParamID(EmvParamFmt1.getClParamID(common));
        for(int i = 0; i < clAIDList.length(); i++) {
            JSONObject curJsonAidLst = clAIDList.getJSONObject(i);
            EmvParamYTModel.ClessEltDsc clessAIDDsc =
                    new EmvParamYTModel.ClessEltDsc();
            String aidKrnlTok = curJsonAidLst.getString("appLabel");
            boolean isAidEltIdFound;
            for(String kernelTok : amexAppTokenL) {
                isAidEltIdFound = false;
                if (aidKrnlTok.contains(kernelTok)) {
                    isAidEltIdFound = true;
                    EmvParamDOL clCommonLst = EmvParamFmt1.get_tlv_dol(
                            clCommon,
                            EmvParamFmt1ToYT.get_cless_amex_tag_dict());
                    clessAIDDsc.dol = EmvParamFmt1.get_tlv_dol(
                            clAIDList.getJSONObject(i),
                            EmvParamFmt1ToYT.get_cless_amex_tag_dict());
                    // AID parameters repeat common parameters
                    clessAIDDsc.dol.dol.addAll(clCommonLst.dol);
                    // Acquirer Identifier
                    if (!clessAIDDsc.dol.is_tlv_present("9F01")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "9F01", "000000000001", "B_"));
                    }
                    // Terminal Application Identifier (AID)
                    if (!clessAIDDsc.dol.is_tlv_present("9F06")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "9F06", "A00000002501", "B_"));
                    }
                    // Terminal Application Version Number
                    if (!clessAIDDsc.dol.is_tlv_present("9F09")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "9F09", "0001", "B_"));
                    }
                    // Terminal Floor Limit
                    if (!clessAIDDsc.dol.is_tlv_present("9F1B")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "9F1B", "00002710", "B_"));
                    }
                    // Terminal Contactless Transaction Limit
                    if (!clessAIDDsc.dol.is_tlv_present("DF00")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF00", "000000015000", "B_"));
                    }
                    // Terminal CVM Required Limit
                    if (!clessAIDDsc.dol.is_tlv_present("DF01")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF01", "000000005000", "B_"));
                    }
                    // Terminal Contactless Floor Limit
                    if (!clessAIDDsc.dol.is_tlv_present("DF02")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF02", "000000000000", "B_"));
                    }
                    // Application Selection Indicator (ASI)
                    if (!clessAIDDsc.dol.is_tlv_present("E001")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "E001", "01", "B_"));
                    }
                    // Terminal Action Code - Default
                    if (!clessAIDDsc.dol.is_tlv_present("DF8120")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF8120", "DC50FC9800", "B_"));
                    }
                    // Terminal Action Code - Denial
                    if (!clessAIDDsc.dol.is_tlv_present("DF8121")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF8121", "0010000000", "B_"));
                    }
                    // Terminal Action Code - Online
                    if (!clessAIDDsc.dol.is_tlv_present("DF8122")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF8122", "DE00FC9800", "B_"));
                    }
                    // Default TDOL
                    if (!clessAIDDsc.dol.is_tlv_present("DFDF19")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DFDF19", "5F2403", "B_"));
                    }
                    // Application Version Number Supported
                    if (!clessAIDDsc.dol.is_tlv_present("DFDF2F")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DFDF2F", "0001", "B_"));
                    }
                    clessAIDDsc.type = EmvParamYTModel.TypeID.clAMEXKrnId.getVal();
                    clessAIDDsc.eltToUpdID = EmvParamYTModel.EltToUpdID.noId.getVal();
                    if (clAMEXalltagDol.dol.isEmpty()) {
                        // add terminal parameters as first loaded element
                        clAMEXalltagDol.clone_dol(clessAIDDsc.dol);
                        EmvParamYTModel.ClessEltDsc clessTermDsc =
                                new EmvParamYTModel.ClessEltDsc();
                        clessTermDsc.type = EmvParamYTModel.TypeID.clAMEXKrnId.getVal();
                        clessTermDsc.eltToUpdID = EmvParamYTModel.EltToUpdID.clTermId.getVal();
                        clessTermDsc.dol = clAMEXalltagDol;
                        // Merchant Category Code
                        if (!clessTermDsc.dol.is_tlv_present("9F15")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F15", "4112", "B_"));
                        }
                        // Merchant Identifier
                        if (!clessTermDsc.dol.is_tlv_present("9F16")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F16", "R&D", "AN_"));
                        }
                        // Terminal Country Code
                        if (!clessTermDsc.dol.is_tlv_present("9F1A")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F1A", "0620", "B_"));
                        }
                        // Interface Device (IFD) Serial Number
                        if (!clessTermDsc.dol.is_tlv_present("9F1E")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F1E", "123456", "AN_"));
                        }
                        // Terminal Capabilities (Follow EMV Book 4 annex A2 and AMEX Guidelines) according to ICS value
                        if (!clessTermDsc.dol.is_tlv_present("9F33")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F33", "E06888", "B_"));
                        }
                        // Terminal Type (Follow EMV Book 4 annex A1 and Expresspay Terminal Technical Specification Table 21.6) according to ICS value
                        if (!clessTermDsc.dol.is_tlv_present("9F35")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F35", "22", "B_"));
                        }
                        // Additional Terminal Capabilities (Follow EMV Book4 Annex A2 and AMEX Guidelines) according to ICS value
                        if (!clessTermDsc.dol.is_tlv_present("9F40")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F40", "7000A0A000", "B_"));
                        }
                        // Merchant Name and Location
                        if (!clessTermDsc.dol.is_tlv_present("9F4E")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F4E", "Railway Operator", "AN_"));
                        }
                        // Expresspay Terminal Capabilities (Follow Expresspay Terminal Technical Specification Table 21.7 and AMEX Guidelines)
                        if (!clessTermDsc.dol.is_tlv_present("9F6D")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F6D", "C0", "B_"));
                        }
                        // Terminal Transaction Capabilities (Follow Expresspay Terminal Technical Specification Table 21.9 and AMEX Guidelines)
                        if (!clessTermDsc.dol.is_tlv_present("9F6E")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F6E", "9CE00003", "B_"));
                        }
                        clessTermDsc.dol.add_tlv(new TLV(
                                "D000", String.format(Locale.US,
                                "%02d", nbAmexAid), "B_"));
                        // Hold Time value (in unit of 100ms) - after completed transaction
                        if (!clessTermDsc.dol.is_tlv_present("DF8130")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "DF8130", "0F", "B_"));
                        }
                        // Online and clearing message for EMV Transaction
                        if (!clessTermDsc.dol.is_tlv_present("DFDF02")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "DFDF02", "50575A828487898A8C8E9495999A9B9C5F205F245F255F285F2A5F2D5F349F019F029F039F069F079F089F099F0A9F0B9F0D9F0E9F0F9F109F119F129F159F169F199F1A9F1C9F1E9F219F249F259F269F279F339F349F359F369F379F389F399F429F4A9F4C9F4E9F5A9F5B9F6D9F6E9F70DFDF17", "B_"));
                        }
                        // Tags participating in online response
                        if (!clessTermDsc.dol.is_tlv_present("DFDF06")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "DFDF06", "8A91", "B_"));
                        }
                        // Socket timeout for online processing (in ms)
                        if (!clessTermDsc.dol.is_tlv_present("DFDF14")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "DFDF14", "00003A98", "B_"));
                        }
                        // Number of connection retries in online processing
                        if (!clessTermDsc.dol.is_tlv_present("DFDF15")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "DFDF15", "00000003", "B_"));
                        }
                        // Maximum length of issuer script
                        if (!clessTermDsc.dol.is_tlv_present("DFDF16")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "DFDF16", "00000080", "B_"));
                        }
                        // Terminal Features, see in AO-AN-Propery
                        if (!clessTermDsc.dol.is_tlv_present("DFDF22")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "DFDF22", "1018", "B_"));
                        }
                        clessTermDsc.dol = model.filterDOLForAIDParam(
                                clessTermDsc.dol, model.clAMEXTermAIDTagList);
                        // not defined in JPMC param file
                        model.add_cless_elt(clessTermDsc);
                    }
                    clessAIDDsc.dol = model.filterDOLForAIDParam(
                            clessAIDDsc.dol, model.clAMEXAIDTagList);
                } else {
                    isAidEltIdFound = false;
                }
                if (isAidEltIdFound) {
                    model.add_cless_elt(clessAIDDsc);
                    model.setIsClAmexConfigured(true);
                    break;
                }
            }
        }
    }
}
