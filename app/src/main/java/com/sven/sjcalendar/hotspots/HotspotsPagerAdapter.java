package com.sven.sjcalendar.hotspots;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.sven.dateview.TimeCalendar;
import com.sven.sjcalendar.widget.RecycledPagerAdapter;

/**
 * Created by Sven.J on 18-5-10.
 */
public class HotspotsPagerAdapter extends RecycledPagerAdapter<RecyclerView> {

    private int mMinDay;
    private int mMaxDay;

    private int mToday;

    public HotspotsPagerAdapter() {
        TimeCalendar calendar = TimeCalendar.getInstance();
        mToday = calendar.getJulianDay();
        calendar.set(1970, 0, 1);
        mMinDay = calendar.getJulianDay();
        calendar.set(2037, 11, 31);
        mMaxDay = calendar.getJulianDay();
    }

    @Override
    public RecyclerView createView(Context context) {
        return new RecyclerView(context);
    }

    @Override
    public void bindView(@NonNull RecyclerView view, int position) {
        Context context = view.getContext();
        view.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        VirtualLayoutManagerEx layoutManager = new VirtualLayoutManagerEx(context);
        DelegateAdapter adapter = new DelegateAdapter(layoutManager, true);
        layoutManager.setAdapterManager(new HotspotsAdapterManager(context, adapter,mMinDay + position));
        view.setLayoutManager(layoutManager);
        view.setAdapter(adapter);
    }

    @Override
    public int getCount() {
        return mMaxDay - mMinDay + 1;
    }

}
