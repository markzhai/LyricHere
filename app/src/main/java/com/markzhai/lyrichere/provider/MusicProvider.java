package com.markzhai.lyrichere.provider;


import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;

import com.markzhai.lyrichere.LHApplication;
import com.markzhai.lyrichere.R;
import com.markzhai.lyrichere.model.MutableMediaMetadata;
import com.markzhai.lyrichere.utils.LogUtils;
import com.markzhai.lyrichere.utils.MusicUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Utility class to get a list of MusicTrack's based on a server-side JSON configuration or
 * local filesystem.
 */
public class MusicProvider {
    private static final String TAG = LogUtils.makeLogTag(MusicProvider.class);
    private static final String CATALOG_URL = "http://storage.googleapis.com/automotive-media/music.json";

    public static final String CUSTOM_METADATA_TRACK_SOURCE = "__SOURCE__";

    private static final String JSON_MUSIC = "music";
    private static final String JSON_TITLE = "title";
    private static final String JSON_ALBUM = "album";
    private static final String JSON_ARTIST = "artist";
    private static final String JSON_GENRE = "genre";
    private static final String JSON_SOURCE = "source";
    private static final String JSON_IMAGE = "image";
    private static final String JSON_TRACK_NUMBER = "trackNumber";
    private static final String JSON_TOTAL_TRACK_COUNT = "totalTrackCount";
    private static final String JSON_DURATION = "duration";

    private static final Uri ART_CONTENT_URI = Uri.parse("content://media/external/audio/albumart");

    // Categorized caches for music track data:
    private ConcurrentMap<String, List<MediaMetadataCompat>> mMusicListByGenre;
    private ConcurrentMap<String, List<MediaMetadataCompat>> mMusicListByArtist;
    private ConcurrentMap<String, List<MediaMetadataCompat>> mMusicListByAlbum;
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
        mMusicListByArtist = new ConcurrentHashMap<>();
        mMusicListByAlbum = new ConcurrentHashMap<>();
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
     * Get an iterator over the list of genres
     *
     * @return genres
     */
    public Iterable<String> getAlbums() {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        return mMusicListByAlbum.keySet();
    }

    /**
     * Get an iterator over the list of genres
     *
     * @return genres
     */
    public Iterable<String> getArtists() {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        return mMusicListByArtist.keySet();
    }

    /**
     * Get music tracks of the given genre
     */
    public Iterable<MediaMetadataCompat> getMusicsByGenre(String genre) {
        if (mCurrentState != State.INITIALIZED || !mMusicListByGenre.containsKey(genre)) {
            return Collections.emptyList();
        }
        return mMusicListByGenre.get(genre);
    }

    /**
     * Get music tracks of the given genre
     */
    public Iterable<MediaMetadataCompat> getMusicsByArtist(String artist) {
        if (mCurrentState != State.INITIALIZED || !mMusicListByArtist.containsKey(artist)) {
            return Collections.emptyList();
        }
        return mMusicListByArtist.get(artist);
    }


    /**
     * Get music tracks of the given genre
     */
    public Iterable<MediaMetadataCompat> getMusicsByAlbum(String album) {
        if (mCurrentState != State.INITIALIZED || !mMusicListByAlbum.containsKey(album)) {
            return Collections.emptyList();
        }
        return mMusicListByAlbum.get(album);
    }



    /**
     * Very basic implementation of a search that filter music tracks with title containing
     * the given query.
     */
    public Iterable<MediaMetadataCompat> searchMusicBySongTitle(String query) {
        return searchMusic(MediaMetadataCompat.METADATA_KEY_TITLE, query);
    }

    /**
     * Very basic implementation of a search that filter music tracks with album containing
     * the given query.
     */
    public Iterable<MediaMetadataCompat> searchMusicByAlbum(String query) {
        return searchMusic(MediaMetadataCompat.METADATA_KEY_ALBUM, query);
    }

    /**
     * Very basic implementation of a search that filter music tracks with artist containing
     * the given query.
     */
    public Iterable<MediaMetadataCompat> searchMusicByArtist(String query) {
        return searchMusic(MediaMetadataCompat.METADATA_KEY_ARTIST, query);
    }

