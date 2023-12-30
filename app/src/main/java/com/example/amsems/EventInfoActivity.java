package com.example.amsems;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class EventInfoActivity extends AppCompatActivity {

    TextView tvName, tvPenalty, tvAudience, tvAttendance;
    EditText edStart, edEnd, edDes;
    ImageView ivEvent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_info);

        tvName = findViewById(R.id.tvEventName);
        edStart = findViewById(R.id.startdateEditText);
        edEnd = findViewById(R.id.enddateEditText);
        edDes = findViewById(R.id.edEventDes);
        tvPenalty = findViewById(R.id.tvPenalty);
        tvAttendance = findViewById(R.id.tvAttendance);
        tvAudience = findViewById(R.id.tvAudience);
        ivEvent = findViewById(R.id.ivEvent);

        Toolbar toolbar = findViewById(R.id.toolbarEventInfo);
        setSupportActionBar(toolbar);

        // Enable the Up button
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Intent intent = getIntent();
        String eventid = intent.getStringExtra("EventID");

        new DisplayAsyncTask().execute(eventid);

    }
    private class DisplayAsyncTask extends AsyncTask<String, Void, Void> {

        byte[] imageData;
        String name, des, pen, aud, att;
        Date start, end;
        @Override
        protected Void doInBackground(String... params) {
            String eventID = params[0];

            try {
                Connection connection = SQL_Connection.connectionClass();

                String query = "SELECT Event_Name, Start_Date, End_Date, Description, Image, Attendance, Exclusive, Penalty FROM tbl_events WHERE Event_ID=?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setString(1, eventID);

                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        if (resultSet.next()) {
                            name = resultSet.getString("Event_Name");
                            start = resultSet.getDate("Start_Date");
                            end = resultSet.getDate("End_Date");
                            des = resultSet.getString("Description");
                            pen = resultSet.getString("Penalty");
                            aud = resultSet.getString("Exclusive");
                            att = resultSet.getString("Attendance");

                            imageData = resultSet.getBytes("Image");
                        }
                    }
                }
            } catch (SQLException e) {
                Log.e(TAG, "SQL Exception: " + e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (imageData != null && imageData.length > 0) {
                try {
                    // Decode image data into a Bitmap
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                    // Set the combined drawable to the ImageView
                    ivEvent.setImageBitmap(bitmap);
                } catch (Exception e) {
                    Log.e(TAG, "Error decoding image: " + e.getMessage());
                    // Handle the case where decoding fails (e.g., set a default image)
                    ivEvent.setImageResource(R.drawable.events);
                }
            } else {
                // Handle the case where imageData is null or empty (set a default image)
                ivEvent.setImageResource(R.drawable.events);
            }

            SimpleDateFormat sdf2 = new SimpleDateFormat("EEEE, MMMM dd, yyyy");

            String startdate = sdf2.format(start);
            String enddate = sdf2.format(end);
            tvName.setText(name);
            edStart.setText(startdate);
            edEnd.setText(enddate);
            edDes.setText(des);
            tvAudience.setText(aud);

            if(att.equals("1")){
                tvAttendance.setText("Required");
            } else {
                tvAttendance.setText("Not Required");
            }

            if(pen.equals("1")){
                tvPenalty.setText("Yes");
            } else {
                tvPenalty.setText("No");
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Handle the Up button click (e.g., navigate back)
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}