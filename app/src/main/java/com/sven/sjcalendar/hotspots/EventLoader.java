package com.sven.sjcalendar.hotspots;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Instances;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import timber.log.Timber;

/**
 * Created by Sven.J on 18-5-10.
 */
public class EventLoader {

    /**
     * The sort order is:
     * 1) events with an earlier start (begin for normal events, startday for allday)
     * 2) events with a later end (end for normal events, endday for allday)
     * 3) the title (unnecessary, but nice)
     *
     * The start and end day is sorted first so that all day events are
     * sorted correctly with respect to events that are >24 hours (and
     * therefore show up in the allday area).
     */
    private static final String SORT_EVENTS_BY = Instances.START_DAY + " ASC, "
            + Instances.START_MINUTE + " ASC, " + Instances.END_DAY + " ASC, "
            + Instances.END_MINUTE + " ASC";
    private static final String SORT_ALLDAY_BY =
            "startDay ASC, endDay ASC, title ASC";
    private static final String DISPLAY_AS_ALLDAY = "dispAllday";

    private static final String EVENTS_WHERE = DISPLAY_AS_ALLDAY + "=0";
    private static final String ALLDAY_WHERE = DISPLAY_AS_ALLDAY + "=1";

    private static final String EVENT_WHERE_BY_ID = Events._ID + "=?";

    // The projection to use when querying instances to build a list of events
    public static final String[] EVENT_PROJECTION = new String[] {
            Instances.TITLE,                 // 0
            Instances.EVENT_LOCATION,        // 1
            Instances.ALL_DAY,               // 2
            Instances.EVENT_TIMEZONE,        // 3
            Instances.EVENT_ID,              // 4
            Instances.BEGIN,                 // 5
            Instances.END,                   // 6
            Instances._ID,                   // 7
            Instances.START_DAY,             // 8
            Instances.END_DAY,               // 9
            Instances.SELF_ATTENDEE_STATUS,  // 10
            Events.DESCRIPTION,              // 11
            Events.CALENDAR_ID,              // 12
            Instances.ALL_DAY + "=1 OR (" + Instances.END + "-" + Instances.BEGIN + ")>="
                    + DateUtils.DAY_IN_MILLIS + " AS " + DISPLAY_AS_ALLDAY, // 13
    };

    // The indices for the projection array above.
    private static final int PROJECTION_TITLE_INDEX = 0;
    private static final int PROJECTION_LOCATION_INDEX = 1;
    private static final int PROJECTION_ALL_DAY_INDEX = 2;
    private static final int PROJECTION_TIMEZONE_INDEX = 3;
    private static final int PROJECTION_EVENT_ID_INDEX = 4;
    private static final int PROJECTION_BEGIN_INDEX = 5;
    private static final int PROJECTION_END_INDEX = 6;
    private static final int PROJECTION_ID_INDEX = 7;
    private static final int PROJECTION_START_DAY_INDEX = 8;
    private static final int PROJECTION_END_DAY_INDEX = 9;
    private static final int PROJECTION_SELF_ATTENDEE_STATUS_INDEX = 10;
    private static final int PROJECTION_DESCRIPTION = 11;
    private static final int PROJECTION_CALENDAR_ID = 12;
    private static final int PROJECTION_DISPLAY_ALLDAY = 13;

    public static ArrayList<Event> loadEvents(Context context, int startDay) {
        ArrayList<Event> events = new ArrayList<>();

        Cursor cEvents = null;
        Cursor cAllday = null;

        try {
            int endDay = startDay;

            String where = EVENTS_WHERE;
            String whereAllday = ALLDAY_WHERE;

            cEvents = instancesQuery(context.getContentResolver(), EVENT_PROJECTION, startDay,
                    endDay, where, null, SORT_EVENTS_BY);
            cAllday = instancesQuery(context.getContentResolver(), EVENT_PROJECTION, startDay,
                    endDay, whereAllday, null, SORT_ALLDAY_BY);

            buildEventsFromCursor(events, cEvents, context, startDay, endDay);
            buildEventsFromCursor(events, cAllday, context, startDay, endDay);
        } catch (Exception e) {
            Timber.e("  Load event failed, %s", e.getMessage());
        } finally {
            if (cEvents != null) {
                cEvents.close();
            }
            if (cAllday != null) {
                cAllday.close();
            }
        }

        return events;
    }

