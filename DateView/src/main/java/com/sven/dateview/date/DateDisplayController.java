package com.sven.dateview.date;

import java.util.Calendar;

/**
 * Created by Sven.J on 18-4-26.
 */
public interface DateDisplayController {
    Calendar getMinDate();
    Calendar getMaxDate();

    int getMinYear();
    int getMaxYear();
}
