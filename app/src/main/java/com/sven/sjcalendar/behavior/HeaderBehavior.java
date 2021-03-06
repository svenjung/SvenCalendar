package com.sven.sjcalendar.behavior;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
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

    private WeakReference<V> mViewRef = null;

    private @BottomSheetBehavior.State int mState = BottomSheetBehavior.STATE_COLLAPSED;

    private View.OnLayoutChangeListener mChildLayoutListener = new View.OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                   int oldLeft, int oldTop, int oldRight, int oldBottom) {
            if (mViewRef != null && mViewRef.get() != null) {
                if (mState == BottomSheetBehavior.STATE_COLLAPSED) {
                    setChildTopAndBottom(0);
                } else if (mState == BottomSheetBehavior.STATE_EXPANDED) {
                    setChildTopAndBottom(1);
                }
            }
        }
    };

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
        // ViewPager左右滑动后会触发parent的onLayoutChild,此时要记录折叠状态,重新定位child
        child.addOnLayoutChangeListener(mChildLayoutListener);
        ensureChild(child);
        mViewRef = new WeakReference<>(child);
        return false;
    }

    @Override
    public boolean onTouchEvent(CoordinatorLayout parent, V child, MotionEvent ev) {
        // 消耗touch事件 -> 屏蔽Header上touch会触发BottomSheetBehavior的scroll
        return true;
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
        mState = newState;
    }

    @Override
    public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        // set image alpha
        mImageView.setAlpha(1 - getInterpolation(slideOffset));

        // offset header
        setChildTopAndBottom(slideOffset);
    }

    private float getInterpolation(float input) {
        // AccelerateDecelerateInterpolator
        // return (float)(Math.cos((input + 1) * Math.PI) / 2.0f) + 0.5f;
        // DecelerateInterpolator
        // return (float)(1.0f - Math.pow((1.0f - input), 2 * 2));
        return input;
    }

    private void setChildTopAndBottom(float slideOffset) {
        int expandedHeight = getExpandedHeight();
        int collapsedHeight = getCollapsedHeight();
        // set image alpha
        int bottom = collapsedHeight + (int) ((1 - slideOffset) * (expandedHeight - collapsedHeight));

        V child = mViewRef.get();
        child.setTop(bottom - expandedHeight);
        child.setBottom(bottom);
    }
}
