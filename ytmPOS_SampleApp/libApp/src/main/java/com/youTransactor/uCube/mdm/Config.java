/**
 * Copyright (C) 2011-2016, YouTransactor. All Rights Reserved.
 * <p>
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youTransactor.uCube.mdm;

import com.youTransactor.uCube.LogManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gbillard on 4/3/16.
 */
public class Config {

	private int type;
	private String label;
	private boolean ciphered;
	private String minVersion;
	private String currentVersion;

	public int getType() {
		return type;
	}

	public String getLabel() {
		return label;
	}

	public boolean isCiphered() {
		return ciphered;
	}

	public String getMinVersion() {
		return minVersion;
	}

	public String getCurrentVersion() {
		return currentVersion;
	}

	public static List<Config> fromJson(JSONObject root) {
		try {
			String status = root.getString(Constants.JSON_RESPONSE_STATUS_FIELD);

			if (!Constants.JSON_RESPONSE_STATUS_SUCCESS.equals(status)) {
				return null;
			}

			root = root.getJSONObject(Constants.JSON_RESPONSE_DATA_FIELD);

			List<Config> result = new ArrayList<>();

			JSONArray cfgList = root.getJSONArray(Constants.JSON_CONFIG_FIELD);

			for (int i = 0; i < cfgList.length(); i++) {
				JSONObject jsonCfg = cfgList.optJSONObject(i);
				Config cfg = new Config();

				cfg.type = jsonCfg.getInt(Constants.JSON_TYPE_FIELD);

				cfg.ciphered = jsonCfg.getBoolean(Constants.JSON_CIPHERED_FIELD);

				if (jsonCfg.has(Constants.JSON_LABEL_FIELD)) {
					cfg.label = jsonCfg.getString(Constants.JSON_LABEL_FIELD);
				}

				if (jsonCfg.has(Constants.JSON_MIN_VERSION_FIELD)) {
					cfg.minVersion = jsonCfg.getString(Constants.JSON_MIN_VERSION_FIELD);
				}

				if (jsonCfg.has(Constants.JSON_CURRENT_VERSION_FIELD)) {
					cfg.currentVersion = jsonCfg.getString(Constants.JSON_CURRENT_VERSION_FIELD);
				}

				result.add(cfg);
			}

			return result;

		} catch (Exception e) {
			LogManager.error(Config.class.getSimpleName(), "JSON parse error", e);
		}

		return null;
	}

}
