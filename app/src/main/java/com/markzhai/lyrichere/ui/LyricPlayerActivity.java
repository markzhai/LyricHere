package com.markzhai.lyrichere.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.markzhai.lyrichere.Constants;
import com.markzhai.lyrichere.R;

public class LyricPlayerActivity extends ActionBarActivity
        implements LyricPlayerFragment.OnFragmentInteractionListener {
    private static final String TAG = LyricPlayerActivity.class.getSimpleName();

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        moveTaskToBack(true);
    }

    /**
     * Override onNewIntent to get new intent when re-entering
     *
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
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
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public void onFragmentInteraction(Uri uri) {
        Toast toast = Toast.makeText(this, "Ya!", Toast.LENGTH_SHORT);
        toast.show();
    }
}