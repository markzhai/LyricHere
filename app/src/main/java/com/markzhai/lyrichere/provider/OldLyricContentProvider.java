package com.markzhai.lyrichere.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.markzhai.lyrichere.app.Constants;
import com.markzhai.lyrichere.utils.DbHelper;
import com.markzhai.lyrichere.utils.LogUtils;

public class OldLyricContentProvider extends ContentProvider {
    private static final String TAG = OldLyricContentProvider.class.getSimpleName();
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private DbHelper dbHelper;

    static {
        sURIMatcher.addURI(Constants.AUTHORITY, Constants.TABLE, Constants.LYRIC_DIR);
        sURIMatcher.addURI(Constants.AUTHORITY, Constants.TABLE + "/#", Constants.LYRIC_ITEM);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DbHelper(getContext());
        //Log.d(TAG, "onCreated");
        return true;
    }

    @Override
    public String getType(Uri uri) {
        switch (sURIMatcher.match(uri)) {
            case Constants.LYRIC_DIR:
                LogUtils.i(TAG, "gotType: " + Constants.LYRIC_TYPE_DIR);
                return Constants.LYRIC_TYPE_DIR;
            case Constants.LYRIC_ITEM:
                LogUtils.i(TAG, "gotType: " + Constants.LYRIC_TYPE_ITEM);
                return Constants.LYRIC_TYPE_ITEM;
            default:
                throw new IllegalArgumentException("Illegal URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri ret = null;

        // Assert correct uri
        if (sURIMatcher.match(uri) != Constants.LYRIC_DIR) {
            throw new IllegalArgumentException("Illegal uri: " + uri);
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long rowId = db.insertWithOnConflict(Constants.TABLE, null,
                values, SQLiteDatabase.CONFLICT_IGNORE);
        // Was insert successful?
        if (rowId != -1) {
            ret = ContentUris.withAppendedId(uri, rowId);
            LogUtils.i(TAG, "inserted uri: " + ret);

            // Notify that data for this uri has changed
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return ret;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String where;

        switch (sURIMatcher.match(uri)) {
            case Constants.LYRIC_DIR:
                // so we count updated rows
                where = selection;
                break;
            case Constants.LYRIC_ITEM:
                long id = ContentUris.parseId(uri);
                where = Constants.Column.ID
                        + "="
                        + id
                        + (TextUtils.isEmpty(selection) ? "" : " and ( "
                        + selection + " )");
                break;
            default:
                throw new IllegalArgumentException("Illegal uri: " + uri);
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int ret = db.update(Constants.TABLE, values, where, selectionArgs);
        if (ret > 0) {
            // Notify that data for this uri has changed
            getContext().getContentResolver().notifyChange(uri, null);
        }
        LogUtils.i(TAG, "updated records: " + ret);
        return ret;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String where;
        switch (sURIMatcher.match(uri)) {
            case Constants.LYRIC_DIR:
                // so we count deleted rows
                where = (selection == null) ? "1" : selection;
                break;
            case Constants.LYRIC_ITEM:
                long id = ContentUris.parseId(uri);
                where = Constants.Column.ID
                        + "="
                        + id
                        + (TextUtils.isEmpty(selection) ? "" : " and ( "
                        + selection + " )");
                break;
            default:
                throw new IllegalArgumentException("Illegal uri: " + uri);
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int ret = db.delete(Constants.TABLE, where, selectionArgs);

        if (ret > 0) {
            // Notify that data for this uri has changed
            getContext().getContentResolver().notifyChange(uri, null);
        }
        LogUtils.i(TAG, "deleted records: " + ret);
        return ret;
    }

    // SELECT username, message, created_at FROM status WHERE user='bob' ORDER
    // BY created_at DESC;
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Constants.TABLE);
        switch (sURIMatcher.match(uri)) {
            case Constants.LYRIC_DIR:
                break;
            case Constants.LYRIC_ITEM:
                qb.appendWhere(Constants.Column.ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Illegal uri: " + uri);
        }

        String orderBy = (TextUtils.isEmpty(sortOrder)) ? Constants.DEFAULT_SORT : sortOrder;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = qb.query(db, projection, selection, selectionArgs,
                null, null, orderBy);

        // register for uri changes
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        //Log.d(TAG, "queried records: " + cursor.getCount());
        return cursor;
    }
}
