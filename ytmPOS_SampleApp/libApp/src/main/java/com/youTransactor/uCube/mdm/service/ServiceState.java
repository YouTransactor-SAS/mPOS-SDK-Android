/**
 * Copyright (C) 2011-2016, YouTransactor. All Rights Reserved.
 * <p/>
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youTransactor.uCube.mdm.service;

/**
 * @author gbillard on 4/25/16.
 */
public enum ServiceState {
	IDLE,
	RETRIEVE_DEVICE_INFOS,
	RETRIEVE_DEVICE_CERTIFICAT,
	REGISTER_DEVICE,
	RETRIEVE_DEVICE_CONFIG,
	CHECK_UPDATE,
	DOWNLOAD_BINARY,
	UPDATE_DEVICE,
	SEND_LOGS,
	RECONNECT
}
