package com.youTransactor.uCube.bluetooth.bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.youTransactor.uCube.LogManager;
import com.youTransactor.uCube.R;
import com.youTransactor.uCube.TaskEvent;
import com.youTransactor.uCube.UIUtils;
import com.youTransactor.uCube.bluetooth.ActivityConnexionListener;
import com.youTransactor.uCube.ContextDataManager;
import com.youTransactor.uCube.rpc.Constants;
import com.youTransactor.uCube.rpc.command.GetInfosCommand;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BtConnexionActivity extends AppCompatActivity {

    private static final int ENABLE_BT_REQUEST_CODE = 4321;

    uCubePairedListAdapter adapter;

    public static ActivityConnexionListener listener;

    private Thread getInfoThread;

    public static void setActivityConnexionListener(ActivityConnexionListener connectionListener){
        listener = connectionListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_ACTION_BAR);

        setContentView(R.layout.activity_connexion);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        if (!bluetoothManager.getAdapter().isEnabled())
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), ENABLE_BT_REQUEST_CODE);
        else
            init();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            try {
                if (getInfoThread != null && getInfoThread.isAlive())
                    getInfoThread.interrupt();
            }catch (Exception e) {
                LogManager.e("error to interrupt Get info thread ", e);
            }

            UIUtils.hideProgressDialog();

            if(listener != null)
                listener.onCancelled();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == ENABLE_BT_REQUEST_CODE) {
            final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

            if (!bluetoothManager.getAdapter().isEnabled()) {
                UIUtils.showOptionDialog(this, getString(R.string.enable_bt_msg),
                        getString(R.string.enable_bt_yes_label),
                        getString(R.string.enable_bt_no_label), (dialog, which) -> {

                            if(which == DialogInterface.BUTTON_POSITIVE)
                                startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), ENABLE_BT_REQUEST_CODE);
                            else
                            if(listener != null) listener.onError();
                        });
            } else
                init();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private List<BluetoothDevice> getPairedUCube() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

        final List<BluetoothDevice> deviceList = new ArrayList<>(adapter.getBondedDevices());
        Collections.sort(deviceList, (lhs, rhs) -> {
            String v1 = lhs.getName();
            if (StringUtils.isBlank(v1)) {
                v1 = lhs.getAddress();
            }

            String v2 = rhs.getName();
            if (StringUtils.isBlank(v1)) {
                v2 = rhs.getAddress();
            }

            return v1.compareToIgnoreCase(v2);
        });

        //todo add filer name = "uCube-*"
        return deviceList;
    }

    private void init() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        List<BluetoothDevice> data = getPairedUCube();

        adapter = new uCubePairedListAdapter(this, data, view -> {
            final int childAdapterPosition = recyclerView.getChildAdapterPosition(view);
            final BluetoothDevice selectedDevice = adapter.getItemAtPosition(childAdapterPosition);

            connectDevice(selectedDevice);
        });

        recyclerView.setAdapter(adapter);
    }

    private void connectDevice(BluetoothDevice device) {

        BluetoothConnexionManager.getInstance().setDeviceAddr(device.getAddress());

        UIUtils.showProgress(this, getString(R.string.connexion_progress));

        getInfoThread = new Thread(() -> new GetInfosCommand(
                Constants.TAG_TERMINAL_SN,
                Constants.TAG_TERMINAL_PN
        ).execute((event, params) -> {
            if (event == TaskEvent.PROGRESS) {
                return;
            }

            runOnUiThread(() -> {

                UIUtils.hideProgressDialog();

                switch (event) {

                    case FAILED:
                        if(listener != null)
                            listener.onError();
                        break;

                    case SUCCESS:
                        //DeviceInfos deviceInfos = new DeviceInfos(((GetInfosCommand) params[0]).getResponseData());

                        ContextDataManager.getInstance().setUCubeName(device.getName());
                        ContextDataManager.getInstance().setUCubeAddress(device.getAddress());

                        if(listener != null)
                            listener.onConnected(device);
                        break;

                    case CANCELLED:
                        if(listener !=null)
                            listener.onCancelled();
                        break;
                }


            });
        }));

        getInfoThread.start();
    }
}
