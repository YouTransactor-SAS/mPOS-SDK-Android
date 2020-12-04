package com.youtransactor.sampleapp.payment;

import android.util.Log;

import com.youTransactor.uCube.ITaskMonitor;
import com.youTransactor.uCube.TaskEvent;
import com.youTransactor.uCube.Tools;
import com.youTransactor.uCube.payment.PaymentContext;
import com.youTransactor.uCube.payment.PaymentMessage;
import com.youTransactor.uCube.payment.task.IUseCardHolderLanguageTask;

import java.util.HashMap;
import java.util.Map;

import static com.youTransactor.uCube.payment.PaymentMessage.*;

public class UseCardHolderLanguageTask implements IUseCardHolderLanguageTask {

    private ITaskMonitor monitor;
    private byte[] selectedCardHolderLanguage;
    private Map<PaymentMessage, String> paymentMessages;

    private static Map<PaymentMessage, String> frPaymentMessage()  {

        Map<PaymentMessage, String> paymentMessages = new HashMap<>();
        // common messages to nfc & smc transaction
        paymentMessages.put(LBL_prepare_context, "Preparation du context");
        paymentMessages.put(LBL_authorization, "Autorisation en cours");
        paymentMessages.put(LBL_wait_card_ok, "Attendez svp");

        // smc messages
        paymentMessages.put(LBL_smc_initialization, "Initialisation en cours");
        paymentMessages.put(LBL_smc_risk_management, "Gestion de risque en cours ");
        paymentMessages.put(LBL_smc_finalization, "Finalisation en cours ");
        paymentMessages.put(LBL_smc_remove_card, "Enlevez la carte SVP");

        //nfc messages
        paymentMessages.put(LBL_nfc_complete, "Finalisation en cours");
        paymentMessages.put(LBL_wait_online_pin_process, "pin online en cours");
        paymentMessages.put(LBL_pin_request, "Entrez votre pin");

        /*  Payment status messages*/
        paymentMessages.put(LBL_approved, "Approuve"); // returned by the application
        paymentMessages.put(LBL_declined, "Refuse"); // returned by the application
        paymentMessages.put(LBL_unsupported_card, "Carte non supporter"); // returned by the application
        paymentMessages.put(LBL_cancelled, "Annuler"); // terminal or application
        paymentMessages.put(LBL_error, "Erreur"); // returned by the application
        paymentMessages.put(LBL_no_card_detected, "Pas de carte detecte");  // returned by the application
        paymentMessages.put(LBL_wrong_activated_reader, "Mauvaise interface active");  // returned by the application
        // nfc specific error status
        paymentMessages.put(LBL_try_other_interface, "Essayez une autre interface"); // returned by terminal
        paymentMessages.put(LBL_end_application, "Fin de application"); // returned by terminal
        paymentMessages.put(LBL_failed, "Echec"); // returned by terminal
        paymentMessages.put(LBL_wrong_nfc_outcome, "Mauvai NFC OUTCOME"); // returned by the application
        // smc specific error status
        paymentMessages.put(LBL_wrong_cryptogram_value, "mauvai cryptogramme"); // returned by the application
        paymentMessages.put(LBL_missing_required_cryptogram, "Cryptogramme non retourné"); // returned by the application

        return paymentMessages;
    }


    private static Map<PaymentMessage, String> enPaymentMessage()  {

        Map<PaymentMessage, String> paymentMessages = new HashMap<>();
        // common messages to nfc & smc transaction
        paymentMessages.put(LBL_prepare_context, "Preparing context");
        paymentMessages.put(LBL_authorization, "Authorization processing");
        paymentMessages.put(LBL_wait_card_ok, "Wait please");
        // smc messages
        paymentMessages.put(LBL_smc_initialization, "initialization processing");
        paymentMessages.put(LBL_smc_risk_management, "risk management processing");
        paymentMessages.put(LBL_smc_finalization, "finalization processing");
        paymentMessages.put(LBL_smc_remove_card, "Remove card, please");

        //nfc messages
        paymentMessages.put(LBL_nfc_complete, "complete processing");
        paymentMessages.put(LBL_wait_online_pin_process, "online pin processing");
        paymentMessages.put(LBL_pin_request, "Enter pin ");

        /*  Payment status messages*/
        paymentMessages.put(LBL_approved, "Approved"); // returned by the application
        paymentMessages.put(LBL_declined, "Declined"); // returned by the application
        paymentMessages.put(LBL_unsupported_card, "Unsupported card"); // returned by the application
        paymentMessages.put(LBL_cancelled, "Cancelled"); // terminal or application
        paymentMessages.put(LBL_error, "Error"); // returned by the application
        paymentMessages.put(LBL_no_card_detected, "No card detected");  // returned by the application
        paymentMessages.put(LBL_wrong_activated_reader, "wrong activated reader");  // returned by the application
        // nfc specific error status
        paymentMessages.put(LBL_try_other_interface, "Try other interface"); // returned by terminal
        paymentMessages.put(LBL_end_application, "End application"); // returned by terminal
        paymentMessages.put(LBL_failed, "Failed"); // returned by terminal
        paymentMessages.put(LBL_wrong_nfc_outcome, "wrong nfc outcome"); // returned by the application
        // smc specific error status
        paymentMessages.put(LBL_wrong_cryptogram_value, "wrong cryptogram"); // returned by the application
        paymentMessages.put(LBL_missing_required_cryptogram, "missing required cryptogram"); // returned by the application

        return paymentMessages;
    }


    @Override
    public void setSelectedCardHolderLanguage(byte[] selectedCardHolderLanguage) {
        Log.d(getClass().getName(), "selected lang : "+ Tools.bytesToHex(selectedCardHolderLanguage));

        this.selectedCardHolderLanguage = selectedCardHolderLanguage;
    }

    @Override
    public Map<PaymentMessage, String> getPaymentMessages() {
        return paymentMessages;
    }

    @Override
    public PaymentContext getContext() {
        return null;
    }

    @Override
    public void setContext(PaymentContext paymentContext) {

    }

    @Override
    public void execute(ITaskMonitor monitor) {

        if(selectedCardHolderLanguage == null || selectedCardHolderLanguage.length < 2) {
            monitor.handleEvent(TaskEvent.CANCELLED);
            return;
        }
        short selectedCardHolderLang = Tools.makeShort(selectedCardHolderLanguage[0], selectedCardHolderLanguage[1]);
        switch (selectedCardHolderLang) {
            case 0x656E:
                this.paymentMessages = enPaymentMessage();
                break;

            case 0x6672:
                this.paymentMessages = frPaymentMessage();
                break;

            default:
                this.paymentMessages = enPaymentMessage();
                break;
        }

        monitor.handleEvent(TaskEvent.SUCCESS);
    }

    @Override
    public void cancel() {
        monitor.handleEvent(TaskEvent.CANCELLED);
    }
}
