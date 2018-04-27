package com.sven.sjcalendar.behavior;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;

import timber.log.Timber;

/**
 * Created by Sven.J on 18-4-19.
 */
public class ToolbarBehavior extends CoordinatorLayout.Behavior<Toolbar>
        implements CollapsingView {

    private WeakReference<Toolbar> mViewRef;

    public ToolbarBehavior() {
    }

    public ToolbarBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, Toolbar child, int layoutDirection) {
        Timber.d("ToolbarBehavior onLayoutChild");
        parent.onLayoutChild(child, layoutDirection);
        mViewRef = new WeakReference<>(child);
        return true;
    }

    @Override
    public int getExpandedHeight() {
        if (mViewRef != null && mViewRef.get() != null) {
            Toolbar toolbar = mViewRef.get();
            return toolbar.getMeasuredHeight();
        }

        return 0;
    }

    @Override
    public int getCollapsedHeight() {
        if (mViewRef != null && mViewRef.get() != null) {
            Toolbar toolbar = mViewRef.get();
            return toolbar.getMinimumHeight() + toolbar.getPaddingTop();
        }

        return 0;
    }

    @SuppressWarnings("unchecked")
    public static ToolbarBehavior from(View toolbar) {
        ViewGroup.LayoutParams params = toolbar.getLayoutParams();
        if (!(params instanceof CoordinatorLayout.LayoutParams)) {
            throw new IllegalArgumentException("The view is not a child of CoordinatorLayout");
        }
        CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) params)
                .getBehavior();
        if (!(behavior instanceof ToolbarBehavior)) {
            throw new IllegalArgumentException(
                    "The view is not associated with ToolbarBehavior");
        }
        return (ToolbarBehavior) behavior;
    }
}
