package com.sven.sjcalendar.hotspots;

import io.reactivex.Observable;

/**
 * 卡片加载器
 * Created by Sven.J on 18-5-10.
 */
public interface AdapterLoader {
    Observable<AdapterInfo> getAdapterInfo();
}
