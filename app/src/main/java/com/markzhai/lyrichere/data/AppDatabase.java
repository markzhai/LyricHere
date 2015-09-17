package com.markzhai.lyrichere.data;

import com.raizlabs.android.dbflow.annotation.Database;

@Database(name = AppDatabase.NAME, version = AppDatabase.VERSION, foreignKeysSupported = true)
public class AppDatabase {

    public static final String NAME = "App";

    public static final int VERSION = 2;

    public static final String AUTHORITY = "com.markzhai.lyrichere.provider";

    public static final String BASE_CONTENT_URI = "content://";
}