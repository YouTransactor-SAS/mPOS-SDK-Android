package com.youtransactor.sampleapp.payment;

import android.content.Context;

import com.youTransactor.uCube.payment.MessageId;
import com.youtransactor.sampleapp.R;

import java.util.HashMap;
import java.util.Map;

public class Localization {
    private static Map<Integer, String> localizationMap;

    public static void Localization_init(Context ctx) {
        localizationMap = new HashMap<>();
        addLocalizedMsg(MessageId.SCR_CLESS_MSG_AMOUNT.getId(), ctx.getString(R.string.SCR_CLESS_MSG_AMOUNT));
        addLocalizedMsg(MessageId.SCR_CLESS_MSG_AMOUNT_OK.getId(), ctx.getString(R.string.SCR_CLESS_MSG_AMOUNT_OK));
        addLocalizedMsg(MessageId.SCR_CLESS_MSG_APPROVED.getId(), ctx.getString(R.string.SCR_CLESS_MSG_APPROVED));
        addLocalizedMsg(MessageId.SCR_CLESS_MSG_CALL_YOUR_BANK.getId(), ctx.getString(R.string.SCR_CLESS_MSG_CALL_YOUR_BANK));
        addLocalizedMsg(MessageId.SCR_CLESS_MSG_CANCEL_OR_ENTER.getId(), ctx.getString(R.string.SCR_CLESS_MSG_CANCEL_OR_ENTER));
        addLocalizedMsg(MessageId.SCR_CLESS_MSG_CARD_ERROR.getId(), ctx.getString(R.string.SCR_CLESS_MSG_CARD_ERROR));
        addLocalizedMsg(MessageId.SCR_CLESS_MSG_DECLINED.getId(), ctx.getString(R.string.SCR_CLESS_MSG_DECLINED));
        addLocalizedMsg(MessageId.SCR_CLESS_MSG_ENTER_AMOUNT.getId(), ctx.getString(R.string.SCR_CLESS_MSG_ENTER_AMOUNT));
        addLocalizedMsg(MessageId.SCR_CLESS_MSG_ENTER_PIN.getId(), ctx.getString(R.string.SCR_CLESS_MSG_ENTER_PIN));
        addLocalizedMsg(MessageId.SCR_CLESS_MSG_INCORRECT_PIN.getId(), ctx.getString(R.string.SCR_CLESS_MSG_INCORRECT_PIN));
        addLocalizedMsg(MessageId.SCR_CLESS_MSG_INSERT_CARD.getId(), ctx.getString(R.string.SCR_CLESS_MSG_INSERT_CARD));
        addLocalizedMsg(MessageId.SCR_CLESS_MSG_NOT_ACCEPTED.getId(), ctx.getString(R.string.SCR_CLESS_MSG_NOT_ACCEPTED));
        addLocalizedMsg(MessageId.SCR_CLESS_MSG_PIN_OK.getId(), ctx.getString(R.string.SCR_CLESS_MSG_PIN_OK));
        addLocalizedMsg(MessageId.SCR_CLESS_MSG_PLEASE_WAIT.getId(), ctx.getString(R.string.SCR_CLESS_MSG_PLEASE_WAIT));
        addLocalizedMsg(MessageId.SCR_CLESS_MSG_PROCESSING_ERROR.getId(), ctx.getString(R.string.SCR_CLESS_MSG_PROCESSING_ERROR));
        addLocalizedMsg(MessageId.SCR_CLESS_MSG_REMOVE_CARD.getId(), ctx.getString(R.string.SCR_CLESS_MSG_REMOVE_CARD));
        addLocalizedMsg(MessageId.SCR_CLESS_MSG_USE_CHIP_READER.getId(), ctx.getString(R.string.SCR_CLESS_MSG_USE_CHIP_READER));
        addLocalizedMsg(MessageId.SCR_CLESS_MSG_USE_MAG_STRIPE.getId(), ctx.getString(R.string.SCR_CLESS_MSG_USE_MAG_STRIPE));
        addLocalizedMsg(MessageId.SCR_CLESS_MSG_TRY_AGAIN.getId(), ctx.getString(R.string.SCR_CLESS_MSG_TRY_AGAIN));
        addLocalizedMsg(MessageId.SCR_CLESS_MSG_WELCOME.getId(), ctx.getString(R.string.SCR_CLESS_MSG_WELCOME));
        addLocalizedMsg(MessageId.SCR_CLESS_MSG_PRESENT_CARD.getId(), ctx.getString(R.string.SCR_CLESS_MSG_PRESENT_CARD));
        addLocalizedMsg(MessageId.SCR_CLESS_MSG_PROCESSING.getId(), ctx.getString(R.string.SCR_CLESS_MSG_PROCESSING));
        addLocalizedMsg(MessageId.SCR_CLESS_MSG_CARD_READ_OK.getId(), ctx.getString(R.string.SCR_CLESS_MSG_CARD_READ_OK));
        addLocalizedMsg(MessageId.SCR_CLESS_MSG_INSERT_OR_SWIPE_CARD.getId(), ctx.getString(R.string.SCR_CLESS_MSG_INSERT_OR_SWIPE_CARD));
        addLocalizedMsg(MessageId.SCR_CLESS_MSG_PRESENT_ONLY_ONE_CARD.getId(), ctx.getString(R.string.SCR_CLESS_MSG_PRESENT_ONLY_ONE_CARD));
        addLocalizedMsg(MessageId.SCR_CLESS_MSG_APPROVED_SIGN.getId(), ctx.getString(R.string.SCR_CLESS_MSG_APPROVED_SIGN));
        addLocalizedMsg(MessageId.SCR_CLESS_MSG_AUTHORIZING_WAIT.getId(), ctx.getString(R.string.SCR_CLESS_MSG_AUTHORIZING_WAIT));
        addLocalizedMsg(MessageId.SCR_CLESS_MSG_INSERT_SWIPE_ANOTHER.getId(), ctx.getString(R.string.SCR_CLESS_MSG_INSERT_SWIPE_ANOTHER));
        addLocalizedMsg(MessageId.SCR_CLESS_MSG_CL_INSERT_CARD.getId(), ctx.getString(R.string.SCR_CLESS_MSG_CL_INSERT_CARD));
        addLocalizedMsg(MessageId.SCR_CLESS_MSG_CLEAR_DISPLAY.getId(), ctx.getString(R.string.SCR_CLESS_MSG_CLEAR_DISPLAY));
        addLocalizedMsg(MessageId.SCR_CLESS_MSG_SEE_PHONE.getId(), ctx.getString(R.string.SCR_CLESS_MSG_SEE_PHONE));
        addLocalizedMsg(MessageId.SCR_CLESS_MSG_PRESENT_CARD_AGAIN.getId(), ctx.getString(R.string.SCR_CLESS_MSG_PRESENT_CARD_AGAIN));
        addLocalizedMsg(MessageId.SCR_CLESS_MSG_LAST_PIN_TRY.getId(), ctx.getString(R.string.SCR_CLESS_MSG_LAST_PIN_TRY));
        addLocalizedMsg(MessageId.SCR_CLESS_MSG_NO_CARD.getId(), ctx.getString(R.string.SCR_CLESS_MSG_NO_CARD));
        addLocalizedMsg(MessageId.SCR_CLESS_MSG_TOO_MANY_TRIES.getId(), ctx.getString(R.string.SCR_CLESS_MSG_TOO_MANY_TRIES));
        addLocalizedMsg(MessageId.SCR_CLESS_MSG_PAYMENT_NOT_ACCEPTED.getId(), ctx.getString(R.string.SCR_CLESS_MSG_PAYMENT_NOT_ACCEPTED));
        addLocalizedMsg(MessageId.SCR_CLESS_MSG_LIMIT_REACHED.getId(), ctx.getString(R.string.SCR_CLESS_MSG_LIMIT_REACHED));
        addLocalizedMsg(MessageId.SCR_CLESS_MSG_TRX_FAIL_CT_Y.getId(), ctx.getString(R.string.SCR_CLESS_MSG_TRX_FAIL_CT_Y));
        addLocalizedMsg(MessageId.SCR_CLESS_MSG_TRX_FAIL_CL_ONLY_CT_Y.getId(), ctx.getString(R.string.SCR_CLESS_MSG_TRX_FAIL_CL_ONLY_CT_Y));
        addLocalizedMsg(MessageId.SCR_CLESS_MSG_TRX_FAIL_CL_ONLY_CT_N.getId(), ctx.getString(R.string.SCR_CLESS_MSG_TRX_FAIL_CL_ONLY_CT_N));
        addLocalizedMsg(MessageId.SCR_CLESS_MSG_NONE.getId(), ctx.getString(R.string.SCR_CLESS_MSG_NONE));
        addLocalizedMsg(MessageId.SCR_CLESS_MSG_GC_PLACE_CARD_ON_TRAY.getId(), ctx.getString(R.string.SCR_CLESS_MSG_GC_PLACE_CARD_ON_TRAY));
        addLocalizedMsg(MessageId.SCR_CT_FIRST_PIN_TEXT.getId(), ctx.getString(R.string.SCR_CT_FIRST_PIN_TEXT));
        addLocalizedMsg(MessageId.SCR_CT_WRONG_PIN_TEXT.getId(), ctx.getString(R.string.SCR_CT_WRONG_PIN_TEXT));
        addLocalizedMsg(MessageId.SCR_CT_LAST_PIN_TEXT.getId(), ctx.getString(R.string.SCR_CT_LAST_PIN_TEXT));
        addLocalizedMsg(MessageId.SCR_CT_PIN_BLOCKED_TEXT.getId(), ctx.getString(R.string.SCR_CT_PIN_BLOCKED_TEXT));
        addLocalizedMsg(MessageId.SCR_CT_PIN_OK_TEXT.getId(), ctx.getString(R.string.SCR_CT_PIN_OK_TEXT));
        addLocalizedMsg(MessageId.SCR_CT_INTERMEDIATE_PIN_TEXT.getId(), ctx.getString(R.string.SCR_CT_INTERMEDIATE_PIN_TEXT));
        addLocalizedMsg(MessageId.SCR_CT_AUTHORIZATION_TEXT.getId(), ctx.getString(R.string.SCR_CT_AUTHORIZATION_TEXT));
        addLocalizedMsg(MessageId.SCR_CT_SMC_INITIALIZATION_TEXT.getId(), ctx.getString(R.string.SCR_CT_SMC_INITIALIZATION_TEXT));
        addLocalizedMsg(MessageId.SCR_CT_SMC_RISK_MANAGEMENT_TEXT.getId(), ctx.getString(R.string.SCR_CT_SMC_RISK_MANAGEMENT_TEXT));
        addLocalizedMsg(MessageId.SCR_CT_SMC_FINALIZATION_TEXT.getId(), ctx.getString(R.string.SCR_CT_SMC_FINALIZATION_TEXT));
        addLocalizedMsg(MessageId.SCR_CT_SMC_REMOVE_CARD_TEXT.getId(), ctx.getString(R.string.SCR_CT_SMC_REMOVE_CARD_TEXT));
        addLocalizedMsg(MessageId.SCR_CT_NFC_COMPLETE_TEXT.getId(), ctx.getString(R.string.SCR_CT_NFC_COMPLETE_TEXT));
        addLocalizedMsg(MessageId.SCR_CT_WAIT_ONLINE_PIN_PROCESS_TEXT.getId(), ctx.getString(R.string.SCR_CT_WAIT_ONLINE_PIN_PROCESS_TEXT));
        addLocalizedMsg(MessageId.SCR_CT_APPROVED_TEXT.getId(), ctx.getString(R.string.SCR_CT_APPROVED_TEXT));
        addLocalizedMsg(MessageId.SCR_CT_DECLINED_TEXT.getId(), ctx.getString(R.string.SCR_CT_DECLINED_TEXT));
        addLocalizedMsg(MessageId.SCR_CT_UNSUPPORTED_CARD_TEXT.getId(), ctx.getString(R.string.SCR_CT_UNSUPPORTED_CARD_TEXT));
        addLocalizedMsg(MessageId.SCR_CT_REFUSED_CARD_TEXT.getId(), ctx.getString(R.string.SCR_CT_REFUSED_CARD_TEXT));
        addLocalizedMsg(MessageId.SCR_CT_NO_CARD_DETECTED_TEXT.getId(), ctx.getString(R.string.SCR_CT_NO_CARD_DETECTED_TEXT));
        addLocalizedMsg(MessageId.SCR_CT_FAILED_TEXT.getId(), ctx.getString(R.string.SCR_CT_FAILED_TEXT));
        addLocalizedMsg(MessageId.SCR_CT_END_APPLICATION_TEXT.getId(), ctx.getString(R.string.SCR_CT_END_APPLICATION_TEXT));
        addLocalizedMsg(MessageId.SCR_CT_TRY_ANOTHER_TEXT.getId(), ctx.getString(R.string.SCR_CT_TRY_ANOTHER_TEXT));
        addLocalizedMsg(MessageId.SCR_CT_WRONG_NFC_OUTCOME_TEXT.getId(), ctx.getString(R.string.SCR_CT_WRONG_NFC_OUTCOME_TEXT));
        addLocalizedMsg(MessageId.SCR_CT_WRONG_CRYPT_VALUE_TEXT.getId(), ctx.getString(R.string.SCR_CT_WRONG_CRYPT_VALUE_TEXT));
        addLocalizedMsg(MessageId.SCR_CT_MISSING_REQUIRED_CRYPTO_TEXT.getId(), ctx.getString(R.string.SCR_CT_MISSING_REQUIRED_CRYPTO_TEXT));
        addLocalizedMsg(MessageId.SCR_CT_APPROVED_SIG_TEXT.getId(), ctx.getString(R.string.SCR_CT_APPROVED_SIG_TEXT));
        addLocalizedMsg(MessageId.SCR_CT_CANCELED_TEXT.getId(), ctx.getString(R.string.SCR_CT_CANCELED_TEXT));
        addLocalizedMsg(MessageId.SCR_CT_REMOVE_CARD_TEXT.getId(), ctx.getString(R.string.SCR_CT_REMOVE_CARD_TEXT));
    }

    public static void addLocalizedMsg(int key, String value) {
        localizationMap.put(key, value);
    }

    public static String getLocalizedMsg(int key) {
        return localizationMap.get(key);
    }

    public static boolean IsExistInLocalizationMap(int key) {
        return localizationMap.containsKey(key);
    }

    public static String getMsg(int key, String msg) {
        if(!IsExistInLocalizationMap(key)){
            return msg;
        }
        else{
            return getLocalizedMsg(key);
        }
    }
}
