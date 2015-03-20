package com.markzhai.lyrichere.ui;

import android.content.Context;

import com.markzhai.lyrichere.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 抽屉菜单条目
 * <p/>
 * Created by markzhai on 2015/3/19.
 */
public class DrawerMenuContents {
    public static final String FIELD_TITLE = "title";
    public static final String FIELD_ICON = "icon";

    private ArrayList<Map<String, ?>> items;
    private Class[] activities;

    public DrawerMenuContents(Context ctx) {
        activities = new Class[2];
        items = new ArrayList<>(2);

        activities[0] = MusicPlayerActivity.class;
        items.add(populateDrawerItem(ctx.getString(R.string.drawer_allmusic_title),
                R.drawable.ic_allmusic_black_24dp));

        activities[1] = LyricExplorerActivity.class;
        items.add(populateDrawerItem(ctx.getString(R.string.drawer_playlists_title),
                R.drawable.ic_playlist_music_black_24dp));
    }

    public List<Map<String, ?>> getItems() {
        return items;
    }

    public Class getActivity(int position) {
        return activities[position];
    }

    public int getPosition(Class activityClass) {
        for (int i = 0; i < activities.length; i++) {
            if (activities[i].equals(activityClass)) {
                return i;
            }
        }
        return -1;
    }

    private Map<String, ?> populateDrawerItem(String title, int icon) {
        HashMap<String, Object> item = new HashMap<>();
        item.put(FIELD_TITLE, title);
        item.put(FIELD_ICON, icon);
        return item;
    }
}
