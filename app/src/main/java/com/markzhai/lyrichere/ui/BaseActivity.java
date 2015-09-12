package com.markzhai.lyrichere.ui;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.graphics.BitmapFactory;
import android.media.MediaMetadata;
import android.media.browse.MediaBrowser;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.hannesdorfmann.mosby.mvp.MvpActivity;
import com.markzhai.lyrichere.MusicService;
import com.markzhai.lyrichere.R;
import com.markzhai.lyrichere.global.GlobalConst;
import com.markzhai.lyrichere.utils.LogUtils;
import com.markzhai.lyrichere.utils.NetworkHelper;
import com.markzhai.lyrichere.utils.ResourceHelper;

import butterknife.ButterKnife;
import icepick.Icepick;

/**
 * Application base activity.
 */
public abstract class BaseActivity extends MvpActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LogUtils.d(GlobalConst.Log.TAG_TRACE, "onCreate:" + this.getClass().getName());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Since our app icon has the same color as colorPrimary, our entry in the Recent Apps
            // list gets weird. We need to change either the icon or the color of the TaskDescription.
            ActivityManager.TaskDescription taskDesc = new ActivityManager.TaskDescription(
                    getTitle().toString(),
                    BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_white),
                    ResourceHelper.getThemeColor(this, R.attr.colorPrimary, android.R.color.darker_gray));
            setTaskDescription(taskDesc);
        }
        Icepick.restoreInstanceState(this, savedInstanceState);
    }


    @Override public void onContentChanged() {
        super.onContentChanged();
        ButterKnife.bind(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogUtils.d(GlobalConst.Log.TAG_TRACE, "onStart:" + this.getClass().getName());
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogUtils.d(GlobalConst.Log.TAG_TRACE, "onStop:" + this.getClass().getName());
    }
}
