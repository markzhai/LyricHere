package com.markzhai.lyrichere.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.markzhai.lyrichere.Constants;
import com.markzhai.lyrichere.model.Lyric;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yifan on 6/4/14.
 */
public class DbUtils {
    private static final String TAG = DbUtils.class.getName();

    /**
     * Update lyric encoding.
     *
     * @return the number of rows updated.
     */
    public static Cursor searchByKeyword(ContentResolver resolver, String keyword) {
        keyword = keyword + "%";
        return resolver.query(Constants.CONTENT_URI, null,
                "title like ? or artist like ? or album like ?", new String[]{keyword, keyword, keyword}, null);
    }

    /**
     * find lyric by media metadata
     *
     * @return String[] with path at index 0, encoding at index 1
     */
    public static String[] findLyric(ContentResolver resolver, String title, String artist, String album) {
        String[] result = null;
        LogHelper.i(TAG, "findLyric for: " + title);
        Cursor cursor = resolver.query(Constants.CONTENT_URI,
                new String[]{Constants.Column.TITLE, Constants.Column.ARTIST,
                        Constants.Column.ALBUM, Constants.Column.PATH, Constants.Column.ENCODING},
                "title like ?",         // selection
                new String[]{title},    // selectionArgs
                null                    // sortOrder
        );
        if (cursor != null) {
            if (cursor.getCount() > 1) {
                int columnIndexTitle = cursor.getColumnIndex(Constants.Column.TITLE);
                int columnIndexArtist = cursor.getColumnIndex(Constants.Column.ARTIST);
                int columnIndexAlbum = cursor.getColumnIndex(Constants.Column.ALBUM);
                int columnIndexPath = cursor.getColumnIndex(Constants.Column.PATH);
                int columnIndexEncoding = cursor.getColumnIndex(Constants.Column.ENCODING);

                String path = null;
                String encoding = null;

                int resultPriority = 10;

                while (cursor.moveToNext()) {
                    String currTitle = cursor.getString(columnIndexTitle);
                    String currArtist = cursor.getString(columnIndexArtist);
                    String currAlbum = cursor.getString(columnIndexAlbum);
                    if (resultPriority > calculatePriority(title, artist, album, currTitle, currArtist, currAlbum)) {
                        path = cursor.getString(columnIndexPath);
                        encoding = cursor.getString(columnIndexEncoding);
                    }
                }
                return new String[]{path, encoding};
            } else if (cursor.getCount() == 1) {
                cursor.moveToFirst();
                result = new String[]{cursor.getString(cursor.getColumnIndex(Constants.Column.PATH)),
                        cursor.getString(cursor.getColumnIndex(Constants.Column.ENCODING))};
            }
        }
        return result;
    }

    /**
     * Update lyric encoding.
     *
     * @return the number of rows updated.
     */
    public static int updateLyricEncoding(ContentResolver resolver, String rowId, Lyric lyric, String encoding) {
        ContentValues values = new ContentValues();

        values.put(Constants.Column.TITLE, lyric.title);
        values.put(Constants.Column.ARTIST, lyric.artist);
        values.put(Constants.Column.ALBUM, lyric.album);
        values.put(Constants.Column.LENGTH, lyric.length);
        values.put(Constants.Column.ENCODING, encoding);
        values.put(Constants.Column.ENCODING_CHANGED, 1);

        return resolver.update(Constants.CONTENT_URI, values, Constants.Column.ID + "= ?",
                new String[]{rowId});
    }

    /**
     * Update lyric encoding.
     *
     * @return the number of rows updated.
     */
    public static int updateLyricEncoding(ContentResolver resolver, String path, String encoding) {
        Lyric lyric = LyricUtils.parseLyric(new File(path), encoding);
        ContentValues values = new ContentValues();

        values.put(Constants.Column.TITLE, lyric.title);
        values.put(Constants.Column.ARTIST, lyric.artist);
        values.put(Constants.Column.ALBUM, lyric.album);
        values.put(Constants.Column.LENGTH, lyric.length);
        values.put(Constants.Column.ENCODING, encoding);
        values.put(Constants.Column.ENCODING_CHANGED, 1);

        return resolver.update(Constants.CONTENT_URI, values, Constants.Column.PATH + "= ?",
                new String[]{path});
    }

