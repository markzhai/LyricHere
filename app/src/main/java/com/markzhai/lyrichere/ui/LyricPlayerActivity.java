package com.markzhai.lyrichere.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;

import com.hannesdorfmann.mosby.mvp.MvpBasePresenter;
import com.hannesdorfmann.mosby.mvp.MvpPresenter;
import com.markzhai.lyrichere.app.Constants;
import com.markzhai.lyrichere.R;

public class LyricPlayerActivity extends BaseActivity {
    private static final String TAG = LyricPlayerActivity.class.getSimpleName();

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        moveTaskToBack(true);
    }

    /**
     * Override onNewIntent to get new intent when re-entering
     *
     * @param intent new intent when re-entering
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String path = getIntent().getStringExtra(Constants.Column.PATH);
        String encoding = getIntent().getStringExtra(Constants.Column.ENCODING);

        setContentView(R.layout.activity_lyric_player);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, LyricPlayerFragment.newInstance(path, encoding))
                    .commit();
        }
    }

    @NonNull
    @Override
    public MvpPresenter createPresenter() {
        return new MvpBasePresenter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
}