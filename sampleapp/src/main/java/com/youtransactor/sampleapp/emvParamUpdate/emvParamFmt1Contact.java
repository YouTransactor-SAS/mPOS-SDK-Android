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

import com.youTransactor.uCube.emv.EmvParamDOL;
import com.youTransactor.uCube.emv.EmvParamYTModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class emvParamFmt1Contact extends EmvParamFmt1{
    static void getEmvModelFromFmt1Input(JSONObject jsonD, EmvParamYTModel model) throws JSONException {
        JSONObject commonJson = jsonD.getJSONObject("common");
        model.setCtParamID(EmvParamFmt1.getCtParamID(commonJson));
        // Fill EMV model contact AID table
        EmvParamDOL commonLst = get_tlv_dol(commonJson,
                EmvParamFmt1ToYT.get_contact_tag_dict());
        commonLst = model.filterDOLForAIDParam(
                commonLst, model.cAIDTagList);
        // get contact structure
        JSONObject cJson = jsonD.getJSONObject("contact");
        // contact common dictionary
        JSONObject cCommon = cJson.getJSONObject("common");
        EmvParamDOL cCommonLst = get_tlv_dol(cCommon,
                EmvParamFmt1ToYT.get_contact_tag_dict());
        cCommonLst = model.filterDOLForAIDParam(
                cCommonLst, model.cAIDTagList);
        // contact AID array
        JSONArray cAIDList = cJson.getJSONArray("aids");
        for(int i = 0; i < cAIDList.length(); i++){
            EmvParamDOL aidP = EmvParamFmt1.get_tlv_dol(
                    cAIDList.getJSONObject(i),
                    EmvParamFmt1ToYT.get_contact_tag_dict());
            // AID parameters repeat common parameters
            aidP.dol.addAll(commonLst.dol);
            aidP.dol.addAll(cCommonLst.dol);
            // update YT model
            model.add_contact_aid(aidP);
        }

        JSONArray cCAPKsList = jsonD.getJSONArray("capks");
        for(int i = 0; i < cCAPKsList.length(); i++){
            JSONObject capkP = cCAPKsList.getJSONObject(i);
            model.add_contact_capk(capkP.getString("RID"),
                    capkP.getString("index"),
                    capkP.getString("exp"),
                    capkP.getString("mod"),
                    capkP.getString("checkSum"),
                    capkP.getString("expiryDate"));
        }
    }
}
