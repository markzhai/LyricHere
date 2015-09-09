package com.markzhai.lyrichere.utils;

import android.content.res.ColorStateList;
import android.os.Build;
import android.widget.ImageView;

/**
 * Lollipop utils.
 * <p/>
 * Created by markzhai on 2015/9/9.
 */
public final class LollipopUtils {

    private LollipopUtils() {
    }

    public static void setImageTintList(ImageView imageView, ColorStateList colorStateList) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imageView.setImageTintList(colorStateList);
        }
    }
}

