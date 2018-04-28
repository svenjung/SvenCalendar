package com.sven.sjcalendar;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.support.v4.view.NestedScrollingParent;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.Arrays;

import timber.log.Timber;

/**
 * Created by Sven.J on 18-4-8.
 */
public class NestedParentView extends LinearLayout implements NestedScrollingParent {

    private NestedScrollingParentHelper parentHelper;

    public NestedParentView(@NonNull Context context) {
        super(context);

        init();
    }

    public NestedParentView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public NestedParentView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        parentHelper = new NestedScrollingParentHelper(this);
    }

    /**
     * 检测一个 View 在给定的方向（up or down）能否竖直滑动
     *
     * @param direction，正数表示下滑
     * 返回 true 表示能在指定的方向滑动，false 反之
     */
    @Override
    public boolean canScrollVertically(int direction) {
        return super.canScrollVertically(direction);
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        Timber.tag("NestedParent").d("## onStartNestedScroll ## child : %s, target : %s", child.getClass().getSimpleName(), target.getClass().getSimpleName());
        Timber.i("onStartNestedScroll, child = %s, target = %s, nestedScrollAxes = %d", child, target, nestedScrollAxes);
        return true;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        Timber.i("onNestedScrollAccepted, child = %s, target = %s, nestedScrollAxes = %d", child, target, nestedScrollAxes);
        parentHelper.onNestedScrollAccepted(child, target, nestedScrollAxes);
    }

    @Override
    public void onStopNestedScroll(View target) {
        Timber.i("onStopNestedScroll");
        parentHelper.onStopNestedScroll(target);
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        Timber.i("onNestedScroll, dxConsumed = %d, dyConsumed = %d, dxUnconsumed = %d, dyUnconsumed = %d", dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        Timber.i("onNestedPreScroll, target view ： %s, Y : %f", target.getClass().getSimpleName(), target.getY());

        Timber.i("                 canScrollVertically : %b, dy : %d", canScrollVertically(1), dy);

        View childAt0 = getChildAt(0);
        if (childAt0 instanceof Toolbar) {
            // childAt0.setY(getY() + dy);
            childAt0.offsetTopAndBottom((int) (dy * 0.5));
            return;
        }

        if (dy > 0 && !canScrollVertically(1)) {
            Timber.i("ParentView在不能下滑的情况下，不可向下移动");
            return;
        }

        if (dy < 0 && !canScrollVertically(-1)) {
            Timber.i("ParentView在不能上滑的情况下，不可向上移动");
            return;
        }

        // 应该移动的Y距离
        final float shouldMoveY = getY() + dy;
        // 获取到父View的容器的引用，这里假定父View容器是View
        final View parent = (View) getParent();

        int consumedY;
        // 如果超过了父View的上边界，只消费子View到父View上边的距离
        if (shouldMoveY <= 0) {
            consumedY = -(int) getY();
        } else if (shouldMoveY >= parent.getHeight() - getHeight()) {
            // 如果超过了父View的下边界，只消费子View到父View
            consumedY = (int) (parent.getHeight() - getHeight() - getY());
        } else {
            // 其他情况下全部消费
            consumedY = dy;
        }

        // 对父View进行移动
        setY(getY() + consumedY);

        // 将父View消费掉的放入consumed数组中
        consumed[1] = consumedY;

        Timber.i("onNestedPreScroll, dx = %d, dy = %d, consumed = %s", dx, dy, Arrays.toString(consumed));
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        Timber.i("onNestedFling, velocityX = %f, velocityY = %f, consumed = %b", velocityX, velocityY, consumed);
        return false;
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        Timber.i("onNestedPreFling, velocityX = %f, velocityY = %f", velocityX, velocityY);
        return false;
    }

    @Override
    public int getNestedScrollAxes() {
        Timber.i("getNestedScrollAxes");
        return parentHelper.getNestedScrollAxes();
    }
}
