/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sven.dateview.date;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;

import com.sven.dateview.TimeCalendar;

import java.util.Locale;

public class SimpleMonthView extends MonthView {
    private EventIndicator mIndicator;
    private TimeCalendar mTime;

    public SimpleMonthView(Context context) {
        this(context, null);
    }

    public SimpleMonthView(Context context, AttributeSet attr) {
        super(context, attr);

        mTime = TimeCalendar.getInstance();
    }

    public void setEventInficator(EventIndicator indicator) {
        mIndicator = indicator;
    }

    @Override
    public void drawMonthDay(Canvas canvas, int year, int month, int day,
            int x, int y, int startX, int stopX, int startY, int stopY) {
        boolean drawCircle = false;
        if (day == mPressedDay || day == mSelectedDay) {
            mSelectedCirclePaint.setColor(mSelectedCircleColor);
            drawCircle = true;
        }
        if (day == mToday) {
            mSelectedCirclePaint.setColor(mTodayCircleColor);
            drawCircle = true;
        }

        if (drawCircle) {
            mSelectedCirclePaint.setAlpha(180);
            canvas.drawCircle(x , y - (MINI_DAY_NUMBER_TEXT_SIZE / 3), DAY_SELECTED_CIRCLE_SIZE,
                    mSelectedCirclePaint);
        }

        // If we have a mindate or maxdate, gray out the day number if it's outside the range.
        if (isOutOfRange(year, month, day)) {
            mMonthNumPaint.setColor(mDisabledDayTextColor);
        } else if (mHasToday && mToday == day) {
            mMonthNumPaint.setColor(mTodayNumberColor);
        } else {
            mMonthNumPaint.setColor(mDayTextColor);
        }

        if (mIndicator != null) {
            mTime.set(year, month, day);
            boolean hasEvent = mIndicator.hasEvents(mTime.getJulianDay());
            if (hasEvent)
            Log.e("MonthView", "day : " + year + "/" + (month + 1) + "/" + day + " hasEvent = " + hasEvent);
        }

        canvas.drawText(String.format(Locale.getDefault(), "%d", day), x, y, mMonthNumPaint);
    }

}
