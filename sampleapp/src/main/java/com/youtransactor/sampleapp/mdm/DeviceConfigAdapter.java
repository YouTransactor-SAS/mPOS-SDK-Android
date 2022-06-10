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
package com.youtransactor.sampleapp.mdm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.youTransactor.uCube.mdm.Config;
import com.youtransactor.sampleapp.R;

import java.util.ArrayList;
import java.util.List;


public class DeviceConfigAdapter extends BaseExpandableListAdapter {

	private final List<Config> configList;

	public DeviceConfigAdapter(List<Config> configList) {
		this.configList = configList != null ? configList : new ArrayList<Config>();
	}

	@Override
	public int getGroupCount() {
		return configList.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return 1;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return configList.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return getGroup(groupPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		Config cfg = configList.get(groupPosition);

		if (convertView == null) {
			LayoutInflater li = LayoutInflater.from(parent.getContext());
			convertView = li.inflate(R.layout.expandable_list_header, null);
		}

		((TextView) convertView.findViewById(R.id.lblListHeader)).setText(cfg.getLabel() != null ? cfg.getLabel() : String.valueOf(cfg.getType()));

		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		Config cfg = configList.get(groupPosition);

		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			convertView = inflater.inflate(R.layout.mdm_device_config_item, null);
		}

		((TextView) convertView.findViewById(R.id.minVersionFld)).setText(cfg.getMinVersion());
		((TextView) convertView.findViewById(R.id.currentVersionFld)).setText(cfg.getCurrentVersion());

		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}

}
