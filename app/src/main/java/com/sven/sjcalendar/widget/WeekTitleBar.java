package com.sven.sjcalendar.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sven.sjcalendar.R;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Sven.J on 18-4-19.
 * 考虑直接用View实现
 */
public class WeekTitleBar extends ViewGroup {
    private static final int DEFAULT_DAYS_IN_WEEK = 7;

    private static final int[] DAY_OF_WEEKS = {
            Calendar.SUNDAY,
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY,
            Calendar.SATURDAY
    };

    private int textSize;
    private int textColor;

    private int mWeekStartDay = Calendar.MONDAY;
    private int mFirstDayOffset = 1;

    public WeekTitleBar(Context context) {
        this(context, null);
    }

    public WeekTitleBar(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public WeekTitleBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WeekTitleBar);
        textSize = a.getDimensionPixelSize(R.styleable.WeekTitleBar_weekTextSize, 12);
        textColor = a.getColor(R.styleable.WeekTitleBar_weekTextColor, Color.BLACK);
        a.recycle();

        addView();
    }

    private void addView() {
        for (int i = 1; i <= DEFAULT_DAYS_IN_WEEK; i++) {
            TextView weekTextView = new TextView(getContext());
            weekTextView.setTextSize(textSize);
            weekTextView.setTextColor(textColor);
            weekTextView.setGravity(Gravity.CENTER);
            addView(weekTextView);
        }
    }

    /**
     * 设置周首日
     * @param weekStartDay see at {@link Calendar}
     */
    public void setFirstDayOfWeek(int weekStartDay) {
        if (weekStartDay < Calendar.SUNDAY || weekStartDay > Calendar.SATURDAY) {
            throw new IllegalArgumentException("Invalid weekStartDay : " + weekStartDay);
        }

        if (weekStartDay != mWeekStartDay) {
            mWeekStartDay = weekStartDay;
            mFirstDayOffset = getOffset(mWeekStartDay);

            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int specWidthSize = MeasureSpec.getSize(widthMeasureSpec);
        int specHeightSize = MeasureSpec.getSize(heightMeasureSpec);
        int specHeightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (specHeightMode == MeasureSpec.AT_MOST) {
            specHeightSize = specWidthSize / DEFAULT_DAYS_IN_WEEK;
        }

        setMeasuredDimension(specWidthSize, specHeightSize);

        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                    specWidthSize / DEFAULT_DAYS_IN_WEEK, MeasureSpec.EXACTLY);
            int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                    specHeightSize, MeasureSpec.EXACTLY);
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childLeft = 0;
        int width, height;
        for (int i = 0; i < getChildCount(); i++) {
            TextView child = (TextView) getChildAt(i);
            child.setText(getDayOfWeekStringForIndex(i));
            width = child.getMeasuredWidth();
            height = child.getMeasuredHeight();
            child.layout(childLeft, 0, childLeft + width, height);
            childLeft += width;
        }
    }

    private String getDayOfWeekStringForIndex(int position) {
        int dayOfWeek = DAY_OF_WEEKS[(position + mFirstDayOffset) % DEFAULT_DAYS_IN_WEEK];
        return getDayOfWeekString(dayOfWeek);
    }

    private String getDayOfWeekString(int dayOfWeek) {
        Locale locale = Locale.getDefault();
        DateFormatSymbols symbols = DateFormatSymbols.getInstance(locale);
        String[] strings = symbols.getShortWeekdays();
        if (strings != null && dayOfWeek < strings.length) {
            return strings[dayOfWeek];
        }

        return null;
    }

    private int getOffset(int dayOfWeek) {
        for (int i = 0; i < DAY_OF_WEEKS.length; i++) {
            if (DAY_OF_WEEKS[i] == dayOfWeek) {
                return i;
            }
        }

        return 0;
    }
}
