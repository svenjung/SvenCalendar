package com.sven.sjcalendar.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.sven.dateview.TimeCalendar;
import com.sven.dateview.date.DatePickerController;
import com.sven.dateview.date.OnDayClickListener;
import com.sven.dateview.date.SimpleMonthView;
import com.sven.sjcalendar.R;

import java.util.HashMap;

import timber.log.Timber;

/**
 * Created by Sven.J on 18-5-2.
 */
public class MonthPagerAdapter extends AbsDatePagerAdapter<SimpleMonthView> {
    private static final int MONTHS_IN_YEAR = 12;

    private TimeCalendar mSelectedDay;

    public MonthPagerAdapter(DatePickerController controller, OnDayClickListener listener) {
        super(controller, listener);

        mSelectedDay = new TimeCalendar(controller.getSelectedDay().getYear(), controller.getSelectedDay().getMonth(),
                controller.getSelectedDay().getDay());
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
    SimpleMonthView createView(Context context) {
        return new SimpleMonthView(context);
    }

    @Override
    void bindView(@NonNull SimpleMonthView view, int position) {
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
}
