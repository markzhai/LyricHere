package com.markzhai.lyrichere;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Broadcast receiver for handling ACTION_MEDIA_BUTTON.
 * <p/>
 * This is needed to create the RemoteControlClient for controlling
 * remote route volume in lock screen. It routes media key events back
 * to main app activity SampleMediaRouterActivity.
 */
public class MediaButtonReceiver extends BroadcastReceiver {

    private static final String TAG = "MediaButtonReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

    }
}