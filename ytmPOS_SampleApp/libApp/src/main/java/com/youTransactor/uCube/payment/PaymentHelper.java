package com.youTransactor.uCube.payment;

import android.os.Bundle;

import com.youTransactor.uCube.LogManager;
import com.youTransactor.uCube.Tools;
import com.youTransactor.uCube.api.UCubePaymentRequest;
import com.youTransactor.uCube.api.YTMPOSProduct;
import com.youTransactor.uCube.payment.service.PaymentService;
import com.youTransactor.uCube.payment.service.SingleEntryPointPaymentService;
import com.youTransactor.uCube.rpc.Constants;
import com.youTransactor.uCube.rpc.DeviceInfos;
import com.youTransactor.uCube.rpc.command.GetInfosCommand;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Observable;
import java.util.ResourceBundle;

public class PaymentHelper extends Observable {

    /**
     * Mode that can be selected in a configuration
     * <li>{@link #MSR}</li>
     * <li>{@link #SMC}</li>
     * <li>{@link #NFC}</li>
     */
    public enum Mode {
        /**
         * Enable the MagStripe mode
         */
        MSR,
        /**
         * Enable the SmartCard mode
         */
        SMC,
        /**
         * Enable the NFC mode
         */
        NFC;
        public static final EnumSet<Mode> ALL_MODES = EnumSet.allOf(Mode.class);
    }

    private enum DeviceType {
        UNKNOWN,
        ERROR,
        CONTACT,
        CONTACTLESS,
    }

    private boolean smc;
    private boolean msr;
    private boolean nfc;
    private UCubePaymentRequest uCubePaymentRequest;
    private YTMPOSProduct ytmposProduct;

    private PaymentContext paymentContext;
    private int mposState;
    private DeviceType deviceType = DeviceType.UNKNOWN;
    private List<Byte> readerList;

    private ResourceBundle msgBundle;
    private Bundle altMsgBundle;

    /* methods */

    public PaymentHelper(YTMPOSProduct ytmposProduct, UCubePaymentRequest uCubePaymentRequest) {
        this(ytmposProduct, uCubePaymentRequest, Mode.ALL_MODES);
    }

    public PaymentHelper(YTMPOSProduct ytmposProduct, UCubePaymentRequest uCubePaymentRequest, EnumSet<Mode> modes) {
        setModes(modes);
        this.ytmposProduct = ytmposProduct;
        this.uCubePaymentRequest = uCubePaymentRequest;
    }

    public void setMsgBundle(ResourceBundle msgBundle) {
        this.msgBundle = msgBundle;
    }

    public void setAltMsgBundle(Bundle bundle) {
        this.altMsgBundle = bundle;
    }

    private void setModes(EnumSet<Mode> modes) {
        msr = modes.contains(Mode.MSR);
        smc = modes.contains(Mode.SMC);
        nfc = modes.contains(Mode.NFC);
    }

    /**
     * Start a payment with values set previously
     */
    public void startPayment() {

        switch (ytmposProduct) {
            case uCube:
                checkuCubeType();
                break;

            case uCube_touch:
                deviceType = DeviceType.CONTACTLESS;
                getDevicePN();
                break;
        }
    }

    private void checkuCubeType() {
        new GetInfosCommand(Constants.TAG_MPOS_MODULE_STATE, Constants.TAG_TERMINAL_PN).execute((event, params) -> {
            switch (event) {
                case PROGRESS:
                    // nothing to do
                    return;

                case FAILED:
                    LogManager.e("failed to get info!");
                    mposState = -1;
                    deviceType = DeviceType.ERROR;
                    end(PaymentState.GET_MPOS_STATE_ERROR);
                    break;

                case SUCCESS:
                    DeviceInfos deviceInfos = new DeviceInfos(((GetInfosCommand) params[0]).getResponseData());

                    switch (mposState = deviceInfos.getNfcModuleState()) {
                        case 0:
                            LogManager.d("Device Contact detected");
                            deviceType = DeviceType.CONTACT;
                            break;
                        default:
                            LogManager.d("Device NFC detected");
                            deviceType = DeviceType.CONTACTLESS;
                            break;
                    }
                    startPaymentMposChecked();
                    break;

                default:
                    LogManager.e("failed to get info! unexpected event: " + event.name());
                    mposState = -1;
                    deviceType = DeviceType.ERROR;
                    end(PaymentState.GET_MPOS_STATE_ERROR);
                    break;
            }
        });
    }

    private void getDevicePN() {
        new GetInfosCommand(Constants.TAG_TERMINAL_PN).execute((event, params) -> {
            switch (event) {
                case PROGRESS:
                    // nothing to do
                    return;

                case FAILED:
                    LogManager.e("failed to get info : part number!");
                    end(PaymentState.GET_PN_ERROR);
                    break;

                case SUCCESS:
                    //new DeviceInfos(((GetInfosCommand) params[0]).getResponseData());

                    startPaymentMposChecked();
                    break;

                default:
                    LogManager.e("failed to get info! unexpected event: " + event.name());
                    break;
            }
        });
    }

