package com.sven.sjcalendar.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.sven.dateview.date.DatePickerController;
import com.sven.dateview.date.OnDayClickListener;
import com.sven.sjcalendar.Reflect;

import java.util.LinkedList;

import timber.log.Timber;

/**
 * Created by Sven.J on 18-5-2.
 */
public abstract class AbsDatePagerAdapter<V extends View> extends RecycledPagerAdapter<V>
        implements ViewPager.OnAdapterChangeListener {

    protected final DatePickerController mController;
    protected final OnDayClickListener mOnDayClickListener;

    protected int mWeekStart;

    protected ViewPager mTargetViewPager = null;

    public AbsDatePagerAdapter(DatePickerController controller, OnDayClickListener listener) {
        mController = controller;
        mOnDayClickListener = listener;

        mWeekStart = controller.getFirstDayOfWeek();
    }

    @Override
    public int getItemPosition(Object object) {
        if (forceNotifyDataSetChanged(object)) {
            return POSITION_NONE;
        }

        return super.getItemPosition(object);
    }

    // 解决notifyDataSetChanged界面不刷新问题
    protected boolean forceNotifyDataSetChanged(Object object) {
        return false;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }


    // This method will call after ViewPager.setAdapter
    // TODO 直接使用构造函数传入ViewPager
    @Override
    public void onAdapterChanged(@NonNull ViewPager viewPager, @Nullable PagerAdapter oldAdapter,
                                 @Nullable PagerAdapter newAdapter) {
        mTargetViewPager = viewPager;
    }

}
