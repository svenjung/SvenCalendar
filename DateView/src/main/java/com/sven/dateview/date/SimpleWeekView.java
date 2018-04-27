package com.sven.dateview.date;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;

import com.sven.dateview.TimeCalendar;

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
        int drawJulianDay = calendar.getJulianday();
        if (mSelectedDay == drawJulianDay) {
            canvas.drawCircle(x , y - (MINI_DAY_NUMBER_TEXT_SIZE / 3), DAY_SELECTED_CIRCLE_SIZE,
                    mSelectedCirclePaint);
        }

        if (mHasToday && mToday == day) {
            mMonthNumPaint.setColor(mTodayNumberColor);
        } else {
            mMonthNumPaint.setColor(Color.BLACK);
        }
        canvas.drawText(String.format("%d", day), x, y, mMonthNumPaint);
    }
}
