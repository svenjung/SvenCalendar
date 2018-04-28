package com.sven.sjcalendar.widget;

import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * 控制滑动的duration, 主要用于ViewPager切换页面跨度过大会出现空白的情况
 * Created by Sven.J on 18-4-27.
 */
public class NoDurationScroller extends Scroller {

    private boolean mScrollWithOutDuration = false;

    public void setNoDuration(boolean duration) {
        mScrollWithOutDuration = duration;
    }

    public NoDurationScroller(Context context) {
        super(context);
    }

    public NoDurationScroller(Context context, Interpolator interpolator) {
        super(context, interpolator);
    }

    public NoDurationScroller(Context context, Interpolator interpolator, boolean flywheel) {
        super(context, interpolator, flywheel);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        if (mScrollWithOutDuration) {
            super.startScroll(startX, startY, dx, dy, 0);
        } else {
            super.startScroll(startX, startY, dx, dy, duration);
        }
    }
}
