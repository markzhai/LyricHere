package com.markzhai.lyrichere.provider;

import com.raizlabs.android.dbflow.annotation.Database;
import com.raizlabs.android.dbflow.annotation.provider.ContentProvider;

public class LyricDatabase {

    public static final String NAME = "TestDatabase";

    public static final int VERSION = 1;

    public static final String AUTHORITY = "com.markzhai.lyrichere.provider";

    public static final String BASE_CONTENT_URI = "content://";
}