    /**
     * Update lyric encoding.
     *
     * @return the number of rows updated.
     */
    public static int updateLyricLastVisit(ContentResolver resolver, String path, Long time) {
        ContentValues values = new ContentValues();

        values.put(Constants.Column.LAST_VISITED_AT, time);

        return resolver.update(Constants.CONTENT_URI, values, Constants.Column.PATH + "= ?",
                new String[]{path});
    }

    /**
     * Get all non-default encoding lyrics in the form of (path, encoding)
     *
     * @return cursor contains result
     */
    public static Map getNonDefaultEncodingMap(ContentResolver resolver) {
        Cursor cursor = resolver.query(Constants.CONTENT_URI,
                new String[]{Constants.Column.PATH, Constants.Column.ENCODING},
                Constants.Column.ENCODING_CHANGED + "!= ?",
                new String[]{"0"},
                null);

        Map<String, String> pathEncodingMap = new HashMap<String, String>();
        String path, encoding;

        while (cursor.moveToNext()) {
            path = cursor.getString(cursor.getColumnIndex(Constants.Column.PATH));
            encoding = cursor.getString(cursor.getColumnIndex(Constants.Column.ENCODING));
            //Log.d(TAG, path + ": " + encoding);
            if (path != null && encoding != null) {
                pathEncodingMap.put(path, encoding);
            }
        }
        return pathEncodingMap;
    }

    public static ContentValues getLyricContentValue(Lyric lyric, String path, Long time, String encoding) {
        ContentValues values = new ContentValues();

        values.put(Constants.Column.TITLE, lyric.title);
        values.put(Constants.Column.ARTIST, lyric.artist);
        values.put(Constants.Column.ALBUM, lyric.album);
        values.put(Constants.Column.LENGTH, lyric.length);

        values.put(Constants.Column.PATH, path);
        values.put(Constants.Column.ENCODING, encoding);

        values.put(Constants.Column.LAST_VISITED_AT, time);
        values.put(Constants.Column.ENCODING_CHANGED, 0);

        return values;
    }

    public static ContentValues getLyricContentValue(Lyric lyric, String path, Long time,
                                                     String encoding, String unknownTag) {
        ContentValues values = new ContentValues();

        values.put(Constants.Column.TITLE, TextUtils.isEmpty(lyric.title) ? unknownTag : lyric.title);
        values.put(Constants.Column.ARTIST, TextUtils.isEmpty(lyric.artist) ? unknownTag : lyric.artist);
        values.put(Constants.Column.ALBUM, TextUtils.isEmpty(lyric.album) ? unknownTag : lyric.album);
        values.put(Constants.Column.LENGTH, lyric.length);

        values.put(Constants.Column.PATH, path);
        values.put(Constants.Column.ENCODING, encoding);

        values.put(Constants.Column.LAST_VISITED_AT, time);
        values.put(Constants.Column.ENCODING_CHANGED, 0);

        return values;
    }

    /**
     * Calculate lyric finder priority
     *
     * @return priority 9 for likely title, 8 for same title/artist/album, 7 for same title and same...
     */
    private static int calculatePriority(String title, String artist, String album, String currTitle,
                                         String currArtist, String currAlbum) {
        int priority = 10;
        boolean countArtist = (artist != null && currArtist != null);
        boolean countAlbum = (album != null && currAlbum != null);

        if (title != null && currTitle != null) {
            if (currTitle.equals(title)) {
                priority -= 2;
            } else if (currTitle.startsWith(title)) {
                priority -= 1;
            }
            if (countArtist && currArtist.equals(artist)) {
                priority -= 2;
            } else if (countArtist && currArtist.startsWith(artist)) {
                priority -= 1;
            }
            if (countAlbum && currAlbum.equals(album)) {
                priority -= 2;
            } else if (countAlbum && currAlbum.startsWith(album)) {
                priority -= 1;
            }
        }
        return priority;
    }
}
