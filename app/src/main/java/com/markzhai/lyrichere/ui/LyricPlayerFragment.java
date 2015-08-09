package com.markzhai.lyrichere.ui;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.markzhai.lyrichere.Constants;
import com.markzhai.lyrichere.R;
import com.markzhai.lyrichere.utils.LogUtils;
import com.markzhai.lyrichere.utils.LyricProvider;
import com.markzhai.lyrichere.workers.LyricEncodingUpdater;
import com.markzhai.lyrichere.workers.LyricLastVisitUpdater;

import cn.zhaiyifan.lyric.widget.LyricView;

public class LyricPlayerFragment extends Fragment implements LyricView.OnLyricUpdateListener {
    private static final String TAG = LyricPlayerFragment.class.getSimpleName();
    private static final String ARG_PARAM_PATH = "path";
    private static final String ARG_PARAM_ENCODING = "encoding";
    private String mFilePath;
    private String mEncoding;

    private LyricView mLyricView;
    private Runnable mUpdateResultsRunnable = new Runnable() {
        public void run() {
            mLyricView.invalidate();
        }
    };
    private Runnable mClearScreenOnFlagRunnable = new Runnable() {
        public void run() {
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    };
    private NotificationManager mNotificationManager;

    private LyricProvider mLyricProvider;

    private SharedPreferences mSharedPreferences;

    public LyricPlayerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of this fragment using the provided parameters.
     *
     * @param path Lyric full path.
     * @return A new instance of fragment LyricPlayerFragment.
     */
    public static LyricPlayerFragment newInstance(String path, String encoding) {
        LogUtils.i(TAG, String.format("newInstance(%s, %s)", path, encoding));
        LyricPlayerFragment fragment = new LyricPlayerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM_PATH, path);
        args.putString(ARG_PARAM_ENCODING, encoding);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mFilePath = getArguments().getString(ARG_PARAM_PATH);
            mEncoding = getArguments().getString(ARG_PARAM_ENCODING);
        }
        mLyricProvider = LyricProvider.getInstance(getActivity());

        mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LogUtils.i(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_lyric_player, container, false);
        mLyricView = (LyricView) view.findViewById(R.id.lyricView);
        mLyricView.setOnLyricUpdateListener(this);
        mLyricView.setLyric(mLyricProvider.get(mFilePath, mEncoding));
        mLyricView.setLyricIndex(0);
        mLyricView.play();
        return view;
    }

    boolean mIsForeground = false;

    @Override
    public void onResume() {
        super.onResume();
        LogUtils.i(TAG, "onResume");

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mIsForeground = true;

        // Not first time createView
        String path = getActivity().getIntent().getStringExtra(Constants.Column.PATH);
        String encoding = getActivity().getIntent().getStringExtra(Constants.Column.ENCODING);

        // new lyric file
        if (path != null && !mFilePath.equals(path)) {
            mFilePath = path;
            mEncoding = encoding;

            mLyricView.stop();
            mLyricView.setLyric(mLyricProvider.get(mFilePath, mEncoding));
            mLyricView.setLyricIndex(0);
            mLyricView.play();

            new LyricLastVisitUpdater(getActivity()).execute(mFilePath);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_encoding, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        switch (mEncoding) {
            case Constants.ENCODE_UTF_8:
                menu.findItem(R.id.action_encoding_utf_8).setChecked(true);
                break;
            case Constants.ENCODE_BIG5:
                menu.findItem(R.id.action_encoding_big5).setChecked(true);
                break;
            case Constants.ENCODE_GBK:
                menu.findItem(R.id.action_encoding_gbk).setChecked(true);
                break;
            case Constants.ENCODE_SJIS:
                menu.findItem(R.id.action_encoding_sjis).setChecked(true);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        String encoding = null;
        switch (id) {
            case R.id.action_encoding_utf_8:
                item.setChecked(true);
                encoding = Constants.ENCODE_UTF_8;
                break;
            case R.id.action_encoding_big5:
                item.setChecked(true);
                encoding = Constants.ENCODE_BIG5;
                break;
            case R.id.action_encoding_sjis:
                item.setChecked(true);
                encoding = Constants.ENCODE_SJIS;
                break;
            case R.id.action_encoding_gbk:
                item.setChecked(true);
                encoding = Constants.ENCODE_GBK;
                break;
            default:
                return false;
        }
        if (encoding != null && !mEncoding.equals(encoding)) {
            LogUtils.i(TAG, "Changed encoding: " + encoding);
            mEncoding = encoding;
            mLyricView.setLyric(mLyricProvider.get(mFilePath, mEncoding));
            mLyricView.invalidate();
            new LyricEncodingUpdater(getActivity()).execute(mFilePath, mEncoding);
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.i(TAG, "onDestroy");
        mLyricView.stop();
    }

    @Override
    public void onStop() {
        super.onStop();
        LogUtils.i(TAG, "onStop");
        mIsForeground = false;
    }

    private NotificationCompat.Builder mNotifyBuilder;
    NotificationCompat.BigTextStyle bigTextStyle;

    @Override
    public void onLyricUpdate() {

        boolean isNotificationOn = mSharedPreferences.getBoolean(getString(R.string.pref_key_notification), true);

        if (isNotificationOn) {
            if (mNotifyBuilder == null) {
                bigTextStyle = new NotificationCompat.BigTextStyle().bigText("...");
                bigTextStyle.setSummaryText(getString(R.string.notification_expand_summary));
                mNotifyBuilder = new NotificationCompat.Builder(getActivity())
                        .setContentTitle(mLyricView.lyric.artist + " - " + mLyricView.lyric.title)
                        .setContentText("...")
                        .setTicker(mLyricView.lyric.title)
                        .setContentIntent(PendingIntent.getActivity(getActivity(), 0, new Intent(getActivity(), LyricPlayerActivity.class), 0))
                        .setSmallIcon(R.drawable.kid)
                        .setStyle(bigTextStyle);
            }

            String sentence = mLyricView.getCurrentSentence();

            //.setSmallIcon(R.drawable.ic_notify_status)
            if (sentence != null) {
                mNotifyBuilder.setContentText(sentence);
                bigTextStyle.bigText(sentence);
                // Because the ID remains unchanged, the existing notification is updated.
                mNotificationManager.notify(Constants.NOTIFY_ID, mNotifyBuilder.build());
            }

            // mHandler.post(mClearScreenOnFlagRunnable);
            // mNotificationManager.cancel(Constants.NOTIFY_ID);
        }
    }
}