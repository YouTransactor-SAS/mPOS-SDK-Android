/*
 * Copyright (C) 2020, YouTransactor. All Rights Reserved.
 * <p>
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */

package com.youTransactor.uCube;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author gbillard on 3/10/16.
 */
public class Tools {

    private static final short SVPP_VERSION_LEN = 4;
    private static final short SVPP_PART_NUMBER_LEN = 15;
    private static final short SVPP_SERIAL_NUMBER_LEN = 5;

    private Tools() {
    }

    public static String parseVersion(byte[] data) {
        StringBuffer buffer = new StringBuffer();

        if (data != null) {
            for (int i = 0; i < data.length; i++) {
                if (buffer.length() > 0) {
                    buffer.append('.');
                }

                buffer.append(String.valueOf(data[i]));
            }
        }

        return buffer.toString();
    }

    /**
     * this function transforms the Partnumber from byte[] to String after deleting the first n spaces unused
     *
     * @param buffer the response of GetInfo
     * @return String format decimal or "" if error
     */
    public static String parsePartNumber(byte[] buffer) {
        if (buffer != null && buffer.length == SVPP_PART_NUMBER_LEN) {
            byte[] realPartNumber = new byte[SVPP_PART_NUMBER_LEN];
            try {

                System.arraycopy(buffer, 0, realPartNumber, 0, SVPP_PART_NUMBER_LEN);
                return new String(realPartNumber, "UTF-8")
                        .replaceAll(" ", "");

            } catch (UnsupportedEncodingException ignored) {
            }
        }

        return "";
    }

    /**
     * this function transform the value of serial number from byte[] to proprietary Format added
     * to the url in both methods(GET,POST), first transformation is to int [] then add the int after
     * multiply it with 256 power its index.
     *
     *example: serial number :00 F1 5C AD 88
     * hexString  , Decimal * Factor
     *  (00)           = 00*256^4
     *  (F1)           = 241*256^3
     *  (5C)           = 92*256^2
     *  @Math.pow function to make the power 5^6 Math.pow(5,6)
     *
     *@reference check the aspect page 55 @IDENT.SetInfoProd(02)
     *
     *
     * @param bytes
     * @return "" if error
     */
    public static String parseSerial(byte[] bytes) {
        if (bytes == null || bytes.length != SVPP_SERIAL_NUMBER_LEN) {
            return "";
        }

        int i = 0;
        int m = 256;
        long decimalong = 0;

		/* creation of int array to transform the bytes to int array in format decimal */
        int[] iarray = new int[bytes.length];

        for (byte b : bytes) {
            iarray[i++] = b & 0xFF;
        }

		/* add the int after multiply it with 256 power its index */
        for (int p = 1; p <= SVPP_SERIAL_NUMBER_LEN; ) {
            /* adding all the result to decimallong */
            decimalong += iarray[(SVPP_SERIAL_NUMBER_LEN - p)] * Math.pow(m, (p - 1));
            p++;
        }

		/* divide the decimallong /4 */
        return String.valueOf(decimalong / 4);
    }

    /**
     * makeUnsignedShort
     * To avoid sign issue when adding two bytes
     */
    public static short makeShort(byte MSB, byte LSB) {
        return (short) (((MSB & 0xFF) * 0x100) + (LSB & 0xFF));
    }

    /**
     * makeUnsignedInt
     * To avoid sign issue when adding two bytes
     */
    public static int makeInt(byte MSB, byte LSB) {
        int msbInt = MSB & 0xff;
        int lsbInt = LSB & 0xff;

        if (msbInt < 0)
            msbInt += 0x100;
        if (lsbInt < 0)
            lsbInt += 0x100;

        return (msbInt * 0x100 + lsbInt);
    }

    /**
     * function of transform the byteArray to Hexadecimal String
     * @param bytes wanted to be transform
     * @return String of HexString
     */
    public static String bytesToHex(byte[] bytes) {
        return bytes == null || bytes.length == 0 ? "" : new String(Hex.encodeHex(bytes)).toUpperCase();
    }

    public static byte[] toBCD_double(double value, int length) {
        NumberFormat formatter = new DecimalFormat("#");
        String val = formatter.format(value);

        return hexStringToByteArray(StringUtils.leftPad(val, length * 2, '0'));
    }

    public static double fromBCD_double(byte[] buff) {
        String val = new String(Hex.encodeHex(buff));
        return Double.valueOf(val);
    }

    public static byte[] toBCD(int value, int length) {
        byte[] res = new byte[length];

        for (int i = length - 1; i >= 0; i--) {
            res[i] = (byte) (value % 10);
            value = value / 10;
            res[i] |= (byte) ((value % 10) << 4);
            value = value / 10;
        }

        return res;
    }

    public static int fromBCD(byte[] buff) {
        int res = 0;

        if (buff != null) {
            for (int i = 0; i < buff.length; i++) {
                res *= 10;
                res += ((buff[i] >> 4) & 0xF);
                res *= 10;
                res += (buff[i] & 0xF);
            }
        }

        return res;
    }

    public static byte[] intToByteArray(int value, int size) {
        byte[] res = new byte[size];

        for (int i = size - 1; i >= 0; i--) {
            res[i] = (byte) value;
            value = value >> 8;
        }

        return res;
    }

    /**
     * this function is to convert the Version from string od decimal in format (xxx.xxx.xxx.xxx) to byteArray
     * with the length 4 bytes every byte has a value in hexadecimal in format(AB), The maximum value is (FF)=255
     * and minimum (00)=0.
     * @param version : the String of version
     * @return Byte[] of the version
     * example : (1.10.100.10) =>  {1,A,64,A}
     */

