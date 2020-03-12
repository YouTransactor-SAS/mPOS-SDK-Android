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

import androidx.annotation.NonNull;

import com.youTransactor.uCube.mdm.Config;

import java.util.List;

/**
 * @author gbillard on 4/5/16.
 */
public class BinaryUpdate {

	private Config cfg;
	private boolean mandatory;

	private byte[] signature;
	private byte[] key;
	private List<byte[]> binaryBlock;

	public BinaryUpdate(@NonNull Config cfg, boolean mandatory) {
		this.cfg = cfg;
		this.mandatory = mandatory;
	}

	public Config getCfg() {
		return cfg;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(boolean isMandatory) {
		this.mandatory = isMandatory;
	}

	public List<byte[]> getBinaryBlock() {
		return binaryBlock;
	}

	public void setBinaryBlock(List<byte[]> binaryBlock) {
		this.binaryBlock = binaryBlock;
	}

	public byte[] getSignature() {
		return signature;
	}

	public void setSignature(byte[] signature) {
		this.signature = signature;
	}

	public byte[] getKey() {
		return key;
	}

	public void setKey(byte[] key) {
		this.key = key;
	}
}
