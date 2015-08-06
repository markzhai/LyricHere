package com.markzhai.lyrichere;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by yifan on 5/17/14.
 */
public class Constants {
    // DB specific constants
    public static final String DB_NAME = "lyric.db";
    public static final int DB_VERSION = 4;
    public static final String TABLE = "lyric";

    // Notification
    public static final int NOTIFY_ID = 5656;

    // Provider specific constants
    public static final String AUTHORITY = "markzhai.lyrichere.app.db.LyricContentProvider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE);
    public static final int LYRIC_ITEM = 1;
    public static final int LYRIC_DIR = 2;
    public static final String LYRIC_TYPE_ITEM =
            "vnd.android.cursor.item/vnd.markzhai.provider.lyric";
    public static final String LYRIC_TYPE_DIR =
            "vnd.android.cursor.dir/vnd.markzhai.provider.lyric";
    public static final String DEFAULT_SORT = Column.ID + " DESC";
    public static final String RECENT_SORT = Column.LAST_VISITED_AT + " DESC";
    public static final String TITLE_SORT = Column.TITLE + " ASC";
    public static final String ENCODE_UTF_8 = "UTF-8";
    public static final String ENCODE_BIG5 = "Big5";
    public static final String ENCODE_GBK = "GBK";
    public static final String ENCODE_SJIS = "MS932";

    public class Column {
        public static final String ID = BaseColumns._ID;
        public static final String TITLE = "title";
        public static final String ARTIST = "artist";
        public static final String ALBUM = "album";
        public static final String LENGTH = "length";
        public static final String PATH = "path";
        public static final String ENCODING = "encode";
        public static final String ENCODING_CHANGED = "encode_changed";
        public static final String LAST_VISITED_AT = "last_visited_at";
    }
}