package com.sven.sjcalendar.hotspots.almanac;

import com.sven.sjcalendar.hotspots.AdapterInfo;
import com.sven.sjcalendar.hotspots.AdapterLoader;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

public class AlmanacAdapterLoader implements AdapterLoader {

    @Override
    public Observable<AdapterInfo> getAdapterInfo() {
        return Observable.create(new ObservableOnSubscribe<AdapterInfo>() {
            @Override
            public void subscribe(ObservableEmitter<AdapterInfo> emitter) throws Exception {
                AdapterInfo info = new AdapterInfo();
                info.mOrder = 1;

                info.mAdapter = new AlmanacAdapter();

                if (!emitter.isDisposed()) {
                    emitter.onNext(info);
                    emitter.onComplete();
                }
            }
        });
    }
}
