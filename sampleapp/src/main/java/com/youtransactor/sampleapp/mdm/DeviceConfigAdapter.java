/*
 * Copyright (C) 2011-2021, YouTransactor. All Rights Reserved.
 *
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youtransactor.sampleapp.mdm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.youTransactor.uCube.mdm.Config;
import com.youtransactor.sampleapp.R;

//import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;


public class DeviceConfigAdapter extends BaseExpandableListAdapter {

	private List<Config> configList;

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
