/*
 * ============================================================================
 *
 * Copyright (c) 2022 YouTransactor
 *
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of YouTransactor
 * ("Confidential Information"). You  shall not disclose or redistribute such
 * Confidential Information and shall use it only in accordance with the terms of
 * the license agreement you entered into with YouTransactor.
 *
 * This software is provided by YouTransactor AS IS, and YouTransactor
 * makes no representations or warranties about the suitability of the software,
 * either express or implied, including but not limited to the implied warranties
 * of merchantability, fitness for a particular purpose or non-infringement.
 * YouTransactor shall not be liable for any direct, indirect, incidental,
 * special, exemplary, or consequential damages suffered by licensee as the
 * result of using, modifying or distributing this software or its derivatives.
 *
 * ==========================================================================
 */
package com.youtransactor.sampleapp.rpc;

import static com.youTransactor.uCube.rpc.Constants.QUICK_MODE;
import static com.youTransactor.uCube.rpc.Constants.SLOW_MODE;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import com.youTransactor.uCube.rpc.DeviceInfos;
import com.youtransactor.sampleapp.R;
import com.youtransactor.sampleapp.YTProduct;

public class FragmentDialogGetInfo extends DialogFragment {

    private final DeviceInfos deviceInfos;
    private final YTProduct ytProduct;

    public FragmentDialogGetInfo(DeviceInfos deviceInfos, YTProduct ytProduct) {
        this.deviceInfos = deviceInfos;
        this.ytProduct = ytProduct;
    }

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder b = new AlertDialog.Builder(getActivity())
                .setTitle("uCube Information")
                .setPositiveButton("ok", (dialog, whichButton) -> dialog.dismiss());

        setCancelable(false);

        View rootView = getActivity().getLayoutInflater().inflate(R.layout.fragment_get_info, null);

        //UI
        TextView terminalSerialNumber = rootView.findViewById(R.id.terminal_serial_number);
        TextView terminalState = rootView.findViewById(R.id.terminal_state);
        TextView batteryState = rootView.findViewById(R.id.battery_state);
        TextView svppVersion = rootView.findViewById(R.id.SVPP_version);
        TextView constFileVersion = rootView.findViewById(R.id.const_file);
        TextView additionalFileContactVersion = rootView.findViewById(R.id.additional_file_contact);
        TextView additionaFileClessVersion = rootView.findViewById(R.id.additional_file_contactless);
        TextView nonSecureFWVersion = rootView.findViewById(R.id.non_secure_fw_version);
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
        TextView bleFirmwareVersion = rootView.findViewById(R.id.ble_version);
        TextView resourcesVersion = rootView.findViewById(R.id.resources_file_version);
        TextView merchantLocale = rootView.findViewById(R.id.merchant_locale);
        TextView supportedLocaleList = rootView.findViewById(R.id.supported_locale_list);
        TextView chargingState = rootView.findViewById(R.id.charging_state);
        TextView speedMode = rootView.findViewById(R.id.speed_mode);
        TextView buildConfiguration = rootView.findViewById(R.id.build_configuration);
        TextView enhancedSREDConfig = rootView.findViewById(R.id.enhanced_sred_config);
        TextView nfcCardDetectConfig = rootView.findViewById(R.id.nfc_card_detect);
        TextView nsfLogs = rootView.findViewById(R.id.nsf_logs);

        if(ytProduct == YTProduct.uCubeTouch) {
            rootView.findViewById(R.id.nfc_section).setVisibility(View.GONE);

        } else if (ytProduct == YTProduct.uCube) {
            rootView.findViewById(R.id.nfc_section).setVisibility(View.VISIBLE);

            NFCModuleState.setText(nfcState(deviceInfos.getNfcModuleState()));

            if (deviceInfos.getNfcModuleState() != 0) {
                NFCFirmwareState.setText(deviceInfos.getNfcFirmwareState());
                NFCFirmwareVersion.setText(deviceInfos.getNfcFirmware());
                EMVL1_CLESS_LIB_VERSION.setText(deviceInfos.getEmvL1ClessLibVersion());
            } else {
                NFCCapability.setVisibility(View.GONE);
                NFCFirmwareState.setVisibility(View.GONE);
                NFCFirmwareVersion.setVisibility(View.GONE);
                EMVL1_CLESS_LIB_VERSION.setVisibility(View.GONE);
            }

        }

        terminalSerialNumber.setText(deviceInfos.getSerial());

        if(deviceInfos.getTerminalState() != null)
            terminalState.setText(deviceInfos.getTerminalState());
        if(deviceInfos.getSvppFirmware() != null)
            svppVersion.setText( deviceInfos.getSvppFirmware());
        if(deviceInfos.getConstFileVersion() != null)
            constFileVersion.setText( deviceInfos.getConstFileVersion());
        if(deviceInfos.getContactAdditionalFileVersion() != null)
            additionalFileContactVersion.setText( deviceInfos.getContactAdditionalFileVersion());
        if(deviceInfos.getContactlessAdditionalFileVersion() != null)
            additionaFileClessVersion.setText(deviceInfos.getContactlessAdditionalFileVersion());
        if(deviceInfos.getNonSecureFirmwareVersion() != null)
            nonSecureFWVersion.setText(deviceInfos.getNonSecureFirmwareVersion());

