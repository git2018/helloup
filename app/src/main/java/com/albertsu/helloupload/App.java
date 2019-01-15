package com.albertsu.helloupload;

import android.app.Application;

import com.facebook.stetho.Stetho;

public class App extends Application {
    private static App instance;

    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Stetho.initializeWithDefaults(this);
    }
}
