package com.youtransactor.sampleapp.emvParamUpdate;

import com.youTransactor.uCube.emv.EmvParamYTModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class emvParamFmt1ContactCAPK extends EmvParamFmt1{
    static void getEmvModelFromFmt1Input(JSONObject jsonD,
                                         EmvParamYTModel model)
            throws JSONException {
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
