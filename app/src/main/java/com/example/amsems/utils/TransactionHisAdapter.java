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

public class TransactionHisAdapter extends RecyclerView.Adapter<TransactionHisAdapter.MyViewHolder> {
    private Context context;
    private ArrayList _id, _date, _ammount;
    public TransactionHisAdapter(Context context, ArrayList _id, ArrayList _date, ArrayList _ammount){
        this.context = context;
        this._id = _id;
        this._date = _date;
        this._ammount = _ammount;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.custom_item_layout2, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.tvDetail.setText(String.valueOf(_date.get(position)));
        holder.tvAmmount.setText(String.valueOf(_ammount.get(position)));
    }
    @Override
    public int getItemCount() {
        return _id.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvDetail, tvAmmount;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDetail = itemView.findViewById(R.id.tvDate);
            tvAmmount = itemView.findViewById(R.id.tvAmmount);
        }
    }
}