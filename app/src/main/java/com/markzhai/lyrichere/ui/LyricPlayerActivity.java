package com.markzhai.lyrichere.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.markzhai.lyrichere.Constants;
import com.markzhai.lyrichere.R;

public class LyricPlayerActivity extends AppCompatActivity {
    private static final String TAG = LyricPlayerActivity.class.getSimpleName();

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        moveTaskToBack(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
}