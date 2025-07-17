package com.youtransactor.sampleapp.keyboard.adapters;

import static com.youTransactor.uCube.rpc.Event.kbd_release;

import com.youTransactor.uCube.rpc.EventListener;
import com.youTransactor.uCube.rpc.RPCManager;
import com.youTransactor.uCube.rpc.command.event.kbd.EventKbd;
import com.youtransactor.sampleapp.keyboard.Keyboard;
import com.youtransactor.sampleapp.keyboard.KeyboardKey;

import java.util.function.Consumer;

public class PhysicalKeyboardAdapter implements Keyboard {

    private Consumer<KeyboardKey> subscriber;

    private final EventListener rpcEventListener = eventCommand -> {
        if (eventCommand.getEvent() == kbd_release) {
            subscriber.accept(keyFromEvent(((EventKbd) eventCommand)));
        }
    };

    public PhysicalKeyboardAdapter() {
    }


    @Override
    public void subscribe(final Consumer<KeyboardKey> keyConsumer) {
        RPCManager.getInstance().registerSvppEventListener(rpcEventListener);
        subscriber = keyConsumer;
    }

    private KeyboardKey keyFromEvent(final EventKbd event) {
        return switch (event.getKey()) {
            case ZERO -> KeyboardKey.ZERO;
            case ONE -> KeyboardKey.ONE;
            case TWO -> KeyboardKey.TWO;
            case THREE -> KeyboardKey.THREE;
            case FOUR -> KeyboardKey.FOUR;
            case FIVE -> KeyboardKey.FIVE;
            case SIX -> KeyboardKey.SIX;
            case SEVEN -> KeyboardKey.SEVEN;
            case EIGHT -> KeyboardKey.EIGHT;
            case NINE -> KeyboardKey.NINE;
            case CONFIRM -> KeyboardKey.CONFIRM;
            case CANCEL -> KeyboardKey.CANCEL;
            case CORRECT -> KeyboardKey.CORRECT;
            case F -> KeyboardKey.F;
            case DOT -> KeyboardKey.DOT;
        };
    }
}
