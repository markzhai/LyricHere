package com.markzhai.lyrichere;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

import com.raizlabs.android.dbflow.config.FlowManager;

public class LHApplication extends Application {

    private static Context context = null;
    private static Application application = null;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        application = this;
        FlowManager.init(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        FlowManager.destroy();
        application = null;
        context = null;
    }

    public static Context getContext() {
        return context;
    }

    public static Resources getResource() {
        return context.getResources();
    }

    public static Application getApplication() {
        return application;
    }
}
