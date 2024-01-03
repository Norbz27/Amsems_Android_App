package com.example.amsems.utils;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amsems.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.MyViewHolder> {
    private Context context;
    private ArrayList _headerTitle, _title, _date;
    public NotificationAdapter(Context context, ArrayList _headerTitle, ArrayList _title, ArrayList _date){
        this.context = context;
        this._headerTitle = _headerTitle;
        this._title = _title;
        this._date = _date;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.custom_notification_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.tvHtitle.setText(String.valueOf(_headerTitle.get(position)));
        holder.tvTitle.setText(String.valueOf(_title.get(position)));
        holder.imageView.setBackgroundDrawable(DrawableUtils.getCircleDrawableWithText(context, String.valueOf(String.valueOf(_headerTitle.get(position)).charAt(0))));
        String givenDateString = String.valueOf(_date.get(position));
        // Parse the given date string to Date object
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date givenDate = null;
        try {
            givenDate = dateFormat.parse(givenDateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Get the current date and time
        Date currentDate = new Date();

        // Calculate the time span
        long timeDifference = currentDate.getTime() - givenDate.getTime();

        // Display the time span in a human-readable format
        CharSequence timeAgo;

        if (timeDifference < DateUtils.MINUTE_IN_MILLIS) {
            timeAgo = "now";
        } else {
            timeAgo = DateUtils.getRelativeTimeSpanString(
                    givenDate.getTime(),
                    currentDate.getTime(),
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE
            );
        }
        holder.tvDatetime.setText(timeAgo);
    }
    @Override
    public int getItemCount() {
        return _headerTitle.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvHtitle, tvTitle, tvDatetime;
        ImageView imageView;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHtitle = itemView.findViewById(R.id.tvNotify);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDatetime = itemView.findViewById(R.id.tvDate);
            imageView = itemView.findViewById(R.id.imageViewLetter);
        }
    }
}