    private static Cursor instancesQuery(ContentResolver cr, String[] projection,
                                               int startDay, int endDay, String selection, String[] selectionArgs, String orderBy) {
        String WHERE_CALENDARS_SELECTED = Calendars.VISIBLE + "=?";
        String[] WHERE_CALENDARS_ARGS = {"1"};
        String DEFAULT_SORT_ORDER = "begin ASC";

        Uri.Builder builder = Instances.CONTENT_BY_DAY_URI.buildUpon();
        ContentUris.appendId(builder, startDay);
        ContentUris.appendId(builder, endDay);
        if (TextUtils.isEmpty(selection)) {
            selection = WHERE_CALENDARS_SELECTED;
            selectionArgs = WHERE_CALENDARS_ARGS;
        } else {
            selection = "(" + selection + ") AND " + WHERE_CALENDARS_SELECTED;
            if (selectionArgs != null && selectionArgs.length > 0) {
                selectionArgs = Arrays.copyOf(selectionArgs, selectionArgs.length + 1);
                selectionArgs[selectionArgs.length - 1] = WHERE_CALENDARS_ARGS[0];
            } else {
                selectionArgs = WHERE_CALENDARS_ARGS;
            }
        }
        return cr.query(builder.build(), projection, selection, selectionArgs,
                orderBy == null ? DEFAULT_SORT_ORDER : orderBy);
    }

    /**
     * Adds all the events from the cursors to the events list.
     *
     * @param events The list of events
     * @param cEvents Events to add to the list
     * @param context
     * @param startDay
     * @param endDay
     */
    public static void buildEventsFromCursor(
            ArrayList<Event> events, Cursor cEvents, Context context, int startDay, int endDay) {
        if (cEvents == null || events == null) {
            Log.e("EventLoader", "buildEventsFromCursor: null cursor or null events list!");
            return;
        }

        int count = cEvents.getCount();

        if (count == 0) {
            return;
        }

        // Sort events in two passes so we ensure the allday and standard events
        // get sorted in the correct order
        cEvents.moveToPosition(-1);
        while (cEvents.moveToNext()) {
            Event e = generateEventFromCursor(cEvents);
            if (e.startDay > endDay || e.endDay < startDay) {
                continue;
            }
            events.add(e);
        }

        Collections.sort(events);
    }

    /**
     * @param cEvents Cursor pointing at event
     * @return An event created from the cursor
     */
    private static Event generateEventFromCursor(Cursor cEvents) {
        Event e = new Event();

        e.id = cEvents.getLong(PROJECTION_EVENT_ID_INDEX);
        e.title = cEvents.getString(PROJECTION_TITLE_INDEX);
        e.location = cEvents.getString(PROJECTION_LOCATION_INDEX);
        e.allDay = cEvents.getInt(PROJECTION_ALL_DAY_INDEX) != 0;
        e.description = cEvents.getString(PROJECTION_DESCRIPTION);

        if (e.title == null || e.title.length() == 0) {
            e.title = "(无标题)";
        }

        long eStart = cEvents.getLong(PROJECTION_BEGIN_INDEX);
        long eEnd = cEvents.getLong(PROJECTION_END_INDEX);

        e.startMillis = eStart;
        e.endMillis = eEnd;

        e.startDay = cEvents.getInt(PROJECTION_START_DAY_INDEX);
        e.endDay = cEvents.getInt(PROJECTION_END_DAY_INDEX);

        return e;
    }
}
