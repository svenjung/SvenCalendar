
package com.sven.dateview;

import android.text.format.DateUtils;
import android.util.Log;

import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author zhimin@cloud.calendar
 */
@SuppressWarnings("WrongConstant")
public class TimeCalendar extends GregorianCalendar {
    private static final long serialVersionUID = -1377248947671505888L;

    public static final int JULIAN_DAY = FIELD_COUNT + 1;

    /**
     * JulianDay 计算公式年（Y），月（M），日（D） <br>
     * JULIANDAY = D - 32075 + 1461 * (Y + 4800 + (M - 14) / 12) / 4 + 367 * (M - 2 - (M - 14) / 12
     * * 12) / 12 - 3 * ((Y + 4900 + (M - 14) / 12) / 100) / 4;
     */
    private static int MIN_JULIAN_DAY = 2415021; // 1900/01/01
    private static int MAX_JULIAN_DAY = 2488069; // 2099/12/31

    /**
     * 1900年1月1日(UTC) 凌晨的时间
     */
    private static final long BASE_DATE_MILLIS = -2208988800000L;

    /**
     * The Julian day of the epoch, that is, January 1, 1970 on the Gregorian calendar.
     */
    public static final int EPOCH_JULIAN_DAY = 2440588;

    public static final TimeZone TIMEZONE_UTC = TimeZone.getTimeZone("UTC");

    public boolean allDay;

    public TimeCalendar() {
        super();
    }

    public static TimeCalendar getInstance() {
        return new TimeCalendar(TimeZone.getDefault());
    }

    public TimeCalendar(int year, int month, int day) {
        super(year, month, day);
    }

    public TimeCalendar(int year, int month, int day, int hour, int minute) {
        super(year, month, day, hour, minute);
    }

    public TimeCalendar(int year, int month, int day, int hour, int minute, int second) {
        super(year, month, day, hour, minute, second);
    }

    public TimeCalendar(Locale locale) {
        super(locale);
    }

    public TimeCalendar(String timezone) {
        super(TimeZone.getTimeZone(timezone));
    }

    public TimeCalendar(TimeZone timezone) {
        super(timezone);
    }

    public TimeCalendar(TimeZone timezone, Locale locale) {
        super(timezone, locale);
    }

    public void setTimeZone(String timezone) {
        if (timezone == null) {
            throw new NullPointerException("timezone is null!");
        }

        setTimeZone(TimeZone.getTimeZone(timezone));
    }

    public void set(TimeCalendar that) {
        that.complete();
        this.allDay = that.allDay;
        setTimeInMillis(that.getTimeInMillis());
    }

    /**
     * Convert this time object so the time represented remains the same, but is instead located in
     * a different timezone. This method automatically calls complete() in some cases.
     */
    public void switchTimezone(String timezone) {
        complete();
        setTimeZone(timezone);
    }

    public int getJulianday() {
        return getJulianDay(getTimeInMillis(), getGmtOffset());
    }

    public long getGmtOffset() {
        complete();
        return getGmtOffset(time);
    }

    /**
     * Returns the offset in milliseconds from UTC for this time zone at {@code time}. The offset
     * includes daylight savings time if the specified date is within the daylight savings time
     * period.
     *
     * @param time the time in UTC milliseconds
     */
    public long getGmtOffset(long time) {
        return getTimeZone().getOffset(time);
    }

    /**
     * Computes the Julian day number for a point in time in a particular timezone. The Julian day
     * for a given date is the same for every timezone. For example, the Julian day for July 1, 2008
     * is 2454649.
     * <p>
     * The Julian day is useful for testing if two events occur on the same calendar date and for
     * determining the relative time of an event from the present ("yesterday", "3 days ago", etc.).
     *
     * @param millis       the time in UTC milliseconds
     * @param offsetMillis the offset from UTC in seconds
     * @return the Julian day
     */
    public static int getJulianDay(long millis, long offsetMillis) {
        long julianDay = (millis + offsetMillis - BASE_DATE_MILLIS) / DateUtils.DAY_IN_MILLIS;
        return (int) julianDay + MIN_JULIAN_DAY;
    }

