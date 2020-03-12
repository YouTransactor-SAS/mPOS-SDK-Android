/*
 * Copyright (C) 2016, YouTransactor. All Rights Reserved.
 *
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youTransactor.uCube.rpc.command;

import com.youTransactor.uCube.rpc.Constants;
import com.youTransactor.uCube.rpc.EMVApplicationDescriptor;
import com.youTransactor.uCube.rpc.RPCCommand;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gbillard on 5/18/16.
 */
public class BuildCandidateListCommand extends RPCCommand {

	private List<EMVApplicationDescriptor> candidateList;

	public BuildCandidateListCommand() {
		super(Constants.BUILD_CANDIDATE_LIST);
	}

	public List<EMVApplicationDescriptor> getCandidateList() {
		return candidateList;
	}

	@Override
	protected boolean parseResponse() {
		int offset = 0;
		byte[] buffer = response.getData();

		candidateList = new ArrayList<>();

		for (int i = buffer[offset++]; i >+ 0; i--) {
			EMVApplicationDescriptor app = EMVApplicationDescriptor.parse(buffer, offset);
			if (app != null) {
				candidateList.add(app);
			}
			offset += Constants.EMV_APPLICATION_CANDIDATE_BLOCK_SIZE;
		}

		return super.parseResponse();
	}

}
