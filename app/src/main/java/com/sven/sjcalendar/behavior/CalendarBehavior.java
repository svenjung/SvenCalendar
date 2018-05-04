package com.sven.sjcalendar.behavior;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.sven.dateview.date.MonthView;
import com.sven.dateview.date.SimpleMonthView;
import com.sven.sjcalendar.R;
import com.sven.sjcalendar.widget.MonthPagerAdapter;
import com.sven.sjcalendar.widget.NoScrollViewPager;

import java.lang.ref.WeakReference;

import timber.log.Timber;

import static com.sven.sjcalendar.behavior.ViewUtils.offsetTopAndBottom;

/**
 * Created by Sven.J on 18-4-19.
 */
public class CalendarBehavior<V extends View> extends CoordinatorLayout.Behavior<V>
        implements CollapsingView, BottomSheetBehavior.BottomSheetCallback,
        View.OnLayoutChangeListener{
    private WeakReference<View> dependentView;

    private WeakReference<V> mViewRef;

    private @BottomSheetBehavior.State int mState = BottomSheetBehavior.STATE_COLLAPSED;

    public CalendarBehavior() {
    }

    public CalendarBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, V child, View dependency) {
        if (dependency != null && dependency.getId() == R.id.header) {
            dependentView = new WeakReference<>(dependency);
        }
        return false;
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, V child, int layoutDirection) {
        // Let the parent lay it out by default
        parent.onLayoutChild(child, layoutDirection);
        child.addOnLayoutChangeListener(this);
        if (mState == BottomSheetBehavior.STATE_COLLAPSED) {
            offsetTopAndBottom(child, getLayoutTop(true));
        } else if (mState == BottomSheetBehavior.STATE_EXPANDED) {
            offsetTopAndBottom(child, getLayoutTop(false));
        }
        mViewRef = new WeakReference<>(child);
        if (child instanceof ViewPager) {
            ViewPager viewPager = (ViewPager) child;
            viewPager.removeOnPageChangeListener(onPageChangeListener);
            viewPager.addOnPageChangeListener(onPageChangeListener);
        }
        return true;
    }

    public View getDependentView() {
        if (dependentView != null) {
            return dependentView.get();
        } else {
            return null;
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
        V child = mViewRef.get();
        if (child != null) {
            return child.getMeasuredHeight();
        } else {
            return 0;
        }
    }

    @Override
    public int getCollapsedHeight() {
        SimpleMonthView monthView = getCurrentMonth();
        if (monthView != null) {
            return monthView.getRowHeight();
        } else {
            return 0;
        }
    }

    private SimpleMonthView getCurrentMonth() {
        if (mViewRef == null || mViewRef.get() == null) {
            return null;
        }

        V child = mViewRef.get();
        if (child instanceof ViewPager) {
            ViewPager viewPager = (ViewPager) mViewRef.get();
            PagerAdapter pagerAdapter = ((ViewPager) mViewRef.get()).getAdapter();
            if (!(pagerAdapter instanceof MonthPagerAdapter)) {
                return null;
            }

            MonthPagerAdapter adapter = (MonthPagerAdapter) pagerAdapter;
            if (adapter == null) {
                return null;
            }

            return adapter.getItem(viewPager.getCurrentItem());
        } else {
            return null;
        }
    }

    private ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        // 滑动过程中, position指向左边的item
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (mViewRef == null || mViewRef.get() == null) {
                return;
            }

            V child = mViewRef.get();
            if (!(child instanceof ViewPager)) {
                return;
            }

            PagerAdapter pagerAdapter = ((ViewPager) mViewRef.get()).getAdapter();
            if (!(pagerAdapter instanceof MonthPagerAdapter)) {
                return;
            }

            MonthPagerAdapter adapter = (MonthPagerAdapter) pagerAdapter;
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

            // 这里实时更新ViewPager高度是左右滑动时,让底部的EventViewPager跟着上下滑动
            // 可以换一个实现方式,固定Month高度,左右滑动时,使用offsetTopAndBottom来
            // 移动底部列表
            ViewGroup.LayoutParams lp = child.getLayoutParams();
            lp.height = currentMonth.getMeasuredHeight() - offsetY;
            child.setLayoutParams(lp);
        }
    };

    @SuppressWarnings("unchecked")
    public static <V extends View> CalendarBehavior from(V view) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
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

        if (mViewRef == null || mViewRef.get() == null) {
            Timber.i("onStateChanged, get ref view failed!");
            return;
        }

        NoScrollViewPager viewPager = (NoScrollViewPager) mViewRef.get();
        viewPager.setScrollEnable(newState == BottomSheetBehavior.STATE_COLLAPSED);
    }

    @Override
    public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        // 移动MonthView所在ViewPager
        SimpleMonthView monthView = getCurrentMonth();
        if (monthView == null) {
            Timber.e("onSlide, get current month failed!");
            return;
        }

        int headerMinHeight = getLayoutTop(false);
        int headerMaxHeight = getLayoutTop(true);
        int selectedRow = monthView.getSelectedRow();
        int rowHeight = monthView.getRowHeight();
        int maxOffset = rowHeight * (selectedRow - 1) + headerMaxHeight - headerMinHeight;
        int newTop = (int) (-slideOffset * maxOffset) + headerMaxHeight;
        offsetTopAndBottom(mViewRef.get(), newTop);
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