    /**
     * @param millis the time in local time zone milliseconds
     * @return the Julian day
     */
    public static int getJulianDay(long millis) {
        return getJulianDay(millis, 0);
    }

    public void clear(String timezone) {
        if (timezone == null) {
            throw new NullPointerException("timezone is null!");
        }
        clear();
        setTimeZone(TimeZone.getTimeZone(timezone));
    }

    /**
     * 重写该方法，最大支持2099年 同时支持返回最大的 julianday
     */
    @Override
    public int getActualMaximum(int field) {
        if (field == YEAR) {
            return 2099;
        }

        if (field == JULIAN_DAY) {
            return MAX_JULIAN_DAY;
        }

        return super.getActualMaximum(field);
    }

    /**
     * 重写该方法，最小支持1900年 同时支持返回最小的 julianday
     */
    @Override
    public int getActualMinimum(int field) {
        if (field == YEAR) {
            return 1900;
        }

        if (field == JULIAN_DAY) {
            return MIN_JULIAN_DAY;
        }

        return super.getActualMinimum(field);
    }

    public String getTimezoneId() {
        return getTimeZone().getID();
    }

    // method in Time

    /**
     * Returns the timezone string that is currently set for the device.
     */
    public static String getCurrentTimezone() {
        return TimeZone.getDefault().getID();
    }

    /**
     * Sets the time of the given Time object to the current time.
     */
    public void setToNow() {
        setTimeInMillis(System.currentTimeMillis());
    }

    /**
     * <p>
     * Sets the time from the given Julian day number, which must be based on the same timezone that
     * is set in this Time object. The "gmtoff" field need not be initialized because the given
     * Julian day may have a different GMT offset than whatever is currently stored in this Time
     * object anyway. After this method returns all the fields will be normalized and the time will
     * be set to 12am at the beginning of the given Julian day.
     * </p>
     * <p>
     * The only exception to this is if 12am does not exist for that day because of daylight saving
     * time. For example, Cairo, Eqypt moves time ahead one hour at 12am on April 25, 2008 and there
     * are a few other places that also change daylight saving time at 12am. In those cases, the
     * time will be set to 1am.
     * </p>
     * <p>
     * Same as {@link android.text.format.Time#setJulianDay}
     * </p>
     *
     * @param julianDay the Julian day in the timezone for this Time object
     * @return the UTC milliseconds for the beginning of the Julian day
     */
    public long setJulianDay(int julianDay) {
        // Don't bother with the GMT offset since we don't know the correct
        // value for the given Julian day. Just get close and then adjust
        // the day.
        long millis = (julianDay - EPOCH_JULIAN_DAY) * DateUtils.DAY_IN_MILLIS;
        setTimeInMillis(millis);

        // Figure out how close we are to the requested Julian day.
        // We can't be off by more than a day.
        int approximateDay = getJulianDay(millis, getGmtOffset(millis));
        int diff = julianDay - approximateDay;
        add(DAY_OF_MONTH, diff);

        // Set the time to 12am and re-normalize.
        set(HOUR_OF_DAY, 0);
        set(MINUTE, 0);
        set(SECOND, 0);
        return getTimeInMillis();
    }

