package com.sven.sjcalendar.hotspots.almanac;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.LayoutHelper;
import com.alibaba.android.vlayout.layout.LinearLayoutHelper;
import com.sven.sjcalendar.R;

public class AlmanacAdapter extends DelegateAdapter.Adapter<AlmanacAdapter.AlmanacHolder> {

    @Override
    public LayoutHelper onCreateLayoutHelper() {
        return new LinearLayoutHelper();
    }

    @Override
    public AlmanacHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return AlmanacHolder.create(parent.getContext(), parent);
    }

    @Override
    public void onBindViewHolder(AlmanacHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 1;
    }

    static class AlmanacHolder extends RecyclerView.ViewHolder {

        static AlmanacHolder create(Context context, ViewGroup parent) {
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.hotspots_item_almanac, parent, false);
            return new AlmanacHolder(view);
        }

        AlmanacHolder(View itemView) {
            super(itemView);
        }
    }
}
