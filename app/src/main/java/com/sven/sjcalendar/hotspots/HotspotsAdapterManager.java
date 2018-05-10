package com.sven.sjcalendar.hotspots;

import android.content.Context;

import com.alibaba.android.vlayout.DelegateAdapter;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Sven.J on 18-5-10.
 */
public class HotspotsAdapterManager {
    private Context mContext;
    private int mTargetDay;

    private DelegateAdapter mDelegateAdapter;

    private Disposable mPreLoader;

    private CompositeDisposable mLoaders;

    public HotspotsAdapterManager(Context context, DelegateAdapter adapter, int day) {
        mContext = context;
        mDelegateAdapter = adapter;
        mTargetDay = day;
    }

    public void startLoadAdapter() {
        EventsLoader loader = new EventsLoader(mContext, mTargetDay);
        mPreLoader = loader.getAdapterInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<AdapterInfo>() {
                    @Override
                    public void accept(AdapterInfo info) throws Exception {
                        mDelegateAdapter.addAdapter(info.mOrder, info.mAdapter);

                        mDelegateAdapter.notifyDataSetChanged();
                    }
                });
    }

    public void stopLoadAdapter() {
        if (mPreLoader != null) {
            mPreLoader.dispose();
        }

        if (mLoaders != null) {
            mLoaders.dispose();
        }
    }

    public static class EventsLoader implements AdapterLoader {

        private Context context;
        private int day;

        public EventsLoader(Context context, int day) {
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

                    info.mAdapter = new EventAdapter(EventLoader.loadEvents(context, day));

                    emitter.onNext(info);
                    emitter.onComplete();
                }

            });
        }
    }
}
