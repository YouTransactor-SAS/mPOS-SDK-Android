package com.youTransactor.uCube.api;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.youTransactor.uCube.ContextDataManager;
import com.youTransactor.uCube.LogManager;
import com.youTransactor.uCube.R;
import com.youTransactor.uCube.bluetooth.service.InitService;
import com.youTransactor.uCube.bluetooth.service.InitServiceState;
import com.youTransactor.uCube.bluetooth.ble.BleConnexionManager;
import com.youTransactor.uCube.bluetooth.bt.BluetoothConnexionManager;
import com.youTransactor.uCube.mdm.MDMManager;
import com.youTransactor.uCube.mdm.service.BinaryUpdate;
import com.youTransactor.uCube.mdm.service.CheckUpdateService;
import com.youTransactor.uCube.mdm.service.SendLogsService;
import com.youTransactor.uCube.mdm.service.ServiceState;
import com.youTransactor.uCube.mdm.service.UpdateService;
import com.youTransactor.uCube.payment.PaymentContext;
import com.youTransactor.uCube.payment.PaymentHelper;
import com.youTransactor.uCube.rpc.Constants;
import com.youTransactor.uCube.rpc.RPCManager;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.annotation.Nonnull;

import static com.youTransactor.uCube.Tools.checkForBleFeature;
import static com.youTransactor.uCube.Tools.checkForBluetooth;

public final class UCubeAPI {
    //todo add comments for every method

    public static void initManagers(@Nonnull Context context) {

        LogManager.initialize(context);

        ContextDataManager.getInstance().init(context);

        if (ContextDataManager.getInstance().getYTProduct() != null) {

            MDMManager.getInstance().setDeviceInfos(ContextDataManager.getInstance().getDeviceInfos());

            MDMManager.getInstance().initialize(ContextDataManager.getInstance().getUCubeSslCertificate());

            switch (ContextDataManager.getInstance().getYTProduct()) {
                case uCube:
                    BluetoothConnexionManager.getInstance().initialize();

                    RPCManager.getInstance().setConnexionManager(BluetoothConnexionManager.getInstance());
                    break;

                case uCube_touch:
                    BleConnexionManager.getInstance().initialize(context);

                    RPCManager.getInstance().setConnexionManager(BleConnexionManager.getInstance());
                    break;
            }
        }
    }

    public static void setup(@Nonnull Context context,
                             @NonNull Activity activity,
                             @NonNull YTMPOSProduct ytmposProduct,
                             @Nonnull UCubeAPIListener uCubeAPIListener) throws Exception {

        if (!checkForBluetooth(context))
            throw new Exception("Error, Device do not support Bluetooth!");

        if (ytmposProduct == YTMPOSProduct.uCube_touch && !checkForBleFeature(context)) {
            throw new Exception("Error, Device do not support BLE!");
        }

        new Thread(() -> {

            if (ContextDataManager.getInstance().getYTProduct() == null ||
                    (ContextDataManager.getInstance().getYTProduct() != null &&
                            ytmposProduct != ContextDataManager.getInstance().getYTProduct())) {

                ContextDataManager.getInstance().removeDevice();

                ContextDataManager.getInstance().setYtProduct(ytmposProduct);
            }

            switch (ytmposProduct) {
                case uCube:
                    BluetoothConnexionManager.getInstance().initialize();

                    RPCManager.getInstance().setConnexionManager(BluetoothConnexionManager.getInstance());
                    break;

                case uCube_touch:
                    BleConnexionManager.getInstance().initialize(context);

                    RPCManager.getInstance().setConnexionManager(BleConnexionManager.getInstance());
                    break;
            }

            LogManager.d("setup Android SDK MPOS lib");

            activity.runOnUiThread(() -> uCubeAPIListener.onFinish(true));

        }).start();
    }

    public static YTMPOSProduct getYTMPOSProduct() throws Exception {
        LogManager.d("get current configured YTMPOSProduct");

        if (ContextDataManager.getContext() == null) {
            throw new Exception("UCubeAPI not initialized call setup before all actions!");
        }

        return ContextDataManager.getInstance().getYTProduct();
    }

