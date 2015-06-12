package com.markzhai.lyrichere.utils;

/**
 * Created by yifan on 6/3/14.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.markzhai.lyrichere.Constants;

public class DbHelper extends SQLiteOpenHelper {
    private static final String TAG = DbHelper.class.getSimpleName();

    public DbHelper(Context context) {
        super(context, Constants.DB_NAME, null, Constants.DB_VERSION);
    }

    // DROP TABLE IF EXISTS lyric;
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = String.format("create table %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "%s TEXT, %s TEXT, %s TEXT, %s INT, %s TEXT, %s TEXT, %s INT, %s INT)",
                Constants.TABLE,
                Constants.Column.ID,
                Constants.Column.TITLE,
                Constants.Column.ARTIST,
                Constants.Column.ALBUM,
                Constants.Column.LENGTH,
                Constants.Column.PATH,
                Constants.Column.ENCODING,
                Constants.Column.ENCODING_CHANGED,
                Constants.Column.LAST_VISITED_AT);
        LogUtils.i(TAG, "onCreate with SQL: " + sql);
        db.execSQL(sql);
    }

    // Gets called whenever existing version != new version, i.e. schema changed
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Typically you do ALTER TABLE ...
        db.execSQL("drop table if exists " + Constants.TABLE);
        onCreate(db);
    }
}