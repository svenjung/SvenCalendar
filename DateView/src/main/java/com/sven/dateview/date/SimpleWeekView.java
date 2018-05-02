package com.sven.dateview.date;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import com.sven.dateview.TimeCalendar;

import java.util.Locale;

/**
 * Created by Sven.J on 18-4-25.
 */
public class SimpleWeekView extends WeekView {

    public SimpleWeekView(Context context) {
        super(context);
    }

    public SimpleWeekView(Context context, AttributeSet attr) {
        super(context, attr);
    }

    @Override
    public void drawWeekDay(Canvas canvas, int year, int month, int day, int x, int y,
                            int startX, int stopX, int startY, int stopY) {
        TimeCalendar calendar = new TimeCalendar(year, month, day);
        int drawJulianDay = calendar.getJulianDay();
        boolean drawCircle = false;

        if (drawJulianDay == mPressedDay || drawJulianDay == mSelectedDay) {
            mSelectedCirclePaint.setColor(mSelectedCircleColor);
            drawCircle = true;
        }
        if (drawJulianDay == mToday) {
            mSelectedCirclePaint.setColor(mTodayCircleColor);
            drawCircle = true;
        }

        if (drawCircle) {
            mSelectedCirclePaint.setAlpha(180);
            canvas.drawCircle(x, y - (MINI_DAY_NUMBER_TEXT_SIZE / 3), DAY_SELECTED_CIRCLE_SIZE,
                    mSelectedCirclePaint);
        }

        // If we have a mindate or maxdate, gray out the day number if it's outside the range.
        if (isOutOfRange(drawJulianDay)) {
            mWeekNumPaint.setColor(mDisabledDayTextColor);
        } else if (mHasToday && mToday == drawJulianDay) {
            mWeekNumPaint.setColor(mTodayNumberColor);
        } else {
            mWeekNumPaint.setColor(mDayTextColor);
        }
        canvas.drawText(String.format(Locale.getDefault(), "%d", day), x, y, mWeekNumPaint);
    }
}