    public static byte[] stringDecimalVersionToHexByteArray(String version) {

        byte versionByteArray[] = new byte[SVPP_VERSION_LEN];

        // look up until dot (.)
        // then put it into byte array
        int offset = 0;
        int i;
        int index;
        String hexStringTemp;
        String intStringTemp;

        // Check string length 0.0.0.0 => 255.255.255.255
        // Count the number of dots

        for (i = 0; i < versionByteArray.length; i++) {

            index = version.indexOf(".", offset);

            // If dot is not found that means we inspect the last element (xxx)
            if (index != -1) {
                intStringTemp = version.substring(offset, index);
                offset = index + 1;
            } else {
                intStringTemp = version.substring(offset);
            }

            hexStringTemp = Integer.toHexString(Integer.parseInt(intStringTemp, 16));
            versionByteArray[i] = Byte.parseByte(hexStringTemp);
        }

        return versionByteArray;
    }


    public static byte[] hexStringToByteArray(String s) {
        try {
            return Hex.decodeHex(s.toCharArray());
        } catch (DecoderException e) {
            return new byte[0];
        }
    }

    public static byte hexStringToByte(String s) {
        byte returnbyte;
        try {
            returnbyte = Hex.decodeHex(s.toCharArray())[0];
        } catch (Exception e) {
            returnbyte = 0x00;
        }
        return returnbyte;
    }

    public static int neg_byte_to_int(byte b) {
        return ((b >= 0) ? b : b + 0x100);
    }

    public static byte intToBcdByte(int i) {
        byte lsb;
        byte msb;

        if (i > 99)
            return (byte) 0xFF;

        lsb = (byte) (i % 10);
        msb = (byte) (i / 10);

        return (byte) (msb * 0x10 + lsb);
    }

    public static int getByteLenFromBits(byte lenbits) {

        int lenbyte;

        lenbyte = (lenbits / 8);

        if ((lenbits % 8) != 0) {
            lenbyte += 1;
        }

        return lenbyte;
    }

    /**
     * code byte array to CRC coded byte array
     *
     * 	 0x00 are replaced by 0x0C
     * 	 0x0A are replaced by 0x0EFA
     * 	 0x08 are replaced by 0x0EF8
     * 	 0x0C are replaced by 0x0EFC
     * 	 0x0D are replaced by 0x0EFD
     * 	 0x0E are replaced by 0x0EFE
     * 	 0x0E0E and 0x0E0F are "EOM - End of message"
     *
     * @param uncoded_buf byte array to code
     * @return CRC coded byte array
     */
    public static byte[] bleCode(byte[] uncoded_buf) {

        List<Byte> coded_buf = new ArrayList<>();

        int i;
        int j = 0;

        for (i = 0; i < uncoded_buf.length; i++) {

            switch (uncoded_buf[i]) {

                case 0x00:
                    coded_buf.add( j++, (byte) 0x0C);
                    break;

                case 0x0A:
                case 0x08:
                case 0x0C:
                case 0x0D:
                case 0x0E:
                    coded_buf.add( j++, (byte) 0x0E);
                    coded_buf.add( j++, (byte)(uncoded_buf[i] + 0xF0));
                    break;

                default:
                    coded_buf.add( j++, uncoded_buf[i]);
                    break;

            }
        }

        coded_buf.add( j++, (byte) 0x0E);
        coded_buf.add( j, (byte) 0x0F);

        return getArray(coded_buf);
    }

    /**
     * decode byte array from CRC code
     *
     * 	 0x0C are replaced by 0x00
     * 	 0x0EFA are replaced by 0x0A
     * 	 0x0EF8 are replaced by 0x08
     * 	 0x0EFC are replaced by 0x0C
     * 	 0x0EFD are replaced by 0x0D
     * 	 0x0EFE are replaced by 0x0E
     * 	 remove x0E0E and 0x0E0F
     *
     * @param coded_buf byte array to decode
     * @return decoded byte array
     */
    public static byte[] bleDecode(byte[] coded_buf) {

        List<Byte> decoded_buf = new ArrayList<>();

        int i;
        int j = 0;


        for (i = 0; i < coded_buf.length; i++) {

            switch (coded_buf[i]) {

                case 0x0C:
                    decoded_buf.add( j++, (byte)0x00);
                    break;

                case 0x0E:
                    if (i+1 < coded_buf.length && (coded_buf[i + 1] != 0x0E)
                            && (coded_buf[i + 1] != 0x0F)) {
                        decoded_buf.add( j++, (byte)(coded_buf[++i] & 0x0F));
                    } else {
                        // skip "0E0E" or "0E0F" patterns
                        i++;
                    }
                    break;

                default:
                    decoded_buf.add( j++, coded_buf[i]);
                    break;

            }

        }
        return getArray(decoded_buf);

    }

    private static byte[] getArray(List<Byte> in) {
        byte[] bytes = new byte[in.size()];
        for (int i = 0; i < in.size(); i++) {
            bytes[i] = in.get(i);
        }

        return bytes;
    }

    public static boolean checkForBluetooth(Context context){
        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        return bluetoothManager != null && bluetoothManager.getAdapter() != null;
    }

    public static boolean checkForBleFeature(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public static boolean isUCubePaired(@NonNull String deviceAddr) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

        final List<BluetoothDevice> deviceList = new ArrayList<>(adapter.getBondedDevices());

        boolean found = false;

        for (BluetoothDevice device : deviceList) {
            if(device.getAddress().equals(deviceAddr))
                found = true;
        }

        return found;
    }
}
