package com.sven.sjcalendar.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.sven.sjcalendar.Reflect;

import java.util.LinkedList;

/**
 * Created by Sven.J on 18-5-10.
 */
public abstract class RecycledPagerAdapter<V extends View> extends PagerAdapter {
    private SparseArray<V> mCachedViews = new SparseArray<>();
    private LinkedList<V> mRecycledViews = new LinkedList<>();

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Context context = container.getContext();
        V view;
        if (mRecycledViews.size() > 0) {
            view = mRecycledViews.removeFirst();
        } else {
            view = createView(context);
        }

        bindView(view, position);

        // add view
        //container.addView(view);
        Reflect.on(container).call("addViewInLayout", view, -1, view.getLayoutParams(), true);
        mCachedViews.put(position, view);

        return view;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        V view = (V) object;
        container.removeView(view);
        mCachedViews.remove(position);
        mRecycledViews.add(view);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public V getItem(int position) {
        return mCachedViews.get(position);
    }

    public abstract V createView(Context context);

    public abstract void bindView(@NonNull V view, int position);
}
