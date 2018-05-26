package com.sven.sjcalendar.hotspots.schedule;

import android.support.annotation.NonNull;

/**
 * Created by Sven.J on 18-5-9.
 */
public class Event implements Comparable<Event> {
    // 事件的跨天属性
    public static int MULTI_NONE = -1;
    public static int MULTI_START = 0;
    public static int MULTI_END = 1;
    public static int MULTI_MIDDLE = 2;

    public long id;
    public String title;
    public String description;
    public String location;

    public long startMillis;
    public long endMillis;
    public String timeZone;
    public boolean allDay;

    public int startDay;
    public int endDay;

    public int multiType;

    @Override
    public int compareTo(@NonNull Event o) {
        if (startMillis != o.startMillis) {
            return Long.compare(startMillis, o.startMillis);
        }

        if (endMillis != o.endMillis) {
            return Long.compare(endMillis, o.endMillis);
        }

        return 0;
    }
}
