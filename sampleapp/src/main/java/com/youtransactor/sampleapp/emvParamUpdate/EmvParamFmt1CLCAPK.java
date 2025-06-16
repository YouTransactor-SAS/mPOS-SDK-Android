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

public class EmvParamFmt1CLCAPK extends EmvParamFmt1{
    static void getEmvModelFromFmt1Input(
            JSONObject jsonD, EmvParamYTModel model) throws JSONException {
        model.setIsClCapkConfigured(false);

        // TODO

//
//        JSONObject clJson = jsonD.getJSONObject("contactless");
//        JSONObject common = jsonD.getJSONObject("common");
//        JSONObject clCommon = clJson.getJSONObject("common");
//        // contact AID array
//        JSONArray clAIDList = clJson.getJSONArray("aids");
//        List<String> capkAppTokenL = new ArrayList<>();
//        capkAppTokenL.add("CAPK");
//        int nbCapkAid = EmvParamFmt1.getNbAIDProfileForKernel(
//                clAIDList, capkAppTokenL);
//        EmvParamDOL clCAPKalltagDol = new EmvParamDOL();
//        model.setClParamID(EmvParamFmt1.getClParamID(common));
//        for(int i = 0; i < clAIDList.length(); i++) {
//            JSONObject curJsonAidLst = clAIDList.getJSONObject(i);
//            EmvParamYTModel.ClessEltDsc clessAIDDsc =
//                    new EmvParamYTModel.ClessEltDsc();
//            String aidKrnlTok = curJsonAidLst.getString("appLabel");
//            boolean isAidEltIdFound = true;
//            for(String kernelTok : capkAppTokenL) {
//                if (aidKrnlTok.contains(kernelTok)) {
//                    EmvParamDOL clCommonLst = null; /*= EmvParamFmt1.get_tlv_dol(
//                            clCommon,
//                            EmvParamFmt1ToYT.get_cless_capk_tag_dict());*/
////                    clessAIDDsc.dol = EmvParamFmt1.get_tlv_dol(
////                            clAIDList.getJSONObject(i),
////                            EmvParamFmt1ToYT.get_cless_capk_tag_dict());
//                    // AID parameters repeat common parameters
//                    clessAIDDsc.dol.dol.addAll(clCommonLst.dol);
//                    // terminal risk management
//                    clessAIDDsc.dol.dol.add((new TLV(
//                            "DFDF22", "0080", "B_")));
//                    // temrinal cless transaction limit
//                    if (!clessAIDDsc.dol.is_tlv_present("DF00")) {
//                        clessAIDDsc.dol.add_tlv(new TLV(
//                                // magic number to define
//                                "DF00", "000000020000", "B_"));
//                    }
//                    // seemingly not applicable for CAPK but to be confirmed
//                    // Try to uncomment if needed:
//                    // Terminal Action Code Default
//                    if (!clessAIDDsc.dol.is_tlv_present("DF8120")) {
//                        clessAIDDsc.dol.add_tlv(new TLV(
//                                "DF8120", "0000000000", "B_"));
//                    }
//                    // Terminal Action Code denial
//                    if (!clessAIDDsc.dol.is_tlv_present("DF8121")) {
//                        clessAIDDsc.dol.add_tlv(new TLV(
//                                "DF8121", "0000000000", "B_"));
//                    }
//                    // Terminal Action Code online
//                    if (!clessAIDDsc.dol.is_tlv_present("DF8122")) {
//                        clessAIDDsc.dol.add_tlv(new TLV(
//                                "DF8122", "0000000000", "B_"));
//                    }
//                    clessAIDDsc.type = EmvParamYTModel.TypeID.clCAPKKrnId.getVal();
//                    clessAIDDsc.eltToUpdID = EmvParamYTModel.EltToUpdID.noId.getVal();
//                    if (clCAPKalltagDol.dol.isEmpty()) {
//                        // add terminal parameters as first loaded element
//                        clCAPKalltagDol.clone_dol(clessAIDDsc.dol);
//                        EmvParamYTModel.ClessEltDsc clessTermDsc =
//                                new EmvParamYTModel.ClessEltDsc();
//                        clessTermDsc.type = EmvParamYTModel.TypeID.clCAPKKrnId.getVal();
//                        clessTermDsc.eltToUpdID = EmvParamYTModel.EltToUpdID.clTermId.getVal();
//                        clessTermDsc.dol = clCAPKalltagDol;
//                        // merchant category type
//                        if (!clessTermDsc.dol.is_tlv_present("9F15")) {
//                            clessTermDsc.dol.add_tlv(new TLV(
//                                    "9F15", "0000", "B_"));
//                        }
//                        // terminal country code is mandatory
//                        if (!clessTermDsc.dol.is_tlv_present("9F1A")) {
//                            clessTermDsc.dol.add_tlv(new TLV(
//                                    "9F1A", "0840", "B_"));
//                        }
//                        // Terminal Floor Limit
//                        if (!clessTermDsc.dol.is_tlv_present("9F1B")) {
//                            clessTermDsc.dol.add_tlv(new TLV(
//                                    "9F1B", "00001388", "B_"));
//                        }
//                        // Terminal Capabilities
//                        if (!clessTermDsc.dol.is_tlv_present("9F33")) {
//                            clessTermDsc.dol.add_tlv(new TLV(
//                                    "9F33", "E06808", "B_"));
//                        }
//                        // Terminal type
//                        if (!clessTermDsc.dol.is_tlv_present("9F35")) {
//                            clessTermDsc.dol.add_tlv(new TLV(
//                                    "9F35", "22", "B_"));
//                        }
//                        // Terminal type
//                        if (!clessTermDsc.dol.is_tlv_present("9F40")) {
//                            clessTermDsc.dol.add_tlv(new TLV(
//                                    "9F40", "22", "B_"));
//                        }
//                        // 9F4E: Merchant Name and Location
//                        if (!clessTermDsc.dol.is_tlv_present("9F4E")) {
//                            clessTermDsc.dol.add_tlv(new TLV(
//                                    "9F4E", "R&D", "AN_"));
//                        }
//                        // alcineo proprietary data
//                        if (!clessTermDsc.dol.is_tlv_present("D000")) {
//                            clessTermDsc.dol.add_tlv(new TLV(
//                                    "D000", String.format(Locale.US,
//                                    "%02d", nbCapkAid), "B_"));
//                        }
//                        // Online and clearing message
//                        clessTermDsc.dol.add_tlv(new TLV(
//                                "DFDF02", "4F50575A8284959A9C5F205F245F2A5F345F369F029F039F089F099F109F199F1A9F1E9F1F9F219F249F269F279F339F349F359F369F379F399F419F6E9F7C", "B_"));
//                        // Clearing Record for Declined Transaction
//                        clessTermDsc.dol.add_tlv(new TLV(
//                                "DFDF04", "4F50575A8284959A9C5F205F245F2A5F345F369F029F039F089F099F109F199F1A9F1E9F1F9F219F249F269F279F339F349F359F369F379F399F419F6E9F7C", "B_"));
//                        // DFDF16: Maximum length of issuer script
//                        clessTermDsc.dol.add_tlv(new TLV(
//                                "DFDF16", "00000080", "B_"));
//                        // DFDF22: Default reader Risk Parameters Checking capabilities
//                        clessTermDsc.dol.add_tlv(new TLV(
//                                "DFDF22", "0080", "B_"));
//                        clessTermDsc.dol = model.filterDOLForAIDParam(
//                                clessTermDsc.dol, model.clCAPKTermAIDTagList);
//                        // not defined in JPMC param file
//                        model.add_cless_elt(clessTermDsc);
//                    }
//                    clessAIDDsc.dol = model.filterDOLForAIDParam(
//                            clessAIDDsc.dol, model.clCAPKAIDTagList);
//                } else {
//                    isAidEltIdFound = false;
//                }
//                if (isAidEltIdFound) {
//                    model.add_cless_elt(clessAIDDsc);
//                    model.setIsCapkConfigured(true);
//                    break;
//                }
//            }
//        }
    }
}
