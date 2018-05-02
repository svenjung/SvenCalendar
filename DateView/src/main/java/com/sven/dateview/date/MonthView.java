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
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.widget.ExploreByTouchHelper;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.sven.dateview.R;
import com.sven.dateview.TimeCalendar;

import java.security.InvalidParameterException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * A calendar-like view displaying a specified month and the appropriate selectable day numbers
 * within the specified month.
 */
public abstract class MonthView extends View {
    private static final String TAG = "MonthView";

    /**
     * These params can be passed into the view to control how it appears.
     * {@link #VIEW_PARAMS_WEEK} is the only required field, though the default
     * values are unlikely to fit most layouts correctly.
     */
    /**
     * This sets the height of this week in pixels
     */
    public static final String VIEW_PARAMS_HEIGHT = "height";
    public static final String VIEW_PARAMS_MONTH = "month";
    public static final String VIEW_PARAMS_YEAR = "year";
    /**
     * This sets one of the days in this view as selected {@link Time#SUNDAY}
     * through {@link Time#SATURDAY}.
     */
    public static final String VIEW_PARAMS_SELECTED_DAY = "selected_day";
    /**
     * Which day the week should start on. {@link Time#SUNDAY} through
     * {@link Time#SATURDAY}.
     */
    public static final String VIEW_PARAMS_WEEK_START = "week_start";
    /**
     * How many days to display at a time. Days will be displayed starting with
     * {@link #mWeekStart}.
     */
    public static final String VIEW_PARAMS_NUM_DAYS = "num_days";
    /**
     * Which month is currently in focus, as defined by {@link Time#month}
     * [0-11].
     */
    public static final String VIEW_PARAMS_FOCUS_MONTH = "focus_month";
    /**
     * If this month should display week numbers. false if 0, true otherwise.
     */
    public static final String VIEW_PARAMS_SHOW_WK_NUM = "show_wk_num";

    protected static int DEFAULT_HEIGHT = 32;
    protected static int MIN_HEIGHT = 10;
    protected static final int DEFAULT_SELECTED_DAY = -1;
    protected static final int DEFAULT_WEEK_START = Calendar.SUNDAY;
    protected static final int DEFAULT_NUM_DAYS = 7;
    protected static final int DEFAULT_SHOW_WK_NUM = 0;
    protected static final int DEFAULT_FOCUS_MONTH = -1;
    protected static final int DEFAULT_NUM_ROWS = 6;
    protected static final int MAX_NUM_ROWS = 6;

    private static final int SELECTED_CIRCLE_ALPHA = 60;

    protected static int DAY_SEPARATOR_WIDTH = 1;
    protected static int MINI_DAY_NUMBER_TEXT_SIZE;
    protected static int MONTH_LABEL_TEXT_SIZE;
    protected static int MONTH_DAY_LABEL_TEXT_SIZE;
    protected static int MONTH_HEADER_SIZE;
    protected static int DAY_SELECTED_CIRCLE_SIZE;

    protected DatePickerController mController;

    // affects the padding on the sides of this view
    protected int mEdgePadding = 0;

    protected Paint mMonthNumPaint;
    protected Paint mSelectedCirclePaint;

    protected int mMonth;
    protected int mYear;
    // Quick reference to the width of this view, matches parent
    protected int mWidth;
    // The height this view should draw at in pixels, set by height param
    protected int mRowHeight = DEFAULT_HEIGHT;
    // If this view contains the today
    protected boolean mHasToday = false;
    // Which day is selected [0-6] or -1 if no day is selected
    protected int mSelectedDay = -1;
    // Which day is today [0-6] or -1 if no day is today
    protected int mToday = DEFAULT_SELECTED_DAY;
    // Which day of the week to start on [0-6]
    protected int mWeekStart = DEFAULT_WEEK_START;
    // How many days to display
    protected int mNumDays = DEFAULT_NUM_DAYS;
    // The number of days + a spot for week number if it is displayed
    protected int mNumCells = mNumDays;

    private final Calendar mCalendar;
    protected final Calendar mDayLabelCalendar;
    private final MonthViewTouchHelper mTouchHelper;

    protected int mNumRows = DEFAULT_NUM_ROWS;

    // Optional listener for handling day click actions
    protected OnDayClickListener mOnDayClickListener;

    // Optional listener for handling day long lick actions
    protected OnDayLongClickListener mOnDayLongClickListener;

    // Whether to prevent setting the accessibility delegate
    private boolean mLockAccessibilityDelegate;

