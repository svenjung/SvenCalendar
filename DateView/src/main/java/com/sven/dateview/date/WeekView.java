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
 * A calendar-like view displaying a specified week and the appropriate selectable day numbers
 * within the specified week.
 */
public abstract class WeekView extends View {
    private static final String TAG = "WeekView";

    /**
     * These params can be passed into the view to control how it appears.
     * {@link #VIEW_PARAMS_WEEK} is the only required field, though the default
     * values are unlikely to fit most layouts correctly.
     */
    /**
     * This sets the height of this week in pixels
     */
    public static final String VIEW_PARAMS_HEIGHT = "height";
    public static final String VIEW_PARAMS_YEAR = "year";

    public static final String VIEW_PARAMS_WEEK_OF_YEAR = "week_of_year";

    public static final String VIEW_PARAMS_WEEK_SINCE_EPOCH = "week_since_epoch";
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
    protected static final int DEFAULT_WEEK_START = Calendar.MONDAY;
    protected static final int DEFAULT_NUM_DAYS = 7;
    protected static final int DEFAULT_SHOW_WK_NUM = 0;
    protected static final int DEFAULT_FOCUS_MONTH = -1;
    protected static final int DEFAULT_NUM_ROWS = 1;
    protected static final int MAX_NUM_ROWS = 1;

    protected static final int EPOCH_DAY_OF_WEEK = Calendar.THURSDAY;

    private static final int SELECTED_CIRCLE_ALPHA = 60;

    protected static int DAY_SEPARATOR_WIDTH = 1;
    protected static int MINI_DAY_NUMBER_TEXT_SIZE;
    protected static int MONTH_LABEL_TEXT_SIZE;
    protected static int MONTH_DAY_LABEL_TEXT_SIZE;
    protected static int MONTH_HEADER_SIZE;
    protected static int DAY_SELECTED_CIRCLE_SIZE;

    // used for scaling to the device density
    protected static float mScale = 0;

    protected DatePickerController mController;

    // affects the padding on the sides of this view
    protected int mEdgePadding = 0;

    protected Paint mWeekNumPaint;
    protected Paint mSelectedCirclePaint;

    // The Julian day of the first day displayed by this item
    protected int mFirstJulianDay = -1;
    // The month of the first day in this week
    protected int mFirstMonth = -1;
    // The month of the last day in this week
    protected int mLastMonth = -1;

    protected int mMonth;
    protected int mYear;

    protected int mWeeksOfYear;
    protected int mWeeksSinceEpoch;

    // Quick reference to the width of this view, matches parent
    protected int mWidth;
    // The height this view should draw at in pixels, set by height param
    protected int mRowHeight = DEFAULT_HEIGHT;
    // If this view contains the today
    protected boolean mHasToday = false;
    // Which day is selected, julian day
    protected int mSelectedDay = -1;
    // Which day is today [1-31] or -1 if no day is today
    protected int mToday = DEFAULT_SELECTED_DAY;
    // Which day of the week to start on [1-7]
    protected int mWeekStart = DEFAULT_WEEK_START;
    // How many days to display
    protected int mNumDays = DEFAULT_NUM_DAYS;
    // The number of days + a spot for week number if it is displayed
    protected int mNumCells = mNumDays;
    // The left edge of the selected day
    protected int mSelectedLeft = -1;
    // The right edge of the selected day
    protected int mSelectedRight = -1;

    private final TimeCalendar mCalendar;
    protected final Calendar mDayLabelCalendar;
    private final WeekViewTouchHelper mTouchHelper;

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

    protected int mPressedDay = -1;
    private int mLastDay = -1;

    private boolean mHasPerformedLongClick = false;
    private CheckForLongPress mPendingCheckForLongPress;
    private CheckForTap mPendingCheckForTap = null;

    public WeekView(Context context) {
        this(context, null);
    }

