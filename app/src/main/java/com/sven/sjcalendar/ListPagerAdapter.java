package com.sven.sjcalendar;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.sven.dateview.TimeCalendar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

/**
 * Created by Sven.J on 18-4-28.
 */
public class ListPagerAdapter extends PagerAdapter {
    private int mDayCount;

    public ListPagerAdapter() {

        TimeCalendar min = new TimeCalendar(1970, 0, 1);
        TimeCalendar max = new TimeCalendar(2037, 11, 31);

        mDayCount = max.getJulianDay() - min.getJulianDay() + 1;
    }

    @Override
    public int getCount() {
        return mDayCount;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Context context = container.getContext();
        RecyclerView recyclerView = new RecyclerView(context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        String[] names = context.getResources().getStringArray(R.array.query_suggestions);
        List<String> mList = new ArrayList<>();
        Collections.addAll(mList, names);
        ListAdapter adapter = new ListAdapter(context, mList);
        adapter.mDay = TimeCalendar.EPOCH_JULIAN_DAY + position;
        recyclerView.setAdapter(adapter);
        RecyclerView.ItemDecoration itemDecoration =
                new ItemDivider(context, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(itemDecoration);

        container.addView(recyclerView);

        return recyclerView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        RecyclerView recyclerView = (RecyclerView) object;
        container.removeView(recyclerView);
    }

    public static class ItemDivider extends DividerItemDecoration {

        public ItemDivider(Context context, int orientation) {
            super(context, orientation);
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            Timber.tag("Divider").i("       ItemDivider, position = %d", ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewLayoutPosition());
        }
    }
}
