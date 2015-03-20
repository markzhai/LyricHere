package com.markzhai.lyrichere.model;


import android.annotation.TargetApi;
import android.media.MediaMetadata;
import android.os.AsyncTask;
import android.os.Build;

import com.markzhai.lyrichere.utils.LogHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Utility class to get a list of MusicTrack's based on a server-side JSON
 * configuration.
 */
public class MusicProvider {
    private static final String TAG = LogHelper.makeLogTag(MusicProvider.class);

    public static final String CUSTOM_METADATA_TRACK_SOURCE = "__SOURCE__";
    // Categorized caches for music track data:
    private ConcurrentMap<String, List<MediaMetadata>> mMusicListByGenre;
    private final ConcurrentMap<String, MutableMediaMetadata> mMusicListById;

    private final Set<String> mFavoriteTracks;

    enum State {
        NON_INITIALIZED, INITIALIZING, INITIALIZED
    }

    private volatile State mCurrentState = State.NON_INITIALIZED;

    public interface Callback {
        void onMusicCatalogReady(boolean success);
    }

    public MusicProvider() {
        mMusicListByGenre = new ConcurrentHashMap<>();
        mMusicListById = new ConcurrentHashMap<>();
        mFavoriteTracks = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    }

    /**
     * Get an iterator over the list of genres
     *
     * @return genres
     */
    public Iterable<String> getGenres() {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        return mMusicListByGenre.keySet();
    }

    /**
     * Get music tracks of the given genre
     */
    public Iterable<MediaMetadata> getMusicsByGenre(String genre) {
        if (mCurrentState != State.INITIALIZED || !mMusicListByGenre.containsKey(genre)) {
            return Collections.emptyList();
        }
        return mMusicListByGenre.get(genre);
    }

    /**
     * Very basic implementation of a search that filter music tracks which title containing
     * the given query.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Iterable<MediaMetadata> searchMusic(String titleQuery) {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        ArrayList<MediaMetadata> result = new ArrayList<>();
        titleQuery = titleQuery.toLowerCase();
        for (MutableMediaMetadata track : mMusicListById.values()) {
            if (track.metadata.getString(MediaMetadata.METADATA_KEY_TITLE).toLowerCase()
                    .contains(titleQuery)) {
                result.add(track.metadata);
            }
        }
        return result;
    }

    /**
     * Return the MediaMetadata for the given musicID.
     *
     * @param musicId The unique, non-hierarchical music ID.
     */
    public MediaMetadata getMusic(String musicId) {
        return mMusicListById.containsKey(musicId) ? mMusicListById.get(musicId).metadata : null;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public synchronized void updateMusic(String musicId, MediaMetadata metadata) {
        MutableMediaMetadata track = mMusicListById.get(musicId);
        if (track == null) {
            return;
        }

        String oldGenre = track.metadata.getString(MediaMetadata.METADATA_KEY_GENRE);
        String newGenre = metadata.getString(MediaMetadata.METADATA_KEY_GENRE);

        track.metadata = metadata;

        // if genre has changed, we need to rebuild the list by genre
        if (!oldGenre.equals(newGenre)) {
            buildListsByGenre();
        }
    }

    public void setFavorite(String musicId, boolean favorite) {
        if (favorite) {
            mFavoriteTracks.add(musicId);
        } else {
            mFavoriteTracks.remove(musicId);
        }
    }

    public boolean isFavorite(String musicId) {
        return mFavoriteTracks.contains(musicId);
    }

    public boolean isInitialized() {
        return mCurrentState == State.INITIALIZED;
    }

    /**
     * Get the list of music tracks from a server and caches the track information
     * for future reference, keying tracks by musicId and grouping by genre.
     */
    public void retrieveMediaAsync(final Callback callback) {
        LogHelper.d(TAG, "retrieveMediaAsync called");
        if (mCurrentState == State.INITIALIZED) {
            // Nothing to do, execute callback immediately
            callback.onMusicCatalogReady(true);
            return;
        }

        // Asynchronously load the music catalog in a separate thread
        new AsyncTask<Void, Void, State>() {
            @Override
            protected State doInBackground(Void... params) {
                retrieveMedia();
                return mCurrentState;
            }

            @Override
            protected void onPostExecute(State current) {
                if (callback != null) {
                    callback.onMusicCatalogReady(current == State.INITIALIZED);
                }
            }
        }.execute();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private synchronized void buildListsByGenre() {
        ConcurrentMap<String, List<MediaMetadata>> newMusicListByGenre = new ConcurrentHashMap<>();

        for (MutableMediaMetadata m : mMusicListById.values()) {
            String genre = m.metadata.getString(MediaMetadata.METADATA_KEY_GENRE);
            List<MediaMetadata> list = newMusicListByGenre.get(genre);
            if (list == null) {
                list = new ArrayList<>();
                newMusicListByGenre.put(genre, list);
            }
            list.add(m.metadata);
        }
        mMusicListByGenre = newMusicListByGenre;
    }

    private synchronized void retrieveMedia() {
        try {
            if (mCurrentState == State.NON_INITIALIZED) {
                mCurrentState = State.INITIALIZING;

                //int slashPos = CATALOG_URL.lastIndexOf('/');
                //String path = CATALOG_URL.substring(0, slashPos + 1);
                return;
                /*
                JSONObject jsonObj = fetchJSONFromUrl(CATALOG_URL);
                if (jsonObj == null) {
                    return;
                }
                JSONArray tracks = jsonObj.getJSONArray(JSON_MUSIC);
                if (tracks != null) {
                    for (int j = 0; j < tracks.length(); j++) {
                        MediaMetadata item = buildFromJSON(tracks.getJSONObject(j), path);
                        String musicId = item.getString(MediaMetadata.METADATA_KEY_MEDIA_ID);
                        mMusicListById.put(musicId, new MutableMediaMetadata(musicId, item));
                    }
                    buildListsByGenre();
                }
                mCurrentState = State.INITIALIZED;
                */
            }
        } finally {
            if (mCurrentState != State.INITIALIZED) {
                // Something bad happened, so we reset state to NON_INITIALIZED to allow
                // retries (eg if the network connection is temporary unavailable)
                mCurrentState = State.NON_INITIALIZED;
            }
        }
    }
}