    /**
     * Parses a date-time string in either the RFC 2445 format or an abbreviated format that does
     * not include the "time" field. For example, all of the following strings are valid:
     * <ul>
     * <li>"20081013T160000Z"</li>
     * <li>"20081013T160000"</li>
     * <li>"20081013"</li>
     * </ul>
     * Returns whether or not the time is in UTC (ends with Z). If the string ends with "Z" then the
     * timezone is set to UTC. If the date-time string included only a date and no time field, then
     * the <code>allDay</code> field of this Time class is set to true and the <code>hour</code>,
     * <code>minute</code>, and <code>second</code> fields are set to zero; otherwise (a time field
     * was included in the date-time string) <code>allDay</code> is set to false. The fields
     * <code>weekDay</code>, <code>yearDay</code>, and <code>gmtoff</code> are always set to zero,
     * and the field <code>isDst</code> is set to -1 (unknown). To set those fields, call
     * {@link android.text.format.Time#normalize(boolean)} after parsing. To parse a date-time string and convert it to UTC
     * milliseconds, do something like this:
     * <p>
     * <pre>
     * Time time = new Time();
     * String date = &quot;20081013T160000Z&quot;;
     * time.parse(date);
     * long millis = time.normalize(false);
     * </pre>
     *
     * @param s the string to parse
     * @return true if the resulting time value is in UTC time
     * @throws android.util.TimeFormatException if s cannot be parsed.
     */
    public boolean parse(String s) {
        if (s == null) {
            throw new NullPointerException("time string is null");
        }
        if (parseInternal(s)) {
            setTimeZone(TimeZone.getTimeZone("UTC"));
            // compute time and fields
            complete();
            return true;
        }

        // compute time and fields
        complete();
        return false;
    }

    /**
     * Parse a time in the current zone in YYYYMMDDTHHMMSS format.
     */
    private boolean parseInternal(String s) {
        int len = s.length();
        if (len < 8) {
            throw new TimeFormatException("String is too short: \"" + s +
                    "\" Expected at least 8 characters.");
        }

        boolean inUtc = false;

        // year
        int n = getChar(s, 0, 1000);
        n += getChar(s, 1, 100);
        n += getChar(s, 2, 10);
        n += getChar(s, 3, 1);
        // year = n;
        set(YEAR, n);

        // month
        n = getChar(s, 4, 10);
        n += getChar(s, 5, 1);
        n--;
        // month = n;
        set(MONTH, n);

        // day of month
        n = getChar(s, 6, 10);
        n += getChar(s, 7, 1);
        // monthDay = n;
        set(DAY_OF_MONTH, n);

        if (len > 8) {
            if (len < 15) {
                throw new TimeFormatException(
                        "String is too short: \"" + s
                                + "\" If there are more than 8 characters there must be at least"
                                + " 15.");
            }
            checkChar(s, 8, 'T');
            allDay = false;

            // hour
            n = getChar(s, 9, 10);
            n += getChar(s, 10, 1);
            // hour = n;
            set(HOUR, n);

            // min
            n = getChar(s, 11, 10);
            n += getChar(s, 12, 1);
            // minute = n;
            set(MINUTE, n);

            // sec
            n = getChar(s, 13, 10);
            n += getChar(s, 14, 1);
            // second = n;
            set(SECOND, n);

            if (len > 15) {
                // Z
                checkChar(s, 15, 'Z');
                inUtc = true;
            }
        } else {
            allDay = true;
            // hour = 0;
            set(HOUR, 0);
            // minute = 0;
            set(MINUTE, 0);
            // second = 0;
            set(SECOND, 0);

            inUtc = true;
        }

        set(MILLISECOND, 0);
        // XXX 不设置这些值，有compute时计算
        // weekDay = 0;
        // yearDay = 0;
        // isDst = -1;
        // gmtoff = 0;
        return inUtc;
    }

    private void checkChar(String s, int spos, char expected) {
        char c = s.charAt(spos);
        if (c != expected) {
            throw new TimeFormatException(String.format(
                    "Unexpected character 0x%02d at pos=%d.  Expected 0x%02d (\'%c\').",
                    (int) c, spos, (int) expected, expected));
        }
    }

    private static int getChar(String s, int spos, int mul) {
        char c = s.charAt(spos);
        if (Character.isDigit(c)) {
            return Character.getNumericValue(c) * mul;
        } else {
            throw new TimeFormatException("Parse error at pos=" + spos);
        }
    }