    Iterable<MediaMetadataCompat> searchMusic(String metadataField, String query) {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        ArrayList<MediaMetadataCompat> result = new ArrayList<>();
        query = query.toLowerCase(Locale.US);
        for (MutableMediaMetadata track : mMusicListById.values()) {
            if (track.metadata.getString(metadataField).toLowerCase(Locale.US).contains(query)) {
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
    public MediaMetadataCompat getMusic(String musicId) {
        return mMusicListById.containsKey(musicId) ? mMusicListById.get(musicId).metadata : null;
    }

    public synchronized void updateMusic(String musicId, MediaMetadataCompat metadata) {
        MutableMediaMetadata track = mMusicListById.get(musicId);
        if (track == null) {
            return;
        }

        String oldGenre = track.metadata.getString(MediaMetadataCompat.METADATA_KEY_GENRE);
        String newGenre = metadata.getString(MediaMetadataCompat.METADATA_KEY_GENRE);

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
        LogUtils.d(TAG, "retrieveMediaAsync called");
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

    private synchronized void buildListsByGenre() {
        ConcurrentMap<String, List<MediaMetadataCompat>> newMusicListByGenre = new ConcurrentHashMap<>();

        for (MutableMediaMetadata m : mMusicListById.values()) {
            String genre = m.metadata.getString(MediaMetadataCompat.METADATA_KEY_GENRE);

            List<MediaMetadataCompat> list = newMusicListByGenre.get(genre);
            if (list == null) {
                list = new ArrayList<>();
                newMusicListByGenre.put(genre, list);
            }

            list.add(m.metadata);
        }
        mMusicListByGenre = newMusicListByGenre;
    }

    private synchronized void buildListsByAlbum() {
        ConcurrentMap<String, List<MediaMetadataCompat>> newMusicListByAlbum = new ConcurrentHashMap<>();

        for (MutableMediaMetadata m : mMusicListById.values()) {
            String album = m.metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);

            List<MediaMetadataCompat> list = newMusicListByAlbum.get(album);
            if (list == null) {
                list = new ArrayList<>();
                newMusicListByAlbum.put(album, list);
            }

            list.add(m.metadata);
        }
        mMusicListByAlbum = newMusicListByAlbum;
    }

    private synchronized void buildListsByArtist() {
        ConcurrentMap<String, List<MediaMetadataCompat>> newMusicListByArtist = new ConcurrentHashMap<>();

        for (MutableMediaMetadata m : mMusicListById.values()) {
            String artist = m.metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);

            List<MediaMetadataCompat> list = newMusicListByArtist.get(artist);
            if (list == null) {
                list = new ArrayList<>();
                newMusicListByArtist.put(artist, list);
            }

            list.add(m.metadata);
        }
        mMusicListByArtist = newMusicListByArtist;
    }

    private static final String UNKNOWN_TAG = LHApplication.getResource().getString(R.string.tag_not_found);

    private synchronized void retrieveMedia() {
        try {
            if (mCurrentState == State.NON_INITIALIZED) {
                mCurrentState = State.INITIALIZING;

                int index;
                long genreId;
                Uri uri;
                Cursor genreMediaCursor = null;

                String[] projection1 = {MediaStore.Audio.Genres.NAME, MediaStore.Audio.Genres._ID};

                String[] projection2 = {
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.TRACK,
                        MediaStore.Audio.Media.SIZE,
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.ALBUM_ID,
                        MediaStore.Audio.Media.DISPLAY_NAME};
                String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

                Cursor genreCursor = LHApplication.getContext().getContentResolver().query(
                        MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI, projection1, null, null, null);

                if (genreCursor.moveToFirst()) {
                    do {
                        String genre = genreCursor.getString(
                                genreCursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME));

                        if (genre != null && genre.trim().equals("")) {
                            genre = UNKNOWN_TAG;
                        }

                        index = genreCursor.getColumnIndexOrThrow(MediaStore.Audio.Genres._ID);
                        genreId = Long.parseLong(genreCursor.getString(index));
                        uri = MediaStore.Audio.Genres.Members.getContentUri("external", genreId);

                        genreMediaCursor = LHApplication.getContext().getContentResolver().query(uri, projection2, selection, null, null);

                        if (genreMediaCursor.moveToFirst()) {
                            do {
                                String title = genreMediaCursor.getString(0);
                                String artist = genreMediaCursor.getString(1);
                                String album = genreMediaCursor.getString(2);
                                long duration = genreMediaCursor.getLong(3);
                                String source = genreMediaCursor.getString(4);
                                int trackNumber = genreMediaCursor.getInt(5);
                                long totalTrackCount = genreMediaCursor.getLong(6);
                                String musicId = genreMediaCursor.getString(7);
                                long albumId = genreMediaCursor.getLong(8);
                                Uri albumArtUri = ContentUris.withAppendedId(ART_CONTENT_URI, albumId);

                                MediaMetadataCompat item = new MediaMetadataCompat.Builder()
                                        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, musicId)
                                        .putString(CUSTOM_METADATA_TRACK_SOURCE, source)
                                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                                        .putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre)
                                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, albumArtUri.toString())
                                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                                        .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, trackNumber)
                                        .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, totalTrackCount)
                                        .build();
                                mMusicListById.put(musicId, new MutableMediaMetadata(musicId, item));

                            } while(genreMediaCursor.moveToNext());
                        }
                    } while(genreCursor.moveToNext());
                }
                genreCursor.close();
                if (genreMediaCursor != null) {
                    genreMediaCursor.close();
                }

                Cursor allSongCursor = MusicUtils.getAllSongsCursor(LHApplication.getContext());
                try {
                    if (allSongCursor.moveToFirst()) {
                        do {
                            String title = allSongCursor.getString(0);
                            String artist = allSongCursor.getString(1);
                            String album = allSongCursor.getString(2);
                            long duration = allSongCursor.getLong(3);
                            String source = allSongCursor.getString(4);
                            int trackNumber = allSongCursor.getInt(5);
                            long totalTrackCount = allSongCursor.getLong(6);
                            String musicId = allSongCursor.getString(7);
                            long albumId = allSongCursor.getLong(8);
                            Uri albumArtUri = ContentUris.withAppendedId(ART_CONTENT_URI, albumId);

                            if (mMusicListById.containsKey(musicId)) {
                                continue;
                            }

                            MediaMetadataCompat item = new MediaMetadataCompat.Builder()
                                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, musicId)
                                    .putString(CUSTOM_METADATA_TRACK_SOURCE, source)
                                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                                    .putString(MediaMetadataCompat.METADATA_KEY_GENRE, UNKNOWN_TAG)
                                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, albumArtUri.toString())
                                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                                    .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, trackNumber)
                                    .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, totalTrackCount)
                                    .build();
                            mMusicListById.put(musicId, new MutableMediaMetadata(musicId, item));

                        } while(allSongCursor.moveToNext());
                    }
                } finally {
                    if (allSongCursor != null) {
                        allSongCursor.close();
                    }
                }

                buildListsByGenre();
                buildListsByArtist();
                buildListsByAlbum();

                mCurrentState = State.INITIALIZED;
            }
        } finally {
            if (mCurrentState != State.INITIALIZED) {
                // Something bad happened, so we reset state to NON_INITIALIZED to allow
                // retries (eg if the network connection is temporary unavailable)
                mCurrentState = State.NON_INITIALIZED;
            }
        }
    }

    private synchronized void retrieveMediaFromGoogle() {
        try {
            if (mCurrentState == State.NON_INITIALIZED) {
                mCurrentState = State.INITIALIZING;
                int slashPos = CATALOG_URL.lastIndexOf('/');
                String path = CATALOG_URL.substring(0, slashPos + 1);
                JSONObject jsonObj = fetchJSONFromUrl(CATALOG_URL);
                if (jsonObj == null) {
                    return;
                }
                JSONArray tracks = jsonObj.getJSONArray(JSON_MUSIC);
                if (tracks != null) {
                    for (int j = 0; j < tracks.length(); j++) {
                        MediaMetadataCompat item = buildFromJSON(tracks.getJSONObject(j), path);
                        String musicId = item.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
                        mMusicListById.put(musicId, new MutableMediaMetadata(musicId, item));
                    }
                    buildListsByGenre();
                }
                mCurrentState = State.INITIALIZED;
            }
        } catch (JSONException e) {
            LogUtils.e(TAG, e, "Could not retrieve music list");
        } finally {
            if (mCurrentState != State.INITIALIZED) {
                // Something bad happened, so we reset state to NON_INITIALIZED to allow
                // retries (eg if the network connection is temporary unavailable)
                mCurrentState = State.NON_INITIALIZED;
            }
        }
    }

    private MediaMetadataCompat buildFromJSON(JSONObject json, String basePath) throws JSONException {
        String title = json.getString(JSON_TITLE);
        String album = json.getString(JSON_ALBUM);
        String artist = json.getString(JSON_ARTIST);
        String genre = json.getString(JSON_GENRE);
        String source = json.getString(JSON_SOURCE);
        String iconUrl = json.getString(JSON_IMAGE);
        int trackNumber = json.getInt(JSON_TRACK_NUMBER);
        int totalTrackCount = json.getInt(JSON_TOTAL_TRACK_COUNT);
        int duration = json.getInt(JSON_DURATION) * 1000; // ms

        LogUtils.d(TAG, "Found music track: ", json);

        // Media is stored relative to JSON file
        if (!source.startsWith("http")) {
            source = basePath + source;
        }
        if (!iconUrl.startsWith("http")) {
            iconUrl = basePath + iconUrl;
        }
        // Since we don't have a unique ID in the server, we fake one using the hashcode of
        // the music source. In a real world app, this could come from the server.
        String id = String.valueOf(source.hashCode());

        // Adding the music source to the MediaMetadata (and consequently using it in the
        // mediaSession.setMetadata) is not a good idea for a real world music app, because
        // the session metadata can be accessed by notification listeners. This is done in this
        // sample for convenience only.
        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, id)
                .putString(CUSTOM_METADATA_TRACK_SOURCE, source)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                .putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, iconUrl)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, trackNumber)
                .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, totalTrackCount)
                .build();
    }

    /**
     * Download a JSON file from a server, parse the content and return the JSON object.
     *
     * @return result JSONObject containing the parsed representation.
     */
    private JSONObject fetchJSONFromUrl(String urlString) {
        BufferedReader reader = null;
        try {
            URLConnection urlConnection = new URL(urlString).openConnection();
            reader = new BufferedReader(new InputStreamReader(
                    urlConnection.getInputStream(), "iso-8859-1"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return new JSONObject(sb.toString());
        } catch (Exception e) {
            LogUtils.e(TAG, "Failed to parse the json for media list", e);
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }
}

