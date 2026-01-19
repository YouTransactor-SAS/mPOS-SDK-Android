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

public class EmvParamFmt1CLDisc extends EmvParamFmt1{
    static void getEmvModelFromFmt1Input(
            JSONObject jsonD, EmvParamYTModel model) throws JSONException {
        model.setIsClDiscConfigured(true);
        JSONObject clJson = jsonD.getJSONObject("contactless");
        JSONObject common = jsonD.getJSONObject("common");
        JSONObject clCommon = clJson.getJSONObject("common");
        // contact AID array
        JSONArray clAIDList = clJson.getJSONArray("aids");
        List<String> discAppTokenL = new ArrayList<>();
        discAppTokenL.add("DISCOVER");
        discAppTokenL.add("DISCOVER DEBIT");
        int nbDiscAid = EmvParamFmt1.getNbAIDProfileForKernel(
                clAIDList, discAppTokenL);
        EmvParamDOL clDISCalltagDol = new EmvParamDOL();
        setClInfoInModel(model, common);
        for(int i = 0; i < clAIDList.length(); i++) {
            JSONObject curJsonAidLst = clAIDList.getJSONObject(i);
            EmvParamYTModel.ClessEltDsc clessAIDDsc =
                    new EmvParamYTModel.ClessEltDsc();
            String aidKrnlTok = curJsonAidLst.getString("appLabel");
            boolean isAidEltIdFound;
            for(String kernelTok : discAppTokenL) {
                if (aidKrnlTok.contains(kernelTok)) {
                    isAidEltIdFound = true;
                    EmvParamDOL clCommonLst = EmvParamFmt1.get_tlv_dol(
                            clCommon,
                            EmvParamFmt1ToYT.get_cless_disc_tag_dict());
                    EmvParamDOL commonLst = EmvParamFmt1.get_tlv_dol(
                            common,
                            EmvParamFmt1ToYT.get_cless_disc_tag_dict());
                    clessAIDDsc.dol = EmvParamFmt1.get_tlv_dol(
                            clAIDList.getJSONObject(i),
                            EmvParamFmt1ToYT.get_cless_disc_tag_dict());
                    // AID parameters repeat common parameters
                    clessAIDDsc.dol.dol.addAll(clCommonLst.dol);
                    clessAIDDsc.dol.dol.addAll(commonLst.dol);

                    // Transaction Type
                    if (!clessAIDDsc.dol.is_tlv_present("9C")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "9C", "00", "B_"));
                    }
                    // Acquirer Identifier
                    if (!clessAIDDsc.dol.is_tlv_present("9F01")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "9F01", "000000000001", "B_"));
                    }
                    // Terminal Application Identifier (AID)
                    if (!clessAIDDsc.dol.is_tlv_present("9F06")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "9F06", "A0000001523010", "B_"));
                    }
                    // Terminal Application Version Number
                    if (!clessAIDDsc.dol.is_tlv_present("9F09")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "9F09", "0001", "B_"));
                    }
                    // Terminal floor limit
                    if (!clessAIDDsc.dol.is_tlv_present("9F1B")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "9F1B", "000000000000", "B_"));
                    }
                    // Terminal Transaction Qualifier (TTQ) (Follow D-PAS Connect Terminal Application Contactless Specification Table 54)
                    if (!clessAIDDsc.dol.is_tlv_present("9F66")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "9F66", "3700C000", "B_"));
                    }
                    // Terminal Contactless Transaction Limit
                    if (!clessAIDDsc.dol.is_tlv_present("DF00")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF00", "000000030000", "B_"));
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
                    // Kernel ID
                    if (!clessAIDDsc.dol.is_tlv_present("DF810C")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF810C", "06", "B_"));
                    }
                    // Terminal Action Code - Default
                    if (!clessAIDDsc.dol.is_tlv_present("DF8120")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF8120", "0000000000", "B_"));
                    }
                    // Terminal Action Code - Denial
                    if (!clessAIDDsc.dol.is_tlv_present("DF8121")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF8121", "0000000000", "B_"));
                    }
                    // Terminal Action Code - Online
                    if (!clessAIDDsc.dol.is_tlv_present("DF8122")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF8122", "0000000000", "B_"));
                    }
                    // Terminal Features, see in AO-AN-Property
                    if (!clessAIDDsc.dol.is_tlv_present("DFDF22")) {
                        // Activate extended selection for B2 card processing
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DFDF22", /*"C0DC"*/ "C0CC", "B_"));
                    }
                    clessAIDDsc.type = EmvParamYTModel.TypeID.clDISCKrnId.getVal();
                    clessAIDDsc.eltToUpdID = EmvParamYTModel.EltToUpdID.noId.getVal();
                    if (clDISCalltagDol.dol.isEmpty()) {
                        // add terminal parameters as first loaded element
                        clDISCalltagDol.clone_dol(clessAIDDsc.dol);
                        EmvParamYTModel.ClessEltDsc clessTermDsc =
                                new EmvParamYTModel.ClessEltDsc();
                        clessTermDsc.type = EmvParamYTModel.TypeID.clDISCKrnId.getVal();
                        clessTermDsc.eltToUpdID = EmvParamYTModel.EltToUpdID.clTermId.getVal();
                        clessTermDsc.dol = clDISCalltagDol;
                        // Transaction Currency Exponent
                        if (!clessTermDsc.dol.is_tlv_present("5F36")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "5F36", "02", "B_"));
                        }
                        // Merchant Category Code
                        if (!clessTermDsc.dol.is_tlv_present("9F15")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F15", "0001", "B_"));
                        }
                        // Merchant Identifier
                        if (!clessTermDsc.dol.is_tlv_present("9F16")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F16", "R&D", "AN_"));
                        }
                        // Terminal Country Code
                        if (!clessTermDsc.dol.is_tlv_present("9F1A")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F1A", "0840", "B_"));
                        }
                        // Terminal Floor Limit
                        if (!clessTermDsc.dol.is_tlv_present("9F1B")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F1B", "00003A98", "B_"));
                        }
                        // Terminal Identification
                        if (!clessTermDsc.dol.is_tlv_present("9F1C")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F1C", "3131323233333434", "B_"));
                        }
                        // Interface Device (IFD) Serial Number
                        if (!clessTermDsc.dol.is_tlv_present("9F1E")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F1E", "3132333435363738", "B_"));
                        }
                        // Terminal Capabilities (Follow EMV Book 4 annex A2 and D-PAS Connect Terminal Application Contactless Specification Table 51) according to ICS value
                        if (!clessTermDsc.dol.is_tlv_present("9F33")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F33", "E06808", "B_"));
                        }
                        // Terminal Type (Follow EMV Book 4 annex A1 and D-PAS Connect Terminal Application Contactless Specification Table 55) according to ICS value
                        if (!clessTermDsc.dol.is_tlv_present("9F35")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F35", "22", "B_"));
                        }
                        // Additional Terminal Capabilities (Follow EMV Book4 Annex A2)
                        if (!clessTermDsc.dol.is_tlv_present("9F40")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F40", "F000A0A000", "B_"));
                        }
                        // Merchant Name and Location
                        if (!clessTermDsc.dol.is_tlv_present("9F4E")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F4E", "Test Merchant", "AN_"));
                        }
                        // Terminal Transaction Qualifier (TTQ) (Follow D-PAS Connect Terminal Application Contactless Specification Table 54)
                        if (!clessTermDsc.dol.is_tlv_present("9F66")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F66", "3700C000", "B_"));
                        }
                        clessTermDsc.dol.add_tlv(new TLV(
                                "D000", String.format(Locale.US,
                                "%02d", nbDiscAid), "B_"));
                        // Loyalty Program ID (optional) 1 byte hexadecimal format with values 6X, 7X and 5X where X=A to F
                        if (!clessTermDsc.dol.is_tlv_present("DF70")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "DF70", "00", "B_"));
                        }
                        // Online and clearing message EMV
                        if (!clessTermDsc.dol.is_tlv_present("DFDF02")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "DFDF02", "575A828E898A95999A9B9C5F245F255F285F345F365F2A9F019F029F039F079F0D9F0E9F0F9F109F169F159F1A9F1C9F1E9F219F269F279F339F349F359F369F379F399F4E9F5BDFDF45", "B_"));
                        }
                        // Tags participating in online response
                        if (!clessTermDsc.dol.is_tlv_present("DFDF06")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "DFDF06", "8A91", "B_"));
                        }
                        // Socket timeout for online processing (in ms)
                        if (!clessTermDsc.dol.is_tlv_present("DFDF14")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "DFDF14", "00002710", "B_"));
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
                        // Terminal Features, see in AO-AN-Property
                        if (!clessTermDsc.dol.is_tlv_present("DFDF22")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "DFDF22", "C0CC", "B_"));
                        }
                        clessTermDsc.dol = model.filterDOLForAIDParam(
                                clessTermDsc.dol, model.clDISCTermAIDTagList);
                        // not defined in JPMC param file
                        model.add_cless_elt(clessTermDsc);
                    }
                    clessAIDDsc.dol = model.filterDOLForAIDParam(
                            clessAIDDsc.dol, model.clDISCAIDTagList);
                } else {
                    isAidEltIdFound = false;
                }
                if (isAidEltIdFound) {
                    model.add_cless_elt(clessAIDDsc);
                    model.setIsClDiscConfigured(true);
                    break;
                }
            }
        }
        EmvParamFmt1.closeLoad(model.isClDiscConfigured(),
                EmvParamYTModel.TypeID.clDISCKrnCloseId,
                model);
    }
}
