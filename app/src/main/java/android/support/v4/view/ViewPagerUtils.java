package android.support.v4.view;

import android.view.View;

import com.sven.sjcalendar.widget.RecycledPagerAdapter;

public class ViewPagerUtils {

    public static View getCurrentView(ViewPager viewPager) {
        final int currentItem = viewPager.getCurrentItem();

        PagerAdapter adapter = viewPager.getAdapter();
        if (adapter instanceof RecycledPagerAdapter) {
            RecycledPagerAdapter rp = (RecycledPagerAdapter) adapter;
            return rp.getItem(currentItem);
        }

        for (int i = 0; i < viewPager.getChildCount(); i++) {
            final View child = viewPager.getChildAt(i);
            final ViewPager.LayoutParams layoutParams = (ViewPager.LayoutParams) child.getLayoutParams();
            if (!layoutParams.isDecor && currentItem == layoutParams.position) {
                return child;
            }
        }

        for (int i = 0; i < viewPager.getChildCount(); i++) {
            final View child = viewPager.getChildAt(i);
            if (child.getId() == currentItem) {
                return child;
            }
        }

        return null;
    }

}
