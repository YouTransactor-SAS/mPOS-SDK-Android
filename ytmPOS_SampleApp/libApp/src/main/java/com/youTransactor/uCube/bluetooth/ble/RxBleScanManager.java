package com.youTransactor.uCube.bluetooth.ble;

import android.app.Activity;
import android.content.Context;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.polidea.rxandroidble2.LogConstants;
import com.polidea.rxandroidble2.LogOptions;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.exceptions.BleException;
import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanResult;
import com.polidea.rxandroidble2.scan.ScanSettings;
import com.youTransactor.uCube.LogManager;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;

public class RxBleScanManager {

    public interface ScanResultListener {
        void onDeviceSelected(RxBleDevice rxBleDevice);
        void onError(Throwable throwable);
    }

    private RecyclerView recyclerView;
    private RxBleClient rxBleClient;
    private Disposable scanDisposable;
    private ScanResultsAdapter resultsAdapter;
    private ScanResultListener scanResultListener;

    private Context context;
    private Activity activity;

    private RxBleClient setupRxBle(Context context) {
        RxBleClient rxBleClient = RxBleClient.create(context);
        RxBleClient.updateLogOptions(new LogOptions.Builder()
                .setLogLevel(LogConstants.WARN)
                .setMacAddressLogSetting(LogConstants.MAC_ADDRESS_FULL)
                .setUuidsLogSetting(LogConstants.UUIDS_FULL)
                .setShouldLogAttributeValues(true)
                .build()
        );

        RxJavaPlugins.setErrorHandler(throwable -> {
            if (throwable instanceof UndeliverableException && throwable.getCause() instanceof BleException) {
                LogManager.e("Suppressed UndeliverableException: " + throwable.toString());
                return; // ignore BleExceptions as they were surely delivered at least once
            }
            // add other custom handlers if needed
            throw new RuntimeException("Unexpected Throwable in RxJavaPlugins error handler", throwable);
        });

        return rxBleClient;
    }

    public void init(Context context, Activity activity, RecyclerView recyclerView) {
        this.context = context;
        this.activity = activity;

        rxBleClient = setupRxBle(context);

        this.recyclerView = recyclerView;

        configureResultList();
    }

    public void scan(ScanResultListener scanResultListener) {

        this.scanResultListener = scanResultListener;

        scanDisposable = rxBleClient.scanBleDevices(
                new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        .build(),
                new ScanFilter.Builder()
                        //todo add filer name = "uCube-Touch-*"
                        .build()
        )
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(this::dispose)
                .subscribe(resultsAdapter::addScanResult, this::onScanFailure);
    }

    public void stopScan() {
        if (isScanning()) {
            /*
             * Stop scanning in onPause callback.
             */
            scanDisposable.dispose();
            scanResultListener = null;
        }
    }

    private boolean isScanning() {
        return scanDisposable != null;
    }

    private void configureResultList() {

        LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(recyclerLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        resultsAdapter = new ScanResultsAdapter(activity, view -> {
            final int childAdapterPosition = recyclerView.getChildAdapterPosition(view);
            final ScanResult itemAtPosition = resultsAdapter.getItemAtPosition(childAdapterPosition);
            onAdapterItemClick(itemAtPosition);
        });
        recyclerView.setAdapter(resultsAdapter);

    }

    private void onAdapterItemClick(ScanResult scanResults) {
        if(scanResultListener != null)
            scanResultListener.onDeviceSelected(scanResults.getBleDevice());

        stopScan();
    }

    private void onScanFailure(Throwable throwable) {
        if(scanResultListener != null)
            scanResultListener.onError(throwable);
    }

    private void dispose() {
        scanDisposable = null;
    }
}
