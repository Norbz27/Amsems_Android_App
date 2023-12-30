package com.example.amsems;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amsems.utils.DrawableUtils;
import com.example.amsems.utils.EventRecyclerViewInterface;

import java.util.ArrayList;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.MyViewHolder> {
    private final EventRecyclerViewInterface eventRecyclerViewInterface;
    private Context context;
    private ArrayList _id, _eventname, _date, _color;
    public EventsAdapter(Context context, ArrayList _id,ArrayList _eventname, ArrayList _date, ArrayList _color, EventRecyclerViewInterface eventRecyclerViewInterface){
        this.context = context;
        this._id = _id;
        this._eventname = _eventname;
        this._date = _date;
        this._color = _color;
        this.eventRecyclerViewInterface = eventRecyclerViewInterface;
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
        holder.id = String.valueOf(_id.get(position));
    }

    @Override
    public int getItemCount() {
        return _eventname.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName, tvDetail;
        ImageView imageView;
        ConstraintLayout cons;
        CardView cardView;
        String id;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvDetail = itemView.findViewById(R.id.tvDate);
            imageView = itemView.findViewById(R.id.imageView3);
            cons = itemView.findViewById(R.id.layout);
            cardView = itemView.findViewById(R.id.cardviewEvent);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(eventRecyclerViewInterface != null){
                        int position = getAdapterPosition();

                        if(position != RecyclerView.NO_POSITION){
                            eventRecyclerViewInterface.onEventClick(position, id);
                        }
                    }
                }
            });
        }
    }
}