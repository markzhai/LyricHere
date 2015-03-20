package com.markzhai.lyrichere;

import android.app.Application;
import android.content.Context;

public class LHApplication extends Application {

    private static Context context = null;
    private static Application application = null;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        application = this;
    }

    public static Context getContext() {
        return context;
    }

    public static Application getApplication() {
        return application;
    }
}
