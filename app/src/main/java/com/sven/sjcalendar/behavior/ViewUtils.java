package com.sven.sjcalendar.behavior;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.view.View;

public class ViewUtils {

    public static void offsetTopAndBottom(@NonNull View view, int targetTop) {
        int currentTop = view.getTop();
        int offsetY = targetTop - currentTop;
        if (offsetY != 0) {
            ViewCompat.offsetTopAndBottom(view, offsetY);
        }
    }

    public static String getViewClass(@NonNull View view) {
        return view.getClass().getSimpleName();
    }
}
