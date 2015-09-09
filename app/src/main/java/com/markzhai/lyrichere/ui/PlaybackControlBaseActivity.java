package com.markzhai.lyrichere.ui;

import android.content.ComponentName;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.browse.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.View;

import com.markzhai.lyrichere.MusicService;
import com.markzhai.lyrichere.R;
import com.markzhai.lyrichere.utils.LogUtils;
import com.markzhai.lyrichere.utils.NetworkHelper;

import java.util.List;

/**
 * Base activity for activities that need to show a playback control fragment when media is playing.
 */
public abstract class PlaybackControlBaseActivity extends ActionBarCastActivity
        implements MediaBrowserProvider, MediaControllerProvider {
    private static final String TAG = LogUtils.makeLogTag("PlaybackControlBaseActivity");

    private MediaBrowserCompat mMediaBrowser;
    private PlaybackControlsFragment mControlsFragment;
    private MediaControllerCompat mMediaController;
    private View mControlsContainer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Connect a media browser just to get the media session token. There are other ways
        // this can be done, for example by sharing the session token directly.
        mMediaBrowser = new MediaBrowserCompat(this, new ComponentName(this, MusicService.class), mMediaBrowserConnectionCallback, null);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mControlsContainer = findViewById(R.id.controls_container);
        mControlsFragment = (PlaybackControlsFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_playback_controls);

        if (mControlsFragment == null) {
            throw new IllegalStateException("Missing fragment with id 'fragment_playback_controls'");
        }

        hidePlaybackControls();

        if (mMediaBrowser != null) {
            mMediaBrowser.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogUtils.d(TAG, "Activity onStop");
        if (mMediaController != null) {
            mMediaController.unregisterCallback(mMediaControllerCallback);
        }
        if (mMediaBrowser != null) {
            mMediaBrowser.disconnect();
        }
    }

    @Override
    public MediaBrowserCompat getMediaBrowser() {
        return mMediaBrowser;
    }

    @Override
    public MediaControllerCompat getSupportMediaController() {
        return mMediaController;
    }

    protected void onMediaControllerConnected() {
        // empty implementation, can be overridden by clients.
    }

    protected void showPlaybackControls() {
        LogUtils.d(TAG, "showPlaybackControls");
        if (NetworkHelper.isOnline(this)) {
            // TODO support animation
//            getFragmentManager().beginTransaction()
//                .setCustomAnimations(
//                    R.animator.slide_in_from_bottom, R.animator.slide_out_to_bottom,
//                    R.animator.slide_in_from_bottom, R.animator.slide_out_to_bottom)
//                .show(mControlsFragment)
//                .commit();
            mControlsContainer.setVisibility(View.VISIBLE);
        }
    }

    protected void hidePlaybackControls() {
        LogUtils.d(TAG, "hidePlaybackControls");
        // TODO support
        //getFragmentManager().beginTransaction()
        //        .hide(mControlsFragment)
        //        .commit();
        mControlsContainer.setVisibility(View.GONE);
    }

    /**
     * Check if the MediaSession is active and in a "playback-able" state
     * (not NONE and not STOPPED).
     *
     * @return true if the MediaSession's state requires playback controls to be visible.
     */
    protected boolean shouldShowControls() {
        MediaControllerCompat mediaController = mMediaController;
        if (mediaController == null ||
                mediaController.getMetadata() == null ||
                mediaController.getPlaybackState() == null) {
            return false;
        }
        switch (mediaController.getPlaybackState().getState()) {
            case PlaybackState.STATE_ERROR:
            case PlaybackState.STATE_NONE:
            case PlaybackState.STATE_STOPPED:
                return false;
            default:
                return true;
        }
    }

    private void connectToSession(MediaSessionCompat.Token token) {
        try {
            LogUtils.d(TAG, "Session Token: " + token);
            mMediaController = new MediaControllerCompat(this, token);

            mMediaController.registerCallback(mMediaControllerCallback);

            if (shouldShowControls()) {
                showPlaybackControls();
            } else {
                LogUtils.d(TAG, "connectionCallback.onConnected: " + "hiding controls because metadata is null");
                hidePlaybackControls();
            }

            if (mControlsFragment != null) {
                mControlsFragment.onConnected();
            }

            onMediaControllerConnected();

        } catch (RemoteException ex) {
            LogUtils.e(TAG, ex.getMessage());
        }
    }

    // Callback that ensures that we are showing the controls
    private final MediaControllerCompat.Callback mMediaControllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onSessionDestroyed() {
                    super.onSessionDestroyed();
                    LogUtils.d(TAG, "onSessionDestroyed");
                }

                @Override
                public void onSessionEvent(String event, Bundle extras) {
                    super.onSessionEvent(event, extras);
                    LogUtils.d(TAG, "onSessionEvent");
                }

                @Override
                public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
                    super.onQueueChanged(queue);
                    LogUtils.d(TAG, "onQueueChanged");
                }

                @Override
                public void onQueueTitleChanged(CharSequence title) {
                    super.onQueueTitleChanged(title);
                    LogUtils.d(TAG, "onQueueTitleChanged");
                }

                @Override
                public void onExtrasChanged(Bundle extras) {
                    super.onExtrasChanged(extras);
                    LogUtils.d(TAG, "onExtrasChanged");
                }

                @Override
                public void onAudioInfoChanged(MediaControllerCompat.PlaybackInfo info) {
                    super.onAudioInfoChanged(info);
                    LogUtils.d(TAG, "onAudioInfoChanged");
                }

                @Override
                public void binderDied() {
                    super.binderDied();
                    LogUtils.d(TAG, "binderDied");
                }

                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {
                    if (shouldShowControls()) {
                        showPlaybackControls();
                    } else {
                        LogUtils.d(TAG, "mediaControllerCallback.onPlaybackStateChanged: " + "hiding controls because state is ",
                                state == null ? "null" : state.getState());
                        hidePlaybackControls();
                    }
                }

                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                    if (shouldShowControls()) {
                        showPlaybackControls();
                    } else {
                        LogUtils.d(TAG, "mediaControllerCallback.onMetadataChanged: " +
                                "hiding controls because metadata is null");
                        hidePlaybackControls();
                    }
                }
            };

    private MediaBrowserCompat.ConnectionCallback mMediaBrowserConnectionCallback =
            new MediaBrowserCompat.ConnectionCallback() {

                @Override
                public void onConnectionSuspended() {
                    super.onConnectionSuspended();
                    LogUtils.d(TAG, "onConnectionSuspended");
                }

                @Override
                public void onConnectionFailed() {
                    super.onConnectionFailed();
                    LogUtils.d(TAG, "onConnectionSuspended");
                }

                @Override
                public void onConnected() {
                    LogUtils.d(TAG, "onConnected");

                    MediaSessionCompat.Token token = mMediaBrowser.getSessionToken();
                    if (token == null) {
                        throw new IllegalArgumentException("No Session token");
                    }
                    connectToSession(token);
                }
            };

}