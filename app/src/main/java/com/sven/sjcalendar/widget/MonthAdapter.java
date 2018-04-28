package com.sven.sjcalendar.widget;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.sven.dateview.TimeCalendar;
import com.sven.dateview.date.DatePickerController;
import com.sven.dateview.date.MonthView;
import com.sven.dateview.date.OnDayClickListener;
import com.sven.dateview.date.OnDayLongClickListener;
import com.sven.dateview.date.SimpleMonthView;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;

import timber.log.Timber;

/**
 * Created by Sven.J on 18-4-18.
 */
public class MonthAdapter extends PagerAdapter {
    private final DatePickerController mController;
    private final OnDayClickListener mOnDayClickListener;

    private final OnDayLongClickListener dayLongClickListener = new OnDayLongClickListener() {
        @Override
        public boolean onDayLongClick(View view, int year, int month, int day) {
            Timber.i("onDayLongClick %d/%d/%d", year, (month + 1), day);
            return true;
        }
    };

    private int mWeekStart;

    private static final int MONTHS_IN_YEAR = 12;

    private SparseArray<SimpleMonthView> mCachedViews;
    private LinkedList<SimpleMonthView> mRecycledViews;

    public MonthAdapter(DatePickerController controller, OnDayClickListener clickListener) {
        mCachedViews = new SparseArray<>();
        mRecycledViews = new LinkedList<>();
        mController = controller;
        mOnDayClickListener = clickListener;
        mWeekStart = controller.getFirstDayOfWeek();
    }

    public void setWeekStart(int weekStart) {
        if (mWeekStart != weekStart) {
            mWeekStart = weekStart;
            notifyDataSetChanged();
        }
    }

    public int getWeekStart() {
        return mWeekStart;
    }

    public MonthView getItem(int position) {
        return mCachedViews.get(position);
    }

    private boolean isSelectedDayInMonth(int year, int month) {
        Calendar calendar = mController.getSelectedDay();
        return calendar.get(Calendar.YEAR) == year && calendar.get(Calendar.MONTH) == month;
    }

    private boolean isTodayInMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) == year && calendar.get(Calendar.MONTH) == month;
    }

    @Override
    public int getCount() {
        return ((mController.getMaxYear() - mController.getMinYear()) + 1) * MONTHS_IN_YEAR;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public int getItemPosition(Object object) {
        // 解决notifyDataSetChanged界面不刷新问题
        if (object instanceof MonthView) {
            return POSITION_NONE;
        }

        return super.getItemPosition(object);
    }

    @Override
    public View instantiateItem(ViewGroup container, int position) {
        Context context = container.getContext();
        SimpleMonthView monthView;
        if (mRecycledViews.size() > 0) {
            monthView = mRecycledViews.removeFirst();
        } else {
            monthView = new SimpleMonthView(context);
        }

        HashMap<String, Integer> drawingParams = new HashMap<>();
        final int month = position % MONTHS_IN_YEAR;
        final int year = position / MONTHS_IN_YEAR + mController.getMinYear();

        int selectedDay = 1;
        if (isSelectedDayInMonth(year, month)) {
            selectedDay = mController.getSelectedDay().get(Calendar.DAY_OF_MONTH);
        } else if (isTodayInMonth(year, month)) {
            selectedDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        }

        drawingParams.put(MonthView.VIEW_PARAMS_HEIGHT, 150);
        drawingParams.put(MonthView.VIEW_PARAMS_SELECTED_DAY, selectedDay);
        drawingParams.put(MonthView.VIEW_PARAMS_YEAR, year);
        drawingParams.put(MonthView.VIEW_PARAMS_MONTH, month);
        drawingParams.put(MonthView.VIEW_PARAMS_WEEK_START, mWeekStart);

        monthView.setMonthParams(drawingParams);

        monthView.setOnDayClickListener(mOnDayClickListener);
        monthView.setOnDayLongClickListener(dayLongClickListener);

        container.addView(monthView);
        mCachedViews.append(position, monthView);

        return monthView;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        SimpleMonthView monthView = (SimpleMonthView) object;
        ViewGroup.LayoutParams lp = container.getLayoutParams();
        if (lp.height != monthView.getMonthHeight()) {
            lp.height = monthView.getMonthHeight();
            container.setLayoutParams(lp);
        }
        super.setPrimaryItem(container, position, object);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        SimpleMonthView view = (SimpleMonthView) object;
        container.removeView(view);
        mCachedViews.remove(position);
        mRecycledViews.add(view);
    }
}
