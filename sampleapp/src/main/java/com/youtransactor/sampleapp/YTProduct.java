package com.youtransactor.sampleapp;

public enum YTProduct {
    uCube("uCube"),
    uCubeTouch("uCubeTouch");

    private String name;

    public String getName() {
        return name;
    }

    YTProduct(String name) {
        this.name = name;
    }
}
