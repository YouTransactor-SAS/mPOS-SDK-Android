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
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

public class EmvParamFmt1CLVisa extends EmvParamFmt1{
    static void getEmvModelFromFmt1Input(
            JSONObject jsonD, EmvParamYTModel model) throws JSONException {
        model.setIsClVisaConfigured(true); // ensure TS are erased
        JSONObject clJson = jsonD.getJSONObject("contactless");
        JSONObject common = jsonD.getJSONObject("common");
        JSONObject clCommon = clJson.getJSONObject("common");
        // contact AID array
        JSONArray clAIDList = clJson.getJSONArray("aids");
        List<String> visaAppTokenL = new ArrayList<>();
        visaAppTokenL.add("VISA");
        visaAppTokenL.add("VISA ELECTRON");
        visaAppTokenL.add("VISA INTERLINK");
        visaAppTokenL.add("VISA US");
        int nbVisaAid = EmvParamFmt1.getNbAIDProfileForKernel(
                clAIDList, visaAppTokenL);
        EmvParamDOL clVISAalltagDol = new EmvParamDOL();
        model.setClParamID(EmvParamFmt1.getClParamID(common));
        for(int i = 0; i < clAIDList.length(); i++) {
            JSONObject curJsonAidLst = clAIDList.getJSONObject(i);
            EmvParamYTModel.ClessEltDsc clessAIDDsc =
                    new EmvParamYTModel.ClessEltDsc();
            String aidKrnlTok = curJsonAidLst.getString("appLabel");
            boolean isAidEltIdFound;
            for(String kernelTok : visaAppTokenL) {
                if (aidKrnlTok.contains(kernelTok)) {
                    isAidEltIdFound = true;
                    EmvParamDOL clCommonLst = EmvParamFmt1.get_tlv_dol(
                            clCommon,
                            EmvParamFmt1ToYT.get_cless_visa_tag_dict());
                    clessAIDDsc.dol = EmvParamFmt1.get_tlv_dol(
                            clAIDList.getJSONObject(i),
                            EmvParamFmt1ToYT.get_cless_visa_tag_dict());
                    // AID parameters repeat common parameters
                    clessAIDDsc.dol.dol.addAll(clCommonLst.dol);
                    // terminal risk management
                    clessAIDDsc.dol.dol.add((new TLV(
                            "DFDF22", "3A00", "B_")));
                    // temrinal cless transaction limit
                    if (!clessAIDDsc.dol.is_tlv_present("DF00")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF00", "000000000000", "B_"));
                    }
                    // Terminal floor limit
                    if (!clessAIDDsc.dol.is_tlv_present("9F1B")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "9F1B", "000000000000", "B_"));
                    }
                    clessAIDDsc.type = EmvParamYTModel.TypeID.clVISAKrnId.getVal();
                    clessAIDDsc.eltToUpdID = EmvParamYTModel.EltToUpdID.noId.getVal();
                    if (clVISAalltagDol.dol.isEmpty()) {
                        // add terminal parameters as first loaded element
                        clVISAalltagDol.clone_dol(clessAIDDsc.dol);
                        EmvParamYTModel.ClessEltDsc clessTermDsc =
                                new EmvParamYTModel.ClessEltDsc();
                        clessTermDsc.type = EmvParamYTModel.TypeID.clVISAKrnId.getVal();
                        clessTermDsc.eltToUpdID = EmvParamYTModel.EltToUpdID.clTermId.getVal();
                        clessTermDsc.dol = clVISAalltagDol;
                        // alcineo proprietary data
                        clessTermDsc.dol.add_tlv(new TLV(
                                "D000", String.format(Locale.US,
                                "%02d", nbVisaAid), "B_"));
                        // Number of supported Reader Limit Sets: always 0
                        clessTermDsc.dol.add_tlv(new TLV(
                                "D001", "00", "B_"));
                        // DFDF02: Online and clearing message: always empty
                        clessTermDsc.dol.add_tlv(new TLV(
                                "DFDF02", "50575A8295999A9C5F205F245F2A5F345F369F029F039F069F109F159F1A9F219F249F269F279F369F379F399F4E9F5D9F5B9F669F6E9F7CDFDF20DFDF23DFDF24DFDF27", "B_"));
                        // DFDF14: Socket Timeout for Online Processing: default set
                        clessTermDsc.dol.add_tlv(new TLV(
                                "DFDF14", "00003A98", "B_"));
                        // DFDF15: Number of connection retries in Online Processing: default set
                        clessTermDsc.dol.add_tlv(new TLV(
                                "DFDF15", "00000001", "B_"));
                        // DFDF16: Maximum length of issuer script
                        clessTermDsc.dol.add_tlv(new TLV(
                                "DFDF16", "00000080", "B_"));
                        // DFDF21: Reader features
                        clessTermDsc.dol.add_tlv(new TLV(
                                "DFDF21", "3800E0", "B_"));
                        // DFDF22: Default reader Risk Parameters Checking capabilities
                        clessTermDsc.dol.add_tlv(new TLV(
                                "DFDF22", "3A00", "B_"));
                        // DFDF23: Default reader Risk Parameters Checking capabilities
                        clessTermDsc.dol.add_tlv(new TLV(
                                "DFDF23", "05", "B_"));
                        clessTermDsc.dol = model.filterDOLForAIDParam(
                                clessTermDsc.dol, model.clVISATermAIDTagList);
                        // 9F4E: Merchant Name and Location
                        clessTermDsc.dol.add_tlv(new TLV(
                                "9F4E", "R&D", "AN_"));
                        clessTermDsc.dol = model.filterDOLForAIDParam(
                                clessTermDsc.dol, model.clVISATermAIDTagList);
                        // seemingly not applicable for VISA but to be confirmed
                        // Try to uncomment if needed:
                        // Terminal Action Code Default
//                        if (!clessTermDsc.dol.is_tlv_present("DF8120")) {
//                            clessTermDsc.dol.add_tlv(new TLV(
//                                    "DF8120", "0000000000", "B_"));
//                        }
//                        // Terminal Action Code denial
//                        if (!clessTermDsc.dol.is_tlv_present("DF8121")) {
//                            clessTermDsc.dol.add_tlv(new TLV(
//                                    "DF8121", "0000000000", "B_"));
//                        }
//                        // Terminal Action Code online
//                        if (!clessTermDsc.dol.is_tlv_present("DF8122")) {
//                            clessTermDsc.dol.add_tlv(new TLV(
//                                    "DF8122", "0000000000", "B_"));
//                        }
                        // terminal country code is mandatory
                        if (!clessTermDsc.dol.is_tlv_present("9F1A")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F1A", "0840", "B_"));
                        }
                        // terminal type is mandatory
                        if (!clessTermDsc.dol.is_tlv_present("9F35")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F35", "22", "B_"));
                        }
                        // additional terminal capabilities
                        if (!clessTermDsc.dol.is_tlv_present("9F40")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F40", "F000A0A000", "B_"));
                        }
                        // merchant category type
                        if (!clessTermDsc.dol.is_tlv_present("9F15")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F15", "0000", "B_"));
                        }
                        // not defined in JPMC param file
                        model.add_cless_elt(clessTermDsc);
                    }
                    clessAIDDsc.dol = model.filterDOLForAIDParam(
                            clessAIDDsc.dol, model.clVISAAIDTagList);
                } else {
                    isAidEltIdFound = false;
                }
                if (isAidEltIdFound) {
                    model.add_cless_elt(clessAIDDsc);
                    model.setIsClVisaConfigured(true);
                    break;
                }
            }
        }
    }
}
