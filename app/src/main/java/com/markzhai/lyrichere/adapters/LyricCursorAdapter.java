package com.markzhai.lyrichere.adapters;

import android.content.Context;
import android.database.Cursor;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorAdapter;

import com.markzhai.lyrichere.utils.DbUtils;
import com.markzhai.lyrichere.utils.LogUtils;

/**
 * Created by yifan on 6/19/14.
 */
public class LyricCursorAdapter extends SimpleCursorAdapter {
    private static final String TAG = LyricCursorAdapter.class.getSimpleName();

    public LyricCursorAdapter(final Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
        setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                LogUtils.d(TAG, "runQuery: " + constraint);
                return DbUtils.searchByKeyword(context.getContentResolver(), constraint.toString());
            }
        });
    }

    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        LogUtils.d(TAG, "runQueryOnBackgroundThread: " + constraint);
        return super.runQueryOnBackgroundThread(constraint);
    }
}
