package com.sven.sjcalendar.hotspots;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.LayoutHelper;
import com.alibaba.android.vlayout.layout.LinearLayoutHelper;
import com.sven.sjcalendar.R;

/**
 * Created by Sven.J on 18-5-11.
 */
public class NopAdapter extends DelegateAdapter.Adapter<NopAdapter.NopViewHolder> {

    @Override
    public LayoutHelper onCreateLayoutHelper() {
        return new LinearLayoutHelper();
    }

    @Override
    public NopViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return NopViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(NopViewHolder holder, int position) {
        // no-op
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public static class NopViewHolder extends RecyclerView.ViewHolder {

        public static NopViewHolder create(ViewGroup parent) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.hotspots_item_nop, parent, false);
            return new NopViewHolder(view);
        }

        public NopViewHolder(View itemView) {
            super(itemView);
        }
    }
}
