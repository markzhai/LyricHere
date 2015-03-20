package com.markzhai.lyrichere.workers;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.markzhai.lyrichere.utils.DbUtils;

/**
 * Created by yifan on 6/25/14.
 */
public class LyricLastVisitUpdater extends AsyncTask<String, Integer, Boolean> {
    private static final String TAG = LyricLastVisitUpdater.class.getSimpleName();
    private Context mContext;
    public LyricLastVisitUpdater(Context context) {
        this.mContext = context;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        if (params == null || params.length < 1)
            return false;
        String path = params[0];

        int num = DbUtils.updateLyricLastVisit(mContext.getContentResolver(), path, System.currentTimeMillis());
        Log.d(TAG, "update" + num);
        return true;
    }
}
