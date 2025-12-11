package com.youtransactor.sampleapp.utils_common.emv_utils;

import android.util.Log;

import com.youTransactor.uCube.TLV;
import com.youTransactor.uCube.emv.EmvParamDOL;
import com.youTransactor.uCube.emv.EmvParamYTModel;

import java.util.List;

public class EmvParamYTModelPrinter {
    private static final String TAG = "EMV Model Printer";
    private static void printDol(EmvParamDOL dol){
        for(TLV tlv : dol.dol){
            Log.i(TAG, tlv.getTag() + ": " +
                    tlv.getLen()+": "+
                    tlv.getVal());
        }
    }
    private static void printEltDolList(String eltDoName,
                                        List<EmvParamDOL> dolList){
        Log.i(TAG, "---- "+eltDoName + " List ----");
        for(int i = 0; i < dolList.size(); i++){
            Log.i(TAG, "-- "+eltDoName + " index: " + i+" --");
            printDol(dolList.get(i));
            Log.i(TAG, "\r\n");
        }
    }
    private static void printClessEltDolList(String eltDoName,
                                             List<EmvParamYTModel.ClessEltDsc>
                                                     eltList){
        Log.i(TAG, "---- "+eltDoName + " List ----");
        for(int i = 0; i < eltList.size(); i++){
            if(!eltList.get(i).dol.dol.isEmpty()) {
                if (i == 0) {
                    Log.i(TAG, "-- " + eltDoName +
                            ": Terminal parameter --");
                } else {
                    Log.i(TAG, "-- " + eltDoName +
                            " index: " + (i - 1) + " --");
                }
                printDol(eltList.get(i).dol);
                Log.i(TAG, "\r\n");
            }
        }
    }
    private static void printClessCAPKDolList(
            List<EmvParamYTModel.ClessEltDsc> eltList){
        String eltDolName = "CLess CAPK";
        Log.i(TAG, "---- "+eltDolName + " List ----");
        for(int i = 0; i < eltList.size(); i++){
            if(!eltList.get(i).dol.dol.isEmpty()) {
                Log.i(TAG, "-- " + eltDolName +
                        " index: " + i + " --");
                printDol(eltList.get(i).dol);
                Log.i(TAG, "\r\n");
            }
        }
    }
    static public void printModel(EmvParamYTModel model){
        Log.i(TAG, "EMV Parameter Contact identifier (version): "+
                model.getCtParamID());
        Log.i(TAG, "EMV Parameter Contact Date: "+
                model.getCtParamDate());
        Log.i(TAG, "EMV Parameter Contact CAPK identifier (version): "+
                model.getCtCAPKID());
        Log.i(TAG, "EMV Parameter Contact CAPK date: "+
                model.getCtCAPKDate());
        Log.i(TAG, "EMV Parameter CLess identifier (version): "+
                model.getClParamID());
        Log.i(TAG, "EMV Parameter CLess Date: "+
                model.getClParamDate());
        Log.i(TAG, "EMV Parameter CLess CAPK identifier (version): "+
                model.getClCAPKID());
        Log.i(TAG, "EMV Parameter CLess CAPK date: "+
                model.getClCAPKDate());
        printEltDolList("Contact AID", model.getcAIDList());
        printEltDolList("Contact CAPK", model.getcCAPKList());
        printClessEltDolList("CLess AID MCL",
                model.getClessMCLEltLst());
        printClessEltDolList("CLess AID VISA",
                model.getClessVISAEltLst());
        printClessEltDolList("CLess AID AMEX",
                model.getClessAMEXEltLst());
        printClessEltDolList("CLess AID JCB",
                model.getClessJCBEltLst());
        printClessEltDolList("CLess AID INTERAC",
                model.getClessINTERACEltLst());
        printClessEltDolList("CLess AID CUP",
                model.getClessCUPEltLst());
        printClessEltDolList("CLess AID DISCOVER",
                model.getClessDISCEltLst());
        printClessCAPKDolList(
                model.getclessCAPKList());
    }
}
