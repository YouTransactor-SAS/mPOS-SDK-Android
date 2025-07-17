package com.youtransactor.sampleapp.keyboard.adapters;

import android.content.Context;
import android.view.LayoutInflater;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.youtransactor.sampleapp.R;
import com.youtransactor.sampleapp.keyboard.Keyboard;
import com.youtransactor.sampleapp.keyboard.KeyboardKey;

import java.util.function.Consumer;

public class NumericKeyboardView extends ConstraintLayout implements Keyboard {


    private Consumer<KeyboardKey> subscriber;

    public NumericKeyboardView(Context context) {
        super(context);
        init(context);
    }

    public NumericKeyboardView(Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public NumericKeyboardView(Context context, android.util.AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.numeric_keyboard, this, true);

        findViewById(R.id.button1).setOnClickListener(unused -> subscriber.accept(KeyboardKey.ONE));
        findViewById(R.id.button2).setOnClickListener(unused -> subscriber.accept(KeyboardKey.TWO));
        findViewById(R.id.button3).setOnClickListener(unused -> subscriber.accept(KeyboardKey.THREE));
        findViewById(R.id.button4).setOnClickListener(unused -> subscriber.accept(KeyboardKey.FOUR));
        findViewById(R.id.button5).setOnClickListener(unused -> subscriber.accept(KeyboardKey.FIVE));
        findViewById(R.id.button6).setOnClickListener(unused -> subscriber.accept(KeyboardKey.SIX));
        findViewById(R.id.button7).setOnClickListener(unused -> subscriber.accept(KeyboardKey.SEVEN));
        findViewById(R.id.button8).setOnClickListener(unused -> subscriber.accept(KeyboardKey.EIGHT));
        findViewById(R.id.button9).setOnClickListener(unused -> subscriber.accept(KeyboardKey.NINE));
        findViewById(R.id.button0).setOnClickListener(unused -> subscriber.accept(KeyboardKey.ZERO));
        findViewById(R.id.button00).setOnClickListener(unused -> subscriber.accept(KeyboardKey.DOUBLE_ZERO));
        findViewById(R.id.buttonDel).setOnClickListener(unused -> subscriber.accept(KeyboardKey.CORRECT));
    }

    @Override
    public void subscribe(final Consumer<KeyboardKey> keyConsumer) {
        subscriber = keyConsumer;
    }
}