    /**
     * Parse a time in RFC 3339 format. This method also parses simple dates (that is, strings that
     * contain no time or time offset). For example, all of the following strings are valid:
     * <ul>
     * <li>"2008-10-13T16:00:00.000Z"</li>
     * <li>"2008-10-13T16:00:00.000+07:00"</li>
     * <li>"2008-10-13T16:00:00.000-07:00"</li>
     * <li>"2008-10-13"</li>
     * </ul>
     * <p>
     * If the string contains a time and time offset, then the time offset will be used to convert
     * the time value to UTC.
     * </p>
     * <p>
     * Returns true if the resulting time value is in UTC time.
     * </p>
     *
     * @param s the string to parse
     * @return true if the resulting time value is in UTC time
     * @throws android.util.TimeFormatException if s cannot be parsed.
     */
    public boolean parse3339(String s) {
        if (s == null) {
            throw new NullPointerException("time string is null");
        }
        if (parse3339Internal(s)) {
            setTimeZone(TimeZone.getTimeZone("UTC"));
            // compute time and fields
            complete();
            return true;
        }

        // compute time and fields
        complete();
        return false;
    }

    private boolean parse3339Internal(String s) {
        int len = s.length();
        if (len < 10) {
            throw new TimeFormatException("String too short --- expected at least 10 characters.");
        }
        boolean inUtc = false;

        // year
        int n = getChar(s, 0, 1000);
        n += getChar(s, 1, 100);
        n += getChar(s, 2, 10);
        n += getChar(s, 3, 1);
        // year = n;
        set(YEAR, n);

        checkChar(s, 4, '-');

        // month
        n = getChar(s, 5, 10);
        n += getChar(s, 6, 1);
        --n;
        // month = n;
        set(MONTH, n);

        checkChar(s, 7, '-');

        // day
        n = getChar(s, 8, 10);
        n += getChar(s, 9, 1);
        // monthDay = n;
        set(DAY_OF_MONTH, n);

        if (len >= 19) {
            // T
            checkChar(s, 10, 'T');
            allDay = false;

            // hour
            n = getChar(s, 11, 10);
            n += getChar(s, 12, 1);

            // Note that this.hour is not set here. It is set later.
            int hour = n;

            checkChar(s, 13, ':');

            // minute
            n = getChar(s, 14, 10);
            n += getChar(s, 15, 1);
            // Note that this.minute is not set here. It is set later.
            int minute = n;

            checkChar(s, 16, ':');

            // second
            n = getChar(s, 17, 10);
            n += getChar(s, 18, 1);
            // second = n;
            set(SECOND, n);

            set(MILLISECOND, 0);

            // skip the '.XYZ' -- we don't care about subsecond precision.

            int tzIndex = 19;
            if (tzIndex < len && s.charAt(tzIndex) == '.') {
                do {
                    tzIndex++;
                } while (tzIndex < len && Character.isDigit(s.charAt(tzIndex)));
            }

            int offset = 0;
            if (len > tzIndex) {
                char c = s.charAt(tzIndex);
                // NOTE: the offset is meant to be subtracted to get from local time
                // to UTC. we therefore use 1 for '-' and -1 for '+'.
                switch (c) {
                    case 'Z':
                        // Zulu time -- UTC
                        offset = 0;
                        break;
                    case '-':
                        offset = 1;
                        break;
                    case '+':
                        offset = -1;
                        break;
                    default:
                        throw new TimeFormatException(String.format(
                                "Unexpected character 0x%02d at position %d.  Expected + or -",
                                (int) c, tzIndex));
                }
                inUtc = true;

                if (offset != 0) {
                    if (len < tzIndex + 6) {
                        throw new TimeFormatException(
                                String.format("Unexpected length; should be %d characters",
                                        tzIndex + 6));
                    }

                    // hour
                    n = getChar(s, tzIndex + 1, 10);
                    n += getChar(s, tzIndex + 2, 1);
                    n *= offset;
                    hour += n;

                    // minute
                    n = getChar(s, tzIndex + 4, 10);
                    n += getChar(s, tzIndex + 5, 1);
                    n *= offset;
                    minute += n;
                }
            }
            // this.hour = hour;
            set(HOUR, hour);
            // this.minute = minute;
            set(MINUTE, minute);

            if (offset != 0) {
                // normalize(false);
                computeTime();
            }
        } else {
            allDay = true;
            // this.hour = 0;
            set(HOUR, 0);
            // this.minute = 0;
            set(MINUTE, 0);
            // this.second = 0;
            set(SECOND, 0);

            set(MILLISECOND, 0);
        }

        // this.weekDay = 0;
        // this.yearDay = 0;
        // this.isDst = -1;
        // this.gmtoff = 0;
        return inUtc;
    }

