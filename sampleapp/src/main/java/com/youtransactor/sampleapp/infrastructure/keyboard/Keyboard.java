package com.youtransactor.sampleapp.infrastructure.keyboard;

import java.util.function.Consumer;

public interface Keyboard {

    void subscribe(final Consumer<KeyboardKey> keyConsumer);

}
