package com.sven.sjcalendar.hotspots;

import android.content.Context;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.sven.sjcalendar.hotspots.almanac.AlmanacAdapterLoader;
import com.sven.sjcalendar.hotspots.schedule.EventLoader;
import com.sven.sjcalendar.hotspots.schedule.SchedulerAdapter;
import com.sven.sjcalendar.hotspots.schedule.SchedulerAdapterLoader;

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

    /**
     * 1.先获取首页卡片配置信息
     * 2.再加载具体卡片
     */
    public void startLoadAdapter() {
        mPreLoader = Observable.merge(createEventLoader().getAdapterInfo(),
                createAlmanacLoader().getAdapterInfo())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<AdapterInfo>() {
            @Override
            public void accept(AdapterInfo info) throws Exception {
                if (info != null) {
                    mDelegateAdapter.addAdapter(info.mAdapter);
                    mDelegateAdapter.notifyDataSetChanged();
                }
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

    private AdapterLoader createEventLoader() {
        return new SchedulerAdapterLoader(mContext, mTargetDay);
    }

    private AdapterLoader createAlmanacLoader() {
        return new AlmanacAdapterLoader();
    }

    private Observable getHotspotsAdapters() {
        return null;
    }
}
