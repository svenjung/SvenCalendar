package com.sven.sjcalendar;

import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.sven.dateview.TimeCalendar;
import com.sven.dateview.date.DatePickerController;
import com.sven.dateview.date.OnDayClickListener;
import com.sven.sjcalendar.behavior.BottomSheetBehavior;
import com.sven.sjcalendar.widget.MonthAdapter;
import com.sven.sjcalendar.widget.MonthViewPager;


import java.util.Calendar;
import java.util.List;

import timber.log.Timber;

public class AllInOneActivity extends AppCompatActivity {

    private MonthViewPager monthViewPager;

    @BottomSheetBehavior.State int mState = BottomSheetBehavior.STATE_COLLAPSED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        final MonthViewPager viewPager = findViewById(R.id.calendarPager);
        monthViewPager = viewPager;
        OnDayClickListener dayClickListener = new OnDayClickListener() {
            @Override
            public void onDayClick(View view, int year, int month, int day) {
            }
        };
        final MonthAdapter adapter = new MonthAdapter(controller, dayClickListener);
        viewPager.setAdapter(adapter);
        Calendar today = Calendar.getInstance();
        viewPager.setCurrentItem((today.get(Calendar.YEAR) - controller.getMinYear()) * 12 + today.get(Calendar.MONTH));

        viewPager.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                Timber.i("------ month view pager relayout...");
            }
        });

        // 使用LiveData监听日历数据变化,实时更新UI
        EventDayLiveData liveData = new EventDayLiveData(this);
        liveData.observe(this, new Observer<List<Integer>>() {
            @Override
            public void onChanged(@Nullable List<Integer> integers) {
            }
        });

        final NoScrollViewPager listViewPager = findViewById(R.id.listViewPager);
        listViewPager.setAdapter(new ListPagerAdapter());
        TimeCalendar time = TimeCalendar.getInstance();
        listViewPager.setCurrentItem(time.getJulianday() - TimeCalendar.EPOCH_JULIAN_DAY);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("日历");
        }

        final FrameLayout header = findViewById(R.id.header);
        final int minHeight = getResources().getDimensionPixelOffset(R.dimen.header_collapsed_height);
        final int maxHeight = getResources().getDimensionPixelOffset(R.dimen.header_expanded_height);

        final BottomSheetBehavior.BottomSheetCallback callback = new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                mState = newState;
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                Timber.i("@@@@@      Header Layout onSlide $$$$$$$$$");
                int bottom = minHeight + (int) ((1 - slideOffset) * (maxHeight - minHeight));
                header.setTop(bottom - header.getHeight());
                header.setBottom(bottom);
            }
        };

//        header.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
//            @Override
//            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
//                Timber.i("@@@@@      Header Layout Changed $$$$$$$$$");
//                if (mState == BottomSheetBehavior.STATE_EXPANDED) {
//                    header.setTop(minHeight - header.getHeight());
//                    header.setBottom(minHeight);
//                }
//            }
//        });

//        listViewPager.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                BottomSheetBehavior behavior = BottomSheetBehavior.from(listViewPager);
//                behavior.setHeaderCallback(callback);
//                //behavior.addBottomSheetCallback(callback);
//                listViewPager.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//            }
//        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, CalendarActivity.class);
            startActivity(i);
            return true;
        } else if (id == R.id.action_today) {
            // set today
            TimeCalendar today = TimeCalendar.getInstance();
            int currentMonth = (today.getYear() - controller.getMinYear()) * 12 + today.getMonth();
            monthViewPager.setCurrentItem(currentMonth, true);
        }
        return super.onOptionsItemSelected(item);
    }

    private DatePickerController controller = new DatePickerController() {
        @Override
        public void onYearSelected(int year) {
        }

        @Override
        public void onDayOfMonthSelected(int year, int month, int day) {
            Intent intent = new Intent(AllInOneActivity.this, FullscreenActivity.class);
            startActivity(intent);
        }

        @Override
        public Calendar getSelectedDay() {
            return Calendar.getInstance();
        }

        @Override
        public int getFirstDayOfWeek() {
            return Calendar.MONDAY;
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
