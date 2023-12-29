package com.example.amsems;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amsems.utils.DrawableUtils;

import java.util.ArrayList;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.MyViewHolder> {
    private Context context;
    private ArrayList _eventname, _date, _color;

    public EventsAdapter(Context context, ArrayList _eventname, ArrayList _date, ArrayList _color){
        this.context = context;
        this._eventname = _eventname;
        this._date = _date;
        this._color = _color;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.events_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.tvEventName.setText(String.valueOf(_eventname.get(position)));
        holder.tvDetail.setText(String.valueOf(_date.get(position)));
        holder.imageView.setBackgroundDrawable(DrawableUtils.getRoundDrawableWithText(context, String.valueOf(String.valueOf(_eventname.get(position)).charAt(0)), String.valueOf(_color.get(position))));
    }

    @Override
    public int getItemCount() {
        return _eventname.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName, tvDetail;
        ImageView imageView;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvDetail = itemView.findViewById(R.id.tvDate);
            imageView = itemView.findViewById(R.id.imageView3);
        }
    }
}