    private void startPaymentMposChecked() {

        paymentContext = new PaymentContext();

        if (!nfc && !msr && !smc) {
            LogManager.e("no card mode selected!");
            end(PaymentState.TRANSACTION_MODE_ERROR);
        }

        if (uCubePaymentRequest.getRiskManagementTask() == null) {
            LogManager.e("no risk management");
            end(PaymentState.RISK_MANAGEMENT_TASK_NULL_ERROR);
        }

        if (uCubePaymentRequest.getAuthorizationTask() == null) {
            LogManager.e("no authorization task");
            end(PaymentState.AUTHORIZATION_TASK_NULL_ERROR);
        }

        paymentContext.setTransactionDate(new Date());
        paymentContext.setAmount(uCubePaymentRequest.getAmount());
        paymentContext.setCurrency(uCubePaymentRequest.getCurrency());
        paymentContext.setTransactionType(uCubePaymentRequest.getTransactionType());
        paymentContext.setPreferredLanguageList(uCubePaymentRequest.getPreferredLanguageList());

        if(msgBundle != null)
            paymentContext.setMsgBundle(msgBundle);
        else
            paymentContext.setAltMsgBundle(altMsgBundle);

        readerList = new ArrayList<>();

        if (msr) {
            // init msr
            readerList.add(Constants.MS_READER);

            if(uCubePaymentRequest.getRequestedPlainTagList() == null) {
                paymentContext.setRequestedPlainTagList(new int[]{Constants.TAG_MSR_BIN});
            } else {
                paymentContext.setRequestedPlainTagList(uCubePaymentRequest.getRequestedPlainTagList());
            }

            if(uCubePaymentRequest.getRequestedSecuredTagList() == null) {
                paymentContext.setRequestedSecuredTagList(new int[]{Constants.TAG_CARD_DATA_BLOCK});
            } else {
                paymentContext.setRequestedSecuredTagList(uCubePaymentRequest.getRequestedSecuredTagList());
            }

            paymentContext.setForceOnlinePIN(uCubePaymentRequest.isForceOnlinePin());
        }

        if (smc) {
            //Init SMC
            readerList.add(Constants.ICC_READER);

            if(uCubePaymentRequest.getRequestedAuthorizationTagList() != null)
                paymentContext.setRequestedAuthorizationTagList(uCubePaymentRequest.getRequestedAuthorizationTagList());
        }

        if (nfc && deviceType == DeviceType.CONTACTLESS) {

            // Init NFC
            readerList.add(Constants.NFC_READER);

            if(uCubePaymentRequest.getRequestedPlainTagList() == null) {
                paymentContext.setRequestedPlainTagList(new int[]{Constants.TAG_MSR_BIN});
            } else {
                paymentContext.setRequestedPlainTagList(uCubePaymentRequest.getRequestedPlainTagList());
            }

            if(uCubePaymentRequest.getRequestedSecuredTagList() == null) {
                paymentContext.setRequestedSecuredTagList(new int[]{Constants.TAG_TRACK2_EQU_DATA});
            } else {
                paymentContext.setRequestedSecuredTagList(uCubePaymentRequest.getRequestedSecuredTagList());
            }

            if(ytmposProduct == YTMPOSProduct.uCube) {
                waitForMpos();
                return;
            }
        }

        startPaymentMposReady();
    }

    private void waitForMpos() {
        new GetInfosCommand(Constants.TAG_MPOS_MODULE_STATE).execute((event, params) -> {
            switch (event) {
                case PROGRESS:
                    // nothing to do
                    return;

                case FAILED:
                    LogManager.e("failed to get info!");
                    mposState = -1;
                    end(PaymentState.GET_MPOS_STATE_ERROR);
                    break;

                case SUCCESS:
                    DeviceInfos deviceInfos = new DeviceInfos(((GetInfosCommand) params[0]).getResponseData());
                    switch (mposState = deviceInfos.getNfcModuleState()) {
                    case 0:
                        LogManager.e("Wait for mpos, Device Contact detected!!!!!");
                        end(PaymentState.NFC_MPOS_ERROR);
                        break;

                    case 1:
                    case 2:
                        LogManager.d("NFC not ready yet");
                        //sleep some and do again
                        try {
                            Thread.sleep(2000);
                            waitForMpos();
                        } catch (InterruptedException e) {
                            e.printStackTrace();

                            LogManager.e("wait for mpos, thread sleep catch exception !!!");
                            end(PaymentState.GET_MPOS_STATE_ERROR);
                        }
                        break;

                    case 3:
                        LogManager.d("NFC ready");
                        startPaymentMposReady();
                        break;

                    default:
                        LogManager.e("unexcpected nfc status: " + mposState);
                        end(PaymentState.GET_MPOS_STATE_ERROR);
                        break;
                }
                    break;

                default:
                    LogManager.e("failed to get info! unexpected event: " + event.name());
                    mposState = -1;
                    end(PaymentState.GET_MPOS_STATE_ERROR);
                    break;
            }
        });
    }

