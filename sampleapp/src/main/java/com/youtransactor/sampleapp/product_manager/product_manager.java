package com.youtransactor.sampleapp.product_manager;

import android.os.Build;
import android.util.Log;

public final class product_manager {
    private static final String TAG = product_manager.class.getSimpleName();
    private static final String propAndroidVersion = Build.MODEL;
    private static final String propProductName = Build.VERSION.RELEASE;
    private static final String bladeFWVersionMajor = "14.1";
    private static final String stickFWVersionMajor = "14.2";

    public static final product_id id;

    static {
        String productName = propProductName;
        Log.e(TAG, String.format("propProductName : %s",productName ));

        String buildVersion = propAndroidVersion;
        Log.e(TAG, String.format("propAndroidVersion : %s",buildVersion ));

        if (buildVersion.contains("BLADE")){
            Log.e(TAG, "Product : Balde");
            id = product_id.blade;
        } else if (buildVersion.contains("CQS291")) {
            Log.e(TAG, "Product : Stick");
            id = product_id.stick;
        } else {
            if (productName.startsWith(bladeFWVersionMajor)) {
                Log.e(TAG, "Product : Balde");
                id = product_id.blade;
            }else if(productName.startsWith(stickFWVersionMajor)) {
                Log.e(TAG, "Product : Stick");
                id = product_id.stick;
            }else{
                Log.e(TAG, "Product : none");
                id = product_id.none;
            }
        }
    }

}
