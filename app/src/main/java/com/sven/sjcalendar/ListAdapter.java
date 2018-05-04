package com.sven.sjcalendar;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sven.dateview.TimeCalendar;

import java.util.List;

import timber.log.Timber;

/**
 * 列表适配器
 * Created by SouthernBox on 2018/1/18.
 */

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.TextHolder> {

    private Context mContext;
    private List<String> mList;

    public int mDay = 0;

    ListAdapter(Context context, List<String> list) {
        mContext = context;
        mList = list;
    }

    @NonNull
    @Override
    public TextHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.item_list, parent, false);
        return new TextHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TextHolder holder, int position) {
        String item = mList.get(position);
        if (mDay != 0) {
            TimeCalendar time = TimeCalendar.getInstance();
            time.setJulianDay(mDay);
            time.allDay = true;
            item = time.format2445() + " ++ " + item;
        }
        final String message = item;
        holder.textView.setText(item);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Timber.i(" onItemClick, message = %s", message);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class TextHolder extends RecyclerView.ViewHolder {

        TextView textView;

        TextHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.tv);
        }
    }
}
