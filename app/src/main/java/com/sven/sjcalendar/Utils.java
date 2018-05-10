package com.sven.sjcalendar;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.text.format.DateUtils;

import com.sven.dateview.TimeCalendar;

import java.util.Calendar;
import java.util.Formatter;
import java.util.Locale;
import java.util.TimeZone;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by Sven.J on 18-5-8.
 */
public class Utils {

    private static StringBuilder mSB = new StringBuilder(50);
    private static Formatter mF = new Formatter(mSB, Locale.getDefault());

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

    public static String formatDateTime(long startMillis, long endMillis, long currentMillis,
                                        String localTimezone, boolean allDay, Context context) {
        // Configure date/time formatting.
        int flagsDate = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
                | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_MONTH
                | DateUtils.FORMAT_ABBREV_WEEKDAY | DateUtils.FORMAT_NUMERIC_DATE;
        int flagsTime = DateUtils.FORMAT_SHOW_TIME;

        return null;
    }

    public static String formatDateRange(Context context, long startMillis, long endMillis, int flags) {
        String date;
        String tz;
        if ((flags & DateUtils.FORMAT_UTC) != 0) {
            tz = TimeCalendar.TIMEZONE_UTC.getID();
        } else {
            tz = TimeZone.getDefault().getID();
        }
        synchronized (mSB) {
            mSB.setLength(0);
            date = DateUtils.formatDateRange(context, mF, startMillis, endMillis, flags,
                    tz).toString();
        }
        return date;
    }

    /**
     * Returns whether the specified time interval is in a single day.
     */
    private static boolean singleDayEvent(long startMillis, long endMillis, long localGmtOffset) {
        if (startMillis == endMillis) {
            return true;
        }

        // An event ending at midnight should still be a single-day event, so check
        // time end-1.
        int startDay = TimeCalendar.getJulianDay(startMillis, localGmtOffset);
        int endDay = TimeCalendar.getJulianDay(endMillis - 1, localGmtOffset);
        return startDay == endDay;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            return false;
        } else {
            NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
            if (networkInfo != null && networkInfo.length > 0) {
                for (NetworkInfo aNetworkInfo : networkInfo) {
                    if (aNetworkInfo.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
