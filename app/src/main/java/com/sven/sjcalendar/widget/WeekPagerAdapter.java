package com.sven.sjcalendar.widget;

import android.arch.lifecycle.Observer;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPagerUtils;
import android.view.ViewGroup;

import com.sven.dateview.TimeCalendar;
import com.sven.dateview.date.DatePickerController;
import com.sven.dateview.date.EventIndicator;
import com.sven.dateview.date.OnDayClickListener;
import com.sven.dateview.date.SimpleWeekView;
import com.sven.sjcalendar.R;

import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

/**
 * Created by Sven.J on 18-5-2.
 */
public class WeekPagerAdapter extends AbsDatePagerAdapter<SimpleWeekView>
        implements Observer<List<Integer>>, EventIndicator {

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
        setSelectedDay(day, true);
    }

    public void setSelectedDay(int day, boolean invalidate) {
        if (mSelectedDay == day) {
            return;
        }

        mSelectedDay = day;
        if (invalidate && mTargetViewPager != null) {
            int targetWeek = TimeCalendar.getWeeksSinceEpochJulianDay(mSelectedDay, mWeekStart);
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
    public SimpleWeekView createView(Context context) {
        return new SimpleWeekView(context);
    }

    @Override
    public void bindView(@NonNull SimpleWeekView view, int position) {
        Context context = view.getContext();
        HashMap<String, Integer> drawingParams = new HashMap<>();
        int rowHeight = context.getResources().getDimensionPixelOffset(R.dimen.week_row_height);
        int weeksSinceEpoch = mMinWeekNum + position;
        int selectedDay = -1;
        if (isSelectedDayInWeek(weeksSinceEpoch)) {
            selectedDay = mSelectedDay;
        }

        if (selectedDay == -1) {
            TimeCalendar today = TimeCalendar.getInstance();
            int todayJulianDay = today.getJulianDay();
            int todayWeekNum = TimeCalendar.getWeeksSinceEpochJulianDay(todayJulianDay, mWeekStart);
            if (isSelectedDayInWeek(todayWeekNum)) {
                selectedDay = todayJulianDay;
            }
        }

        drawingParams.put(SimpleWeekView.VIEW_PARAMS_WEEK_SINCE_EPOCH, weeksSinceEpoch);
        drawingParams.put(SimpleWeekView.VIEW_PARAMS_HEIGHT, rowHeight);
        drawingParams.put(SimpleWeekView.VIEW_PARAMS_SELECTED_DAY, selectedDay);
        drawingParams.put(SimpleWeekView.VIEW_PARAMS_WEEK_START, mWeekStart);

        view.setWeekParams(drawingParams);
        view.setOnDayClickListener(mOnDayClickListener);
        view.setDatePickerController(mController);
        view.setEventIndicator(this);

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                rowHeight);
        view.setLayoutParams(lp);
    }

    @Override
    public int getCount() {
        return mMaxWeekNum - mMinWeekNum + 1;
    }

    @Override
    protected boolean forceNotifyDataSetChanged(Object object) {
        return object instanceof SimpleWeekView;
    }

    private boolean isSelectedDayInWeek(int currentWeek) {
        return TimeCalendar.getWeeksSinceEpochJulianDay(mSelectedDay, mWeekStart) == currentWeek;
    }

    private List<Integer> mEventDays = null;

    @Override
    public void onChanged(@Nullable List<Integer> list) {
        mEventDays = list;

        SimpleWeekView current = (SimpleWeekView) ViewPagerUtils.getCurrentView(mTargetViewPager);
        if (current != null) {
            current.invalidate();
        }
    }

    @Override
    public boolean hasEvents(int julianDay) {
        return mEventDays != null && mEventDays.contains(julianDay);
    }
}
