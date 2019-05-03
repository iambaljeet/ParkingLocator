package com.app.parkinglocator.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

/**
 * Created by Simranjeet on 3/14/2017.
 */

public class TextViewBold extends android.support.v7.widget.AppCompatTextView {

    public TextViewBold(Context context) {
        super(context);
        applyCustomFont(context);
    }

    public TextViewBold(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyCustomFont(context);
    }

    public TextViewBold(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        applyCustomFont(context);
    }

    private void applyCustomFont(Context context) {
        Typeface customFont = FontCache.getTypeface("Roboto_Bold.ttf", context);
        setTypeface(customFont);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
//        if (text.length() > 0) {
//            text = String.valueOf(text.charAt(0)).toUpperCase() + text.subSequence(1, text.length());
//        }
        super.setText(text, type);
    }

}