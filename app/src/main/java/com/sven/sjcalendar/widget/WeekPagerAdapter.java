package com.sven.sjcalendar.widget;

import android.content.Context;
import android.support.annotation.NonNull;

import com.sven.dateview.TimeCalendar;
import com.sven.dateview.date.DatePickerController;
import com.sven.dateview.date.OnDayClickListener;
import com.sven.dateview.date.SimpleWeekView;
import com.sven.sjcalendar.R;

import java.util.HashMap;

/**
 * Created by Sven.J on 18-5-2.
 */
public class WeekPagerAdapter extends AbsDatePagerAdapter<SimpleWeekView> {

    private int mSelectedDay;

    private int mMinWeekNum;
    private int mMaxWeekNum;

    public WeekPagerAdapter(DatePickerController controller, OnDayClickListener listener) {
        super(controller, listener);

        mSelectedDay = controller.getSelectedDay().getJulianDay();
        mMinWeekNum = TimeCalendar.getWeeksSinceEpochJulianDay(controller.getMinDate().getJulianDay(),
                mWeekStart);

        mMaxWeekNum = TimeCalendar.getWeeksSinceEpochJulianDay(controller.getMaxDate().getJulianDay(),
                mWeekStart);
    }

    public void setSelectedDay(int day) {
        if (mSelectedDay == day) {
            return;
        }

        mSelectedDay = day;

        int targetWeek = TimeCalendar.getWeeksSinceEpochJulianDay(mSelectedDay, mWeekStart);

        if (mTargetViewPager != null) {
            int childCount = mTargetViewPager.getChildCount();
            int weekNum;
            for (int i = 0; i < childCount; i++) {
                SimpleWeekView weekView = (SimpleWeekView) mTargetViewPager.getChildAt(i);
                weekNum = TimeCalendar.getWeeksSinceEpochJulianDay(weekView.getSelectedDay(), mWeekStart);
                if (weekNum == targetWeek) {
                    weekView.setSelectedDay(mSelectedDay);
                }
            }
        }
    }

    @Override
    SimpleWeekView createView(Context context) {
        return new SimpleWeekView(context);
    }

    @Override
    void bindView(@NonNull SimpleWeekView view, int position) {
        Context context = view.getContext();
        HashMap<String, Integer> drawingParams = new HashMap<>();
        int rowHeight = context.getResources().getDimensionPixelOffset(R.dimen.week_row_height);
        int weeksSinceEpoch = mMinWeekNum + position;
        int selectedDay = -1;
        if (isSelectedDayInWeek(weeksSinceEpoch)) {
            selectedDay = mSelectedDay;
        }

        drawingParams.put(SimpleWeekView.VIEW_PARAMS_WEEK_SINCE_EPOCH, weeksSinceEpoch);
        drawingParams.put(SimpleWeekView.VIEW_PARAMS_HEIGHT, rowHeight);
        drawingParams.put(SimpleWeekView.VIEW_PARAMS_SELECTED_DAY, selectedDay);
        drawingParams.put(SimpleWeekView.VIEW_PARAMS_WEEK_START, mWeekStart);

        view.setWeekParams(drawingParams);
        view.setOnDayClickListener(mOnDayClickListener);
    }

    @Override
    public int getCount() {
        return mMaxWeekNum - mMinWeekNum + 1;
    }

    private boolean isSelectedDayInWeek(int currentWeek) {
        return TimeCalendar.getWeeksSinceEpochJulianDay(mSelectedDay, mWeekStart) == currentWeek;
    }
}
