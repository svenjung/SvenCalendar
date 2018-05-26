package com.sven.sjcalendar;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;

import com.sven.dateview.TimeCalendar;
import com.sven.dateview.date.DatePickerController;
import com.sven.dateview.date.MonthView;
import com.sven.dateview.date.OnDayClickListener;
import com.sven.dateview.date.OnDayLongClickListener;
import com.sven.dateview.date.SimpleMonthView;
import com.sven.dateview.date.SimpleWeekView;
import com.sven.dateview.date.WeekView;
import com.sven.sjcalendar.behavior.BottomSheetBehavior;
import com.sven.sjcalendar.behavior.BottomSheetBehavior.BottomSheetCallback;
import com.sven.sjcalendar.behavior.BottomSheetBehavior.SimpleBottomSheetCallback;
import com.sven.sjcalendar.event.EventDayLiveData;
import com.sven.sjcalendar.hotspots.HotspotsPagerAdapter;
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

    private BottomSheetBehavior mBottomSheetBehavior;

    private EventDayLiveData mEventDayLiveData;

    // 日期变更的类型
    private static final int DAY_CHANGE_FROM_WEEK = 1;
    private static final int DAY_CHANGE_FROM_MONTH = 2;
    private static final int DAY_CHANGE_FROM_DAY = 3;
    private static final int DAY_CHANGE_FROM_TIME = 4;

    @IntDef({DAY_CHANGE_FROM_WEEK, DAY_CHANGE_FROM_MONTH, DAY_CHANGE_FROM_DAY, DAY_CHANGE_FROM_TIME})
    @Retention(RetentionPolicy.SOURCE)
    private @interface DayChangeType {
    }

    private int mState = BottomSheetBehavior.STATE_COLLAPSED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置窗口背景为透明, 降低绘制层级
        getWindow().getDecorView().setBackgroundColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_home2);

        mWeekStart = TimeCalendar.MONDAY;
        mSelectedDay = TimeCalendar.getInstance();

        initView();

        Timber.i("           onCreate");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Timber.i("           onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timber.i("           onResume");
        mEventDayLiveData = new EventDayLiveData(this);
        mEventDayLiveData.observe(this, mMonthPagerAdapter);
        mEventDayLiveData.observe(this, mWeekPagerAdapter);
    }

    @Override
    protected void onPause() {
        mEventDayLiveData.removeObserver(mMonthPagerAdapter);
        mEventDayLiveData.removeObserver(mWeekPagerAdapter);
        super.onPause();
        Timber.i("           onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Timber.i("           onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Timber.i("           onDestroy");
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mBottomSheetBehavior != null &&
                    mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mMonthPager = findViewById(R.id.monthPager);
        mWeekPager = findViewById(R.id.weekPager);
        mListPager = findViewById(R.id.listPager);

        mWeekTitleBar = findViewById(R.id.headerWeekTitle);
        mWeekTitleBar.setFirstDayOfWeek(mWeekStart);

        mMonthPagerAdapter = new MonthPagerAdapter(mController, mDayClickListener, mDayLongClickListener);
        mMonthPager.addOnAdapterChangeListener(mMonthPagerAdapter);
        mMonthPager.setAdapter(mMonthPagerAdapter);

        mWeekPagerAdapter = new WeekPagerAdapter(mController, mDayClickListener);
        mWeekPager.addOnAdapterChangeListener(mWeekPagerAdapter);
        mWeekPager.setAdapter(mWeekPagerAdapter);

        mListPager.setAdapter(new HotspotsPagerAdapter());
        //mListPager.setAdapter(new ListPagerAdapter());

        mListPager.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mBottomSheetBehavior = BottomSheetBehavior.from(mListPager);
                        mBottomSheetBehavior.addBottomSheetCallback(mStateCallback);
                        mListPager.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });

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

    // MonthViewPager的onPageSelected会触发两次？？ WTF
    private void onDayChanged(@DayChangeType int type) {
        Timber.i("             onDayChanged, type = %d, selected day = %s", type, mSelectedDay.format2445());
        updateTitle();

        mMonthPager.removeOnPageChangeListener(mMonthChangeListener);
        mWeekPager.removeOnPageChangeListener(mWeekChangeListener);
        mListPager.removeOnPageChangeListener(mListPageChangeListener);

        switch (type) {
            case DAY_CHANGE_FROM_TIME:
                Timber.i("@@@ Refresh : Month,  Week,  List");
                // refresh month
                mMonthPagerAdapter.setSelectedDay(mSelectedDay.getJulianDay());
                mMonthPager.setCurrentItem(getMonthPosition(mSelectedDay));
                // refresh week
                mWeekPagerAdapter.setSelectedDay(mSelectedDay.getJulianDay());
                mWeekPager.setCurrentItem(getWeekPosition(mSelectedDay),
                        mState == BottomSheetBehavior.STATE_EXPANDED);
                // 点击选中日期时,底部ViewPager切换页面不做动画
                mListPager.setCurrentItem(mSelectedDay.getDaysSinceEpoch(), false);
                break;
            case DAY_CHANGE_FROM_DAY:
                Timber.i("@@@ Refresh : Month,  Week");
                // refresh month
                mMonthPager.setCurrentItem(getMonthPosition(mSelectedDay));
                mMonthPagerAdapter.setSelectedDay(mSelectedDay.getJulianDay());
                // refresh week
                mWeekPagerAdapter.setSelectedDay(mSelectedDay.getJulianDay());
                mWeekPager.setCurrentItem(getWeekPosition(mSelectedDay));
                break;
            case DAY_CHANGE_FROM_MONTH:
                Timber.i("@@@ Refresh : Week,  List");
                mMonthPagerAdapter.setSelectedDay(mSelectedDay.getJulianDay(), false);
                // refresh week
                mWeekPagerAdapter.setSelectedDay(mSelectedDay.getJulianDay());
                mWeekPager.setCurrentItem(getWeekPosition(mSelectedDay), false);
                // 点击选中日期时,底部ViewPager切换页面不做动画
                mListPager.setCurrentItem(mSelectedDay.getDaysSinceEpoch(), true);
                break;
            case DAY_CHANGE_FROM_WEEK:
                Timber.i("@@@ Refresh : Month,  List");
                mWeekPagerAdapter.setSelectedDay(mSelectedDay.getJulianDay(), false);
                // refresh month
                mMonthPager.setCurrentItem(getMonthPosition(mSelectedDay));
                mMonthPagerAdapter.setSelectedDay(mSelectedDay.getJulianDay());
                // 点击选中日期时,底部ViewPager切换页面不做动画
                mListPager.setCurrentItem(mSelectedDay.getDaysSinceEpoch(), false);
                break;
        }

        mListPager.addOnPageChangeListener(mListPageChangeListener);
        mMonthPager.addOnPageChangeListener(mMonthChangeListener);
        mWeekPager.addOnPageChangeListener(mWeekChangeListener);
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

    private OnDayLongClickListener mDayLongClickListener = new OnDayLongClickListener() {
        @Override
        public boolean onDayLongClick(View view, int year, int month, int day) {
            TimeCalendar time = TimeCalendar.getInstance();
            time.set(year, month, day);
            Intent intent = new Intent("com.android.calendar.QUICK_EVENT_INSERT");
            intent.putExtra("eventBeginTime", time.getTimeInMillis());
            intent.putExtra("title", "SjCalendar create event");
            intent.setType("vnd.android.cursor.item/event");

            startActivity(intent);
            return true;
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
                mMonthPager.setVisibility(View.INVISIBLE);
            } else {
                mWeekPager.setVisibility(View.INVISIBLE);
                mMonthPager.setVisibility(View.VISIBLE);
            }

            mState = newState;
        }
    };

    private OnPageChangeListener mListPageChangeListener = new SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            int selectedDay = position + TimeCalendar.EPOCH_JULIAN_DAY;
            mSelectedDay.setJulianDay(selectedDay);

            Timber.i("     List selected, current day = %s", mSelectedDay.format2445());
            onDayChanged(DAY_CHANGE_FROM_DAY);
        }
    };

    private OnPageChangeListener mMonthChangeListener = new SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            SimpleMonthView monthView = mMonthPagerAdapter.getItem(mMonthPager.getCurrentItem());
            if (monthView == null) {
                return;
            }
            int year = monthView.getYear();
            int month = monthView.getMonth();
            int day = monthView.getSelectedDay();

            mSelectedDay = new TimeCalendar(year, month, day);
            onDayChanged(DAY_CHANGE_FROM_MONTH);
        }
    };

    private OnPageChangeListener mWeekChangeListener = new SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            SimpleWeekView weekView = mWeekPagerAdapter.getItem(mWeekPager.getCurrentItem());
            if (weekView == null) {
                return;
            }
            int julianDay = weekView.getSelectedDay();
            mSelectedDay.setJulianDay(julianDay);

            onDayChanged(DAY_CHANGE_FROM_WEEK);
        }
    };

}
