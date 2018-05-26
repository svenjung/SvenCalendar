package com.sven.sjcalendar.hotspots.schedule;

import android.content.Context;

import com.sven.sjcalendar.hotspots.AdapterInfo;
import com.sven.sjcalendar.hotspots.AdapterLoader;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * TODO register events changed observer
 */
public class SchedulerAdapterLoader implements AdapterLoader {
    private Context context;
    private int day;

    public SchedulerAdapterLoader(Context context, int day) {
        this.context = context;
        this.day = day;
    }

    @Override
    public Observable<AdapterInfo> getAdapterInfo() {
        return Observable.create(new ObservableOnSubscribe<AdapterInfo>() {
            @Override
            public void subscribe(ObservableEmitter<AdapterInfo> emitter) throws Exception {
                AdapterInfo info = new AdapterInfo();
                info.mOrder = 0;
                info.mAdapter = new SchedulerAdapter(EventLoader.loadEvents(context, day));

                emitter.onNext(info);
                emitter.onComplete();
            }

        });
    }
}