    public static UCubeInfo getUCubeInfo() throws Exception {

        if (ContextDataManager.getContext() == null) {
            throw new Exception("UCubeAPI not initialized call setup before all actions!");
        }

        if (!ContextDataManager.getInstance().isuCubePaired()) {
            throw new Exception("Error no paired device. " +
                    "use connect(Activity activity, YTMPOSProduct ytmposProduct) to pair one");
        }

        LogManager.d("get info of paired ucube");

        return new UCubeInfo(
                ContextDataManager.getInstance().getYTProduct(),
                ContextDataManager.getInstance().getUCubeAddress(),
                ContextDataManager.getInstance().getUCubeName(),
                ContextDataManager.getInstance().getUCubeSerial(),
                ContextDataManager.getInstance().getUCubePartNumber(),
                ContextDataManager.getInstance().getUCubeFirmwareVersion(),
                ContextDataManager.getInstance().getUCubeFirmwareSTVersion(),
                ContextDataManager.getInstance().getUCubeIICConfigVersion(),
                ContextDataManager.getInstance().getUCubeNFCConfigVersion(),
                ContextDataManager.getInstance().getUCubeNfcAvailable()
        );
    }

    public static void connect(@Nonnull Activity activity,
                               @NonNull UCubeConnectListener uCubeConnectListener) throws Exception {
        if (ContextDataManager.getContext() == null) {
            throw new Exception("UCubeAPI not initialized call setup before all actions!");
        }

        if (ContextDataManager.getInstance().getYTProduct() == null) {
            throw new Exception("Error It is mandatory to select one YTMPOSProduct before. " +
                    "Call connect(Activity activity, YTMPOSProduct ytmposProduct)");
        }

        LogManager.d("connect ucube");

        new Thread(() -> {
            InitService initService = new InitService(activity, ContextDataManager.getInstance().getYTProduct());

            initService.execute((event1, params1) -> activity.runOnUiThread(() -> {
                switch (event1) {
                    case PROGRESS:
                        switch ((InitServiceState) params1[0]) {
                            case IDLE:
                                uCubeConnectListener.onProgress(UCubeAPIState.IDLE);
                                break;

                            case CANCEL_ALL:
                                uCubeConnectListener.onProgress(UCubeAPIState.CANCEL_ALL);
                                break;

                            case PAIR_DEVICE:
                                uCubeConnectListener.onProgress(UCubeAPIState.PAIR_DEVICE);
                                break;

                            case RETRIEVE_DEVICE_INFO:
                                uCubeConnectListener.onProgress(UCubeAPIState.RETRIEVE_DEVICE_INFOS);
                                break;

                            case RETRIEVE_DEVICE_CERTIFICATE:
                                uCubeConnectListener.onProgress(UCubeAPIState.RETRIEVE_DEVICE_CERTIFICAT);
                                break;

                            case REGISTER_DEVICE:
                                uCubeConnectListener.onProgress(UCubeAPIState.REGISTER_DEVICE);
                                break;
                        }
                        break;

                    case SUCCESS:

                        UCubeInfo uCubeInfo = null;

                        try {
                            uCubeInfo = getUCubeInfo();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        uCubeConnectListener.onFinish(true, uCubeInfo);
                        break;

                    case CANCELLED:
                    case FAILED:

                        uCubeConnectListener.onFinish(false, null);
                        break;
                }
            }));

        }).start();
    }

    public static void pay(@Nonnull Context context,
                           @Nonnull UCubePaymentRequest uCubePaymentRequest,
                           @Nonnull UCubePaymentListener uCubePaymentListener) throws Exception {

        if (ContextDataManager.getContext() == null) {
            throw new Exception("UCubeAPI not initialized call setup before all actions!");
        }

        if (ContextDataManager.getInstance().getYTProduct() == null) {
            throw new Exception("Error It is mandatory to select one YTMPOSProduct before. " +
                    "Call connect(Activity activity, YTMPOSProduct ytmposProduct)");
        }

        if (!ContextDataManager.getInstance().isuCubePaired()) {
            throw new Exception("Error, Non device paired! It is mandatory to connect UCubeAPI first. " +
                    "use API connect(Activity activity, YTMPOSProduct ytmposProduct)");
        }

        LogManager.d("Do payment");

        startPayment(context, uCubePaymentRequest, uCubePaymentListener);
    }

    public static void checkUpdate(@NonNull Activity activity,
                                   boolean forceUpdate,
                                   boolean checkOnlyFirmwareVersion,
                                   @Nonnull UCubeCheckUpdateListener uCubeCheckUpdateListener) {

        LogManager.d("check update");

        new Thread(() -> {
            CheckUpdateService svc = new CheckUpdateService()
                    .setForceUpdate(forceUpdate)
                    .setCheckOnlyFirmwareVersion(checkOnlyFirmwareVersion);

            svc.execute((event, params) -> activity.runOnUiThread(() -> {
                switch (event) {
                    case PROGRESS:
                        switch ((ServiceState) params[0]) {
                            case RETRIEVE_DEVICE_INFOS:
                                uCubeCheckUpdateListener.onProgress(UCubeAPIState.RETRIEVE_DEVICE_INFOS);
                                break;

                            case RETRIEVE_DEVICE_CONFIG:
                                uCubeCheckUpdateListener.onProgress(UCubeAPIState.RETRIEVE_DEVICE_CONFIG);
                                break;

                            case CHECK_UPDATE:
                                uCubeCheckUpdateListener.onProgress(UCubeAPIState.CHECK_UPDATE);
                                break;
                        }
                        return;

                    case CANCELLED:
                    case FAILED:
                        uCubeCheckUpdateListener.onFinish(false, null, null);
                        break;

                    case SUCCESS:
                        uCubeCheckUpdateListener.onFinish(true, svc.getUpdateList(), svc.getCfgList());
                        break;
                }
            }));

        }).start();
    }

    public static void update(@NonNull Activity activity,
                              final @NonNull List<BinaryUpdate> updateList,
                              @Nonnull UCubeAPIListener uCubeAPIUpdateListener) throws Exception {

        if (ContextDataManager.getContext() == null) {
            throw new Exception("UCubeAPI not initialized call setup before all actions!");
        }

        if (ContextDataManager.getInstance().getYTProduct() == null) {
            throw new Exception("Error It is mandatory to select one YTMPOSProduct before. " +
                    "Call connect(Activity activity, YTMPOSProduct ytmposProduct)");
        }

        if (!ContextDataManager.getInstance().isuCubePaired()) {
            throw new Exception("Error, Non device paired! It is mandatory to connect UCubeAPI first. " +
                    "use API connect(Activity activity, YTMPOSProduct ytmposProduct)");
        }

        LogManager.d("update ucube");

        new Thread(() -> new UpdateService(updateList).execute((event, params) -> activity.runOnUiThread(() -> {
            switch (event) {
                case PROGRESS:
                    switch ((ServiceState) params[0]) {
                        case RETRIEVE_DEVICE_INFOS:
                            uCubeAPIUpdateListener.onProgress(UCubeAPIState.RETRIEVE_DEVICE_INFOS);
                            break;

                        case RETRIEVE_DEVICE_CERTIFICAT:
                            uCubeAPIUpdateListener.onProgress(UCubeAPIState.RETRIEVE_DEVICE_CERTIFICAT);
                            break;

                        case DOWNLOAD_BINARY:
                            uCubeAPIUpdateListener.onProgress(UCubeAPIState.DOWNLOAD_BINARY);
                            break;

                        case UPDATE_DEVICE:
                            uCubeAPIUpdateListener.onProgress(UCubeAPIState.UPDATE_DEVICE);
                            break;

                        case RECONNECT:
                            uCubeAPIUpdateListener.onProgress(UCubeAPIState.RECONNECT);
                            break;
                    }
                    return;

                case CANCELLED:
                case FAILED:
                    uCubeAPIUpdateListener.onFinish(false);
                    break;

                case SUCCESS:
                    uCubeAPIUpdateListener.onFinish(true);
                    break;
            }
        }))).start();

    }

    public static void sendLogs(Activity activity, @Nonnull UCubeAPIListener uCubeAPIListener) throws Exception {

        try {
            if (!LogManager.hasLogs()) {

                if (LogManager.isEnabled())
                    LogManager.d("No logs to send");
                else
                    LogManager.d("Logs not enabled");

                uCubeAPIListener.onFinish(false);
                return;
            }
        } catch (Exception e) {
            throw new Exception("LogManager catch exception : " + e.getMessage() + ". May be should you call setup() before");
        }

        new Thread(() -> new SendLogsService().execute((event, params) -> activity.runOnUiThread(() -> {
            switch (event) {
                case PROGRESS:
                    if (params[0] == ServiceState.SEND_LOGS) {
                        uCubeAPIListener.onProgress(UCubeAPIState.SEND_LOGS);
                    }
                    break;

                case FAILED:
                    uCubeAPIListener.onFinish(false);
                    break;

                case SUCCESS:
                    uCubeAPIListener.onFinish(true);
                    break;
            }
        }))).start();
    }

    public static void deletePairedUCube() throws Exception {

        if (ContextDataManager.getContext() == null) {
            throw new Exception("UCubeAPI not initialized call setup before all actions!");
        }

        ContextDataManager.getInstance().removeDevice();
    }

    public static void close() {

        MDMManager.getInstance().stop();
        RPCManager.getInstance().stop();

        YTMPOSProduct ytmposProduct = ContextDataManager.getInstance().getYTProduct();
        if (ytmposProduct == null)
            return;

        switch (ytmposProduct) {
            case uCube:
                BluetoothConnexionManager.getInstance().stop();
                break;

            case uCube_touch:
                BleConnexionManager.getInstance().stop();
                break;
        }

    }

    private static void startPayment(Context context, UCubePaymentRequest uCubePaymentRequest,
                                     UCubePaymentListener uCubePaymentListener) {

        YTMPOSProduct ytmposProduct = ContextDataManager.getInstance().getYTProduct();
        PaymentHelper paymentHelper = new PaymentHelper(ytmposProduct, uCubePaymentRequest);

        if (ytmposProduct == YTMPOSProduct.uCube_touch)
            paymentHelper = new PaymentHelper(ytmposProduct, uCubePaymentRequest,
                    EnumSet.of(PaymentHelper.Mode.SMC, PaymentHelper.Mode.NFC));

        //todo set msg from ucube pay req
        try {
            ResourceBundle msgBundle = new PropertyResourceBundle(context.getResources().openRawResource(R.raw.ucube_strings));
            paymentHelper.setMsgBundle(msgBundle);

        } catch (IOException ignore) {

            Bundle altMsgBundle = new Bundle();
            altMsgBundle.putString("LBL_wait", "Please wait");
            altMsgBundle.putString("LBL_wait_legacy", "Please wait");
            altMsgBundle.putString("LBL_wait_card_ok", "Please wait");
            altMsgBundle.putString("LBL_approved", "Approved");
            altMsgBundle.putString("LBL_declined", "Declined");
            altMsgBundle.putString("LBL_use_chip", "Use Chip");
            altMsgBundle.putString("LBL_authorization", "Authorization");
            altMsgBundle.putString("LBL_pin_prompt", "{0} {1}\nEnter PIN");
            altMsgBundle.putString("LBL_no_card_detected", "No card detected");
            altMsgBundle.putString("LBL_remove_card", "Remove card");
            altMsgBundle.putString("LBL_unsupported_card", "Unsupported card");
            altMsgBundle.putString("LBL_refused_card", "Card refused");
            altMsgBundle.putString("LBL_cancelled", "Cancelled");
            altMsgBundle.putString("LBL_try_other_interface", "Try other interface");
            altMsgBundle.putString("LBL_cfg_error", "Config error");
            altMsgBundle.putString("MSG_wait_card", "{0} {1}\nInsert card");
            altMsgBundle.putString("GLOBAL_centered", "FF");
            altMsgBundle.putString("GLOBAL_yposition", "0C");
            altMsgBundle.putString("GLOBAL_font_id", "00");

            paymentHelper.setAltMsgBundle(altMsgBundle);
        }

        paymentHelper.addObserver((o, arg) -> {
            if (o instanceof PaymentHelper) {

                final PaymentContext paymentContext = (PaymentContext) arg;

                switch (paymentContext.getPaymentStatus()) {
                    case STARTED:
                    case AUTHORIZE:
                    case DEFAULT_INIT:
                        break;

                    case ENTER_SECURE_SESSION:
                        uCubePaymentListener.onStart(paymentContext.getKsn());
                        break;

                    default:
                        UCubePaymentResponse uCubePaymentResponse = new UCubePaymentResponse(
                                getCardLabel(paymentContext.getActivatedReader()),
                                paymentContext,
                                ContextDataManager.getInstance().getUCubeName(),
                                ContextDataManager.getInstance().getUCubeAddress(),
                                ContextDataManager.getInstance().getUCubePartNumber(),
                                ContextDataManager.getInstance().getUCubeSerial());

                        uCubePaymentListener.onFinish(true, uCubePaymentResponse);
                        break;
                }


            }
        });

        paymentHelper.startPayment();
    }

    private static String getCardLabel(byte cardType) {
        switch (cardType) {
            case Constants.MS_READER:
                return "MagStripe";

            case Constants.ICC_READER:
                return "SmartCard";

            case Constants.NFC_READER:
                return "NFC";

            default:
                LogManager.e("Card type (" + cardType + ") is unknown.");
                return "BAD_CARD_TYPE";
        }
    }

}
