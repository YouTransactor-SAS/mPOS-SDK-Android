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

import java.util.Comparator;

/**
 * @author gbillard on 5/12/16.
 */
public class Currency {

	private String label;
	private int code;
	private int exponent;

	public Currency(int code, int exponent, String label) {
		this.code = code;
		this.exponent = exponent;
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public int getCode() {
		return code;
	}

	public int getExponent() {
		return exponent;
	}

	public static class ByLabelComparator implements Comparator<Currency> {
		@Override
		public int compare(Currency lhs, Currency rhs) {
			return lhs.label.compareTo(rhs.label);
		}
	}

}