        partNumber.setText(deviceInfos.getPartNumber());
        OSVersion.setText(deviceInfos.getOsVersion());

        if(deviceInfos.getBleFirmwareVersion()== null) {
            rootView.findViewById(R.id.ble_section).setVisibility(View.GONE);
        }else {
            rootView.findViewById(R.id.ble_section).setVisibility(View.VISIBLE);
            bleFirmwareVersion.setText(deviceInfos.getBleFirmwareVersion());
        }

        if(deviceInfos.getResourcesVersion() == null) {
            rootView.findViewById(R.id.resource_file_section).setVisibility(View.GONE);
        }else {
            rootView.findViewById(R.id.resource_file_section).setVisibility(View.VISIBLE);
            resourcesVersion.setText(deviceInfos.getResourcesVersion());
        }

        if(deviceInfos.getIccEmvConfigVersion() != null)
            emvConfigurationVersion.setText(deviceInfos.getIccEmvConfigVersion());
        if(deviceInfos.getNfcEmvConfigVersion() != null)
            emvClessConfigurationVersion.setText(deviceInfos.getNfcEmvConfigVersion());

        USBCapability.setText(deviceInfos.isUsbCapability() ? "USB Capability" : "NO USB Capability");

        if(deviceInfos.getBatteryState() >= 0)
            batteryState.setText(deviceInfos.getBatteryState()+ "%");

        if(deviceInfos.getAutoPowerOffTimeout() >= 0)
            automaticPowerOffTimeOut.setText(deviceInfos.getAutoPowerOffTimeout() + " Sec");

        if(deviceInfos.getMerchantLocale() != null) {
            String tmp = deviceInfos.getMerchantLocale();
            merchantLocale.setText(tmp);
        }

        if(deviceInfos.getSpeedMode() == SLOW_MODE) {
            speedMode.setText("SLOW MODE");
        } else if(deviceInfos.getSpeedMode() == QUICK_MODE) {
            speedMode.setText("QUICK MODE");
        }

        if(deviceInfos.getSupportedLocaleList() != null && !deviceInfos.getSupportedLocaleList().isEmpty()) {
            StringBuilder locales = new StringBuilder();
            for(String locale : deviceInfos.getSupportedLocaleList()) {
                locales.append(locale).append(", ");
            }

            supportedLocaleList.setText(locales.toString());
        }

        switch (deviceInfos.getChargingStatus()) {
            case 0x00:
                chargingState.setText("unplugged");
                break;

            case 0x01:
                chargingState.setText("plugged");
                break;

            case 0x03:
                chargingState.setText("Battery is Full");
                break;
        }

        if(deviceInfos.getBuildConfiguration() != null) {
            buildConfiguration.setText(deviceInfos.getBuildConfiguration());
        }

        if(deviceInfos.getNfcCardDetectConfiguration()  >= 0) {
            switch (deviceInfos.getNfcCardDetectConfiguration()) {
                case 0 :
                    nfcCardDetectConfig.setText("NFC card detect OFF");
                    break;
                case 1:
                    nfcCardDetectConfig.setText("NFC card detect ON");
                    break;
            }
        }

        if(deviceInfos.isEnhancedSredOn())
            enhancedSREDConfig.setText("enhanced SRED ON");
        else
            enhancedSREDConfig.setText("SRED is NOT enhanced");

        if(deviceInfos.getNsfLogs() != null && deviceInfos.getNsfLogs().length > 0) {
            nsfLogs.setText("Available");
        }

        b.setView(rootView);
        return b.create();
    }

    private String nfcState(byte nfcModuleState) {
        String nfcStateLabel ="";

        switch (nfcModuleState) {
            case 0x00:
                nfcStateLabel = "no mpos module available";
                break;
            case 0x01:
                nfcStateLabel = "mpos module initializing";
                break;
            case 0x02:
                nfcStateLabel = "mpos initialization done";
                break;
            case 0x03:
                nfcStateLabel = "mpos module is ready";
                break;
            case 0x04:
                nfcStateLabel = "mpos module is in bootloader mode";
                break;
            case 0x05:
                nfcStateLabel = "mpos bootloader initializing";
                break;
            case 0x06:
                nfcStateLabel = "mpos module faces an internal error";
                break;
            case 0x07:
                nfcStateLabel = "mpos module firmware not loaded";
                break;
            case 0x08:
                nfcStateLabel = "mpos firmware update is ongoing";
                break;
            case 0x09:
                nfcStateLabel = "mpos app firmware is corrupted";
                break;
            case 0x10:
                nfcStateLabel = "mpos bootloader firmware is corrupted";
                break;
        }

        return nfcStateLabel;
    }

}

