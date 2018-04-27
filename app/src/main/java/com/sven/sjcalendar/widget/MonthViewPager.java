package com.sven.sjcalendar.widget;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.AttributeSet;

import com.sven.dateview.date.MonthView;
import com.sven.sjcalendar.NoScrollViewPager;

/**
 * Created by Sven.J on 18-4-18.
 */
public class MonthViewPager extends NoScrollViewPager {
    // 定义month属性
    public MonthViewPager(Context context) {
        super(context);
    }

    public MonthViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setAdapter(PagerAdapter adapter) {
        if (adapter instanceof MonthAdapter) {
            super.setAdapter(adapter);
        } else {
            throw new IllegalArgumentException("The setAdapter method of MonthViewPager required CalendarMonthAdapter");
        }
    }

    @Override
    public MonthAdapter getAdapter() {
        return (MonthAdapter) super.getAdapter();
    }

    public static MonthView getCurrentView(MonthViewPager viewPager) {
        if (viewPager != null && viewPager.getAdapter() != null) {
            MonthAdapter adapter = viewPager.getAdapter();
            int currentItem = viewPager.getCurrentItem();
            return adapter.getItem(currentItem);
        }

        return null;
    }

}
