package com.markzhai.lyrichere;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.markzhai.lyrichere.ui.LyricExplorerActivity;
import com.markzhai.lyrichere.utils.LogUtils;

/**
 * Receive broadcast from music player apps and pop lyric notifications.
 * <p>
 * Created by yifan on 6/21/14.
 */
public class MusicBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = MusicBroadcastReceiver.class.getSimpleName();
    private static final int NOTIFICATION_ID = 5657;

    private String mTitle = null;
    private String mArtist = null;
    private String mAlbum = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.i(TAG, "Action : " + intent.getAction());
        String artist = intent.getStringExtra("artist");
        String title = intent.getStringExtra("track");
        String album = intent.getStringExtra("album");

        // at least we should have a track title
        if (title == null) {
            return;
        }

        boolean isPlaying = intent.getBooleanExtra("isplaying", true);
        isPlaying = intent.getBooleanExtra("playing", isPlaying);
        isPlaying = intent.getBooleanExtra("spotifyPlaying", isPlaying);

        LogUtils.i(TAG, String.valueOf("title: " + title));
        LogUtils.i(TAG, String.valueOf("isPlaying: " + isPlaying));

        if (isPlaying && !isSameTrack(title, artist, album)) {
            addNotification(context);
        } else {
            removeNotification(context);
        }
    }

    public void addNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mNotifyBuilder;
        Intent intent = new Intent(context, LyricExplorerActivity.class);
        intent.putExtra(Constants.Column.TITLE, mTitle);
        intent.putExtra(Constants.Column.ARTIST, mArtist);
        intent.putExtra(Constants.Column.ALBUM, mAlbum);
        mNotifyBuilder = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(makeNotificationText())
                .setTicker(context.getString(R.string.notification_ticker))
                .setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT))
                .setSmallIcon(R.drawable.play);
        notificationManager.notify(NOTIFICATION_ID, mNotifyBuilder.build());
    }

    public void removeNotification(Context paramContext) {
        ((NotificationManager) paramContext.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID);
    }

    private String makeNotificationText() {
        String text = null;
        if (!TextUtils.isEmpty(mTitle)) {
            if (TextUtils.isEmpty(mArtist)) {
                text = mTitle;
            } else {
                text = String.format("%s - %s", mArtist, mTitle);
            }
        }
        return text;
    }

    /**
     * Check if new received meta is the same track as before
     */
    private synchronized boolean isSameTrack(String title, String artist, String album) {
        boolean isSame = true;
        if (title != null) {
            // mTitle is null or different from title, then different track
            if (!TextUtils.equals(title, mTitle) || !TextUtils.equals(artist, mArtist) || !TextUtils.equals(album, mAlbum)) {
                isSame = false;
            } else {
                isSame = true;
            }
        }
        if (!isSame) {
            mTitle = title;
            mArtist = artist;
            mAlbum = album;
        }
        LogUtils.i(TAG, "isSameTrack: " + String.valueOf(isSame));
        return isSame;
    }
}