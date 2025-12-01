package com.youtransactor.sampleapp.payment;

import static com.youTransactor.uCube.rpc.Constants.EMVTag.*;

import com.youTransactor.uCube.Tools;
import java.io.ByteArrayOutputStream;

public class PaymentTagOverrideFactory {
    public static byte[] getClessCVMReqLimit(byte[] cvmReqValue){
        byte [] ret_ba;
        ret_ba = Tools.appendBytes(new byte[] {
                // CVM limit override for all kernels except Mastercard
                (byte) (TAG_ALCINEO_READER_CVM_REQ_LIMIT >> 8),
                TAG_ALCINEO_READER_CVM_REQ_LIMIT & 0xFF,
                (byte) cvmReqValue.length}, cvmReqValue);
        // CVM limit override for Mastercard only
        ret_ba = Tools.appendBytes(ret_ba, new byte[] {
                (byte) (TAG_ALCINEO_MCL_READER_CVM_LIMIT >> 16),
                (byte) (TAG_ALCINEO_MCL_READER_CVM_LIMIT >> 8),
                (byte) (TAG_ALCINEO_MCL_READER_CVM_LIMIT & 0xFF),
                (byte) cvmReqValue.length});
        ret_ba = Tools.appendBytes(ret_ba, cvmReqValue);
        return ret_ba;
    }

    public static byte[] getContactTerminalCapabilities(byte[] termCapabilities){
        byte [] ret_ba;
        ret_ba = Tools.appendBytes(new byte[] {
                // CVM limit override for all kernels except Mastercard
                (byte) (TAG_9F33_TERMINAL_CAPABILITIES >> 8),
                TAG_9F33_TERMINAL_CAPABILITIES & 0xFF,
                (byte) termCapabilities.length}, termCapabilities);
        return ret_ba;
    }

    public static byte[] getContactPinParam(byte min_digit,
                                            byte max_digit,
                                            int first_digit_to,
                                            int inter_digit_to,
                                            int total_to){

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        if(min_digit > 0) {
            outputStream.write(TAG_NB_MIN_KEYS >> 8);
            outputStream.write(TAG_NB_MIN_KEYS & 0xFF);
            outputStream.write(1);
            outputStream.write(min_digit);
        }
        if(max_digit > 0) {
            outputStream.write(TAG_NB_MAX_KEYS >> 8);
            outputStream.write(TAG_NB_MAX_KEYS & 0xFF);
            outputStream.write(1);
            outputStream.write(max_digit);
        }
        if(first_digit_to > 0) {
            outputStream.write(TAG_FIRST_TO >> 8);
            outputStream.write(TAG_FIRST_TO & 0xFF);
            outputStream.write(2);
            outputStream.write((byte)(first_digit_to / 100));
            outputStream.write((byte)(first_digit_to % 100));
        }
        if(inter_digit_to > 0) {
            outputStream.write(TAG_INTER_TO >> 8);
            outputStream.write(TAG_INTER_TO & 0xFF);
            outputStream.write(2);
            outputStream.write((byte)(inter_digit_to / 100));
            outputStream.write((byte)(inter_digit_to % 100));
        }
        if(total_to > 0) {
            outputStream.write(TAG_TOTAL_TO >> 8);
            outputStream.write(TAG_TOTAL_TO & 0xFF);
            outputStream.write(2);
            outputStream.write((byte)(total_to / 100));
            outputStream.write((byte)(total_to % 100));
        }
        byte [] ret_ba = outputStream.toByteArray();

        return ret_ba;
    }
}
