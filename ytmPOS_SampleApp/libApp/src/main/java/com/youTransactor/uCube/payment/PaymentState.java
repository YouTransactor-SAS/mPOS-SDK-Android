/*
 * Copyright (C) 2016, YouTransactor. All Rights Reserved.
 *
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youTransactor.uCube.payment;

/**
 * @author gbillard on 5/12/16.
 */
public enum PaymentState {
	DEFAULT_INIT,
	GET_PN_ERROR,
	GET_MPOS_STATE_ERROR,
	TRANSACTION_MODE_ERROR,
	RISK_MANAGEMENT_TASK_NULL_ERROR,
	AUTHORIZATION_TASK_NULL_ERROR,
	DEVICE_TYPE_ERROR,
	NFC_MPOS_ERROR,
	CARD_WAIT_FAILED,
	CANCELLED,
	STARTED,
	ENTER_SECURE_SESSION,
	CARD_REMOVED,
	CHIP_REQUIRED,
	UNSUPPORTED_CARD,
	TRY_OTHER_INTERFACE,
	REFUSED_CARD,
	ERROR,
	AUTHORIZE,
	APPROVED,
	DECLINED;
}
