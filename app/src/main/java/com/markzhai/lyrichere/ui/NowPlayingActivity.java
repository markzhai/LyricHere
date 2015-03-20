package com.markzhai.lyrichere.ui;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

import com.markzhai.lyrichere.utils.LogHelper;

/**
 * The activity for the Now Playing Card PendingIntent.
 * https://developer.android.com/training/tv/playback/now-playing.html
 *
 * This activity determines which activity to launch based on the current UI mode.
 */
public class NowPlayingActivity extends Activity {

    private static final String TAG = LogHelper.makeLogTag(NowPlayingActivity.class);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        Intent newIntent;
        UiModeManager uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
        if (uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
            Log.d(TAG, "Running on a TV Device");
            // TODO: add launch Android TV "Now Playing" activity
            // newIntent = new Intent(this, TvNowPlayingActivity.class);
            throw new UnsupportedOperationException("Android TV is not yet supported");
        } else {
            Log.d(TAG, "Running on a non-TV Device");
            newIntent = new Intent(this, MusicPlayerActivity.class);
        }
        startActivity(newIntent);
        finish();
    }
}

