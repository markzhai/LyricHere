package com.markzhai.lyrichere.utils;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.markzhai.lyrichere.LHApplication;

import java.io.IOException;
import java.io.InputStream;

public class BitmapHelper {

    public static Bitmap scaleBitmap(Bitmap src, int maxWidth, int maxHeight) {
        double scaleFactor = Math.min(
                ((double) maxWidth) / src.getWidth(), ((double) maxHeight) / src.getHeight());
        return Bitmap.createScaledBitmap(src,
                (int) (src.getWidth() * scaleFactor), (int) (src.getHeight() * scaleFactor), false);
    }

    public static Bitmap fetchAndRescaleBitmap(String uri, int width, int height)
            throws IOException {

        ContentResolver res = LHApplication.getContext().getContentResolver();
        InputStream in = res.openInputStream(Uri.parse(uri));

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();

        int actualW = bmOptions.outWidth;
        int actualH = bmOptions.outHeight;

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = Math.min(actualW / width, actualH / height);

        return BitmapFactory.decodeStream(in, null, bmOptions);
    }
}
