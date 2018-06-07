package com.sven.sjcalendar.hotspots.schedule;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.LayoutHelper;
import com.alibaba.android.vlayout.layout.LinearLayoutHelper;
import com.sven.dateview.TimeCalendar;
import com.sven.sjcalendar.R;
import com.sven.sjcalendar.Utils;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Adapter for scheduler card
 * Created by Sven.J on 18-5-9.
 */
@SuppressWarnings("unchecked")
public class SchedulerAdapter extends DelegateAdapter.Adapter<SchedulerAdapter.EventViewHolder> {

    private List<Event> mEvents;

    public SchedulerAdapter(List<Event> events) {
        mEvents = events;


        final Handler handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                mEvents = (List<Event>) msg.obj;
                Timber.d("handle events changed...");
                notifyDataSetChanged();
                return true;
            }
        });

//        new Thread() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(5000);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//                ArrayList<Event> events = new ArrayList<>(mEvents);
//                TimeCalendar time = TimeCalendar.getInstance();
//
//                for (int i = 0; i < 15; i ++) {
//                    Event event = new Event();
//                    event.title = "异步刷新增加的事件, " + i;
//                    event.description = "异步刷新增加事件";
//                    event.startMillis = time.getTimeInMillis();
//                    event.endMillis = time.getTimeInMillis() + 3600 * 1000;
//                    event.startDay = time.getJulianDay();
//                    event.endDay = time.getJulianDay();
//
//                    events.add(event);
//                }
//
//                Message message = handler.obtainMessage();
//                //events.clear();
//                message.obj = events;
//                message.sendToTarget();
//            }
//        }.start();
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
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        Timber.d("onAttachedToRecyclerView");
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return mEvents == null ? 0 : mEvents.size();
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {

        static EventViewHolder create(Context context, ViewGroup container) {
            View view = LayoutInflater.from(context).inflate(R.layout.hotspots_item_event, container, false);
            return new EventViewHolder(view);
        }

        private TextView title;
        private TextView time;

        EventViewHolder(View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.title);
            time = itemView.findViewById(R.id.time);
        }

        void bindView(Event event) {
            title.setText(event.title);
            int flags = DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME;
            time.setText(Utils.formatDateRange(time.getContext(), event.startMillis, event.endMillis, flags));
        }
    }
}
