package com.markzhai.lyrichere.workers;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.markzhai.lyrichere.app.Constants;
import com.markzhai.lyrichere.R;
import com.markzhai.lyrichere.utils.DbUtils;
import com.markzhai.lyrichere.utils.FileUtils;
import com.markzhai.lyrichere.utils.LogUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.Map;

import cn.zhaiyifan.lyric.LyricUtils;
import cn.zhaiyifan.lyric.model.Lyric;

/**
 * Created by yifan on 6/3/14.
 */
public class  Finder extends AsyncTask<File, Integer, Integer> {

    private static final String TAG = Finder.class.getSimpleName();
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy");
    private Context mContext;
    private File currentDir;

    public Finder(Context context) {
        this.mContext = context;
    }

    @Override
    protected void onPostExecute(Integer result) {
        LogUtils.i(TAG, String.format("Find %d lyrics under %s", result, currentDir.getAbsolutePath()));
        mContext = null;
    }

    @Override
    protected Integer doInBackground(File... params) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        Integer result = 0;
        currentDir = params[0];

        boolean isRememberEncoding = sharedPreferences.getBoolean(mContext.getString(R.string.pref_key_remember_encoding), true);
        Map<String, String> pathEncodingMap = null;
        if (isRememberEncoding) {
            pathEncodingMap = DbUtils.getNonDefaultEncodingMap(mContext.getContentResolver());
        }

        LogUtils.i(TAG, "Finder directory: " + currentDir.getAbsolutePath());

        String[] children = currentDir.list();

        if (children == null) {
            LogUtils.i(TAG, "null children");
            return result;
        }

        boolean showHidden = false;
        boolean showSystem = false;

        LinkedList<String> fileList = new LinkedList<String>();

        for (String fileName : children) {
            fileList.add(currentDir.getAbsolutePath() + File.separator + fileName);
        }

        mContext.getContentResolver().delete(Constants.CONTENT_URI, null, null);

        while (!fileList.isEmpty()) {
            String currentFilePath = fileList.pop();

            File f = new File(currentFilePath);

            if (!f.exists()) {
                continue;
            }
            if (FileUtils.isProtected(f) && !showSystem) {
                continue;
            }
            if (f.isHidden() && !showHidden) {
                continue;
            }

            // Add files under directory
            if (f.isDirectory()) {
                String[] newFileList = f.list();
                for (String newFileName : newFileList) {
                    fileList.add(currentFilePath + File.separator + newFileName);
                }
                continue;
            }

            // Not directory but not lyric file, ignore
            if (!currentFilePath.endsWith(".lrc")) {
                continue;
            }

            // If encoding of the same file has been changed, keep its original encoding.
            String encoding = null;
            if (isRememberEncoding && pathEncodingMap != null) {
                encoding = pathEncodingMap.get(f.getAbsolutePath());
            }
            // Not in map or remember old encoding disabled
            if (encoding == null) {
                encoding = sharedPreferences.getString(mContext.getString(R.string.pref_key_default_encoding), null);
            }

            if (encoding == null) {
                encoding = sharedPreferences.getString("pref_default_encoding_key", Constants.ENCODE_UTF_8);
            }

            Lyric lyric = LyricUtils.parseLyric(f, encoding);
            ContentValues values = DbUtils.getLyricContentValue(lyric,
                    f.getAbsolutePath(), System.currentTimeMillis(), encoding, mContext.getString(R.string.tag_not_found));

            // URI is registered at application AndroidManifest.xml
            Uri uri = mContext.getContentResolver().insert(Constants.CONTENT_URI, values);
            if (uri != null) {
                LogUtils.i(TAG, String.format("(id %d): %s - %s, [%s]", Integer.valueOf(uri.getLastPathSegment()),
                        lyric.artist, lyric.title, f.getAbsolutePath()));
            }
            result++;
        }
        return result;
    }
}