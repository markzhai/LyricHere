package com.markzhai.lyrichere.workers;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.markzhai.lyrichere.app.Constants;
import com.markzhai.lyrichere.R;
import com.markzhai.lyrichere.ui.LyricPlayerActivity;
import com.markzhai.lyrichere.utils.DbUtils;

/**
 * Created by yifan on 6/25/14.
 */
public class LyricOpener extends AsyncTask<String, Integer, String[]> {
    private static final String TAG = LyricOpener.class.getSimpleName();
    private Context mContext;

    public LyricOpener(Context context) {
        this.mContext = context;
    }

    @Override
    protected void onPostExecute(String[] result) {
        super.onPostExecute(result);

        if (result == null) {
            Toast.makeText(mContext, mContext.getString(R.string.lyric_not_found), Toast.LENGTH_SHORT).show();

        } else {
            Intent intent = new Intent(mContext, LyricPlayerActivity.class);
            intent.putExtra(Constants.Column.PATH, result[0]);
            intent.putExtra(Constants.Column.ENCODING, result[1]);

            mContext.startActivity(intent);
        }
    }

    @Override
    protected String[] doInBackground(String... params) {
        if (params == null || params.length < 3)
            return null;
        return DbUtils.findLyric(mContext.getContentResolver(), params[0], params[1], params[2]);
    }
}