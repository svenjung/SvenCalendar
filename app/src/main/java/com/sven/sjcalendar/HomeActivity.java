package com.sven.sjcalendar;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;

import com.sven.dateview.TimeCalendar;
import com.sven.dateview.date.DatePickerController;
import com.sven.dateview.date.MonthView;
import com.sven.dateview.date.OnDayClickListener;
import com.sven.dateview.date.WeekView;
import com.sven.sjcalendar.behavior.BottomSheetBehavior;
import com.sven.sjcalendar.behavior.BottomSheetBehavior.BottomSheetCallback;
import com.sven.sjcalendar.behavior.BottomSheetBehavior.SimpleBottomSheetCallback;
import com.sven.sjcalendar.widget.MonthPagerAdapter;
import com.sven.sjcalendar.widget.NoScrollViewPager;
import com.sven.sjcalendar.widget.WeekPagerAdapter;
import com.sven.sjcalendar.widget.WeekTitleBar;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import timber.log.Timber;

public class HomeActivity extends AppCompatActivity {

    private int mWeekStart;
    private TimeCalendar mSelectedDay;

    private NoScrollViewPager mMonthPager;
    private NoScrollViewPager mWeekPager;
    private NoScrollViewPager mListPager;

    private WeekTitleBar mWeekTitleBar;

    private MonthPagerAdapter mMonthPagerAdapter;
    private WeekPagerAdapter mWeekPagerAdapter;

    // 日期变更的类型
    private static final int DAY_CHANGE_FROM_WEEK = 1;
    private static final int DAY_CHANGE_FROM_MONTH = 2;
    private static final int DAY_CHANGE_FROM_DAY = 3;
    private static final int DAY_CHANGE_FROM_TIME = 4;

    @IntDef({DAY_CHANGE_FROM_WEEK, DAY_CHANGE_FROM_MONTH, DAY_CHANGE_FROM_DAY, DAY_CHANGE_FROM_TIME})
    @Retention(RetentionPolicy.SOURCE)
    private @interface DayChangeType {}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home2);

        mWeekStart = TimeCalendar.MONDAY;
        mSelectedDay = TimeCalendar.getInstance();

        initView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        if (id == R.id.action_today) {
            mSelectedDay = TimeCalendar.getInstance();
            onDayChanged(DAY_CHANGE_FROM_TIME);
        }

        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mMonthPager = findViewById(R.id.monthPager);
        mWeekPager = findViewById(R.id.weekPager);
        mListPager = findViewById(R.id.listPager);

        mWeekTitleBar = findViewById(R.id.headerWeekTitle);
        mWeekTitleBar.setFirstDayOfWeek(mWeekStart);

        mMonthPagerAdapter = new MonthPagerAdapter(mController, mDayClickListener);
        mMonthPager.addOnAdapterChangeListener(mMonthPagerAdapter);
        mMonthPager.setAdapter(mMonthPagerAdapter);

        mWeekPagerAdapter = new WeekPagerAdapter(mController, mDayClickListener);
        mWeekPager.addOnAdapterChangeListener(mWeekPagerAdapter);
        mWeekPager.setAdapter(mWeekPagerAdapter);

        mListPager.setAdapter(new ListPagerAdapter());
        mListPager.setCurrentItem(mSelectedDay.getDaysSinceEpoch());

        mListPager.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                BottomSheetBehavior behavior = BottomSheetBehavior.from(mListPager);
                behavior.addBottomSheetCallback(mStateCallback);
                mListPager.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        mListPager.addOnPageChangeListener(mListPageChangeListener);

        onDayChanged(DAY_CHANGE_FROM_TIME);
    }

    private void updateTitle() {
        setTitle(mSelectedDay.getYear() + "/" + (mSelectedDay.getMonth() + 1) + "/" + mSelectedDay.getDay());
    }

    private int getMonthPosition(TimeCalendar time) {
        return (time.getYear() - mController.getMinYear()) * 12 + mSelectedDay.getMonth();
    }

    private int getWeekPosition(TimeCalendar time) {
        int selectedWeek = TimeCalendar.getWeeksSinceEpochJulianDay(time.getJulianDay(),
                mWeekStart);

        int minWeek = TimeCalendar.getWeeksSinceEpochJulianDay(mController.getMinDate().getJulianDay(),
                mWeekStart);

        return selectedWeek - minWeek;
    }

    // FIXME Month和Week中点击选中日期时,会触发两次onDayChanged
    // 第二次为ListViewPager的切换触发
    private void onDayChanged(@DayChangeType int type) {
        Timber.i("             onDayChanged, type = %d", type);
        updateTitle();

        if (type != DAY_CHANGE_FROM_MONTH) {
            // refresh month
            mMonthPager.setCurrentItem(getMonthPosition(mSelectedDay));
            mMonthPagerAdapter.setSelectedDay(mSelectedDay.getJulianDay());
        }

        if (type != DAY_CHANGE_FROM_WEEK) {
            // refresh week
            mWeekPager.setCurrentItem(getWeekPosition(mSelectedDay));

            mWeekPagerAdapter.setSelectedDay(mSelectedDay.getJulianDay());
        }

        if (type != DAY_CHANGE_FROM_DAY) {
            // 点击选中日期时,底部ViewPager切换页面不做动画
            mListPager.setCurrentItem(mSelectedDay.getDaysSinceEpoch(), false);
        }

    }

    private OnDayClickListener mDayClickListener = new OnDayClickListener() {
        @Override
        public void onDayClick(View view, int year, int month, int day) {
            if (year == mSelectedDay.getYear() && month == mSelectedDay.getMonth() &&
                    day == mSelectedDay.getDay()) {
                return;
            }

            mSelectedDay.set(year, month, day);

            if (view instanceof MonthView) {
                onDayChanged(DAY_CHANGE_FROM_MONTH);
            } else if (view instanceof WeekView) {
                onDayChanged(DAY_CHANGE_FROM_WEEK);
            }
        }
    };

    private DatePickerController mController = new DatePickerController() {
        @Override
        public TimeCalendar getSelectedDay() {
            return mSelectedDay;
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
        public TimeCalendar getMinDate() {
            return new TimeCalendar(1970, 0, 1);
        }

        @Override
        public TimeCalendar getMaxDate() {
            return new TimeCalendar(2037, 11, 31);
        }
    };

    private BottomSheetCallback mStateCallback = new SimpleBottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                mWeekPager.setVisibility(View.VISIBLE);
            } else {
                mWeekPager.setVisibility(View.INVISIBLE);
            }
        }
    };

    private ViewPager.OnPageChangeListener mListPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            int selectedDay = position + TimeCalendar.EPOCH_JULIAN_DAY;
            mSelectedDay.setJulianDay(selectedDay);
            onDayChanged(DAY_CHANGE_FROM_DAY);
        }
    };
}
