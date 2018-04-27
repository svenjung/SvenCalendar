package com.sven.sjcalendar.behavior;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sven.sjcalendar.R;
import com.sven.sjcalendar.widget.WeekTitleBar;

import java.lang.ref.WeakReference;

import timber.log.Timber;

/**
 * Created by Sven.J on 18-4-20.
 */
public class HeaderBehavior<V extends View> extends CoordinatorLayout.Behavior<V>
        implements CollapsingView, BottomSheetBehavior.BottomSheetCallback {

    private ImageView mImageView;
    private WeekTitleBar mWeekTitle;

    private int mImageViewId;
    private int mWeekTitleId;

    WeakReference<V> mViewRef = null;

    public HeaderBehavior() {
        super();
    }

    public HeaderBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HeaderBehavior_Layout);
        mImageViewId = a.getResourceId(R.styleable.HeaderBehavior_Layout_behavior_imageId, -1);
        mWeekTitleId = a.getResourceId(R.styleable.HeaderBehavior_Layout_behavior_weekTitleId, -1);
        a.recycle();
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, V child, int layoutDirection) {
        ensureChild(child);
        mViewRef = new WeakReference<>(child);
        return false;
    }

    private void ensureChild(V child) {
        // First clear out the current child
        mImageView = null;
        mWeekTitle = null;

        if (mImageViewId != -1) {
            mImageView = child.findViewById(mImageViewId);
        }

        if (mWeekTitleId != -1) {
            mWeekTitle = child.findViewById(mWeekTitleId);
        }
    }

    @Override
    public int getExpandedHeight() {
        V child = mViewRef.get();
        ensureChild(child);
        if (mImageView == null || mImageView.getVisibility() != View.VISIBLE) {
            return child.getMinimumHeight();
        }

        return mImageView.getMeasuredHeight();
    }

    @Override
    public int getCollapsedHeight() {
        V child = mViewRef.get();
        ensureChild(child);
        return child.getMinimumHeight();
    }

    @SuppressWarnings("unchecked")
    public static <V extends View> HeaderBehavior from(V toolbar) {
        ViewGroup.LayoutParams params = toolbar.getLayoutParams();
        if (!(params instanceof CoordinatorLayout.LayoutParams)) {
            throw new IllegalArgumentException("The view is not a child of CoordinatorLayout");
        }
        CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) params)
                .getBehavior();
        if (!(behavior instanceof HeaderBehavior)) {
            throw new IllegalArgumentException(
                    "The view is not associated with HeaderBehavior");
        }

        // init refs view
        HeaderBehavior headerBehavior = (HeaderBehavior) behavior;
        headerBehavior.mViewRef = new WeakReference(toolbar);

        return (HeaderBehavior) behavior;
    }

    @Override
    public void onStateChanged(@NonNull View bottomSheet, int newState) {

    }

    @Override
    public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        int expandedHeight = getExpandedHeight();
        int collapsedHeight = getCollapsedHeight();

        // set image alpha
        int bottom = collapsedHeight + (int) ((1 - slideOffset) * (expandedHeight - collapsedHeight));
        mImageView.setAlpha(1 - getInterpolation(slideOffset));

        // offset header
        V child = mViewRef.get();
        child.setTop(bottom - expandedHeight);
        child.setBottom(bottom);
    }

    private float getInterpolation(float input) {
        // AccelerateDecelerateInterpolator
        // return (float)(Math.cos((input + 1) * Math.PI) / 2.0f) + 0.5f;
        // DecelerateInterpolator
        // return (float)(1.0f - Math.pow((1.0f - input), 2 * 2));
        return input;
    }

}
