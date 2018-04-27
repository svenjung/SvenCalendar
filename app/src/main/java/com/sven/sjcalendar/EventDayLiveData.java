package com.sven.sjcalendar;

import android.arch.lifecycle.LiveData;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.provider.CalendarContract;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sven.J on 18-1-3.
 */

public class EventDayLiveData extends LiveData<List<Integer>> {

    private ContentResolver mContentResolver;

    private ContentObserver mContentObserver = new ContentObserver(null) {
        @Override
        public void onChange(boolean selfChange) {
            Log.i("EventDayLiveData", "onChange, " + Thread.currentThread().getName());
            postValue(new ArrayList<Integer>());
        }
    };

    public EventDayLiveData(Context context) {
        mContentResolver = context.getContentResolver();
    }

    @Override
    protected void onActive() {
        Log.i("EventDayLiveData", "onActive");
        mContentResolver.registerContentObserver(CalendarContract.Events.CONTENT_URI, true, mContentObserver);
    }

    @Override
    protected void onInactive() {
        Log.i("EventDayLiveData", "onInactive");
        mContentResolver.unregisterContentObserver(mContentObserver);
    }
}
