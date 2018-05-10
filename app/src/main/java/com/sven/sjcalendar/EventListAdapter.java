package com.sven.sjcalendar;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.sven.dateview.TimeCalendar;
import com.sven.dateview.date.DatePickerController;
import com.sven.dateview.date.OnDayClickListener;
import com.sven.sjcalendar.widget.AbsDatePagerAdapter;
import com.sven.sjcalendar.widget.RecycledPagerAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Sven.J on 18-5-3.
 */
public class EventListAdapter extends RecycledPagerAdapter<RecyclerView> {
    private int mDayCount;

    public EventListAdapter() {
        TimeCalendar min = new TimeCalendar(1970, 0, 1);
        TimeCalendar max = new TimeCalendar(2037, 11, 31);

        mDayCount = max.getJulianDay() - min.getJulianDay() + 1;
    }

    @Override
    public int getCount() {
        return mDayCount;
    }

    @Override
    public RecyclerView createView(Context context) {
        RecyclerView recyclerView = new RecyclerView(context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        return recyclerView;
    }

    @Override
    public void bindView(@NonNull RecyclerView view, int position) {
        String[] names = view.getContext().getResources().getStringArray(R.array.query_suggestions);
        List<String> mList = new ArrayList<>();
        Collections.addAll(mList, names);
        ListAdapter adapter = new ListAdapter(view.getContext(), mList);
        adapter.mDay = TimeCalendar.EPOCH_JULIAN_DAY + position;
        view.setAdapter(adapter);
        RecyclerView.ItemDecoration itemDecoration =
                new DividerItemDecoration(view.getContext(), DividerItemDecoration.VERTICAL);
        view.addItemDecoration(itemDecoration);
    }

}
