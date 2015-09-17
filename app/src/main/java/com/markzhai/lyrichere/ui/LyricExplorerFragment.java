package com.markzhai.lyrichere.ui;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter.ViewBinder;

import com.markzhai.lyrichere.app.Constants;
import com.markzhai.lyrichere.R;
import com.markzhai.lyrichere.adapters.LyricCursorAdapter;
import com.markzhai.lyrichere.utils.LogUtils;
import com.markzhai.lyrichere.widget.LyricSearchView;
import com.markzhai.lyrichere.workers.LyricEncodingUpdater;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the OnFragmentInteractionListener interface.
 */
public class LyricExplorerFragment extends ListFragment implements LoaderCallbacks<Cursor>,
        SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    private static final String TAG = LyricExplorerFragment.class.getSimpleName();
    private static final String ARG_PARAM = "section_type";
    private static final String[] FROM = {Constants.Column.TITLE, Constants.Column.ARTIST,
            Constants.Column.ALBUM};
    private static final int[] TO = {R.id.listview_item_left_one, R.id.listview_item_left_two,
            R.id.listview_item_right};
    private static final int LOADER_ID = 56;
    /**
     * Handles custom binding of data to view.
     */
    private static final ViewBinder VIEW_BINDER = new ViewBinder() {
        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            long timestamp;
            // Custom binding
            switch (view.getId()) {
                /*
                case R.id.listview_item_left_one:
                    timestamp = cursor.getLong(columnIndex);
                    CharSequence relTime = DateUtils
                            .getRelativeTimeSpanString(timestamp);
                    ((TextView) view).setText(relTime);
                    return true; */
                default:
                    return false;
            }
        }
    };
    private LyricSearchView mSearchView;
    private int mParam;
    private OnFragmentInteractionListener mListener;
    private LyricCursorAdapter mAdapter;

    /**
     * For the fragment manager to instantiate the fragment (e.g. upon screen orientation changes).
     */
    public LyricExplorerFragment() {

    }

    public static LyricExplorerFragment newInstance(int param) {
        LyricExplorerFragment fragment = new LyricExplorerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM, param);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam = getArguments().getInt(ARG_PARAM);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new LyricCursorAdapter(getActivity(), R.layout.explorer_item,
                null, FROM, TO, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        mAdapter.setViewBinder(VIEW_BINDER);

        setListAdapter(mAdapter);

        // Done on a separate thread
        getLoaderManager().initLoader(LOADER_ID, null, this);

        registerForContextMenu(getListView());
        getListView().setTextFilterEnabled(true);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater m = getActivity().getMenuInflater();
        menu.setHeaderTitle(R.string.menu_encoding_title);
        m.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        boolean handled = false;
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        // info.id will return row id
        // select * from lyric where title like "De%";

        Cursor cursor = (Cursor) mAdapter.getItem(info.position);
        String path = cursor.getString(cursor.getColumnIndex(Constants.Column.PATH));
        switch (item.getItemId()) {
            case R.id.menu_encoding_big5:
                new LyricEncodingUpdater(getActivity()).execute(path,
                        cursor.getString(cursor.getColumnIndex(Constants.Column.ID)),
                        Constants.ENCODE_BIG5);
                handled = true;
                break;
            case R.id.menu_encoding_gbk:
                new LyricEncodingUpdater(getActivity()).execute(path,
                        cursor.getString(cursor.getColumnIndex(Constants.Column.ID)),
                        Constants.ENCODE_GBK);
                handled = true;
                break;
            case R.id.menu_encoding_sjis:
                new LyricEncodingUpdater(getActivity()).execute(path,
                        cursor.getString(cursor.getColumnIndex(Constants.Column.ID)),
                        Constants.ENCODE_SJIS);
                handled = true;
                break;
            case R.id.menu_encoding_utf_8:
                new LyricEncodingUpdater(getActivity()).execute(path,
                        cursor.getString(cursor.getColumnIndex(Constants.Column.ID)),
                        Constants.ENCODE_UTF_8);
                handled = true;
                break;
            default:
                break;
        }
        if (handled) {
            return true;
        } else {
            return super.onContextItemSelected(item);
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
    public void onDestroy() {
        if (mAdapter != null && mAdapter.getCursor() != null) {
            mAdapter.getCursor().close();
        }
        super.onDestroy();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (null != mListener) {
            Cursor cursor = (Cursor) mAdapter.getItem(position);

            // TODO: Check if file exists
            String path = cursor.getString(cursor.getColumnIndex(Constants.Column.PATH));
            String encoding = cursor.getString(cursor.getColumnIndex(Constants.Column.ENCODING));

            Intent intent = new Intent(getActivity(), LyricPlayerActivity.class);
            intent.putExtra(Constants.Column.PATH, path);
            intent.putExtra(Constants.Column.ENCODING, encoding);

            startActivity(intent);

            // Notify the active callbacks interface.
            mListener.onFragmentInteraction(path);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id != LOADER_ID)
            return null;
        LogUtils.i(TAG, "onCreateLoader for mParam: " + mParam);

        CursorLoader cursorLoader = null;
        switch (mParam) {
            case 1:
                cursorLoader = new CursorLoader(getActivity(), Constants.CONTENT_URI,
                        null, null, null, Constants.TITLE_SORT);
                break;
            case 2:
                cursorLoader = new CursorLoader(getActivity(), Constants.CONTENT_URI,
                        null, null, null, Constants.RECENT_SORT);
                break;
            default:
                cursorLoader = new CursorLoader(getActivity(), Constants.CONTENT_URI,
                        null, null, null, Constants.DEFAULT_SORT);
                break;
        }
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        LogUtils.i(TAG, "onLoadFinished with cursor: " + cursor.getCount());
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.lyric_explorer_fragment, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        mSearchView = (LyricSearchView) menu.findItem(R.id.action_search).getActionView();

        if (mSearchView != null) {
            // Assumes current activity is the searchable activity
            mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
            mSearchView.setIconifiedByDefault(true);
            mSearchView.setOnQueryTextListener(this);
            mSearchView.setSubmitButtonEnabled(false);
            mSearchView.setOnCloseListener(this);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        LogUtils.i(TAG, "onQueryTextSubmit: " + query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        LogUtils.i(TAG, "onQueryTextChange: " + newText);
        if (TextUtils.isEmpty(newText.trim())) {
            getLoaderManager().restartLoader(LOADER_ID, null, this);
            /*
            mAdapter.getFilter().filter(constraint, new FilterListener() {
                public void onFilterComplete(int count) {
                    // assuming your activity manages the Cursor
                    // (which is a recommended way)
                    stopManagingCursor(oldCursor);
                    final Cursor newCursor = adapter.getCursor();
                    startManagingCursor(newCursor);
                    // safely close the oldCursor
                    if (oldCursor != null && !oldCursor.isClosed()) {
                        oldCursor.close();
                    }
                }
            });
            */
        } else {
            mAdapter.getFilter().filter(newText);
        }
        return true;
    }

    @Override
    public boolean onClose() {
        LogUtils.i(TAG, "onClose");
        return false;
    }

    /**
     * http://developer.android.com/training/basics/fragments/communicating.html
     */
    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(String id);
    }
}