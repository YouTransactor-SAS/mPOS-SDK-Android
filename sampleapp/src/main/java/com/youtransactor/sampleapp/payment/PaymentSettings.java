/*
 * Copyright (c) 2024 JABIL Payment Solution
 *
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of JABIL Payment Solution
 * ("Confidential Information"). You  shall not disclose or redistribute such
 * Confidential Information and shall use it only in accordance with the terms of
 * the license agreement you entered into with JABIL Payment Solution.
 *
 * This software is provided by JABIL Payment Solution AS IS, and JABIL Payment Solution
 * makes no representations or warranties about the suitability of the software,
 * either express or implied, including but not limited to the implied warranties
 * of merchantability, fitness for a particular purpose or non-infringement.
 * JABIL Payment Solution shall not be liable for any direct, indirect, incidental,
 * special, exemplary, or consequential damages suffered by licensee as the
 * result of using, modifying or distributing this software or its derivatives.
 */
package com.youtransactor.sampleapp.payment;

import com.youTransactor.uCube.rpc.OnlinePinBlockFormatType;

public class PaymentSettings {
    public int cardWaitTimeout = 30;
    public boolean forceOnlinePin;
    public OnlinePinBlockFormatType onlinePinBlockFormat;
    public boolean forceAuthorisation;
    public boolean keepSecureSession;
    public boolean forceDebug;
    public boolean isPinBypassAllowed;
    public boolean skipCardRemoval = true;
    public boolean skipStartingSteps = true;
    public boolean retrieveF5Tag;
    public int posEntryMode;
    public int dukpt_key_slot;
    boolean overrideParameter;
    boolean loopMode;
    PaymentFragment.pay_sdse_mode sdse_mode = PaymentFragment.pay_sdse_mode.VOLTAGE;
}