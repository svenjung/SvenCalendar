package com.sven.sjcalendar.behavior;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.view.View;

public class ViewUtils {

    /**
     *  Offset this view's vertical location to the specified top
     * @param view target view
     * @param targetTop target new top
     */
    public static void offsetTopAndBottom(@NonNull View view, int targetTop) {
        int currentTop = view.getTop();
        int offsetY = targetTop - currentTop;
        if (offsetY != 0) {
            ViewCompat.offsetTopAndBottom(view, offsetY);
        }
    }

}
