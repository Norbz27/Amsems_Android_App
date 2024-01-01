package com.example.amsems.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.amsems.R;

import java.util.ArrayList;

public class UpCommingEventAdapter extends RecyclerView.Adapter<UpCommingEventAdapter.MyViewHolder> {
    private Context context;
    private ArrayList _id, _eventname, _date, _image;
    public UpCommingEventAdapter(Context context, ArrayList _id, ArrayList _eventname, ArrayList _date, ArrayList _image){
        this.context = context;
        this._id = _id;
        this._eventname = _eventname;
        this._date = _date;
        this._image = _image;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.custom_layout_up_comming, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.tvEventName.setText(String.valueOf(_eventname.get(position)));
        holder.tvEventDate.setText(String.valueOf(_date.get(position)));
        Glide.with(context)
                .load(_image.get(position))
                .into(holder.ivEvent);
    }
    @Override
    public int getItemCount() {
        return _eventname.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName, tvEventDate;
        ImageView ivEvent;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName3);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            ivEvent = itemView.findViewById(R.id.ivEventImg);
        }
    }
}