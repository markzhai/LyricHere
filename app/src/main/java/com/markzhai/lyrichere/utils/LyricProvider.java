package com.markzhai.lyrichere.utils;

import android.app.Activity;
import android.content.Context;

import java.io.File;

import com.markzhai.lyrichere.model.Lyric;

/**
 * May add get lyric from internet later
 */
public class LyricProvider {
    private static final String TAG = LyricProvider.class.getSimpleName();
    private static LyricProvider mInstance;
    private Context mContext;
    private LyricCache memCache;

    public LyricProvider(Activity activity) {
        mContext = activity;
        memCache = LyricCache.getInstance(activity);
    }

    public final static LyricProvider getInstance(final Activity activity) {
        if (mInstance == null) {
            mInstance = new LyricProvider(activity);
        }
        return mInstance;
    }

    public Lyric get(String path, String encoding) {
        String key = path + encoding;
        Lyric lyric = memCache.get(key);
        if (lyric == null) {
            lyric = LyricUtils.parseLyric(new File(path), encoding);
            memCache.add(key, lyric);
        }
        return lyric;
    }
}