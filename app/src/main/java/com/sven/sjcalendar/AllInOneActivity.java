package com.sven.sjcalendar;

import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.sven.dateview.date.DatePickerController;
import com.sven.dateview.date.MonthView;
import com.sven.dateview.date.OnDayClickListener;
import com.sven.sjcalendar.widget.MonthAdapter;
import com.sven.sjcalendar.widget.MonthViewPager;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

public class AllInOneActivity extends AppCompatActivity {

    private MonthViewPager monthViewPager;

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

        EventDayLiveData liveData = new EventDayLiveData(this);
        liveData.observe(this, new Observer<List<Integer>>() {
            @Override
            public void onChanged(@Nullable List<Integer> integers) {
            }
        });

        final RecyclerView recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        String[] names = getResources().getStringArray(R.array.query_suggestions);
        List<String> mList = new ArrayList<>();
        Collections.addAll(mList, names);
        recyclerView.setAdapter(new ListAdapter(this, mList));
        RecyclerView.ItemDecoration itemDecoration =
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(itemDecoration);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("日历");
        }
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
            int currentMonth = (2018 - controller.getMinYear()) * 12 + 3;
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
