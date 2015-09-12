package com.markzhai.lyrichere.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.markzhai.lyrichere.AlbumArtCache;
import com.markzhai.lyrichere.MusicService;
import com.markzhai.lyrichere.R;
import com.markzhai.lyrichere.utils.LogUtils;

import butterknife.Bind;

/**
 * A class that shows the Media Queue to the user.
 * <p/>
 * Created by markzhai on 2015/3/19.
 */
public class PlaybackControlsFragment extends BaseFragment {

    private static final String TAG = LogUtils.makeLogTag(PlaybackControlsFragment.class);

    @Bind(R.id.play_pause)
    ImageButton mPlayPause;
    @Bind(R.id.title)
    TextView mTitle;
    @Bind(R.id.artist)
    TextView mSubtitle;
    @Bind(R.id.extra_info)
    TextView mExtraInfo;
    @Bind(R.id.album_art)
    ImageView mAlbumArt;

    private String mArtUrl;

    private MediaControllerProvider mMediaControllerProvider;

    // Receive callbacks from the MediaController. Here we update our state such as which queue
    // is being shown, the current title and description and the PlaybackState.
    private final MediaControllerCompat.Callback mCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
            LogUtils.d(TAG, "Received playback state change to state ", state.getState());
            PlaybackControlsFragment.this.onPlaybackStateChanged(state);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            if (metadata == null) {
                return;
            }
            LogUtils.d(TAG, "Received metadata state change to mediaId=",
                    metadata.getDescription().getMediaId(),
                    " song=", metadata.getDescription().getTitle());
            PlaybackControlsFragment.this.onMetadataChanged(metadata);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_playback_controls, container, false);

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FullScreenPlayerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                MediaMetadataCompat metadata = mMediaControllerProvider.
                        getSupportMediaController().getMetadata();
                if (metadata != null) {
                    intent.putExtra(MusicPlayerActivity.EXTRA_CURRENT_MEDIA_DESCRIPTION,
                            metadata.getDescription());
                }
                startActivity(intent);
            }
        });
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPlayPause.setEnabled(true);
        mPlayPause.setOnClickListener(mButtonListener);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mMediaControllerProvider = (MediaControllerProvider) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mMediaControllerProvider = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        LogUtils.d(TAG, "fragment.onStart");
        if (mMediaControllerProvider.getSupportMediaController() != null) {
            onConnected();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        LogUtils.d(TAG, "fragment.onStop");
        if (mMediaControllerProvider.getSupportMediaController() != null) {
            mMediaControllerProvider.getSupportMediaController().unregisterCallback(mCallback);
        }
    }

    public void onConnected() {
        MediaControllerCompat controller = mMediaControllerProvider.getSupportMediaController();
        LogUtils.d(TAG, "onConnected, mediaController==null? ", controller == null);
        if (controller != null) {
            onMetadataChanged(controller.getMetadata());
            onPlaybackStateChanged(controller.getPlaybackState());
            controller.registerCallback(mCallback);
        }
    }

    private void onMetadataChanged(MediaMetadataCompat metadata) {
        LogUtils.d(TAG, "onMetadataChanged ", metadata);
        if (getActivity() == null) {
            LogUtils.w(TAG, "onMetadataChanged called when getActivity null," +
                    "this should not happen if the callback was properly unregistered. Ignoring.");
            return;
        }
        if (metadata == null) {
            return;
        }

        mTitle.setText(metadata.getDescription().getTitle());
        mSubtitle.setText(metadata.getDescription().getSubtitle());
        String artUrl = null;
        if (metadata.getDescription().getIconUri() != null) {
            artUrl = metadata.getDescription().getIconUri().toString();
        }
        if (!TextUtils.equals(artUrl, mArtUrl)) {
            mArtUrl = artUrl;
            Bitmap art = metadata.getDescription().getIconBitmap();
            AlbumArtCache cache = AlbumArtCache.getInstance();
            if (art == null) {
                art = cache.getIconImage(mArtUrl);
            }
            if (art != null) {
                mAlbumArt.setImageBitmap(art);
            } else {
                cache.fetch(artUrl, new AlbumArtCache.FetchListener() {
                            @Override
                            public void onFetched(String artUrl, Bitmap bitmap, Bitmap icon) {
                                if (icon != null) {
                                    LogUtils.d(TAG, "album art icon of w=", icon.getWidth(),
                                            " h=", icon.getHeight());
                                    if (isAdded()) {
                                        mAlbumArt.setImageBitmap(icon);
                                    }
                                }
                            }
                        }
                );
            }
        }
    }

    public void setExtraInfo(String extraInfo) {
        if (extraInfo == null) {
            mExtraInfo.setVisibility(View.GONE);
        } else {
            mExtraInfo.setText(extraInfo);
            mExtraInfo.setVisibility(View.VISIBLE);
        }
    }

    private void onPlaybackStateChanged(PlaybackStateCompat state) {
        LogUtils.d(TAG, "onPlaybackStateChanged ", state);
        if (getActivity() == null) {
            LogUtils.w(TAG, "onPlaybackStateChanged called when getActivity null," +
                    "this should not happen if the callback was properly unregistered. Ignoring.");
            return;
        }
        if (state == null) {
            return;
        }
        boolean enablePlay = false;
        switch (state.getState()) {
            case PlaybackStateCompat.STATE_PAUSED:
            case PlaybackStateCompat.STATE_STOPPED:
                enablePlay = true;
                break;
            case PlaybackStateCompat.STATE_ERROR:
                LogUtils.e(TAG, "error playbackstate: ", state.getErrorMessage());
                Toast.makeText(getActivity(), state.getErrorMessage(), Toast.LENGTH_LONG).show();
                break;
        }

        if (enablePlay) {
            mPlayPause.setImageDrawable(
                    ActivityCompat.getDrawable(getActivity(), R.drawable.ic_play_arrow_black_36dp));
        } else {
            mPlayPause.setImageDrawable(
                    ActivityCompat.getDrawable(getActivity(), R.drawable.ic_pause_black_36dp));
        }

        MediaControllerCompat controller = mMediaControllerProvider.getSupportMediaController();
        String extraInfo = null;
        if (controller != null && controller.getExtras() != null) {
            String castName = controller.getExtras().getString(MusicService.EXTRA_CONNECTED_CAST);
            if (castName != null) {
                extraInfo = getResources().getString(R.string.casting_to_device, castName);
            }
        }
        setExtraInfo(extraInfo);
    }

    private final View.OnClickListener mButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PlaybackStateCompat stateObj = mMediaControllerProvider.getSupportMediaController().getPlaybackState();
            final int state = stateObj == null ?
                    PlaybackStateCompat.STATE_NONE : stateObj.getState();
            LogUtils.d(TAG, "Button pressed, in state " + state);
            switch (v.getId()) {
                case R.id.play_pause:
                    LogUtils.d(TAG, "Play button pressed, in state " + state);
                    if (state == PlaybackStateCompat.STATE_PAUSED ||
                            state == PlaybackStateCompat.STATE_STOPPED ||
                            state == PlaybackStateCompat.STATE_NONE) {
                        playMedia();
                    } else if (state == PlaybackStateCompat.STATE_PLAYING ||
                            state == PlaybackStateCompat.STATE_BUFFERING ||
                            state == PlaybackStateCompat.STATE_CONNECTING) {
                        pauseMedia();
                    }
                    break;
            }
        }
    };

    private void playMedia() {
        MediaControllerCompat controller = mMediaControllerProvider.getSupportMediaController();
        if (controller != null) {
            controller.getTransportControls().play();
        }
    }

    private void pauseMedia() {
        MediaControllerCompat controller = mMediaControllerProvider.getSupportMediaController();
        if (controller != null) {
            controller.getTransportControls().pause();
        }
    }
}
