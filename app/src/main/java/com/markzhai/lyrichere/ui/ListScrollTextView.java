package com.markzhai.lyrichere.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by yifan on 6/5/14.
 */
public class ListScrollTextView extends TextView {

    public ListScrollTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
        rotate();
    }

    public ListScrollTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        rotate();
    }

    public ListScrollTextView(Context context) {
        super(context);
        init();
        rotate();
    }

    private void rotate() {
        setSelected(true);
    }

    private void init() {
        if (!isInEditMode()) {
        }
    }
}