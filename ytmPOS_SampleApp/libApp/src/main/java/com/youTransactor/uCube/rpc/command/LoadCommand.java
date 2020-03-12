/**
 * Copyright (C) 2011-2016, YouTransactor. All Rights Reserved.
 * <p>
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youTransactor.uCube.rpc.command;

import com.youTransactor.uCube.rpc.Constants;
import com.youTransactor.uCube.rpc.RPCCommand;
import com.youTransactor.uCube.rpc.RPCMessage;

import java.util.LinkedList;
import java.util.List;

/**
 * @author gbillard on 4/8/16.
 */
public class LoadCommand extends RPCCommand {

	private List<byte[]> blockList;

	public LoadCommand() {
		super(Constants.LOAD_COMMAND);
	}

	public LoadCommand(List<byte[]> blockList) {
		this();
		setBlockList(blockList);
	}

	@Override
	public void processMessage(RPCMessage message) {
		if (message == null || message.getStatus() != Constants.SUCCESS_STATUS || blockList == null || blockList.isEmpty()) {
			super.processMessage(message);
			return;
		}

		/* process next block */
		payload = null;

		start();
	}

	public void setBlockList(List<byte[]> blockList) {
		this.blockList = new LinkedList<>(blockList);
	}

	@Override
	protected byte[] createPayload() {
		if (blockList == null || blockList.isEmpty()) {
			return new byte[0];
		}

		byte[] block = blockList.remove(0);

		/* last block flag = 1byte, block length = 2bytes */
		byte[] data = new byte[block.length + 3];

		data[0] = (byte) (blockList.isEmpty() ? 0x01 : 0x00);

		data[1] = (byte) (block.length / 0x100);
		data[2] = (byte) (block.length % 0x100);

		System.arraycopy(block, 0, data, 3, block.length);

		return data;
	}

}
