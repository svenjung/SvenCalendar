package com.sven.sjcalendar.hotspots;

import android.support.annotation.NonNull;

/**
 * Created by Sven.J on 18-5-9.
 */
public class Event implements Comparable<Event> {
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
