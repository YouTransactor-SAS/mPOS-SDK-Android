package com.youtransactor.sampleapp.keyboard;

import java.util.function.Consumer;

public interface Keyboard {

    void subscribe(final Consumer<KeyboardKey> keyConsumer);

}
