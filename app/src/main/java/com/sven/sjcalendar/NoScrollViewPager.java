package com.sven.sjcalendar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.sven.sjcalendar.widget.NoDurationScroller;

/**
 * Created by Sven.J on 18-4-4.
 */
public class NoScrollViewPager extends ViewPager {
    boolean mEnableScroll = true;

    // 不能与ViewPager中的mScroller变量同名
    private NoDurationScroller scroller;

    public NoScrollViewPager(Context context) {
        this(context, null);
    }

    public NoScrollViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        initScroller(context);
    }

    private void initScroller(Context context) {
        scroller = new NoDurationScroller(context);
        try {
            Reflect.on(this).set("mScroller", scroller);
        } catch (Exception e) {
            Log.e("NoScrollViewPager", "set mScroller failed, " + e.getMessage());
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mEnableScroll) {
            return super.onInterceptTouchEvent(ev);
        } else {
            return false;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // mEnableScroll为false时,拦截touch事件
        return !mEnableScroll || super.onTouchEvent(ev);
    }

    public void setScrollEnable(boolean enable) {
        mEnableScroll = enable;
    }

    @Override
    public void setCurrentItem(int item) {
        setCurrentItem(item, true);
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        if (Math.abs(getCurrentItem() - item) > 7) {
            scroller.setNoDuration(true);
            super.setCurrentItem(item, smoothScroll);
            scroller.setNoDuration(false);
        } else {
            scroller.setNoDuration(false);
            super.setCurrentItem(item, smoothScroll);
        }
    }
}
