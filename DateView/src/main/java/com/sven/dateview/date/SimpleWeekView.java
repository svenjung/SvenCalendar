package com.sven.dateview.date;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import com.sven.dateview.TimeCalendar;

import java.util.Locale;

/**
 * Created by Sven.J on 18-4-25.
 */
public class SimpleWeekView extends WeekView {
    private EventIndicator mIndicator;
    private TimeCalendar mTime;

    private Paint mEventIndicatorPaint;
    private int mEventIndicatorColor;

    public SimpleWeekView(Context context) {
        this(context, null);
    }

    public SimpleWeekView(Context context, AttributeSet attr) {
        super(context, attr);

        mTime = TimeCalendar.getInstance();

        mEventIndicatorColor = 0xFF696969;
        mEventIndicatorPaint = new Paint();
        mEventIndicatorPaint.setFakeBoldText(true);
        mEventIndicatorPaint.setAntiAlias(true);
        mEventIndicatorPaint.setColor(mEventIndicatorColor);
        mEventIndicatorPaint.setTextAlign(Paint.Align.CENTER);
        mEventIndicatorPaint.setStyle(Paint.Style.FILL);
    }

    public void setEventIndicator(EventIndicator indicator) {
        mIndicator = indicator;
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

        if (mIndicator != null) {
            mTime.set(year, month, day);
            if (mIndicator.hasEvents(mTime.getJulianDay())) {
                canvas.drawCircle(x, y + MINI_DAY_NUMBER_TEXT_SIZE, 8, mEventIndicatorPaint);
            }
        }

        canvas.drawText(String.format(Locale.getDefault(), "%d", day), x, y, mWeekNumPaint);
    }
}
