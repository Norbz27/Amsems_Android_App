package com.example.amsems;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amsems.utils.NotificationAdapter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;

public class NotificationActivity extends AppCompatActivity {
    ArrayList<String> _headerTitle, _title, _date;
    RecyclerView recNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        recNotifications = findViewById(R.id.recNotifications);

        _headerTitle = new ArrayList<String>();
        _title = new ArrayList<String>();
        _date = new ArrayList<String>();

        _headerTitle.clear();
        _title.clear();
        _date.clear();

        Toolbar toolbar = findViewById(R.id.toolbarNotification);
        setSupportActionBar(toolbar);

        // Enable the Up button
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        displayEventNotifications();
        displayAnnouncementNotifications();

        // Sort the data by date_time
        sortDataByDate();

        NotificationAdapter eventAdapter = new NotificationAdapter(this, _headerTitle, _title, _date);
        recNotifications.setAdapter(eventAdapter);
        recNotifications.setLayoutManager(new LinearLayoutManager(this));
    }

    public void displayEventNotifications() {
        try {
            Connection connection = SQL_Connection.connectionClass();

            if (connection != null) {
                String queryEvent = "SELECT Event_Name, Date_Time FROM tbl_events";
                try (PreparedStatement preparedStatement = connection.prepareStatement(queryEvent);
                     ResultSet resultSet = preparedStatement.executeQuery()) {

                    if (resultSet != null) {
                        while (resultSet.next()) {
                            String title = resultSet.getString("Event_Name");
                            String datetime = resultSet.getString("Date_Time");

                            _headerTitle.add("Event");
                            _title.add(title);
                            _date.add(datetime);
                        }
                    }
                } finally {
                    connection.close();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void displayAnnouncementNotifications() {
        try {
            Connection connection = SQL_Connection.connectionClass();

            if (connection != null) {
                String queryAnnouncement = "SELECT Announcement_Title, Date_Time FROM tbl_Announcement";
                try (PreparedStatement preparedStatement = connection.prepareStatement(queryAnnouncement);
                     ResultSet resultSet = preparedStatement.executeQuery()) {

                    if (resultSet != null) {
                        while (resultSet.next()) {
                            String title = resultSet.getString("Announcement_Title");
                            String datetime = resultSet.getString("Date_Time");

                            _headerTitle.add("Announcement");
                            _title.add(title);
                            _date.add(datetime);
                        }
                    }
                } finally {
                    connection.close();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void sortDataByDate() {
        ArrayList<String> sortedHeaderTitle = new ArrayList<>();
        ArrayList<String> sortedTitle = new ArrayList<>();
        ArrayList<String> sortedDate = new ArrayList<>();

        // Create a list of DateWrapper objects to hold the original index along with the date
        ArrayList<DateWrapper> dateWrappers = new ArrayList<>();
        for (int i = 0; i < _date.size(); i++) {
            dateWrappers.add(new DateWrapper(i, _date.get(i)));
        }

        // Sort the DateWrapper list based on the date
        Collections.sort(dateWrappers, new Comparator<DateWrapper>() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            @Override
            public int compare(DateWrapper wrapper1, DateWrapper wrapper2) {
                try {
                    Date d1 = dateFormat.parse(wrapper1.getDate());
                    Date d2 = dateFormat.parse(wrapper2.getDate());
                    return d1.compareTo(d2);
                } catch (ParseException e) {
                    e.printStackTrace();
                    return 0;
                }
            }
        });

        // Populate the sorted arrays
        for (DateWrapper dateWrapper : dateWrappers) {
            int index = dateWrapper.getIndex();
            sortedHeaderTitle.add(_headerTitle.get(index));
            sortedTitle.add(_title.get(index));
            sortedDate.add(_date.get(index));
        }

        // Update the original ArrayLists
        _headerTitle = sortedHeaderTitle;
        _title = sortedTitle;
        _date = sortedDate;
    }

    // Helper class to hold the original index along with the date
    private static class DateWrapper {
        private final int index;
        private final String date;

        public DateWrapper(int index, String date) {
            this.index = index;
            this.date = date;
        }

        public int getIndex() {
            return index;
        }

        public String getDate() {
            return date;
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
