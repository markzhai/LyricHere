package com.markzhai.lyrichere.ui;

import android.media.browse.MediaBrowser;

/**
 * Provides media content offered by a link MediaBrowserService.
 * <p/>
 * Created by markzhai on 2015/3/19.
 */
public interface MediaBrowserProvider {
    MediaBrowser getMediaBrowser();
}