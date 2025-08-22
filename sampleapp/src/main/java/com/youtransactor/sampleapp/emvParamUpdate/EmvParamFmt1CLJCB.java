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

public class EmvParamFmt1CLJCB extends EmvParamFmt1{
    static void getEmvModelFromFmt1Input(
            JSONObject jsonD, EmvParamYTModel model) throws JSONException {
        model.setIsClJcbConfigured(true);
        JSONObject clJson = jsonD.getJSONObject("contactless");
        JSONObject common = jsonD.getJSONObject("common");
        JSONObject clCommon = clJson.getJSONObject("common");
        // contact AID array
        JSONArray clAIDList = clJson.getJSONArray("aids");
        List<String> jcbAppTokenL = new ArrayList<>();
        jcbAppTokenL.add("JCB");
        int nbJcbAid = EmvParamFmt1.getNbAIDProfileForKernel(
                clAIDList, jcbAppTokenL);
        EmvParamDOL clJCBalltagDol = new EmvParamDOL();
        model.setClParamID(EmvParamFmt1.getClParamID(common));
        for(int i = 0; i < clAIDList.length(); i++) {
            JSONObject curJsonAidLst = clAIDList.getJSONObject(i);
            EmvParamYTModel.ClessEltDsc clessAIDDsc =
                    new EmvParamYTModel.ClessEltDsc();
            String aidKrnlTok = curJsonAidLst.getString("appLabel");
            boolean isAidEltIdFound;
            for(String kernelTok : jcbAppTokenL) {
                if (aidKrnlTok.contains(kernelTok)) {
                    isAidEltIdFound = true;
                    EmvParamDOL clCommonLst = EmvParamFmt1.get_tlv_dol(
                            clCommon,
                            EmvParamFmt1ToYT.get_cless_jcb_tag_dict());
                    clessAIDDsc.dol = EmvParamFmt1.get_tlv_dol(
                            clAIDList.getJSONObject(i),
                            EmvParamFmt1ToYT.get_cless_jcb_tag_dict());
                    // AID parameters repeat common parameters
                    clessAIDDsc.dol.dol.addAll(clCommonLst.dol);

                    // Transaction Type
                    if (!clessAIDDsc.dol.is_tlv_present("9C")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "9C", "00", "B_"));
                    }
                    // Acquirer Identifier
                    if (!clessAIDDsc.dol.is_tlv_present("9F01")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "9F01", "000000000010", "B_"));
                    }
                    // Terminal Application Identifier (AID)
                    if (!clessAIDDsc.dol.is_tlv_present("9F06")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "9F06", "A0000000651010", "B_"));
                    }
                    // Terminal Contactless Transaction Limit
                    if (!clessAIDDsc.dol.is_tlv_present("DF00")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF00", "000000020000", "B_"));
                    }
                    // Terminal CVM Required Limit
                    if (!clessAIDDsc.dol.is_tlv_present("DF01")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF01", "000000010000", "B_"));
                    }
                    // On-Device CVM Contactless Transaction Limit
                    if (!clessAIDDsc.dol.is_tlv_present("DF03")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF03", "000000025000", "B_"));
                    }
                    // Kernel ID
                    if (!clessAIDDsc.dol.is_tlv_present("DF810C")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF810C", "05", "B_"));
                    }
                    // Terminal Action Code - Default
                    if (!clessAIDDsc.dol.is_tlv_present("DF8120")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF8120", "9040008000", "B_"));
                    }
                    // Terminal Action Code - Denial
                    if (!clessAIDDsc.dol.is_tlv_present("DF8121")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF8121", "0410000000", "B_"));
                    }
                    // Terminal Action Code - Online
                    if (!clessAIDDsc.dol.is_tlv_present("DF8122")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF8122", "9060009000", "B_"));
                    }
                    // Threshold Value for Biased Random Selection
                    if (!clessAIDDsc.dol.is_tlv_present("DFDF10")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DFDF10", "000000002000", "B_"));
                    }
                    // Target Percentage to be Used for Biased Random Selection
                    if (!clessAIDDsc.dol.is_tlv_present("DFDF11")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DFDF11", "00", "B_"));
                    }
                    // Maximum Target Percentage to be Used for Biased Random Selection
                    if (!clessAIDDsc.dol.is_tlv_present("DFDF12")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DFDF12", "00", "B_"));
                    }
                    // Combination Options, see AO-AN-Property and JCB_Contactless_Terminal_Spec annex A3
                    if (!clessAIDDsc.dol.is_tlv_present("DFDF44")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DFDF44", "7B00", "B_"));
                    }
                    // Terminal Interchange Profile (Follow AO-AN-Property and JCB_Contactless_Terminal_Spec annex A7) according to ICS value
                    if (!clessAIDDsc.dol.is_tlv_present("DFDF45")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DFDF45", "708000", "B_"));
                    }
                    // Removal timeout
                    if (!clessAIDDsc.dol.is_tlv_present("DFDF46")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DFDF46", "1000", "B_"));
                    }
                    clessAIDDsc.type = EmvParamYTModel.TypeID.clJCBKrnId.getVal();
                    clessAIDDsc.eltToUpdID = EmvParamYTModel.EltToUpdID.noId.getVal();
                    if (clJCBalltagDol.dol.isEmpty()) {
                        // add terminal parameters as first loaded element
                        clJCBalltagDol.clone_dol(clessAIDDsc.dol);
                        EmvParamYTModel.ClessEltDsc clessTermDsc =
                                new EmvParamYTModel.ClessEltDsc();
                        clessTermDsc.type = EmvParamYTModel.TypeID.clJCBKrnId.getVal();
                        clessTermDsc.eltToUpdID = EmvParamYTModel.EltToUpdID.clTermId.getVal();
                        clessTermDsc.dol = clJCBalltagDol;
                        // Merchant Category Code
                        if (!clessTermDsc.dol.is_tlv_present("9F15")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F15", "7032", "B_"));
                        }
                        // Terminal Country Code
                        if (!clessTermDsc.dol.is_tlv_present("9F1A")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F1A", "0392", "B_"));
                        }
                        // Terminal Floor Limit
                        if (!clessTermDsc.dol.is_tlv_present("DF02")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F1B", "00000000", "B_"));
                        }
                        // Terminal Capabilities (Follow EMV Book 4 annex A2 to fill 3 bytes) according to ICS value
                        if (!clessTermDsc.dol.is_tlv_present("9F33")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F33", "E06808", "B_"));
                        }
                        // Terminal Type (Follow EMV Book 4 annex A1 to fill 1 byte) according to ICS value
                        if (!clessTermDsc.dol.is_tlv_present("9F35")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F35", "22", "B_"));
                        }
                        // Additional Terminal Capabilities (Follow EMV Book4 Annex A2) according to ICS value
                        if (!clessTermDsc.dol.is_tlv_present("9F40")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F40", "F000A0F000", "B_"));
                        }
                        // Merchant Name and Location
                        if (!clessTermDsc.dol.is_tlv_present("9F4E")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F4E", "XX MERCHANT YY LOCATION", "AN_"));
                        }
                        // Clearing Record for Approved/online Legacy/EMV
                        if (!clessTermDsc.dol.is_tlv_present("DFDF02")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "DFDF02", "4F50575A8284959A9C5F205F245F2A5F345F369F029F039F089F099F109F199F1A9F1E9F1F9F219F249F269F279F339F349F359F369F379F399F419F6E9F7C", "B_"));
                        }
                        // Clearing Record for Declined Transaction
                        if (!clessTermDsc.dol.is_tlv_present("DFDF04")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "DFDF04", "4F50575A8284959A9C5F205F245F2A5F345F369F029F039F089F099F109F199F1A9F1E9F1F9F219F249F269F279F339F349F359F369F379F399F419F6E9F7C", "B_"));
                        }
                        // Maximum length of issuer script
                        if (!clessTermDsc.dol.is_tlv_present("DFDF16")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "DFDF16", "00000080", "B_"));
                        }
                        // Terminal Features, see AO-AN-Property according to ICS value
                        if (!clessTermDsc.dol.is_tlv_present("DFDF22")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "DFDF22", "0080", "B_"));
                        }

                        // Number of supported AIDs
                        clessTermDsc.dol.add_tlv(new TLV(
                                "D000", String.format(Locale.US,
                                "%02d", nbJcbAid), "B_"));
                        clessTermDsc.dol = model.filterDOLForAIDParam(
                                clessTermDsc.dol, model.clJCBTermAIDTagList);
                        // not defined in JPMC param file
                        model.add_cless_elt(clessTermDsc);
                    }
                    clessAIDDsc.dol = model.filterDOLForAIDParam(
                            clessAIDDsc.dol, model.clJCBAIDTagList);
                } else {
                    isAidEltIdFound = false;
                }
                if (isAidEltIdFound) {
                    model.add_cless_elt(clessAIDDsc);
                    model.setIsClJcbConfigured(true);
                    break;
                }
            }
        }
    }
}
