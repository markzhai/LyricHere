package com.markzhai.lyrichere.workers;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;

import com.markzhai.lyrichere.utils.DbUtils;
import com.markzhai.lyrichere.utils.LyricUtils;

/**
 * Created by yifan on 6/15/14.
 */
public class LyricEncodingUpdater extends AsyncTask<String, Integer, Boolean> {
    private static final String TAG = LyricEncodingUpdater.class.getSimpleName();
    private Context mContext;

    public LyricEncodingUpdater(Context context) {
        this.mContext = context;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        if (params == null || params.length < 2)
            return false;

        String path = params[0];
        if (params.length == 3) {
            String rowId = params[1];
            String targetEncoding = params[2];

            DbUtils.updateLyricEncoding(mContext.getContentResolver(),
                    rowId,
                    LyricUtils.parseLyric(new File(path), targetEncoding),
                    targetEncoding);
            return true;
        } else if (params.length == 2) {
            String targetEncoding = params[1];
            DbUtils.updateLyricEncoding(mContext.getContentResolver(), path, targetEncoding);
            return true;
        }
        return false;
    }
}