    public WeekView(Context context, AttributeSet attr) {
        super(context, attr);
        Resources res = context.getResources();

        mDayLabelCalendar = Calendar.getInstance();
        mCalendar = TimeCalendar.getInstance();

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

        mRowHeight = res.getDimensionPixelOffset(R.dimen.date_picker_view_animator_height) / MAX_NUM_ROWS;
        // Set up accessibility components.
        mTouchHelper = getWeekViewTouchHelper();
        ViewCompat.setAccessibilityDelegate(this, mTouchHelper);
        ViewCompat.setImportantForAccessibility(this, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
        mLockAccessibilityDelegate = true;

        // Sets up any standard paints that will be used
        initView();
    }

    public void setDatePickerController(DatePickerController controller) {
        mController = controller;
    }

    protected WeekViewTouchHelper getWeekViewTouchHelper() {
        return new WeekViewTouchHelper(this);
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
        if (mTouchHelper.dispatchHoverEvent(event)) {
            return true;
        }
        return super.dispatchHoverEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int julianDay = getDayFromLocation(event.getX(), event.getY());

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastDay = julianDay;
                mHasPerformedLongClick = false;
                if (julianDay > 0 && !isOutOfRange(julianDay)) {
                    if (mPendingCheckForTap == null) {
                        mPendingCheckForTap = new CheckForTap();
                    }
                    mPendingCheckForTap.day = julianDay;
                    postDelayed(mPendingCheckForTap, 25);
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
                if (julianDay != mLastDay) {
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

    private void performDayClick(int julianDay) {
        // If the min / max date are set, only process the click if it's a valid selection.
        if (isOutOfRange(julianDay)) {
            return;
        }

        if (mOnDayClickListener != null) {
            mCalendar.setJulianDay(julianDay);
            mOnDayClickListener.onDayClick(WeekView.this, mCalendar.getYear(),
                    mCalendar.getMonth(), mCalendar.getDay());
        }

        setSelectedDay(julianDay, false);

        playSoundEffect(SoundEffectConstants.CLICK);

        // This is a no-op if accessibility is turned off.
        mTouchHelper.sendEventForVirtualView(julianDay, AccessibilityEvent.TYPE_VIEW_CLICKED);
    }

    private void performDayLongClick(int julianDay) {
        // If the min / max date are set, only process the click if it's a valid selection.
        if (isOutOfRange(julianDay)) {
            return;
        }

        if (mOnDayLongClickListener != null) {
            mCalendar.setJulianDay(julianDay);
            mOnDayLongClickListener.onDayLongClick(WeekView.this, mCalendar.getYear(),
                    mCalendar.getMonth(), mCalendar.getDay());
        }

        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

        // This is a no-op if accessibility is turned off.
        mTouchHelper.sendEventForVirtualView(julianDay, AccessibilityEvent.TYPE_VIEW_LONG_CLICKED);
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

        mWeekNumPaint = new Paint();
        mWeekNumPaint.setAntiAlias(true);
        mWeekNumPaint.setTextSize(MINI_DAY_NUMBER_TEXT_SIZE);
        mWeekNumPaint.setStyle(Style.FILL);
        mWeekNumPaint.setTextAlign(Align.CENTER);
        mWeekNumPaint.setFakeBoldText(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawWeekNums(canvas);
    }

    protected int mDayOfWeekStart = 0;

    /**
     * Sets all the parameters for displaying this week. The only required
     * parameter is the week number. Other parameters have a default value and
     * will only update if a new value is included, except for focus month,
     * which will always default to no focus month if no value is passed in. See
     * {@link #VIEW_PARAMS_HEIGHT} for more info on parameters.
     *
     * @param params A map of the new parameters, see
     *            {@link #VIEW_PARAMS_HEIGHT}
     */
    public void setWeekParams(HashMap<String, Integer> params) {
        if (!params.containsKey(VIEW_PARAMS_WEEK_SINCE_EPOCH)) {
            throw new InvalidParameterException("You must specify weeks since epoch for this view");
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

        mWeeksSinceEpoch = params.get(VIEW_PARAMS_WEEK_SINCE_EPOCH);

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

        calculateFields();
        mNumCells = DEFAULT_NUM_DAYS;
        TimeCalendar time = TimeCalendar.getInstance();
        for (int i = 0; i < mNumCells; i++) {
            final int julianDay = mFirstJulianDay + i;
            time.setJulianDay(julianDay);
            if (today.sameDay(time)) {
                mHasToday = true;
                mToday = today.getJulianDay();
            }
        }
        mNumRows = 1;

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

    public void reuse() {
        mNumRows = DEFAULT_NUM_ROWS;
        requestLayout();
    }

    private void calculateFields() {
        mFirstJulianDay = TimeCalendar.EPOCH_JULIAN_DAY -
                findDayOffset(EPOCH_DAY_OF_WEEK) + mWeeksSinceEpoch * 7;
        TimeCalendar time = new TimeCalendar();
        time.setJulianDay(mFirstJulianDay);

        mYear = time.getYear();
        mMonth = time.getMonth();
        mWeeksOfYear = time.getWeekOfYear();

        // 选中日期不在当周时, 设置第一天被选中
        if (mSelectedDay < mFirstJulianDay || mSelectedDay > mFirstJulianDay + 6) {
            mSelectedDay = mFirstJulianDay;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mRowHeight);
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

    public int getWeeksOfYear() {
        return 0;
    }

    /**
     * Draws the week and month day numbers for this week. Override this method
     * if you need different placement.
     *
     * @param canvas The canvas to draw on
     */
    protected void drawWeekNums(Canvas canvas) {
        int y = (((mRowHeight + MINI_DAY_NUMBER_TEXT_SIZE) / 2) - DAY_SEPARATOR_WIDTH);
        final float dayWidthHalf = (mWidth - mEdgePadding * 2) / (mNumDays * 2.0f);
        TimeCalendar calendar = TimeCalendar.getInstance();
        int julianDay;
        for (int i = 0; i < mNumCells; i++) {
            julianDay = mFirstJulianDay + i;

            final int x = (int)((2 * i + 1) * dayWidthHalf + mEdgePadding);
            int yRelativeToDay = (mRowHeight + MINI_DAY_NUMBER_TEXT_SIZE) / 2 - DAY_SEPARATOR_WIDTH;

            final int startX = (int)(x - dayWidthHalf);
            final int stopX = (int)(x + dayWidthHalf);
            final int startY = (int)(y - yRelativeToDay);
            final int stopY = (int)(startY + mRowHeight);
            calendar.setJulianDay(julianDay);
            drawWeekDay(canvas, calendar.getYear(), calendar.getMonth(), calendar.getDay(),
                    x, y, startX, stopX, startY, stopY);
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
    public abstract void drawWeekDay(Canvas canvas, int year, int month, int day,
                                     int x, int y, int startX, int stopX, int startY, int stopY);

    protected int findDayOffset() {
        return (mDayOfWeekStart < mWeekStart ? (mDayOfWeekStart + mNumDays) : mDayOfWeekStart)
                - mWeekStart;
    }

    private int findDayOffset(int dayOfWeek) {
        return (dayOfWeek < mWeekStart ? (dayOfWeek + mNumDays) : dayOfWeek) - mWeekStart;
    }

    /**
     * Calculates the julian day that the given x position is in, accounting for week
     * number. Returns the day or -1 if the position wasn't in a day.
     *
     * @param x The x position of the touch event
     * @return The julian day, or -1 if the position wasn't in a day
     */
    public int getDayFromLocation(float x, float y) {
        final int day = getInternalDayFromLocation(x, y);
        if (day < mFirstJulianDay || day >= mNumCells + mFirstJulianDay) {
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
        int column = (int) ((x - dayStart) * mNumDays / (mWidth - dayStart - mEdgePadding));

        return column + mFirstJulianDay;
    }

    protected boolean isOutOfRange(int julianDay) {
        return julianDay < mController.getMinDate().getJulianDay() ||
                julianDay > mController.getMaxDate().getJulianDay();
    }

    /**
     * @return The date that has accessibility focus, or {@code null} if no date
     *         has focus
     */
    public Calendar getAccessibilityFocus() {
        final int day = mTouchHelper.getFocusedVirtualView();
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
    protected class WeekViewTouchHelper extends ExploreByTouchHelper {
        private static final String DATE_FORMAT = "dd MMMM yyyy";

        private final Rect mTempRect = new Rect();
        private final TimeCalendar mTempCalendar = TimeCalendar.getInstance();

        public WeekViewTouchHelper(View host) {
            super(host);
        }

        public void setFocusedVirtualView(int virtualViewId) {
            getAccessibilityNodeProvider(WeekView.this).performAction(
                    virtualViewId, AccessibilityNodeInfoCompat.ACTION_ACCESSIBILITY_FOCUS, null);
        }

        public void clearFocusedVirtualView() {
            final int focusedVirtualView = getFocusedVirtualView();
            if (focusedVirtualView != ExploreByTouchHelper.INVALID_ID) {
                getAccessibilityNodeProvider(WeekView.this).performAction(
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
            for (int day = mFirstJulianDay; day < mNumCells + mFirstJulianDay; day++) {
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
            final int cellHeight = mRowHeight;
            final int cellWidth = ((mWidth - (2 * mEdgePadding)) / mNumDays);
            final int index = day - mFirstJulianDay;
            final int column = (index % mNumDays);
            final int x = (offsetX + (column * cellWidth));

            rect.set(x, 0, (x + cellWidth), cellHeight);
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
            mTempCalendar.setJulianDay(day);
            final CharSequence date = DateFormat.format(DATE_FORMAT,
                    mTempCalendar.getTimeInMillis());

            if (day == mSelectedDay) {
                return getContext().getString(R.string.item_is_selected, date);
            }

            return date;
        }
    }

    public int getWeekHeight() {
        return mRowHeight;
    }

}
