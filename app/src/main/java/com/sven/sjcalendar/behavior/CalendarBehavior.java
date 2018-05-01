package com.sven.sjcalendar.behavior;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.sven.dateview.date.MonthView;
import com.sven.sjcalendar.R;
import com.sven.sjcalendar.widget.MonthAdapter;
import com.sven.sjcalendar.widget.MonthViewPager;

import java.lang.ref.WeakReference;

import static com.sven.sjcalendar.behavior.ViewUtils.offsetTopAndBottom;

/**
 * Created by Sven.J on 18-4-19.
 */
public class CalendarBehavior extends CoordinatorLayout.Behavior<MonthViewPager>
        implements CollapsingView, BottomSheetBehavior.BottomSheetCallback,
        View.OnLayoutChangeListener{
    private WeakReference<View> dependentView;

    private WeakReference<MonthViewPager> mViewRef;

    private @BottomSheetBehavior.State int mState = BottomSheetBehavior.STATE_COLLAPSED;

    public CalendarBehavior() {
    }

    public CalendarBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, MonthViewPager child, View dependency) {
        if (dependency != null && dependency.getId() == R.id.header) {
            dependentView = new WeakReference<>(dependency);
        }
        return false;
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, MonthViewPager child, int layoutDirection) {
        // Let the parent lay it out by default
        parent.onLayoutChild(child, layoutDirection);
        child.addOnLayoutChangeListener(this);
        if (mState == BottomSheetBehavior.STATE_COLLAPSED) {
            offsetTopAndBottom(child, getLayoutTop(true));
        } else if (mState == BottomSheetBehavior.STATE_EXPANDED) {
            offsetTopAndBottom(child, getLayoutTop(false));
        }
        mViewRef = new WeakReference<>(child);
        child.removeOnPageChangeListener(onPageChangeListener);
        child.addOnPageChangeListener(onPageChangeListener);
        return true;
    }

    public View getDependentView() {
        if (dependentView != null) {
            return dependentView.get();
        } else {
            return null;
        }
    }

    private int getDependentViewHeight() {
        View dependentView = getDependentView();
        if (dependentView != null && dependentView.getVisibility() == View.VISIBLE) {
            return dependentView.getMeasuredHeight();
        } else {
            return 0;
        }
    }

    private int getLayoutTop(boolean expanded) {
        View dependentView = getDependentView();
        if (dependentView != null) {
            HeaderBehavior behavior = HeaderBehavior.from(dependentView);
            if (behavior != null) {
                if (expanded) {
                    return behavior.getExpandedHeight();
                } else {
                    return behavior.getCollapsedHeight();
                }
            }
        }

        return 0;
    }

    @Override
    public int getExpandedHeight() {
        // 左右滑动过程中，要获取ViewPager的高度
        if (mViewRef != null && mViewRef.get() != null) {
            return mViewRef.get().getHeight();
        }
        MonthView monthView = getCurrentView();
        return monthView == null ? 0 : monthView.getHeight();
    }

    @Override
    public int getCollapsedHeight() {
        MonthView monthView = getCurrentView();
        return monthView == null ? 0 : monthView.getRowHeight();
    }

    private MonthView getCurrentView() {
        if (mViewRef != null && mViewRef.get() != null && mViewRef.get().getAdapter() != null) {
            MonthViewPager viewPager = mViewRef.get();
            MonthAdapter adapter = viewPager.getAdapter();
            return adapter.getItem(viewPager.getCurrentItem());
        }

        return null;
    }

    private ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        // 滑动过程中, position指向左边的item
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (positionOffset == 0.0) {
                return;
            }

            if (mViewRef == null || mViewRef.get() == null) {
                return;
            }

            MonthViewPager viewPager = mViewRef.get();
            MonthAdapter adapter = viewPager.getAdapter();
            if (adapter == null) {
                return;
            }

            int currentPos, nextPos;
            currentPos = position;
            nextPos = position + 1;

            if (currentPos == nextPos) {
                return;
            }
            MonthView currentMonth = adapter.getItem(currentPos);
            MonthView nextMonth = adapter.getItem(nextPos);

            if (currentMonth == null || nextMonth == null) {
                return;
            }

            int translateY = currentMonth.getMonthHeight() - nextMonth.getMonthHeight();
            int offsetY = (int) (translateY * positionOffset);
            if (offsetY == 0) {
                return;
            }

            ViewGroup.LayoutParams lp = viewPager.getLayoutParams();
            lp.height = currentMonth.getMeasuredHeight() - offsetY;
            viewPager.setLayoutParams(lp);
        }

        @Override
        public void onPageSelected(int position) {
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    @SuppressWarnings("unchecked")
    public static CalendarBehavior from(MonthViewPager monthViewPager) {
        ViewGroup.LayoutParams params = monthViewPager.getLayoutParams();
        if (!(params instanceof CoordinatorLayout.LayoutParams)) {
            throw new IllegalArgumentException("The view is not a child of CoordinatorLayout");
        }
        CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) params)
                .getBehavior();
        if (!(behavior instanceof CalendarBehavior)) {
            throw new IllegalArgumentException("The view is not associated with CalendarBehavior");
        }
        return (CalendarBehavior) behavior;
    }

    @Override
    public void onStateChanged(@NonNull View bottomSheet, int newState) {
        mState = newState;
    }

    @Override
    public void onSlide(@NonNull View bottomSheet, float slideOffset) {

    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom,
                               int oldLeft, int oldTop, int oldRight, int oldBottom) {
        if (mViewRef == null || mViewRef.get() == null) {
            return;
        }
        View child = mViewRef.get();
        if (mState == BottomSheetBehavior.STATE_COLLAPSED) {
            offsetTopAndBottom(child, getLayoutTop(false));
        } else if (mState == BottomSheetBehavior.STATE_EXPANDED) {
            offsetTopAndBottom(child, getLayoutTop(true));
        }
    }
}
