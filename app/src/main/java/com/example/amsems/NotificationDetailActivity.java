package com.example.amsems;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.amsems.utils.ALoadingDialog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class NotificationDetailActivity extends AppCompatActivity {

    TextView tvHeader, tvTitle, tvFrom, tvDatetime, tvMessage;
    private SharedPreferences sharedPreferences;
    private String studentId;
    private ALoadingDialog aLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_detail);

        aLoadingDialog = new ALoadingDialog(this);
        tvHeader = findViewById(R.id.tvHtitle);
        tvTitle = findViewById(R.id.tvTitle);
        tvFrom = findViewById(R.id.tvFrom);
        tvDatetime = findViewById(R.id.tvDatetime);
        tvMessage = findViewById(R.id.tvMessage);

        sharedPreferences = getSharedPreferences("stud_info", MODE_PRIVATE);
        studentId = sharedPreferences.getString("studentID", "null");

        Toolbar toolbar = findViewById(R.id.tbNotifDetail);
        setSupportActionBar(toolbar);

        // Enable the Up button
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Intent intent = getIntent();
        String datetime = intent.getStringExtra("DateTime");
        String header = intent.getStringExtra("Header");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            Date givenDate = dateFormat.parse(datetime);
            SimpleDateFormat dateFormat2 = new SimpleDateFormat("MMM dd, yyyy hh:mm a");
            String datetimeM = dateFormat2.format(givenDate);
            tvHeader.setText(header);
            tvDatetime.setText("@ "+datetimeM);
            aLoadingDialog.show();
            if(header.equals("Announcement")){
                SimpleDateFormat dbdateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                new displayAnnouncementDetail().execute(dbdateFormat.format(givenDate));
            }else if (header.equals("Guidance")){
                SimpleDateFormat dbdateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                new displayGuidanceDetail().execute(dbdateFormat.format(givenDate));
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
    private class displayAnnouncementDetail extends AsyncTask<String, Void, Void> {
        String title, des;
        @Override
        protected Void doInBackground(String... params) {
            String givendatetime = params[0];
            try {
                Connection connection = SQL_Connection.connectionClass();
                //Toast.makeText(NotificationDetailActivity.this, givendatetime, Toast.LENGTH_SHORT).show();
                if (connection != null) {
                    String queryAnnouncement = "SELECT Announcement_Title, Announcement_Description,Date_Time FROM tbl_Announcement WHERE FORMAT(Date_Time, 'yyyy-MM-dd HH:mm:ss') = ?";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(queryAnnouncement)) {
                        preparedStatement.setString(1, givendatetime);

                        try (ResultSet resultSet = preparedStatement.executeQuery()) {
                            if (resultSet != null) {
                                while (resultSet.next()) {
                                    title = resultSet.getString("Announcement_Title");
                                    des = resultSet.getString("Announcement_Description");
                                }
                            }
                        }
                    } finally {
                        connection.close();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            tvFrom.setText("From Student Affair Office");
            tvTitle.setText(title);
            tvMessage.setText(des);
            aLoadingDialog.cancel();
        }
    }
    private class displayGuidanceDetail extends AsyncTask<String, Void, Void> {
        String title, des;
        @Override
        protected Void doInBackground(String... params) {
            String givendatetime = params[0];
            try {
                Connection connection = SQL_Connection.connectionClass();
                //Toast.makeText(NotificationDetailActivity.this, givendatetime, Toast.LENGTH_SHORT).show();
                if (connection != null) {
                    String queryAnnouncement = "SELECT  Student_ID, Message , Date_Time FROM tbl_absenteeism_notified WHERE FORMAT(Date_Time, 'yyyy-MM-dd HH:mm:ss') = ? AND Student_ID = ?";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(queryAnnouncement)) {
                        preparedStatement.setString(1, givendatetime);
                        preparedStatement.setString(2, studentId);

                        try (ResultSet resultSet = preparedStatement.executeQuery()) {
                            if (resultSet != null) {
                                while (resultSet.next()) {
                                    title = "Student Consecutive Absences";
                                    des = resultSet.getString("Message");

                                }
                            }
                        }
                    } finally {
                        connection.close();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            tvFrom.setText("From Guidance Office");
            tvTitle.setText(title);
            tvMessage.setText(des);
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