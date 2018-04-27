package com.sven.sjcalendar.behavior;

/**
 * Created by Sven.J on 18-4-19.
 */
public interface CollapsingView {
    /**
     * 获取View展开时的高度
     */
    int getExpandedHeight();

    /**
     * 获取View折叠时的高度
     */
    int getCollapsedHeight();
}
