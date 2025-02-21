/*
 * ============================================================================
 *
 * Copyright (c) 2024 YouTransactor
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
import com.youTransactor.uCube.emv.EmvTagDescription;
import com.youTransactor.uCube.log.LogManager;
import com.youTransactor.uCube.TLV;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.Dictionary;
import java.util.Iterator;

public class EmvParamFmt1 {
    static protected EmvParamDOL get_tlv_dol(
            JSONObject fmt1Keys,
            Dictionary<String,
            String> tag_dict) {
        String key = "";
        String value;
        String tag;
        String format;
        EmvParamDOL dol = null;
        try {
            dol = new EmvParamDOL();
            Iterator<String> it = fmt1Keys.keys();
            while (it.hasNext()) {
                key = it.next();
                value = fmt1Keys.getString(key);
//            LogManager.d(key + " : " + value);
                tag = tag_dict.get(key);
                if(tag != null) {
                    format = EmvTagDescription.getTagValFormat(tag);
                    dol.add_tlv(new TLV(tag, value, format));
                }else{
                    LogManager.w( "FMT1: tag is NOT taken into account: "+key);
                }
            }
        } catch (Exception e) {
            LogManager.e( "FMT1: get_tlv_dol error: " + key, e);
        }
        return dol;
    }

    protected static int getNbAIDProfileForKernel(
            JSONArray clAIDList, String kernel_tok) throws JSONException {
        int nbAIDForKrnl = 0;
        // parse param a first time to retrieve how many AID profiles for each kernel
        for(int i = 0; i < clAIDList.length(); i++){
            JSONObject curJsonAidLst = clAIDList.getJSONObject(i);
            String aid_krnl_tok = curJsonAidLst.getString("appLabel");
            // when AID token partially or fully matches the analyzed kernel token
            int index = aid_krnl_tok.indexOf(kernel_tok);
            if(index != -1){
                nbAIDForKrnl++;
            }
        }
        return nbAIDForKrnl;
    }

    private void getEmvModelFromFmt1Input(InputStream input, EmvParamYTModel model) {
        try {
            // Contact parameter translation
            JSONObject jsonD = new JSONObject(IOUtils.toString(input));
            emvParamFmt1Contact.getEmvModelFromFmt1Input(jsonD, model);
            // Contactless parameter translation
            JSONObject clJson = jsonD.getJSONObject("contactless");
            EmvParamFmt1CLVisa.getEmvModelFromFmt1Input(clJson, model);
        } catch (Exception e) {
            LogManager.e("EMV model from FMT1 conversion fail", e);
        }
    }

    public static EmvParamYTModel from(InputStream input) {
        EmvParamYTModel model = new EmvParamYTModel();
        new EmvParamFmt1().getEmvModelFromFmt1Input(input, model);
        return model;
    }

}
