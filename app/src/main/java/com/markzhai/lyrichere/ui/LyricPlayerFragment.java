package com.markzhai.lyrichere.ui;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import com.markzhai.lyrichere.utils.LogHelper;
import com.markzhai.lyrichere.utils.LyricProvider;
import com.markzhai.lyrichere.workers.LyricEncodingUpdater;
import com.markzhai.lyrichere.workers.LyricLastVisitUpdater;

public class LyricPlayerFragment extends Fragment {
    private static final String TAG = LyricPlayerFragment.class.getSimpleName();
    private static final String ARG_PARAM_PATH = "path";
    private static final String ARG_PARAM_ENCODING = "encoding";
    private String mFilePath;
    private String mEncoding;

    private OnFragmentInteractionListener mListener;

    private LyricView mLyricView;
    private Handler mHandler = new Handler();
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
    private UIUpdateRunnable mUiUpdateRunnable = null;
    private Thread mUiUpdateThread = null;
    private NotificationManager mNotificationManager;

    private LyricProvider mLyricProvider;
    private boolean mIsForeground;

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
        LogHelper.i(TAG, String.format("newInstance(%s, %s)", path, encoding));
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
        LogHelper.i(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_lyric_player, container, false);
        mLyricView = (LyricView) view.findViewById(R.id.textLyric);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        LogHelper.i(TAG, "onResume");

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mIsForeground = true;
        // Not first time createView
        String path = getActivity().getIntent().getStringExtra(Constants.Column.PATH);
        String encoding = getActivity().getIntent().getStringExtra(Constants.Column.ENCODING);
        if (path != null && !mFilePath.equals(path)) {
            mFilePath = path;
            mEncoding = encoding;

            mUiUpdateRunnable.stop();
            while (mUiUpdateThread.isAlive()) {
                // wait till dead
            }
            mUiUpdateRunnable.reset();
            mUiUpdateThread = new Thread(mUiUpdateRunnable);
            mLyricView.setLyric(mLyricProvider.get(mFilePath, mEncoding));
            mLyricView.setLyricIndex(0);
            mUiUpdateThread.start();

            new LyricLastVisitUpdater(this.getActivity()).execute(mFilePath);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        LogHelper.i(TAG, "onCreateOptionsMenu");
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_encoding, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mEncoding.equals(Constants.ENCODE_UTF_8)) {
            menu.findItem(R.id.action_encoding_utf_8).setChecked(true);
        } else if (mEncoding.equals(Constants.ENCODE_BIG5)) {
            menu.findItem(R.id.action_encoding_big5).setChecked(true);
        } else if (mEncoding.equals(Constants.ENCODE_GBK)) {
            menu.findItem(R.id.action_encoding_gbk).setChecked(true);
        } else if (mEncoding.equals(Constants.ENCODE_SJIS)) {
            menu.findItem(R.id.action_encoding_sjis).setChecked(true);
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
            LogHelper.i(TAG, "Changed encoding: " + encoding);
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
        LogHelper.i(TAG, "onDestroy");
        mUiUpdateRunnable.stop();
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStop() {
        super.onStop();
        LogHelper.i(TAG, "onStop");
        mIsForeground = false;
    }

    @Override
    public void onStart() {
        super.onStart();
        LogHelper.i(TAG, "onStart");
        if (mUiUpdateRunnable == null && mUiUpdateThread == null) {
            mUiUpdateRunnable = new UIUpdateRunnable();
            mUiUpdateThread = new Thread(mUiUpdateRunnable);
            mLyricView.setLyric(mLyricProvider.get(mFilePath, mEncoding));
            new LyricLastVisitUpdater(this.getActivity()).execute(mFilePath);
        }
        if (!mUiUpdateThread.isAlive() && !mUiUpdateRunnable.isStopped() && !mUiUpdateRunnable.mStopped) {
            mUiUpdateThread.start();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        LogHelper.i(TAG, "onPause");
    }

    public boolean isForeground() {
        return mIsForeground;
    }

    /**
     * See <a href="http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

    private class UIUpdateRunnable implements Runnable {
        private long mStartTime = -1;
        private long mNextSentenceTime = -1;
        private boolean mStopping = false;
        private boolean mStopped = false;
        private NotificationCompat.Builder mNotifyBuilder;

        public void reset() {
            mStartTime = -1;
            mNextSentenceTime = -1;
            mStopping = false;
        }

        public void stop() {
            mStopping = true;
        }

        public boolean isStopped() {
            return mStopped;
        }

        // TODO: Improve user touch response
        public void run() {
            if (mStartTime == -1) {
                mStartTime = System.currentTimeMillis();
            }

            boolean isNotificationOn = mSharedPreferences.getBoolean(getString(R.string.pref_key_notification), true);

            NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle().bigText("...");

            if (isNotificationOn) {
                bigTextStyle.setSummaryText(getString(R.string.notification_expand_summary));
                mNotifyBuilder = new NotificationCompat.Builder(getActivity())
                        .setContentTitle(mLyricView.getLyric().getArtist() + " - " + mLyricView.getLyric().getTitle())
                        .setContentText("...")
                        .setTicker(mLyricView.getLyric().getTitle())
                        .setContentIntent(PendingIntent.getActivity(getActivity(), 0, new Intent(getActivity(), LyricPlayerActivity.class), 0))
                        .setSmallIcon(R.drawable.kid)
                        .setStyle(bigTextStyle);
                //mNotificationManager.notify(Constants.NOTIFY_ID, mNotifyBuilder.build());
            }

            while (mLyricView.getLyricIndex() != -2) {
                if (mStopping) {
                    mStopped = true;
                    return;
                }
                long ts = System.currentTimeMillis() - mStartTime;
                if (ts >= mNextSentenceTime || mLyricView.checkUpdate()) {
                    mNextSentenceTime = mLyricView.updateIndex(ts);
                    // LogHelper.i(TAG, String.format("mNextSentenceTime: %d, ts %d, mLyricIndex: %d", mNextSentenceTime, ts, mLyricView.getLyricIndex()));

                    // Redraw only when fragment is visible
                    if (mIsForeground) {
                        mHandler.post(mUpdateResultsRunnable);
                    } else if (isNotificationOn) {
                        String sentence = mLyricView.getCurrentSentence();

                        //.setSmallIcon(R.drawable.ic_notify_status)
                        if (sentence != null) {
                            mNotifyBuilder.setContentText(sentence);
                            bigTextStyle.bigText(sentence);
                        }
                        // Because the ID remains unchanged, the existing notification is updated.
                        mNotificationManager.notify(Constants.NOTIFY_ID, mNotifyBuilder.build());
                    }
                }
                if (mNextSentenceTime == -1) {
                    mStopped = true;
                    if (isNotificationOn) {
                        mNotificationManager.cancel(Constants.NOTIFY_ID);
                    }
                    // Clear KEEP_SCREEN_ON flag when finish playing
                    mHandler.post(mClearScreenOnFlagRunnable);
                    return;
                }
            }
        }
    }
}