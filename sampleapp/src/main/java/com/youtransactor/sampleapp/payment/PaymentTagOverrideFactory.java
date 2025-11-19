package com.youtransactor.sampleapp.payment;

import static com.youTransactor.uCube.rpc.Constants.EMVTag.TAG_9F33_TERMINAL_CAPABILITIES;
import static com.youTransactor.uCube.rpc.Constants.EMVTag.TAG_ALCINEO_MCL_READER_CVM_LIMIT;
import static com.youTransactor.uCube.rpc.Constants.EMVTag.TAG_ALCINEO_READER_CVM_REQ_LIMIT;

import com.youTransactor.uCube.Tools;

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
}
