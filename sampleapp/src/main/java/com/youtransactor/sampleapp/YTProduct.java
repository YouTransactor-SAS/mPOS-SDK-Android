/*
 * Copyright (C) 2011-2021, YouTransactor. All Rights Reserved.
 *
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youtransactor.sampleapp;

public enum YTProduct {
    uCube("uCube"),
    uCubeTouch("uCubeTouch");

    private final String name;

    public String getName() {
        return name;
    }

    YTProduct(String name) {
        this.name = name;
    }
}
