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
import com.sven.dateview.date.OnDayLongClickListener;
import com.sven.dateview.date.SimpleMonthView;
import com.sven.sjcalendar.R;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Sven.J on 18-5-2.
 */
public class MonthPagerAdapter extends AbsDatePagerAdapter<SimpleMonthView>
        implements Observer<List<Integer>>, EventIndicator {
    private static final int MONTHS_IN_YEAR = 12;

    private TimeCalendar mSelectedDay;

    private OnDayLongClickListener mOnDayLongClickListener;

    public MonthPagerAdapter(DatePickerController controller, OnDayClickListener listener) {
        super(controller, listener);

        mSelectedDay = new TimeCalendar(controller.getSelectedDay().getYear(), controller.getSelectedDay().getMonth(),
                controller.getSelectedDay().getDay());
    }

    public MonthPagerAdapter(DatePickerController controller, OnDayClickListener listener,
                             OnDayLongClickListener longClickListener) {
        this(controller, listener);
        mOnDayLongClickListener = longClickListener;
    }

    public void setSelectedDay(int julianDay) {
        TimeCalendar time = TimeCalendar.getInstance();
        time.setJulianDay(julianDay);
        if (mSelectedDay.sameDay(time)) {
            return;
        }

        mSelectedDay.setJulianDay(julianDay);

        if (mTargetViewPager != null) {
            int childCount = mTargetViewPager.getChildCount();
            int yearOfMonth, monthOfMonth;
            for (int i = 0; i < childCount; i++) {
                SimpleMonthView monthView = (SimpleMonthView) mTargetViewPager.getChildAt(i);
                yearOfMonth = monthView.getYear();
                monthOfMonth = monthView.getMonth();

                if (time.getYear() == yearOfMonth && time.getMonth() == monthOfMonth) {
                    monthView.setSelectedDay(time.getDay());
                }
            }
        }
    }

    @Override
    public SimpleMonthView createView(Context context) {
        return new SimpleMonthView(context);
    }

    @Override
    public void bindView(@NonNull SimpleMonthView view, int position) {
        Context context = view.getContext();
        HashMap<String, Integer> drawingParams = new HashMap<>();
        int month = position % MONTHS_IN_YEAR;
        int year = position / MONTHS_IN_YEAR + mController.getMinYear();

        int selectedDay = 1;
        if (isSelectedDayInMonth(year, month)) {
            selectedDay = mSelectedDay.getDay();
        }

        int rowHeight = context.getResources().getDimensionPixelOffset(R.dimen.week_row_height);

        drawingParams.put(SimpleMonthView.VIEW_PARAMS_HEIGHT, rowHeight);
        drawingParams.put(SimpleMonthView.VIEW_PARAMS_SELECTED_DAY, selectedDay);
        drawingParams.put(SimpleMonthView.VIEW_PARAMS_YEAR, year);
        drawingParams.put(SimpleMonthView.VIEW_PARAMS_MONTH, month);
        drawingParams.put(SimpleMonthView.VIEW_PARAMS_WEEK_START, mWeekStart);

        view.setMonthParams(drawingParams);
        view.setOnDayClickListener(mOnDayClickListener);
        view.setOnDayLongClickListener(mOnDayLongClickListener);
        view.setDatePickerController(mController);
        view.setEventIndicator(this);

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                view.getMonthHeight());
        view.setLayoutParams(lp);
    }

    @Override
    public int getCount() {
        return ((mController.getMaxYear() - mController.getMinYear()) + 1) * MONTHS_IN_YEAR;
    }

    @Override
    protected boolean forceNotifyDataSetChanged(Object object) {
        return object instanceof SimpleMonthView;
    }

    // TODO 把这部分代码移至CalendarBehavior的onPageSelected回调中
    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        SimpleMonthView monthView = (SimpleMonthView) object;
        ViewGroup.LayoutParams lp = container.getLayoutParams();
        if (lp != null && lp.height != monthView.getMonthHeight()) {
            lp.height = monthView.getMonthHeight();
            container.setLayoutParams(lp);
        }

        super.setPrimaryItem(container, position, object);
    }

    private boolean isSelectedDayInMonth(int year, int month) {
        return mSelectedDay.getYear() == year && mSelectedDay.getMonth() == month;
    }

    private List<Integer> mEventDays = null;

    @Override
    public void onChanged(@Nullable List<Integer> list) {
        mEventDays = list;

        SimpleMonthView current = (SimpleMonthView) ViewPagerUtils.getCurrentView(mTargetViewPager);
        if (current != null)
            current.invalidate();
    }

    @Override
    public boolean hasEvents(int julianDay) {
        return mEventDays != null && mEventDays.contains(julianDay);
    }

}
