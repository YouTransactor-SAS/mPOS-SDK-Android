/*
 * Copyright (C) 2011-2020, YouTransactor. All Rights Reserved.
 * <p/>
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youtransactor.sampleapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import com.youTransactor.uCube.rpc.DeviceInfos;

import java.util.Objects;

public class FragmentDialogGetInfo extends DialogFragment {

    private DeviceInfos deviceInfos;

    public FragmentDialogGetInfo(DeviceInfos deviceInfos) {
        this.deviceInfos = deviceInfos;
    }

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder b = new AlertDialog.Builder(getActivity())
                .setTitle("uCube Informations")
                .setPositiveButton("ok", (dialog, whichButton) -> dialog.dismiss());

        setCancelable(false);

        View rootView = Objects.requireNonNull(getActivity())
                .getLayoutInflater().inflate(R.layout.fragment_get_info, null);

        //UI
        TextView terminalSerialNumber = rootView.findViewById(R.id.terminal_serial_number);
        TextView terminalState = rootView.findViewById(R.id.terminal_state);
        TextView batteryState = rootView.findViewById(R.id.battery_state);
        TextView svppVersion = rootView.findViewById(R.id.SVPP_version);
        TextView partNumber = rootView.findViewById(R.id.part_number);
        TextView OSVersion = rootView.findViewById(R.id.OS_version);
        TextView NFCModuleState = rootView.findViewById(R.id.NFC_Module_state);
        TextView emvConfigurationVersion = rootView.findViewById(R.id.emv_configuration_version);
        TextView emvClessConfigurationVersion = rootView.findViewById(R.id.emv_cless_configuration_version);
        TextView automaticPowerOffTimeOut = rootView.findViewById(R.id.automatic_power_off_time_out);
        TextView USBCapability = rootView.findViewById(R.id.USB_capability);
        TextView NFCCapability = rootView.findViewById(R.id.NFC_capability);
        TextView NFCFirmwareState = rootView.findViewById(R.id.NFC_Firmware_State);
        TextView NFCFirmwareVersion = rootView.findViewById(R.id.NFC_Firmware_Version);
        TextView EMVL1_CLESS_LIB_VERSION = rootView.findViewById(R.id.EMVL1_Cell_Lib_Version);

        terminalSerialNumber.setText(deviceInfos.getSerial());
        terminalState.setText(deviceInfos.getTerminalState());

        String batteryStateStr ="";
        switch (deviceInfos.getBatteryState()) {
            case 0x01:
                batteryStateStr = "100%";
                break;
            case 0x02:
                batteryStateStr = "75%";
                break;
            case 0x03:
                batteryStateStr = "50%";
                break;
            case 0x04:
                batteryStateStr = "Bellow 25%";
                break;
        }
        batteryState.setText(batteryStateStr);

        svppVersion.setText(deviceInfos.getSvppFirmware());
        partNumber.setText(deviceInfos.getPartNumber());
        OSVersion.setText(deviceInfos.getOsVersion());

        String nfcModuleState ="";
        switch (deviceInfos.getNfcModuleState()) {
            case 0x00:
                nfcModuleState = "no mpos module available";
                break;
            case 0x01:
                nfcModuleState = "mpos module initializing";
                break;
            case 0x02:
                nfcModuleState = "mpos initialization done";
                break;
            case 0x03:
                nfcModuleState = "mpos module is ready";
                break;
            case 0x04:
                nfcModuleState = "mpos module is in bootloader mode";
                break;
            case 0x05:
                nfcModuleState = "mpos bootloader initializing";
                break;
            case 0x06:
                nfcModuleState = "mpos module faces an internal error";
                break;
            case 0x07:
                nfcModuleState = "mpos module firmware not loaded";
                break;
            case 0x08:
                nfcModuleState = "mpos firmware update is ongoing";
                break;
            case 0x09:
                nfcModuleState = "mpos app firmware is corrupted";
                break;
            case 0x10:
                nfcModuleState = "mpos bootloader firmware is corrupted";
                break;
        }
        NFCModuleState.setText(nfcModuleState);

        emvConfigurationVersion.setText(deviceInfos.getIccEmvConfigVersion());
        emvClessConfigurationVersion.setText(deviceInfos.getNfcEmvConfigVersion());
        automaticPowerOffTimeOut.setText(Integer.toString(deviceInfos.getAutoPowerOffTimeout()));
        USBCapability.setText(deviceInfos.isUsbCapability() ? "USB Capability" : "NO USB Capability");

        if (deviceInfos.getNfcModuleState() != 0) {
            NFCFirmwareState.setText(deviceInfos.getNfcFirmwareState());
            NFCFirmwareVersion.setText(deviceInfos.getNfcFirmware());
            EMVL1_CLESS_LIB_VERSION.setText(deviceInfos.getEmvL1ClessLibVersion());
        } else {
            NFCCapability.setText("NONE");
            NFCFirmwareState.setVisibility(View.GONE);
            NFCFirmwareVersion.setVisibility(View.GONE);
            EMVL1_CLESS_LIB_VERSION.setVisibility(View.GONE);
        }

        b.setView(rootView);
        return b.create();
    }


}

