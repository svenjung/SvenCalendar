package com.sven.sjcalendar;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by Sven.J on 18-4-8.
 */
public class CalendarApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // init timber
        Timber.plant(new Timber.DebugTree());
    }
}
