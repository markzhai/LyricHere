package com.markzhai.lyrichere.utils;

import android.annotation.TargetApi;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.os.Build;

import com.markzhai.lyrichere.model.MusicProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Utility class to help on queue related tasks.
 */
public class QueueHelper {

    private static final String TAG = LogHelper.makeLogTag(QueueHelper.class);

    public static List<MediaSession.QueueItem> getPlayingQueue(String mediaId, MusicProvider musicProvider) {

        // extract the browsing hierarchy from the media ID:
        String[] hierarchy = MediaIDHelper.getHierarchy(mediaId);

        if (hierarchy.length != 2) {
            LogHelper.e(TAG, "Could not build a playing queue for this mediaId: ", mediaId);
            return null;
        }

        String categoryType = hierarchy[0];
        String categoryValue = hierarchy[1];
        LogHelper.d(TAG, "Creating playing queue for ", categoryType, ",  ", categoryValue);

        Iterable<MediaMetadata> tracks = null;
        // This sample only supports genre and by_search category types.
        if (categoryType.equals(MediaIDHelper.MEDIA_ID_MUSICS_BY_GENRE)) {
            tracks = musicProvider.getMusicsByGenre(categoryValue);
        } else if (categoryType.equals(MediaIDHelper.MEDIA_ID_MUSICS_BY_SEARCH)) {
            tracks = musicProvider.searchMusic(categoryValue);
        }

        if (tracks == null) {
            LogHelper.e(TAG, "Unrecognized category type: ", categoryType, " for mediaId ", mediaId);
            return null;
        }

        return convertToQueue(tracks, hierarchy[0], hierarchy[1]);
    }

    public static List<MediaSession.QueueItem> getPlayingQueueFromSearch(String query, MusicProvider musicProvider) {
        LogHelper.d(TAG, "Creating playing queue for musics from search ", query);
        return convertToQueue(musicProvider.searchMusic(query), MediaIDHelper.MEDIA_ID_MUSICS_BY_SEARCH, query);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static int getMusicIndexOnQueue(Iterable<MediaSession.QueueItem> queue, String mediaId) {
        int index = 0;
        for (MediaSession.QueueItem item : queue) {
            if (mediaId.equals(item.getDescription().getMediaId())) {
                return index;
            }
            index++;
        }
        return -1;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static int getMusicIndexOnQueue(Iterable<MediaSession.QueueItem> queue, long queueId) {
        int index = 0;
        for (MediaSession.QueueItem item : queue) {
            if (queueId == item.getQueueId()) {
                return index;
            }
            index++;
        }
        return -1;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static List<MediaSession.QueueItem> convertToQueue(Iterable<MediaMetadata> tracks, String... categories) {
        List<MediaSession.QueueItem> queue = new ArrayList<>();
        int count = 0;
        for (MediaMetadata track : tracks) {
            // We create a hierarchy-aware mediaID, so we know what the queue is about by looking
            // at the QueueItem media IDs.
            String hierarchyAwareMediaID = MediaIDHelper.createMediaID(
                    track.getDescription().getMediaId(), categories);

            MediaMetadata trackCopy = new MediaMetadata.Builder(track)
                    .putString(MediaMetadata.METADATA_KEY_MEDIA_ID, hierarchyAwareMediaID)
                    .build();

            // We don't expect queues to change after created, so we use the item index as the
            // queueId. Any other number unique in the queue would work.
            MediaSession.QueueItem item = new MediaSession.QueueItem(trackCopy.getDescription(), count++);
            queue.add(item);
        }
        return queue;

    }

    /**
     * Create a random queue. For simplicity sake, instead of a random queue, we create a
     * queue using the first genre.
     *
     * @param musicProvider the provider used for fetching music.
     * @return list containing {@link MediaSession.QueueItem}'s
     */
    public static List<MediaSession.QueueItem> getRandomQueue(MusicProvider musicProvider) {
        Iterator<String> genres = musicProvider.getGenres().iterator();
        if (!genres.hasNext()) {
            return Collections.emptyList();
        }
        String genre = genres.next();
        Iterable<MediaMetadata> tracks = musicProvider.getMusicsByGenre(genre);

        return convertToQueue(tracks, MediaIDHelper.MEDIA_ID_MUSICS_BY_GENRE, genre);
    }

    public static boolean isIndexPlayable(int index, List<MediaSession.QueueItem> queue) {
        return (queue != null && index >= 0 && index < queue.size());
    }
}

