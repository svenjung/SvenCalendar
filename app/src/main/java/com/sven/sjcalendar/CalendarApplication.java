package com.sven.sjcalendar;

import android.app.Application;

import com.github.moduth.blockcanary.BlockCanary;
import com.squareup.leakcanary.LeakCanary;

import timber.log.Timber;

/**
 * Created by Sven.J on 18-4-8.
 */
public class CalendarApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        // Normal app init code...
        LeakCanary.install(this);
        BlockCanary.install(this, new AppBlockCanaryContext()).start();

        // init timber
        Timber.plant(new Timber.DebugTree());
    }
}
