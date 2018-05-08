package com.sven.sjcalendar;

import android.os.Handler;
import com.sven.dateview.TimeCalendar;

import java.util.Calendar;

/**
 * Created by Sven.J on 18-5-8.
 */
public class Utils {

    // Calculate the time until midnight + 1 second and set the handler to
    // do run the runnable
    public static void setMidnightUpdater(Handler h, Runnable r, String timezone) {
        if (h == null || r == null || timezone == null) {
            return;
        }
        long now = System.currentTimeMillis();
        TimeCalendar calendar = TimeCalendar.getInstance();
        calendar.setTimeZone(timezone);
        calendar.setTimeInMillis(now);

        long runInMillis = (24 * 3600 - calendar.get(Calendar.HOUR_OF_DAY) * 3600 - calendar.get(Calendar.MINUTE) * 60
                - calendar.get(Calendar.SECOND) + 1) * 1000;

        h.removeCallbacks(r);
        h.postDelayed(r, runInMillis);
    }

    // Stop the midnight update thread
    public static void resetMidnightUpdater(Handler h, Runnable r) {
        if (h == null || r == null) {
            return;
        }
        h.removeCallbacks(r);
    }

}
