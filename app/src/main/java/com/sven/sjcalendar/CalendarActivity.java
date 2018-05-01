package com.sven.sjcalendar;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.sven.dateview.TimeCalendar;
import com.sven.dateview.date.DatePickerController;
import com.sven.dateview.date.MonthView;
import com.sven.dateview.date.OnDayClickListener;
import com.sven.sjcalendar.widget.MonthAdapter;
import com.sven.sjcalendar.widget.WeekAdapter;
import com.sven.sjcalendar.widget.WeekTitleBar;

import java.util.Calendar;
import java.util.Random;

public class CalendarActivity extends AppCompatActivity {

    private WeekTitleBar mWeekTitleBar;
    private NoScrollViewPager mMonthViewPager;
    private NoScrollViewPager mWeekViewPager;

    private MonthAdapter mMonthAdapter;
    private WeekAdapter mWeekAdapter;

    private OnDayClickListener mOnDayClickListener = new OnDayClickListener() {
        @Override
        public void onDayClick(View view, int year, int month, int day) {
            setTitleDate(year, month, day);
        }
    };

    private static final int[] WEEK_STARTS = new int[] {
            Calendar.SUNDAY,
            Calendar.SATURDAY,
            Calendar.MONDAY
    };

    private View.OnClickListener mViewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.changeWeekStart) {
                Random random = new Random();
                int weekStart = WEEK_STARTS[random.nextInt(1000) % 3];

                onWeekStartChanged(weekStart);
            } else if (id == R.id.backToday) {
                backToday();
            }
        }
    };

    private TimeCalendar mSelectedTime = TimeCalendar.getInstance();

    private int mWeekStart = Calendar.SUNDAY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        mWeekTitleBar = findViewById(R.id.weekTitle);
        mMonthViewPager = findViewById(R.id.monthView);
        mWeekViewPager = findViewById(R.id.weekView);

        mWeekTitleBar.setFirstDayOfWeek(controller.getFirstDayOfWeek());

        mMonthAdapter = new MonthAdapter(controller, mOnDayClickListener);
        mWeekAdapter = new WeekAdapter(controller, mOnDayClickListener);
        mMonthViewPager.setAdapter(mMonthAdapter);
        mWeekViewPager.setAdapter(mWeekAdapter);

        mMonthViewPager.setCurrentItem(getMonthPosition(mSelectedTime));
        mWeekViewPager.setCurrentItem(getWeekPosition(mSelectedTime));

        setTitleDate(mSelectedTime.getYear(), mSelectedTime.getMonth(), mSelectedTime.getDayOfMonth());

        mMonthViewPager.addOnPageChangeListener(mMonthChangeListener);
        mWeekViewPager.addOnPageChangeListener(mWeekChangeListener);

        findViewById(R.id.changeWeekStart).setOnClickListener(mViewClickListener);
        findViewById(R.id.backToday).setOnClickListener(mViewClickListener);
    }

    private void setTitleDate(int year, int month, int day) {
        mSelectedTime.set(year, month, day);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(year + "年" + (month + 1) + "月" + day + "日");
        }
    }

    private void onWeekStartChanged(int weekStart) {
        if (mWeekStart != weekStart) {
            mWeekStart = weekStart;

            mWeekTitleBar.setFirstDayOfWeek(weekStart);
            mMonthAdapter.setWeekStart(weekStart);

            int weekPosition = getWeekPosition(mSelectedTime);
            mWeekAdapter.setWeekStart(weekStart);
            mWeekViewPager.setCurrentItem(weekPosition);
        }
    }

    private void backToday() {
        TimeCalendar today = TimeCalendar.getInstance();

        mMonthViewPager.setCurrentItem(getMonthPosition(today));
        mWeekViewPager.setCurrentItem(getWeekPosition(today));
    }

    // 当前事件从1970年1月1日开始计算的月数
    private int getMonthPosition(TimeCalendar calendar) {
        return (calendar.getYear() - controller.getMinYear()) * 12 + calendar.getMonth();
    }

    // 计算当前时间从1970年1月1日开始计算的周数
    private int getWeekPosition(TimeCalendar calendar) {
        return TimeCalendar.getWeeksSinceEpochJulianDay(calendar.getJulianDay(),
                controller.getFirstDayOfWeek());
    }

    private ViewPager.OnPageChangeListener mMonthChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            MonthView monthView = mMonthAdapter.getItem(position);
            setTitleDate(monthView.getYear(), monthView.getMonth(), monthView.getSelectedDay());
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private ViewPager.OnPageChangeListener mWeekChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private DatePickerController controller = new DatePickerController() {
        @Override
        public void onYearSelected(int year) {
        }

        @Override
        public void onDayOfMonthSelected(int year, int month, int day) {
        }

        @Override
        public Calendar getSelectedDay() {
            return mSelectedTime;
        }

        @Override
        public int getFirstDayOfWeek() {
            return mWeekStart;
        }

        @Override
        public int getMinYear() {
            return 1970;
        }

        @Override
        public int getMaxYear() {
            return 2037;
        }

        @Override
        public Calendar getMinDate() {
            Calendar calendar = Calendar.getInstance();
            calendar.set(1970, 0,1);
            return calendar;
        }

        @Override
        public Calendar getMaxDate() {
            Calendar calendar = Calendar.getInstance();
            calendar.set(2037, 11,31);
            return calendar;
        }

        @Override
        public void tryVibrate() {
        }
    };
}
