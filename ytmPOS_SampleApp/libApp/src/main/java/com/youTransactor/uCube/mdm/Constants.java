/**
 * Copyright (C) 2011-2016, YouTransactor. All Rights Reserved.
 * <p/>
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youTransactor.uCube.mdm;

/**
 * @author gbillard on 4/4/16.
 */
public class Constants {

	private Constants() {}

	public static final String JSON_RESPONSE_DATA_FIELD = "data";
	public static final String JSON_RESPONSE_STATUS_FIELD = "status";
	public static final String JSON_RESPONSE_STATUS_SUCCESS = "ok";

	public static final String JSON_CONFIG_FIELD = "config";

	public static final String JSON_TYPE_FIELD = "type";
	public static final String JSON_LABEL_FIELD = "label";
	public static final String JSON_CIPHERED_FIELD = "ciphered";
	public static final String JSON_MIN_VERSION_FIELD = "min_version";
	public static final String JSON_CURRENT_VERSION_FIELD = "current_version";

	public final static int SVPP_FIRMWARE_TYPE = 0;
	public final static int ST_FIRMWARE_TYPE = 3;
	public final static int ICC_CONFIG_TYPE = 4;
	public final static int NFC_CONFIG_TYPE = 5;

}
