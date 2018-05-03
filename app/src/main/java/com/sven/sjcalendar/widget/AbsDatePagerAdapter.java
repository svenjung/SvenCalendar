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

import java.util.LinkedList;

import timber.log.Timber;

/**
 * Created by Sven.J on 18-5-2.
 */
public abstract class AbsDatePagerAdapter<V extends View> extends PagerAdapter
        implements ViewPager.OnAdapterChangeListener {

    protected final DatePickerController mController;
    protected final OnDayClickListener mOnDayClickListener;

    protected int mWeekStart;

    private final SparseArray<V> mCachedViews;
    private final LinkedList<V> mRecycledViews;

    protected ViewPager mTargetViewPager = null;

    public AbsDatePagerAdapter(DatePickerController controller, OnDayClickListener listener) {
        mController = controller;
        mOnDayClickListener = listener;

        mWeekStart = controller.getFirstDayOfWeek();

        mCachedViews = new SparseArray<>();
        mRecycledViews = new LinkedList<>();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Context context = container.getContext();
        V view;
        if (mRecycledViews.size() > 0) {
            view = mRecycledViews.removeFirst();
        } else {
            view = createView(context);
        }

        bindView(view, position);

        container.addView(view);
        mCachedViews.put(position, view);

        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        V view = (V) object;
        container.removeView(view);
        mCachedViews.remove(position);
        mRecycledViews.add(view);
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

    public V getItem(int position) {
        return mCachedViews.get(position);
    }

    // This method will call after ViewPager.setAdapter
    // TODO 直接使用构造函数传入ViewPager
    @Override
    public void onAdapterChanged(@NonNull ViewPager viewPager, @Nullable PagerAdapter oldAdapter,
                                 @Nullable PagerAdapter newAdapter) {
        mTargetViewPager = viewPager;
    }

    abstract V createView(Context context);

    abstract void bindView(@NonNull V view, int position);

}
