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

import com.example.amsems.utils.ALoadingDialog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class ActivityInfo_Activity extends AppCompatActivity {
    TextView tvName;
    EditText edDate, edTime, edDes;
    ImageView ivActivity;
    ALoadingDialog aLoadingDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        tvName = findViewById(R.id.tvActivityName);
        edDate = findViewById(R.id.dateEditText);
        edTime = findViewById(R.id.timeEditText);
        ivActivity = findViewById(R.id.ivActivity);
        edDes = findViewById(R.id.edActivityDes);

        aLoadingDialog = new ALoadingDialog(this);

        Toolbar toolbar = findViewById(R.id.toolbarActivityInfo);
        setSupportActionBar(toolbar);

        // Enable the Up button
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Intent intent = getIntent();
        String activityid = intent.getStringExtra("ActivityID");

        aLoadingDialog.show();
        new DisplayAsyncTask().execute(activityid);
    }
    private class DisplayAsyncTask extends AsyncTask<String, Void, Void> {

        byte[] imageData;
        String name, des;
        Date date;
        Time time;
        @Override
        protected Void doInBackground(String... params) {
            String activityID = params[0];

            try {
                Connection connection = SQL_Connection.connectionClass();

                String query = "SELECT Activity_Name, Date, Time, Description, Image FROM tbl_activities WHERE Activity_ID=?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setString(1, activityID);

                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        if (resultSet.next()) {
                            name = resultSet.getString("Activity_Name");
                            date = resultSet.getDate("Date");
                            time = resultSet.getTime("Time");
                            des = resultSet.getString("Description");

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
                    ivActivity.setImageBitmap(bitmap);
                } catch (Exception e) {
                    Log.e(TAG, "Error decoding image: " + e.getMessage());
                    // Handle the case where decoding fails (e.g., set a default image)
                    ivActivity.setImageResource(R.drawable.events);
                }
            } else {
                // Handle the case where imageData is null or empty (set a default image)
                ivActivity.setImageResource(R.drawable.events);
            }

            SimpleDateFormat sdf2 = new SimpleDateFormat("EEEE, MMMM dd, yyyy");
            SimpleDateFormat sdf3 = new SimpleDateFormat("hh:mm a");


            String formattedDate = sdf2.format(date);
            String formattedTime = sdf3.format(time);
            tvName.setText(name);
            edDate.setText(formattedDate);
            edTime.setText(formattedTime);
            edDes.setText(des);

            aLoadingDialog.cancel();
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