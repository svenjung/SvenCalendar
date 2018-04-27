package com.sven.sjcalendar.widget;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.sven.dateview.TimeCalendar;
import com.sven.dateview.date.DatePickerController;
import com.sven.dateview.date.SimpleWeekView;
import com.sven.dateview.date.WeekView;

import java.util.Calendar;
import java.util.HashMap;

import timber.log.Timber;

/**
 * Created by Sven.J on 18-4-25.
 */
public class WeekAdapter extends PagerAdapter {
    private final DatePickerController mController;
    private int mWeekStart;

    public WeekAdapter(DatePickerController controller) {
        mController = controller;

        mWeekStart = controller.getFirstDayOfWeek();
    }

    public void setWeekStart(int weekStart) {
        if (mWeekStart != weekStart) {
            mWeekStart = weekStart;
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        // To-do 根据最大最小值计算count
        return getMaxWeeks() + 1;
    }

    @Override
    public int getItemPosition(Object object) {
        // 解决notifyDataSetChanged界面不刷新问题
        if (object instanceof WeekView) {
            return POSITION_NONE;
        }

        return super.getItemPosition(object);
    }

    @Override
    public View instantiateItem(ViewGroup container, int position) {
        Context context = container.getContext();
        SimpleWeekView weekView = new SimpleWeekView(context);
        HashMap<String, Integer> drawingParams = new HashMap<>();
        drawingParams.put(WeekView.VIEW_PARAMS_WEEK_SINCE_EPOCH, position);
        drawingParams.put(WeekView.VIEW_PARAMS_HEIGHT, 150);
        drawingParams.put(WeekView.VIEW_PARAMS_WEEK_START, mWeekStart);

        Timber.d("draw week, week start = %d", mWeekStart);

        Calendar day = mController.getSelectedDay();
        int selectedDay = getSelectedDay(day.get(Calendar.YEAR), day.get(Calendar.MONTH), day.get(Calendar.DAY_OF_MONTH));

        drawingParams.put(WeekView.VIEW_PARAMS_SELECTED_DAY, selectedDay);

        weekView.setWeekParams(drawingParams);
        container.addView(weekView);

        return weekView;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        SimpleWeekView weekView = (SimpleWeekView) object;
        ViewGroup.LayoutParams lp = container.getLayoutParams();
        if (lp.height != weekView.getWeekHeight()) {
            lp.height = weekView.getWeekHeight();
            container.setLayoutParams(lp);
        }

        super.setPrimaryItem(container, position, object);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        SimpleWeekView weekView = (SimpleWeekView) object;
        container.removeView(weekView);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    private int getMaxWeeks() {
        TimeCalendar calendar = TimeCalendar.getInstance();
        calendar.set(2037, 11, 31);

        int maxJulianDay = calendar.getJulianday();
        return TimeCalendar.getWeeksSinceEpochJulianDay(maxJulianDay, mController.getFirstDayOfWeek());
    }

    private int getSelectedDay(int year, int month, int day) {
        TimeCalendar calendar = new TimeCalendar(year, month, day);
        return calendar.getJulianday();
    }
}
