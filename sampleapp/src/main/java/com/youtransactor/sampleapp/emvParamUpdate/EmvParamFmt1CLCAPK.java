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
import com.youTransactor.uCube.emv.EmvParamYTModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
public class EmvParamFmt1CLCAPK extends EmvParamFmt1{
    static void getEmvModelFromFmt1Input(
            JSONObject jsonD, EmvParamYTModel model) throws JSONException {
        model.setIsClCapkConfigured(false);
        JSONArray clCapkList = jsonD.getJSONArray("capks");
        for(int i = 0; i < clCapkList.length(); i++) {
            EmvParamYTModel.ClessEltDsc clessCAPKDsc =
                    new EmvParamYTModel.ClessEltDsc();
            clessCAPKDsc.dol = EmvParamFmt1.get_tlv_dol(
                    clCapkList.getJSONObject(i),
                    EmvParamFmt1ToYT.get_cless_capk_tag_dict());
            clessCAPKDsc.type = EmvParamYTModel.TypeID.clCAPKId.getVal();
            clessCAPKDsc.eltToUpdID = EmvParamYTModel.EltToUpdID.noId.getVal();
            model.add_cless_capk(clessCAPKDsc);
            model.setIsClCapkConfigured(true);
        }
    }
}
