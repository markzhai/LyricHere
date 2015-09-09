package com.markzhai.lyrichere.ui;

import android.support.v4.media.browse.MediaBrowserCompat;

/**
 * Provides media content offered by a link MediaBrowserService.
 * <p/>
 * Created by markzhai on 2015/3/19.
 */
public interface MediaBrowserProvider {
    MediaBrowserCompat getMediaBrowser();
}