package com.youtransactor.sampleapp.infrastructure.keyboard;

public enum KeyboardKey {
    ONE("1"),
    TWO("2"),
    THREE("3"),
    FOUR("4"),
    FIVE("5"),
    SIX("6"),
    SEVEN("7"),
    EIGHT("8"),
    NINE("9"),
    ZERO("0"),
    DOUBLE_ZERO("00"),
    F(""),
    DOT(""),
    CANCEL(""),
    CORRECT(""),
    CONFIRM("");

    public final String correspondingValue;

    KeyboardKey(String correspondingValue) {
        this.correspondingValue = correspondingValue;
    }
}
