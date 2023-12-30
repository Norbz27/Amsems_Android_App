package com.example.amsems.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amsems.R;

import java.util.ArrayList;

public class EventPenaltyAdapter extends RecyclerView.Adapter<EventPenaltyAdapter.MyViewHolder> {
    private Context context;
    private ArrayList _id, _eventname, _date, _ammount;
    public EventPenaltyAdapter(Context context, ArrayList _id,ArrayList _eventname, ArrayList _date, ArrayList _ammount){
        this.context = context;
        this._id = _id;
        this._eventname = _eventname;
        this._date = _date;
        this._ammount = _ammount;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.custom_item_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.tvEventName.setText(String.valueOf(_eventname.get(position)));
        holder.tvDetail.setText(String.valueOf(_date.get(position)));
        holder.tvAmmount.setText(String.valueOf(_ammount.get(position)));
    }
    @Override
    public int getItemCount() {
        return _eventname.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName, tvDetail, tvAmmount;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName2);
            tvDetail = itemView.findViewById(R.id.tvDate);
            tvAmmount = itemView.findViewById(R.id.tvAmmount);
        }
    }
}