    protected int mDayTextColor;
    protected int mTodayNumberColor;
    protected int mSelectedNumberColor;
    protected int mDisabledDayTextColor;

    protected int mTodayCircleColor;
    protected int mSelectedCircleColor;

    /**
     * Whether the long press's action has been invoked.  The tap's action is invoked on the
     * up event while a long press is invoked as soon as the long press duration is reached, so
     * a long press could be performed before the tap is checked, in which case the tap's action
     * should not be invoked.
     */
    private boolean mHasPerformedLongClick;

    protected int mPressedDay = -1;

    private int mLastDay = -1;

    private CheckForLongPress mPendingCheckForLongPress;
    private CheckForTap mPendingCheckForTap = null;

    public MonthView(Context context) {
        this(context, null);
    }

    public MonthView(Context context, AttributeSet attr) {
        super(context, attr);
        Resources res = context.getResources();

        mDayLabelCalendar = Calendar.getInstance();
        mCalendar = Calendar.getInstance();

        mDayTextColor = res.getColor(R.color.black);
        mTodayNumberColor = res.getColor(R.color.white);
        mSelectedNumberColor = res.getColor(R.color.neutral_pressed);
        mDisabledDayTextColor = res.getColor(R.color.date_picker_text_disabled);

        mTodayCircleColor = res.getColor(R.color.red);
        mSelectedCircleColor = res.getColor(R.color.darker_blue);

        MINI_DAY_NUMBER_TEXT_SIZE = res.getDimensionPixelSize(R.dimen.day_number_size);
        MONTH_LABEL_TEXT_SIZE = res.getDimensionPixelSize(R.dimen.month_label_size);
        MONTH_DAY_LABEL_TEXT_SIZE = res.getDimensionPixelSize(R.dimen.month_day_label_text_size);
        MONTH_HEADER_SIZE = res.getDimensionPixelOffset(R.dimen.month_list_item_header_height);
        DAY_SELECTED_CIRCLE_SIZE = res
                .getDimensionPixelSize(R.dimen.day_number_select_circle_radius);

        mRowHeight = (res.getDimensionPixelOffset(R.dimen.date_picker_view_animator_height)) / MAX_NUM_ROWS;
        // Set up accessibility components.
        mTouchHelper = getMonthViewTouchHelper();
        ViewCompat.setAccessibilityDelegate(this, mTouchHelper);
        ViewCompat.setImportantForAccessibility(this, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
        mLockAccessibilityDelegate = true;

        // Sets up any standard paints that will be used
        initView();
    }

    public void setDatePickerController(DatePickerController controller) {
        mController = controller;
    }

    protected MonthViewTouchHelper getMonthViewTouchHelper() {
        return new MonthViewTouchHelper(this);
    }

    @Override
    public void setAccessibilityDelegate(AccessibilityDelegate delegate) {
        // Workaround for a JB MR1 issue where accessibility delegates on
        // top-level ListView items are overwritten.
        if (!mLockAccessibilityDelegate) {
            super.setAccessibilityDelegate(delegate);
        }
    }

    public void setOnDayClickListener(OnDayClickListener listener) {
        mOnDayClickListener = listener;
    }

    public void setOnDayLongClickListener(OnDayLongClickListener listener) {
        mOnDayLongClickListener = listener;
    }

    @Override
    public boolean dispatchHoverEvent(MotionEvent event) {
        // First right-of-refusal goes the touch exploration helper.
        return mTouchHelper.dispatchHoverEvent(event) || super.dispatchHoverEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int day = getDayFromLocation(event.getX(), event.getY());

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastDay = day;
                mHasPerformedLongClick = false;
                if (day > 0 && !isOutOfRange(mYear, mMonth, day)) {
                    if (mPendingCheckForTap == null) {
                        mPendingCheckForTap = new CheckForTap();
                    }
                    mPendingCheckForTap.day = day;
                    postDelayed(mPendingCheckForTap, /*ViewConfiguration.getTapTimeout()*/25);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!mHasPerformedLongClick) {
                    if (mPressedDay != -1) {
                        performDayClick(mPressedDay);
                    }
                }

                removeTapCallback();
                removeLongPressCallback();
                setPressedDay(-1);
                mHasPerformedLongClick = false;
                break;
            case MotionEvent.ACTION_MOVE:
                // Be lenient about moving outside of touch down day rect
                if (day != mLastDay) {
                    setPressedDay(-1);
                    // Remove any future long press/tap checks
                    removeTapCallback();
                    removeLongPressCallback();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                setPressedDay(-1);
                removeTapCallback();
                removeLongPressCallback();
                mHasPerformedLongClick = false;
                break;
        }
        return true;
    }

    private void performDayClick(int day) {
        // If the min / max date are set, only process the click if it's a valid selection.
        if (isOutOfRange(mYear, mMonth, day)) {
            return;
        }

        if (mOnDayClickListener != null) {
            mOnDayClickListener.onDayClick(this, mYear, mMonth, day);
        }

        setSelectedDay(day, false);

        playSoundEffect(SoundEffectConstants.CLICK);

        // This is a no-op if accessibility is turned off.
        mTouchHelper.sendEventForVirtualView(day, AccessibilityEvent.TYPE_VIEW_CLICKED);
    }

    private void performDayLongClick(int day) {
        // If the min / max date are set, only process the click if it's a valid selection.
        if (isOutOfRange(mYear, mMonth, day)) {
            return;
        }

        if (mOnDayLongClickListener != null) {
            mOnDayLongClickListener.onDayLongClick(MonthView.this, mYear, mMonth, day);
        }

        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

        // This is a no-op if accessibility is turned off.
        mTouchHelper.sendEventForVirtualView(day, AccessibilityEvent.TYPE_VIEW_LONG_CLICKED);
    }

    private void setPressedDay(int day) {
        if (mPressedDay != day) {
            mPressedDay = day;

            // refresh press state
            invalidate();
        }
    }

    private void removeTapCallback() {
        if (mPendingCheckForTap != null) {
            removeCallbacks(mPendingCheckForTap);
        }
    }

    private void removeLongPressCallback() {
        if (mPendingCheckForLongPress != null) {
            removeCallbacks(mPendingCheckForLongPress);
        }
    }

    private void checkForLongClick(int delayOffset, int day) {
        if (mOnDayLongClickListener != null) {
            if (mPendingCheckForLongPress == null) {
                mPendingCheckForLongPress = new CheckForLongPress();
            }

            mPendingCheckForLongPress.day = day;
            postDelayed(mPendingCheckForLongPress, ViewConfiguration.getLongPressTimeout() - delayOffset);
        }
    }

    private final class CheckForLongPress implements Runnable {
        public int day;

        @Override
        public void run() {
            performDayLongClick(day);
            mHasPerformedLongClick = true;
        }
    }

    private final class CheckForTap implements Runnable {
        public int day;
        @Override
        public void run() {
            setPressedDay(day);
            checkForLongClick(ViewConfiguration.getTapTimeout(), day);
        }
    }

    /**
     * Sets up the text and style properties for painting. Override this if you
     * want to use a different paint.
     */
    protected void initView() {
        mSelectedCirclePaint = new Paint();
        mSelectedCirclePaint.setFakeBoldText(true);
        mSelectedCirclePaint.setAntiAlias(true);
        mSelectedCirclePaint.setColor(mTodayNumberColor);
        mSelectedCirclePaint.setTextAlign(Align.CENTER);
        mSelectedCirclePaint.setStyle(Style.FILL);
        mSelectedCirclePaint.setAlpha(SELECTED_CIRCLE_ALPHA);

        mMonthNumPaint = new Paint();
        mMonthNumPaint.setAntiAlias(true);
        mMonthNumPaint.setTextSize(MINI_DAY_NUMBER_TEXT_SIZE);
        mMonthNumPaint.setStyle(Style.FILL);
        mMonthNumPaint.setTextAlign(Align.CENTER);
        mMonthNumPaint.setFakeBoldText(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawMonthNums(canvas);
    }

    protected int mDayOfWeekStart = 0;

    /**
     * Sets all the parameters for displaying this week. The only required
     * parameter is the week number. Other parameters have a default value and
     * will only update if a new value is included, except for focus month,
     * which will always default to no focus month if no value is passed in. See
     * {@link #VIEW_PARAMS_HEIGHT} for more info on parameters.
     *
     * @param params A map of the new parameters, see {@link #VIEW_PARAMS_HEIGHT}
     */
    public void setMonthParams(HashMap<String, Integer> params) {
        if (!params.containsKey(VIEW_PARAMS_MONTH) && !params.containsKey(VIEW_PARAMS_YEAR)) {
            throw new InvalidParameterException("You must specify month and year for this view");
        }
        setTag(params);
        // We keep the current value for any params not present
        if (params.containsKey(VIEW_PARAMS_HEIGHT)) {
            mRowHeight = params.get(VIEW_PARAMS_HEIGHT);
            if (mRowHeight < MIN_HEIGHT) {
                mRowHeight = MIN_HEIGHT;
            }
        }
        if (params.containsKey(VIEW_PARAMS_SELECTED_DAY)) {
            mSelectedDay = params.get(VIEW_PARAMS_SELECTED_DAY);
        }

        // Allocate space for caching the day numbers and focus values
        mMonth = params.get(VIEW_PARAMS_MONTH);
        mYear = params.get(VIEW_PARAMS_YEAR);

        // Figure out what day today is
        final TimeCalendar today = TimeCalendar.getInstance();
        mHasToday = false;
        mToday = -1;

        mCalendar.set(Calendar.MONTH, mMonth);
        mCalendar.set(Calendar.YEAR, mYear);
        mCalendar.set(Calendar.DAY_OF_MONTH, 1);
        mDayOfWeekStart = mCalendar.get(Calendar.DAY_OF_WEEK);

        if (params.containsKey(VIEW_PARAMS_WEEK_START)) {
            mWeekStart = params.get(VIEW_PARAMS_WEEK_START);
        } else {
            mWeekStart = mCalendar.getFirstDayOfWeek();
        }

        mNumCells = getDaysInMonth(mMonth, mYear);
        for (int i = 0; i < mNumCells; i++) {
            final int day = i + 1;
            if (sameDay(day, today)) {
                mHasToday = true;
                mToday = day;
            }
        }
        mNumRows = calculateNumRows();

        // Invalidate cached accessibility information.
        mTouchHelper.invalidateRoot();
    }

    // 外部通过API设置选中日期的才需要重新绘制View
    public void setSelectedDay(int day) {
        setSelectedDay(day, true);
    }

    private void setSelectedDay(int day, boolean invalidate) {
        if (mSelectedDay != day) {
            mSelectedDay = day;

            if (invalidate) {
                invalidate();
            }
        }
    }

    public int getSelectedDay() {
        return mSelectedDay;
    }

    public void setWeekStart(int weekStart) {
        if (mWeekStart != weekStart) {
            mWeekStart = weekStart;
            invalidate();
        }
    }

    public void reuse() {
        mNumRows = DEFAULT_NUM_ROWS;
        requestLayout();
    }

    private int getDaysInMonth(int year, int month) {
        TimeCalendar calendar = new TimeCalendar(year, month, 1);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    private int calculateNumRows() {
        int offset = findDayOffset();
        int dividend = (offset + mNumCells) / mNumDays;
        int remainder = (offset + mNumCells) % mNumDays;
        return (dividend + (remainder > 0 ? 1 : 0));
    }

    private boolean sameDay(int day, TimeCalendar today) {
        return mYear == today.getYear() &&
                mMonth == today.getMonth() &&
                day == today.getDay();
    }

    public int getRowHeight() {
        return mRowHeight;
    }

    public int getMonthHeight() {
        return mRowHeight * mNumRows;
    }

    public int getSelectedRow() {
        int offset = ((mSelectedDay + findDayOffset()) % DEFAULT_NUM_DAYS == 0) ? 0 : 1;
        return (mSelectedDay + findDayOffset()) / DEFAULT_NUM_DAYS + offset;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mRowHeight * mNumRows);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;

        // Invalidate cached accessibility information.
        mTouchHelper.invalidateRoot();
    }

    public int getMonth() {
        return mMonth;
    }

    public int getYear() {
        return mYear;
    }

    /**
     * Draws the week and month day numbers for this week. Override this method
     * if you need different placement.
     *
     * @param canvas The canvas to draw on
     */
    protected void drawMonthNums(Canvas canvas) {
        int y = (((mRowHeight + MINI_DAY_NUMBER_TEXT_SIZE) / 2) - DAY_SEPARATOR_WIDTH);
        final float dayWidthHalf = (mWidth - mEdgePadding * 2) / (mNumDays * 2.0f);
        int j = findDayOffset();
        for (int dayNumber = 1; dayNumber <= mNumCells; dayNumber++) {
            final int x = (int)((2 * j + 1) * dayWidthHalf + mEdgePadding);

            int yRelativeToDay = (mRowHeight + MINI_DAY_NUMBER_TEXT_SIZE) / 2 - DAY_SEPARATOR_WIDTH;

            final int startX = (int)(x - dayWidthHalf);
            final int stopX = (int)(x + dayWidthHalf);
            final int startY = (int)(y - yRelativeToDay);
            final int stopY = (int)(startY + mRowHeight);
            drawMonthDay(canvas, mYear, mMonth, dayNumber, x, y, startX, stopX, startY, stopY);

            j++;
            if (j == mNumDays) {
                j = 0;
                y += mRowHeight;
            }
        }
    }

    /**
     * This method should draw the month day.  Implemented by sub-classes to allow customization.
     *
     * @param canvas  The canvas to draw on
     * @param year  The year of this month day
     * @param month  The month of this month day
     * @param day  The day number of this month day
     * @param x  The default x position to draw the day number
     * @param y  The default y position to draw the day number
     * @param startX  The left boundary of the day number rect
     * @param stopX  The right boundary of the day number rect
     * @param startY  The top boundary of the day number rect
     * @param stopY  The bottom boundary of the day number rect
     */
    public abstract void drawMonthDay(Canvas canvas, int year, int month, int day,
            int x, int y, int startX, int stopX, int startY, int stopY);

    protected int findDayOffset() {
        return (mDayOfWeekStart < mWeekStart ? (mDayOfWeekStart + mNumDays) : mDayOfWeekStart)
                - mWeekStart;
    }

    /**
     * Calculates the day that the given x position is in, accounting for week
     * number. Returns the day or -1 if the position wasn't in a day.
     *
     * @param x The x position of the touch event
     * @return The day number, or -1 if the position wasn't in a day
     */
    public int getDayFromLocation(float x, float y) {
        final int day = getInternalDayFromLocation(x, y);
        if (day < 1 || day > mNumCells) {
            return -1;
        }
        return day;
    }

    /**
     * Calculates the day that the given x position is in, accounting for week
     * number.
     *
     * @param x The x position of the touch event
     * @return The day number
     */
    protected int getInternalDayFromLocation(float x, float y) {
        int dayStart = mEdgePadding;
        if (x < dayStart || x > mWidth - mEdgePadding) {
            return -1;
        }
        // Selection is (x - start) / (pixels/day) == (x -s) * day / pixels
        int row = (int) (y) / mRowHeight;
        int column = (int) ((x - dayStart) * mNumDays / (mWidth - dayStart - mEdgePadding));

        int day = column - findDayOffset() + 1;
        day += row * mNumDays;
        return day;
    }

    /**
     * @return true if the specified year/month/day are within the range set by minDate and maxDate.
     * If one or either have not been set, they are considered as Integer.MIN_VALUE and
     * Integer.MAX_VALUE.
     */
    protected boolean isOutOfRange(int year, int month, int day) {
        if (isBeforeMin(year, month, day)) {
            return true;
        } else if (isAfterMax(year, month, day)) {
            return true;
        }

        return false;
    }

    private boolean isBeforeMin(int year, int month, int day) {
        if (mController == null) {
            return false;
        }
        Calendar minDate = mController.getMinDate();
        if (minDate == null) {
            return false;
        }

        if (year < minDate.get(Calendar.YEAR)) {
            return true;
        } else if (year > minDate.get(Calendar.YEAR)) {
            return false;
        }

        if (month < minDate.get(Calendar.MONTH)) {
            return true;
        } else if (month > minDate.get(Calendar.MONTH)) {
            return false;
        }

        if (day < minDate.get(Calendar.DAY_OF_MONTH)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isAfterMax(int year, int month, int day) {
        if (mController == null) {
            return false;
        }
        Calendar maxDate = mController.getMaxDate();
        if (maxDate == null) {
            return false;
        }

        if (year > maxDate.get(Calendar.YEAR)) {
            return true;
        } else if (year < maxDate.get(Calendar.YEAR)) {
            return false;
        }

        if (month > maxDate.get(Calendar.MONTH)) {
            return true;
        } else if (month < maxDate.get(Calendar.MONTH)) {
            return false;
        }

        if (day > maxDate.get(Calendar.DAY_OF_MONTH)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return The date that has accessibility focus, or {@code null} if no date
     *         has focus
     */
    public Calendar getAccessibilityFocus() {
        final int day = mTouchHelper.getAccessibilityFocusedVirtualViewId();
        if (day >= 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(mYear, mMonth, day);
            return calendar;
        }
        return null;
    }

    /**
     * Clears accessibility focus within the view. No-op if the view does not
     * contain accessibility focus.
     */
    public void clearAccessibilityFocus() {
        mTouchHelper.clearFocusedVirtualView();
    }

    /**
     * Attempts to restore accessibility focus to the specified date.
     *
     * @param day The date which should receive focus
     * @return {@code false} if the date is not valid for this month view, or
     *         {@code true} if the date received focus
     */
    public boolean restoreAccessibilityFocus(Calendar day) {
        if ((day.get(Calendar.YEAR) != mYear) || (day.get(Calendar.MONTH) != mMonth)
                || (day.get(Calendar.DAY_OF_MONTH) > mNumCells)) {
            return false;
        }
        mTouchHelper.setFocusedVirtualView(day.get(Calendar.DAY_OF_MONTH));
        return true;
    }

    /**
     * Provides a virtual view hierarchy for interfacing with an accessibility
     * service.
     */
    protected class MonthViewTouchHelper extends ExploreByTouchHelper {
        private static final String DATE_FORMAT = "dd MMMM yyyy";

        private final Rect mTempRect = new Rect();
        private final Calendar mTempCalendar = Calendar.getInstance();

        public MonthViewTouchHelper(View host) {
            super(host);
        }

        public void setFocusedVirtualView(int virtualViewId) {
            getAccessibilityNodeProvider(MonthView.this).performAction(
                    virtualViewId, AccessibilityNodeInfoCompat.ACTION_ACCESSIBILITY_FOCUS, null);
        }

        public void clearFocusedVirtualView() {
            final int focusedVirtualView = getAccessibilityFocusedVirtualViewId();
            if (focusedVirtualView != ExploreByTouchHelper.INVALID_ID) {
                getAccessibilityNodeProvider(MonthView.this).performAction(
                        focusedVirtualView,
                        AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS,
                        null);
            }
        }

        @Override
        protected int getVirtualViewAt(float x, float y) {
            final int day = getDayFromLocation(x, y);
            if (day >= 0) {
                return day;
            }
            return ExploreByTouchHelper.INVALID_ID;
        }

        @Override
        protected void getVisibleVirtualViews(List<Integer> virtualViewIds) {
            for (int day = 1; day <= mNumCells; day++) {
                virtualViewIds.add(day);
            }
        }

        @Override
        protected void onPopulateEventForVirtualView(int virtualViewId, AccessibilityEvent event) {
            event.setContentDescription(getItemDescription(virtualViewId));
        }

        @Override
        protected void onPopulateNodeForVirtualView(int virtualViewId,
                AccessibilityNodeInfoCompat node) {
            getItemBounds(virtualViewId, mTempRect);

            node.setContentDescription(getItemDescription(virtualViewId));
            node.setBoundsInParent(mTempRect);
            node.addAction(AccessibilityNodeInfo.ACTION_CLICK);

            if (virtualViewId == mSelectedDay) {
                node.setSelected(true);
            }

        }

        @Override
        protected boolean onPerformActionForVirtualView(int virtualViewId, int action,
                                                        Bundle arguments) {
            switch (action) {
                case AccessibilityNodeInfo.ACTION_CLICK:
                    performDayClick(virtualViewId);
                    return true;
                case AccessibilityNodeInfo.ACTION_LONG_CLICK:
                    performDayLongClick(virtualViewId);
                    return true;
            }

            return false;
        }

        /**
         * Calculates the bounding rectangle of a given time object.
         *
         * @param day The day to calculate bounds for
         * @param rect The rectangle in which to store the bounds
         */
        protected void getItemBounds(int day, Rect rect) {
            final int offsetX = mEdgePadding;
            final int offsetY = 0;
            final int cellHeight = mRowHeight;
            final int cellWidth = ((mWidth - (2 * mEdgePadding)) / mNumDays);
            final int index = ((day - 1) + findDayOffset());
            final int row = (index / mNumDays);
            final int column = (index % mNumDays);
            final int x = (offsetX + (column * cellWidth));
            final int y = (offsetY + (row * cellHeight));

            rect.set(x, y, (x + cellWidth), (y + cellHeight));
        }

        /**
         * Generates a description for a given time object. Since this
         * description will be spoken, the components are ordered by descending
         * specificity as DAY MONTH YEAR.
         *
         * @param day The day to generate a description for
         * @return A description of the time object
         */
        protected CharSequence getItemDescription(int day) {
            mTempCalendar.set(mYear, mMonth, day);
            final CharSequence date = DateFormat.format(DATE_FORMAT,
                    mTempCalendar.getTimeInMillis());

            if (day == mSelectedDay) {
                return getContext().getString(R.string.item_is_selected, date);
            }

            return date;
        }
    }

}
