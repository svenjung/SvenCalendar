package com.sven.sjcalendar.event;

import android.arch.lifecycle.MutableLiveData;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.icu.util.TimeZone;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.EventDays;
import android.provider.CalendarContract.Instances;

import com.sven.dateview.TimeCalendar;
import com.sven.sjcalendar.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * 加载有事件的天,监听数据库变化
 * Created by Sven.J on 18-5-7.
 */
public class EventDayLiveData extends MutableLiveData<List<Integer>> {

    private static final String[] PROJECTION = new String[]{
            Instances.START_DAY,
            Instances.END_DAY
    };
    private static final int INDEX_START_DAY = 0;
    private static final int INDEX_END_DAY = 1;

    public static final String SELECTION = Calendars.VISIBLE + "=1";

    private ContentResolver mContentResolver;
    private Handler mMainHandler;

    private ContentObserver mContentObserver;

    private PublishProcessor<Object> triggers;
    private Disposable mEventBus;
    private CompositeDisposable mQueries;

    // 事件加载的起始天, julianDay
    private int mStartDay;
    // 事件加载的结束天, julianDay
    private int mEndDay;

    private Runnable mTimeChangeUpdater = new Runnable() {
        @Override
        public void run() {
            // reload event day
            if (triggers != null) {
                triggers.onNext("TimeChange");
            }
        }
    };

    public EventDayLiveData(Context content) {
        mMainHandler = new Handler(Looper.getMainLooper());
        mContentResolver = content.getApplicationContext().getContentResolver();
        mQueries = new CompositeDisposable();
        triggers = PublishProcessor.create();

        mContentObserver = new ContentObserver(mMainHandler) {
            @Override
            public void onChange(boolean selfChange) {
                // reload event day
                if (triggers != null) {
                    triggers.onNext("EventChange");
                }
            }
        };

        TimeCalendar time = new TimeCalendar(1970, 0, 1);
        mStartDay = time.getJulianDay();
        time.set(2037, 11,31);
        mEndDay = time.getJulianDay();
    }

    @Override
    protected void onActive() {
        Timber.d("    onActive");
        // 过滤 200 毫秒内的多次加载请求
        mEventBus = triggers.throttleFirst(200, TimeUnit.MILLISECONDS)
                .doOnNext(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        Timber.d(" receive action : %s", o.toString());
                    }
                })
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        loadEventDays();
                    }
                });

        //Utils.setMidnightUpdater(mMainHandler, mTimeChangeUpdater, TimeZone.getDefault().getID());
        mContentResolver.registerContentObserver(CalendarContract.CONTENT_URI, true, mContentObserver);
        triggers.onNext("InitValue");
    }

    @Override
    protected void onInactive() {
        Timber.d("    onInactive");
        mContentResolver.unregisterContentObserver(mContentObserver);
        //Utils.resetMidnightUpdater(mMainHandler, mTimeChangeUpdater);

        if (mQueries != null) {
            mQueries.dispose();
        }

        if (mEventBus != null) {
            mEventBus.dispose();
        }
    }

    private void loadEventDays() {
        final Disposable disposable = Observable.create(
                new ObservableOnSubscribe<List<Integer>>() {
                    @Override
                    public void subscribe(ObservableEmitter<List<Integer>> emitter) throws Exception {
                        if (emitter.isDisposed()) {
                            return;
                        }

                        try {
                            emitter.onNext(queryEventDay());
                            emitter.onComplete();
                        } catch (Exception e) {
                            emitter.onError(e);
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Integer>>() {
                    @Override
                    public void accept(List<Integer> list) throws Exception {
                        setValue(list);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Timber.i(throwable,"Query event day failed");
                    }
                });

        mQueries.add(disposable);
    }

    private List<Integer> queryEventDay() {
        Uri.Builder builder = EventDays.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, mStartDay);
        ContentUris.appendId(builder, mEndDay);
        Uri uri = builder.build();


        Cursor c = mContentResolver.query(uri, PROJECTION, SELECTION, null, null);
        List<Integer> dayList = new ArrayList<>();

        if (c != null && c.moveToFirst()) {
            while (!c.isAfterLast()) {
                int startDay = c.getInt(INDEX_START_DAY);
                int endDay = c.getInt(INDEX_END_DAY);
                dayList.add(startDay);
                c.moveToNext();
            }
        }

        if (c != null) {
            c.close();
        }

        return dayList;
    }
}
