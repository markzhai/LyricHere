package com.markzhai.lyrichere.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.media.MediaMetadata;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A collection of utility methods, all static from CastCompanionLibrary.
 */
public class Utils {

    private static final String TAG = LogHelper.makeLogTag(Utils.class);
    private static final String KEY_IMAGES = "images";
    private static final String KEY_URL = "movie-urls";
    private static final String KEY_CONTENT_TYPE = "content-type";
    private static final String KEY_STREAM_TYPE = "stream-type";
    private static final String KEY_STREAM_DURATION = "stream-duration";
    private static final String KEY_CUSTOM_DATA = "custom-data";
    private static final String KEY_TRACK_ID = "track-id";
    private static final String KEY_TRACK_CONTENT_ID = "track-custom-id";
    private static final String KEY_TRACK_NAME = "track-name";
    private static final String KEY_TRACK_TYPE = "track-type";
    private static final String KEY_TRACK_SUBTYPE = "track-subtype";
    private static final String KEY_TRACK_LANGUAGE = "track-language";
    private static final String KEY_TRACK_CUSTOM_DATA = "track-custom-data";
    private static final String KEY_TRACKS_DATA = "track-data";
    public static final boolean IS_KITKAT_OR_ABOVE = Build.VERSION.SDK_INT
            >= Build.VERSION_CODES.KITKAT;

    /**
     * Formats time in milliseconds to hh:mm:ss string format.
     *
     * @param millis
     * @return
     */
    public static String formatMillis(int millis) {
        String result = "";
        int hr = millis / 3600000;
        millis %= 3600000;
        int min = millis / 60000;
        millis %= 60000;
        int sec = millis / 1000;
        if (hr > 0) {
            result += hr + ":";
        }
        if (min >= 0) {
            if (min > 9) {
                result += min + ":";
            } else {
                result += "0" + min + ":";
            }
        }
        if (sec > 9) {
            result += sec;
        } else {
            result += "0" + sec;
        }
        return result;
    }

    /**
     * Shows a (long) toast.
     *
     * @param context
     * @param resourceId
     */
    public static void showToast(Context context, int resourceId) {
        Toast.makeText(context, context.getString(resourceId), Toast.LENGTH_LONG).show();
    }

    /**
     * Saves a string value under the provided key in the preference manager. If <code>value</code>
     * is <code>null</code>, then the provided key will be removed from the preferences.
     *
     * @param context
     * @param key
     * @param value
     */
    public static void saveStringToPreference(Context context, String key, String value) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        if (null == value) {
            // we want to remove
            pref.edit().remove(key).apply();
        } else {
            pref.edit().putString(key, value).apply();
        }
    }

    /**
     * Saves a float value under the provided key in the preference manager. If <code>value</code>
     * is <code>Float.MIN_VALUE</code>, then the provided key will be removed from the preferences.
     *
     * @param context
     * @param key
     * @param value
     */
    public static void saveFloatToPreference(Context context, String key, float value) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        if (Float.MIN_VALUE == value) {
            // we want to remove
            pref.edit().remove(key).apply();
        } else {
            pref.edit().putFloat(key, value).apply();
        }

    }

    /**
     * Saves a long value under the provided key in the preference manager. If <code>value</code>
     * is <code>Long.MIN_VALUE</code>, then the provided key will be removed from the preferences.
     *
     * @param context
     * @param key
     * @param value
     */
    public static void saveLongToPreference(Context context, String key, long value) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        if (Long.MIN_VALUE == value) {
            // we want to remove
            pref.edit().remove(key).apply();
        } else {
            pref.edit().putLong(key, value).apply();
        }

    }

    /**
     * Saves a boolean value under the provided key in the preference manager. If <code>value</code>
     * is <code>null</code>, then the provided key will be removed from the preferences.
     *
     * @param context
     * @param key
     * @param value
     */
    public static void saveBooleanToPreference(Context context, String key, Boolean value) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        if (value == null) {
            // we want to remove
            pref.edit().remove(key).apply();
        } else {
            pref.edit().putBoolean(key, value).apply();
        }
    }

    /**
     * Retrieves a String value from preference manager. If no such key exists, it will return
     * <code>null</code>.
     *
     * @param context
     * @param key
     * @return
     */
    public static String getStringFromPreference(Context context, String key) {
        return getStringFromPreference(context, key, null);
    }

    /**
     * Retrieves a String value from preference manager. If no such key exists, it will return
     * <code>defaultValue</code>.
     *
     * @param context
     * @param key
     * @param defaultValue
     * @return
     */
    public static String getStringFromPreference(Context context, String key, String defaultValue) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(key, defaultValue);
    }

    /**
     * Retrieves a float value from preference manager. If no such key exists, it will return
     * <code>Float.MIN_VALUE</code>.
     *
     * @param context
     * @param key
     * @return
     */
    public static float getFloatFromPreference(Context context, String key) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getFloat(key, Float.MIN_VALUE);
    }

    /**
     * Retrieves a boolean value from preference manager. If no such key exists, it will return the
     * value provided as <code>defaultValue</code>
     *
     * @param context
     * @param key
     * @param defaultValue
     * @return
     */
    public static boolean getBooleanFromPreference(Context context, String key,
                                                   boolean defaultValue) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(key, defaultValue);
    }

    /**
     * Retrieves a long value from preference manager. If no such key exists, it will return the
     * value provided as <code>defaultValue</code>
     *
     * @param context
     * @param key
     * @param defaultValue
     * @return
     */
    public static long getLongFromPreference(Context context, String key,
                                             long defaultValue) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getLong(key, defaultValue);
    }

    /**
     * Returns the SSID of the wifi connection, or <code>null</code> if there is no wifi.
     */
    public static String getWifiSsid(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (null != wifiInfo) {
            return wifiInfo.getSSID();
        }
        return null;
    }

    /**
     * Scale and center-crop a bitmap to fit the given dimensions.
     */
    public static Bitmap scaleCenterCrop(Bitmap source, int newHeight, int newWidth) {
        if (source == null) {
            return null;
        }
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();

        float xScale = (float) newWidth / sourceWidth;
        float yScale = (float) newHeight / sourceHeight;
        float scale = Math.max(xScale, yScale);

        float scaledWidth = scale * sourceWidth;
        float scaledHeight = scale * sourceHeight;

        float left = (newWidth - scaledWidth) / 2;
        float top = (newHeight - scaledHeight) / 2;

        RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);

        Bitmap destination = Bitmap.createBitmap(newWidth, newHeight, source.getConfig());
        Canvas canvas = new Canvas(destination);
        canvas.drawBitmap(source, null, targetRect, null);

        return destination;
    }
}