    private void startPaymentMposReady() {
        byte[] activatedReaders = new byte[readerList.size()];
        for (int i = 0; i < activatedReaders.length; i++) {
            activatedReaders[i] = readerList.get(i);
        }

        final PaymentService svc;

        switch (deviceType) {
            case CONTACT:
                svc = new PaymentService(paymentContext, activatedReaders);
                break;

            case CONTACTLESS:
                svc = new SingleEntryPointPaymentService(paymentContext, activatedReaders);
                break;

            default:
                LogManager.e("error, no device type for payment mode");
                end(PaymentState.DEVICE_TYPE_ERROR);
                return;
        }

        svc.setCardWaitTimeout(uCubePaymentRequest.getCardWaitTimeout());
        svc.setApplicationSelectionProcessor(uCubePaymentRequest.getApplicationSelectionTask());
        svc.setRiskManagementTask(uCubePaymentRequest.getRiskManagementTask());
        svc.setAuthorizationProcessor(uCubePaymentRequest.getAuthorizationTask());

        new Thread(() -> svc.execute((event, params) -> {
            LogManager.d("payment service event : " + event);

            switch (event) {
                case PROGRESS:
                    switch (paymentContext.getPaymentStatus()) {
                        case STARTED:
                            LogManager.d("PAYMENT START");
                            break;

                        case ENTER_SECURE_SESSION:
                            LogManager.d("ENTER SECURE SESSION");

                            setChanged();

                            notifyObservers(paymentContext);

                            break;

                        default:
                            // TODO: 09/11/16 what should we do
                            LogManager.d("paymentProgress not started: " + paymentContext.getPaymentStatus());
                            break;
                    }
                    break;

                case SUCCESS: // payment finished
                    LogManager.d("payment success : response status : " + paymentContext.getPaymentStatus());

                    send_fail_logs();

                    return;

                case FAILED:
                    //Set payment status to error
                    paymentContext.setPaymentStatus(PaymentState.ERROR);

                    LogManager.e("payment Failed !!");

                    send_fail_logs();

                    return;

                default:
                    LogManager.e("unexpected event: " + event.name());

                    end(PaymentState.ERROR);

                    break;
            }

        })).start();
    }

    /**
     * Notify the observer
     */
    private void end(PaymentState paymentState) {

        paymentContext.setPaymentStatus(paymentState);

        setChanged();

        notifyObservers(paymentContext);
    }

    /**
     * Fill the message with the requested logs then notify the message to the listener.
     * If no tags is required, the message is just transmitted, if any.
     */
    private void send_fail_logs() {
        if (uCubePaymentRequest.isSystemFailureInfo())
            send_fail_logs_1();
        else if (uCubePaymentRequest.isSystemFailureInfo2())
            send_fail_logs_2();
        else
            end(paymentContext.getPaymentStatus());
    }

    private void send_fail_logs_1() {
        new GetInfosCommand(Constants.TAG_SYSTEM_FAILURE_LOG_RECORD_1).execute((event, params) -> {
            switch (event) {
                case PROGRESS:
                    // nothing to do
                    return;

                case FAILED:
                    LogManager.e("failed to get logs_1!");
                    break;

                case SUCCESS:
                    paymentContext.setSystemFailureInfo(((GetInfosCommand) params[0]).getResponseData());
                    LogManager.d("Logs1: " + Tools.bytesToHex(paymentContext.getSystemFailureInfo()));
                    break;

                default:
                    LogManager.e("failed to get logs1! unexpected event: " + event.name());
                    break;
            }

            if (uCubePaymentRequest.isSystemFailureInfo2())
                send_fail_logs_2();
            else
                end(paymentContext.getPaymentStatus());

        });
    }

    private void send_fail_logs_2() {

        new GetInfosCommand(Constants.TAG_SYSTEM_FAILURE_LOG_RECORD_2).execute((event, params) -> {
            switch (event) {
                case PROGRESS:
                    // nothing to do
                    return;

                case FAILED:
                    LogManager.e("failed to get logs_2!");
                    break;

                case SUCCESS:
                    paymentContext.setSystemFailureInfo2(((GetInfosCommand) params[0]).getResponseData());
                    LogManager.d("Logs2: " + Tools.bytesToHex(paymentContext.getSystemFailureInfo2()));
                    break;

                default:
                    LogManager.e("failed to get logs2! unexpected event: " + event.name());
                    break;
            }

            end(paymentContext.getPaymentStatus());

        });
    }
}