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

public class EmvParamFmt1CLMCL extends EmvParamFmt1{
    static void getEmvModelFromFmt1Input(
            JSONObject jsonD, EmvParamYTModel model) throws JSONException {
        boolean isAidEltIdFound;
        JSONObject clJson = jsonD.getJSONObject("contactless");
        JSONObject clCommon = clJson.getJSONObject("common");
        JSONObject common = jsonD.getJSONObject("common");
        // contact AID array
        JSONArray clAIDList = clJson.getJSONArray("aids");
        List<String> mclAppTokenL = new ArrayList<>();
        mclAppTokenL.add("MASTERCARD");
        mclAppTokenL.add("MAESTRO");
        int nbMclAid = EmvParamFmt1.getNbAIDProfileForKernel(
                clAIDList, mclAppTokenL);
        EmvParamDOL clMCLalltagDol = new EmvParamDOL();
        model.setClParamID(EmvParamFmt1.getClParamID(common));
        for(int i = 0; i < clAIDList.length(); i++) {
            JSONObject curJsonAidLst = clAIDList.getJSONObject(i);
            EmvParamYTModel.ClessEltDsc clessAIDDsc =
                    new EmvParamYTModel.ClessEltDsc();
            String aidKrnlTok = curJsonAidLst.getString("appLabel");
            for(String kernelTok : mclAppTokenL) {
                if (aidKrnlTok.contains(kernelTok)) {
                    EmvParamDOL clCommonLst = EmvParamFmt1.get_tlv_dol(
                            clCommon,
                            EmvParamFmt1ToYT.get_cless_mcl_tag_dict());
                    clessAIDDsc.dol = EmvParamFmt1.get_tlv_dol(
                            clAIDList.getJSONObject(i),
                            EmvParamFmt1ToYT.get_cless_mcl_tag_dict());
                    // AID parameters repeat common parameters
                    clessAIDDsc.dol.dol.addAll(clCommonLst.dol);
                    clessAIDDsc.type = EmvParamYTModel.TypeID.clMCLKrnId.getVal();
                    clessAIDDsc.eltToUpdID = EmvParamYTModel.EltToUpdID.noId.getVal();
                    if (clMCLalltagDol.dol.isEmpty()) {
                        // add terminal parameters as first loaded element
                        clMCLalltagDol.clone_dol(clessAIDDsc.dol);
                        EmvParamYTModel.ClessEltDsc clessTermDsc =
                                new EmvParamYTModel.ClessEltDsc();
                        clessTermDsc.type = EmvParamYTModel.TypeID.clMCLKrnId.getVal();
                        clessTermDsc.eltToUpdID = EmvParamYTModel.EltToUpdID.clTermId.getVal();
                        clessTermDsc.dol = clMCLalltagDol;
                        // alcineo proprietary data
                        clessTermDsc.dol.add_tlv(new TLV(
                                "D000", String.format(Locale.US,
                                "%02d", nbMclAid), "B_"));
                        // DFDF02: Online and clearing message: always empty
                        clessTermDsc.dol.add_tlv(new TLV(
                                "DFDF02", "", "B_"));
                        // DFDF14: Socket Timeout for Online Processing: default set
                        clessTermDsc.dol.add_tlv(new TLV(
                                "DFDF14", "00003A98", "B_"));
                        // DFDF15: Number of connection retries in Online Processing: default set
                        clessTermDsc.dol.add_tlv(new TLV(
                                "DFDF15", "00000001", "B_"));
                        // terminal country code is mandatory
                        if (!clessTermDsc.dol.is_tlv_present("9F1A")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "9F1A", "0840", "B_"));
                        }
                        // Card data input capability
                        if (!clessTermDsc.dol.is_tlv_present("DF8117")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "DF8117", "20", "B_"));
                        }
                        // default UDOL
                        if (!clessTermDsc.dol.is_tlv_present("DF811A")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "DF811A", "9F6A04", "B_"));
                        }
                        // List of proprietary tags that can be updated via R-APDU
                        if (!clessTermDsc.dol.is_tlv_present("DFDF45")) {
                            clessTermDsc.dol.add_tlv(new TLV(
                                    "DFDF45", "5056575A5F245F255F285F305F345F2D5F536F7077808284878C8E8F9092949F059F079F089F0D9F0E9F0F9F109F119F129F179F1F9F209F249F269F279F329F369F389F3B9F429F449F469F479F489F4A9F4B9F4C9F4D9F509F519F549F5B9F5D9F5E9F5F9F609F619F629F639F649F659F669F679F699F6B9F6E9F6F9F709F719F729F739F749F759F769F779F789F799F7D9F7FA5BF0CDF4BDF8101DF8102DF8302DF8303DF8304DF83059F199F25", "B_"));
                        }
                        clessTermDsc.dol = model.filterDOLForAIDParam(
                                clessTermDsc.dol, model.clMCLTermAIDTagList);
                        // not defined in JPMC param file
                        model.add_cless_elt(clessTermDsc);
                    }

                    // TACs
                    if (!clessAIDDsc.dol.is_tlv_present("DF8120")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF8120", "0000000000", "B_"));
                    }
                    if (!clessAIDDsc.dol.is_tlv_present("DF8121")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF8121", "0000000000", "B_"));
                    }
                    if (!clessAIDDsc.dol.is_tlv_present("DF8122")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF8122", "0000000000", "B_"));
                    }
                    // terminal type
                    if (!clessAIDDsc.dol.is_tlv_present("9F35")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "9F35", "22", "B_"));
                    }
                    // additional term capabilities
                    if (!clessAIDDsc.dol.is_tlv_present("9F40")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "9F40", "6000803001", "B_"));
                    }
                    // mobile support indicator
                    if (!clessAIDDsc.dol.is_tlv_present("9F7E")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "9F7E", "01", "B_"));
                    }
                    // kernel id
                    if (!clessAIDDsc.dol.is_tlv_present("DF810C")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF810C", "02", "B_"));
                    }
                    // CVM Capability - CVM Required
                    if (!clessAIDDsc.dol.is_tlv_present("DF8118")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF8118", "00", "B_"));
                    }
                    // CVM Capability - No CVM Required
                    if (!clessAIDDsc.dol.is_tlv_present("DF8119")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF8119", "08", "B_"));
                    }
                    // Kernel Configuration
                    if (!clessAIDDsc.dol.is_tlv_present("DF811B")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF811B", "B0", "B_"));
                    }
                    // Max Lifetime of Torn Transaction Log Record
                    if (!clessAIDDsc.dol.is_tlv_present("DF811C")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF811C", "0000", "B_"));
                    }
                    // Max Number of Torn Transaction Log Records
                    if (!clessAIDDsc.dol.is_tlv_present("DF811D")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF811D", "00", "B_"));
                    }
                    // Mag-stripe CVM Capability - CVM Required
                    if (!clessAIDDsc.dol.is_tlv_present("DF811E")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF811E", "20", "B_"));
                    }
                    // Security Capability
                    if (!clessAIDDsc.dol.is_tlv_present("DF811F")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF811F", "48", "B_"));
                    }
                    // Reader Contactless Floor Limit
                    if (!clessAIDDsc.dol.is_tlv_present("DF8123")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF8123", "000000000000", "B_"));
                    }
                    // Reader Contactless Transaction Limit (No On-device CVM)
                    if (!clessAIDDsc.dol.is_tlv_present("DF8124")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF8124", "000099999999", "B_"));
                    }
                    // Reader Contactless Transaction Limit (On-device CVM)
                    if (!clessAIDDsc.dol.is_tlv_present("DF8125")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF8125", "000099999999", "B_"));
                    }
                    // Reader CVM Required Limit
                    if (!clessAIDDsc.dol.is_tlv_present("DF8126")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF8126", "000000010000", "B_"));
                    }
                    // Mag-stripe CVM Capability - No CVM Required
                    if (!clessAIDDsc.dol.is_tlv_present("DF812C")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF812C", "08", "B_"));
                    }
                    if (!clessAIDDsc.dol.is_tlv_present("DF812D")) {
                        clessAIDDsc.dol.add_tlv(new TLV(
                                "DF812D", "00000000", "B_"));
                    }

                    clessAIDDsc.dol = model.filterDOLForAIDParam(
                            clessAIDDsc.dol, model.clMCLAIDTagList);
                    isAidEltIdFound = true;
                } else {
                    isAidEltIdFound = false;
                }
                if (isAidEltIdFound) {
                    model.add_cless_elt(clessAIDDsc);
                    break;
                }
            }
        }
    }
}
