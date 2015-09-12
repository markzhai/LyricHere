package com.markzhai.lyrichere.ui;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import icepick.Icepick;

/**
 * Base Fragment with dependency injection.
 * <p/>
 * Created by zyf on 2015/9/12.
 */
public class BaseFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        int layoutRes = getLayoutRes();
        if (layoutRes == 0) {
            throw new IllegalArgumentException(
                    "getLayoutRes() returned 0, which is not allowed. "
                            + "If you don't want to use getLayoutRes() but implement your own view for this "
                            + "fragment manually, then you have to override onCreateView();");
        } else {
            return inflater.inflate(layoutRes, container, false);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    /**
     * Return the layout resource like R.layout.my_layout
     *
     * @return the layout resource or zero ("0"), if you don't want to have an UI
     */
    @LayoutRes
    protected int getLayoutRes() {
        return 0;
    }
}
