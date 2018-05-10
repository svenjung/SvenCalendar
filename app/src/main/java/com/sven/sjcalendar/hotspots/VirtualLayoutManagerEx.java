package com.sven.sjcalendar.hotspots;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import com.alibaba.android.vlayout.VirtualLayoutManager;

/**
 * Created by Sven.J on 18-5-10.
 */
public class VirtualLayoutManagerEx extends VirtualLayoutManager {
    private HotspotsAdapterManager adapterManager;

    public VirtualLayoutManagerEx(@NonNull Context context) {
        super(context);
    }

    public VirtualLayoutManagerEx(@NonNull Context context, int orientation) {
        super(context, orientation);
    }

    public VirtualLayoutManagerEx(@NonNull Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public void setAdapterManager(HotspotsAdapterManager adapterManager) {
        this.adapterManager = adapterManager;
    }

    @Override
    public void onAttachedToWindow(RecyclerView view) {
        super.onAttachedToWindow(view);

        // start loader
        if (adapterManager != null) {
            adapterManager.startLoadAdapter();
        }
    }

    @Override
    public void onDetachedFromWindow(RecyclerView view, RecyclerView.Recycler recycler) {
        super.onDetachedFromWindow(view, recycler);

        // cancel loader
        if (adapterManager != null) {
            adapterManager.stopLoadAdapter();
        }
    }
}
