package com.markzhai.lyrichere.utils;

import android.content.Context;
import android.support.v4.util.LruCache;

import com.markzhai.lyrichere.model.Lyric;

/**
 * Created by yifan on 6/12/14.
 */
public class LyricCache {
    private static final String TAG = LyricCache.class.getSimpleName();
    private static LyricCache sInstance;
    private LruCache<String, Lyric> mLruCache;

    public LyricCache(Context context) {
        init(context);
    }

    public final static LyricCache getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new LyricCache(context.getApplicationContext());
        }
        return sInstance;
    }

    public void init(final Context context) {
        mLruCache = new LruCache<String, Lyric>(50);
    }

    public Lyric get(String key) {
        if (key == null) {
            return null;
        }
        if (mLruCache != null) {
            Lyric lyric = mLruCache.get(key);
            if (lyric != null) {
                return lyric;
            }
        }
        return null;
    }

    public void add(String key, Lyric lyric) {
        if (key == null || lyric == null) {
            return;
        }
        if (get(key) == null) {
            mLruCache.put(key, lyric);
        }
    }

    public void remove(final String key) {
        if (mLruCache != null) {
            mLruCache.remove(key);
        }
    }

    public void clearMemCache() {
        if (mLruCache != null) {
            mLruCache.evictAll();
        }
        System.gc();
    }
}
