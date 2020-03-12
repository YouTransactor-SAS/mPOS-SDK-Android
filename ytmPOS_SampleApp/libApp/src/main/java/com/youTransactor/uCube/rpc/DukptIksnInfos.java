/**
 * Copyright (C) 2011-2017, YouTransactor. All Rights Reserved.
 * <p/>
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youTransactor.uCube.rpc;

import com.youTransactor.uCube.Tools;

/**
 * Created by bfourcade on 13/07/2017.
 */

public class DukptIksnInfos {

    public static final int IDX_IPEK = 0;
    public static final int IDX_IDEK = 1;
    public static final int NB_DUKPT_KEYS = 2;
    private static final int IDX_KSI = 0;
    private static final int IDX_KSN = 1;
    private static final int IDX_KLC = 2;
    private static final int IDX_TSC = 3;
    private static final int IDX_END = -1;
    private byte[] v;
    private String hv;
    private String[] iKSI;
    private String[] iKSN;
    private String[] iKLC;
    private String[] iTSC;

    public DukptIksnInfos() {
    }

    public DukptIksnInfos(byte[] value, String hvalue) {

        this.v = value;
        this.hv = hvalue;
        this.iKSI = new String[NB_DUKPT_KEYS];
        this.iKSN = new String[NB_DUKPT_KEYS];
        this.iKLC = new String[NB_DUKPT_KEYS];
        this.iTSC = new String[NB_DUKPT_KEYS];

        init();
    }

    public String getKsi(int id) {
        return iKSI[id];
    }

    private void init() {

        int i = 0;
        int id;
        int lenbyte;

        // The record start with the id of DUKPY key
        id = this.v[i++];

        // Let's parse it
        while (i < this.v.length) {

            switch (this.v[i]) {

                case IDX_KSI:
                    i++;
                    lenbyte = Tools.getByteLenFromBits(this.v[i]);
                    i++;
                    this.iKSI[id] = this.hv.substring(i * 2, (i + lenbyte) * 2);
                    i += lenbyte;
                    break;

                case IDX_KSN:
                    i++;
                    lenbyte = Tools.getByteLenFromBits(this.v[i]);
                    i++;
                    this.iKSN[id] = this.hv.substring(i * 2, (i + lenbyte) * 2);
                    i += lenbyte;
                    break;

                case IDX_KLC:
                    i++;
                    lenbyte = Tools.getByteLenFromBits(this.v[i]);
                    i++;
                    this.iKLC[id] = this.hv.substring(i * 2, (i + lenbyte) * 2);
                    i += lenbyte;
                    break;

                case IDX_TSC:
                    i++;
                    lenbyte = Tools.getByteLenFromBits(this.v[i]);
                    i++;
                    this.iTSC[id] = this.hv.substring(i * 2, (i + lenbyte) * 2);
                    i += lenbyte;
                    break;

                case IDX_END:
                    /// The record ends with FF
                    i++;
                    if (i < this.v.length - 1) {
                        // Is there an other record ?
                        id = this.v[i];
                    }
                    i++;
                    break;

                default:
                    // In case of problem, this will insure that we get out of the loop at some point
                    i++;
                    break;
            }
        }
    }
}