    /**
     * Format according to RFC 2445 DATE-TIME type.
     * <p>
     * The same as format("%Y%m%dT%H%M%S"), or format("%Y%m%dT%H%M%SZ") for a Time with a timezone
     * set to "UTC". Same as {@link android.text.format.Time#format2445()}
     */
    public String format2445() {
        char[] buf = new char[allDay ? 8 : 16];
        int n = get(YEAR);

        buf[0] = toChar(n / 1000);
        n %= 1000;
        buf[1] = toChar(n / 100);
        n %= 100;
        buf[2] = toChar(n / 10);
        n %= 10;
        buf[3] = toChar(n);

        n = get(MONTH) + 1;
        buf[4] = toChar(n / 10);
        buf[5] = toChar(n % 10);

        n = get(DAY_OF_MONTH);
        buf[6] = toChar(n / 10);
        buf[7] = toChar(n % 10);

        if (allDay) {
            return new String(buf, 0, 8);
        }

        buf[8] = 'T';

        n = get(HOUR_OF_DAY);
        buf[9] = toChar(n / 10);
        buf[10] = toChar(n % 10);

        n = get(MINUTE);
        buf[11] = toChar(n / 10);
        buf[12] = toChar(n % 10);

        n = get(SECOND);
        buf[13] = toChar(n / 10);
        buf[14] = toChar(n % 10);

        if ("UTC".equals(getTimezoneId())) {
            // The letter 'Z' is appended to the end.
            buf[15] = 'Z';
            return new String(buf, 0, 16);
        } else {
            return new String(buf, 0, 15);
        }
    }

    private char toChar(int n) {
        return (n >= 0 && n <= 9) ? (char) (n + '0') : ' ';
    }

    /**
     * Get week number for Time
     */
    public int getWeekNumber() {
        return day2TimeDay(get(DAY_OF_WEEK));
    }

    public static int compare(TimeCalendar a, TimeCalendar b) {
        return a.compareTo(b);
    }

    public boolean sameDay(TimeCalendar o) {
        return getYear() == o.getYear() && getMonth() == o.getMonth()
                && getDayOfMonth() == o.getDayOfMonth();
    }

    public static class TimeFormatException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public TimeFormatException(String s) {
            super(s);
        }
    }

    /**
     * Convert week day in Calendar to weekday in Time
     *
     * @param weekday in Calendar
     * @return weekday in Time {@link android.text.format.Time#weekDay}
     */
    private int day2TimeDay(int weekday) {
        return (weekday + 6) % 7;
    }

    public int getYear() {
        return get(YEAR);
    }

    public int getMonth() {
        return get(MONTH);
    }

    public int getDayOfMonth() {
        return get(DAY_OF_MONTH);
    }

    public int getWeekOfYear() {
        return get(WEEK_OF_YEAR);
    }

    /**
     * Returns the week since {@link #EPOCH_JULIAN_DAY} (Jan 1, 1970)
     * adjusted for first day of week.
     * <p>
     * This takes a julian day and the week start day and calculates which
     * week since {@link #EPOCH_JULIAN_DAY} that day occurs in,
     * starting at 0. *Do not* use this to compute the ISO week number for
     * the year.
     *
     * @param julianDay      The julian day to calculate the week number for
     * @param firstDayOfWeek Which week day is the first day of the week,
     *                       see {@link #SUNDAY}
     * @return Weeks since the epoch
     */
    public static int getWeeksSinceEpochJulianDay(int julianDay, int firstDayOfWeek) {
        int diff = THURSDAY - firstDayOfWeek;
        if (diff < 0) {
            diff += 7;
        }
        int refDay = EPOCH_JULIAN_DAY - diff;
        return (julianDay - refDay) / 7;
    }

}
