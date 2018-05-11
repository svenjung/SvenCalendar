package com.sven.sjcalendar.hotspots;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.LayoutHelper;
import com.alibaba.android.vlayout.layout.LinearLayoutHelper;
import com.sven.sjcalendar.R;

import java.util.List;

/**
 * Created by Sven.J on 18-5-9.
 */
public class EventAdapter extends DelegateAdapter.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> mEvents;

    public EventAdapter(List<Event> events) {
        mEvents = events;
    }

    @Override
    public LayoutHelper onCreateLayoutHelper() {
        return new LinearLayoutHelper();
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return EventViewHolder.create(parent.getContext(), parent);
    }

    @Override
    public void onBindViewHolder(EventViewHolder holder, int position) {
        holder.bindView(mEvents.get(position));
    }

    @Override
    public int getItemCount() {
        return mEvents == null ? 0 : mEvents.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {

        public static EventViewHolder create(Context context, ViewGroup container) {
            View view = LayoutInflater.from(context).inflate(R.layout.hotspots_event, container, false);
            return new EventViewHolder(view);
        }

        private TextView title;
        private TextView time;

        public EventViewHolder(View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.title);
            time = itemView.findViewById(R.id.time);
        }

        public void bindView(Event event) {
            title.setText(event.title);
        }
    }
}