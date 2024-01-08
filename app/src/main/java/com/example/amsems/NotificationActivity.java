package com.example.amsems;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amsems.utils.ALoadingDialog;
import com.example.amsems.utils.NotificationAdapter;
import com.example.amsems.utils.NotificationRecyclerViewInterface;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.PusherEvent;
import com.pusher.client.channel.SubscriptionEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;

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
import java.util.Locale;
import java.util.Objects;

public class NotificationActivity extends AppCompatActivity implements NotificationRecyclerViewInterface {
    ArrayList<String> _headerTitle, _title, _date;
    RecyclerView recNotifications;
    ALoadingDialog aLoadingDialog;
    private String studentId;
    private SharedPreferences sharedPreferences;
    Channel channel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        recNotifications = findViewById(R.id.recNotifications);
        aLoadingDialog = new ALoadingDialog(this);

        sharedPreferences = getSharedPreferences("stud_info", MODE_PRIVATE);
        studentId = sharedPreferences.getString("studentID", "null");

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

        aLoadingDialog.show();
        new DisplayNotificationTask().execute();

        PusherOptions options = new PusherOptions();
        options.setCluster("ap1");

        Pusher pusher = new Pusher("6cc843a774ea227a754f", options);

        pusher.connect(new ConnectionEventListener() {
            @Override
            public void onConnectionStateChange(ConnectionStateChange change) {
                Log.i("Pusher", "State changed from " + change.getPreviousState() +
                        " to " + change.getCurrentState());
            }

            @Override
            public void onError(String message, String code, Exception e) {
                Log.i("Pusher", "There was a problem connecting! " +
                        "\ncode: " + code +
                        "\nmessage: " + message +
                        "\nException: " + e
                );
            }
        }, ConnectionState.ALL);

        channel = pusher.subscribe("amsems");

        pusher1();
        pusher2();
        pusher3();
    }
    public void pusher1(){
        channel.bind("notification", new SubscriptionEventListener() {
            @Override
            public void onEvent(PusherEvent event) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        _headerTitle.clear();
                        _title.clear();
                        _date.clear();
                        new DisplayNotificationTask().execute();
                    }
                });
            }
        });
    }
    public void pusher2(){
        channel.bind("events", new SubscriptionEventListener() {
            @Override
            public void onEvent(PusherEvent event) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        _headerTitle.clear();
                        _title.clear();
                        _date.clear();
                        new DisplayNotificationTask().execute();
                    }
                });
            }
        });
    }
    public void pusher3(){
        channel.bind(studentId, new SubscriptionEventListener() {
            @Override
            public void onEvent(PusherEvent event) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        _headerTitle.clear();
                        _title.clear();
                        _date.clear();
                        new DisplayNotificationTask().execute();
                    }
                });
            }
        });
    }

    @Override
    public void onEventClick(int position, String datetime, String hdTitle) {
        if(hdTitle.equals("Announcement") || hdTitle.equals("Guidance")) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US);
            Date givenDate = null;
            try {
                givenDate = dateFormat.parse(datetime);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            SimpleDateFormat dbdateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Intent intent = new Intent(this, NotificationDetailActivity.class);
            intent.putExtra("DateTime", dbdateFormat.format(givenDate));
            intent.putExtra("Header", hdTitle);
            startActivity(intent);
        }
    }

    private class DisplayNotificationTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            displayEventNotifications();
            displayAnnouncementNotifications();
            displayGuidanceNotifications();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // Update UI after executing the task
            // Sort the data by date_time
            sortDataByDate();

            NotificationAdapter eventAdapter = new NotificationAdapter(NotificationActivity.this, _headerTitle, _title, _date, NotificationActivity.this);
            recNotifications.setAdapter(eventAdapter);
            recNotifications.setLayoutManager(new LinearLayoutManager(NotificationActivity.this));
            aLoadingDialog.cancel();
        }
    }
    public void displayEventNotifications() {
        try {
            Connection connection = SQL_Connection.connectionClass();

            if (connection != null) {
                String queryEvent = "SELECT Event_ID, Event_Name, Date_Time FROM tbl_events";
                try (PreparedStatement preparedStatement = connection.prepareStatement(queryEvent);
                     ResultSet resultSet = preparedStatement.executeQuery()) {

                    if (resultSet != null) {
                        while (resultSet.next()) {
                            String eventID = resultSet.getString("Event_ID");
                            String title = resultSet.getString("Event_Name");
                            String datetime = resultSet.getString("Date_Time");

                            _headerTitle.add("Event");
                            _title.add(title);
                            _date.add(datetime);

                            if(isEventForSpecificStudents(eventID)){
                                String query2 = "SELECT e.Event_ID, e.Event_Name, s.ID AS id, e.Date_Time FROM tbl_events e LEFT JOIN tbl_student_accounts s ON CHARINDEX(s.FirstName + ' ' + s.LastName, e.Specific_Students) > 0 OR CHARINDEX(s.LastName + ' ' + s.FirstName, e.Specific_Students) > 0 LEFT JOIN tbl_departments d ON s.Department = d.Department_ID WHERE e.Exclusive = 'Specific Students' AND s.ID = ? AND e.Event_ID = ? ORDER BY e.Date_Time DESC";
                                try (PreparedStatement preparedStatement2 = connection.prepareStatement(query2)) {
                                    preparedStatement2.setString(1, studentId);
                                    preparedStatement2.setString(2, eventID);

                                    try (ResultSet resultSet2 = preparedStatement2.executeQuery()) {
                                        if (resultSet2.next()) {
                                            String title2 = resultSet2.getString("Event_Name");
                                            String datetime2 = resultSet2.getString("Date_Time");
                                            _headerTitle.add("Event required your presence");
                                            _title.add(title2);
                                            _date.add(datetime2);
                                        }
                                    }
                                }
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
    }
    private boolean isEventForSpecificStudents(String eventId) {
        try (Connection cn = SQL_Connection.connectionClass();
             PreparedStatement stmt = cn.prepareStatement("SELECT Exclusive FROM tbl_events WHERE Event_ID = ? OR ? IS NULL")) {

            cn.setAutoCommit(true); // Optional, depending on your requirements
            stmt.setObject(1, eventId != null ? eventId : null);
            stmt.setObject(2, eventId != null ? eventId : null);

            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    Object result = resultSet.getObject("Exclusive");
                    return result != null && result.toString().equals("Specific Students");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your needs
        }
        return false;
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
    public void displayGuidanceNotifications() {
        try {
            Connection connection = SQL_Connection.connectionClass();

            if (connection != null) {
                String query = "SELECT Message, Date_Time FROM tbl_absenteeism_notified WHERE Student_ID = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setString(1, studentId);

                    try (ResultSet resultSet = preparedStatement.executeQuery()) {

                        if (resultSet != null) {
                            while (resultSet.next()) {
                                String title = resultSet.getString("Message");
                                String datetime = resultSet.getString("Date_Time");

                                _headerTitle.add("Guidance");
                                _title.add(title);
                                _date.add(datetime);
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

        // Sort the DateWrapper list based on the date in descending order
        Collections.sort(dateWrappers, new Comparator<DateWrapper>() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            @Override
            public int compare(DateWrapper wrapper1, DateWrapper wrapper2) {
                try {
                    Date d1 = dateFormat.parse(wrapper1.getDate());
                    Date d2 = dateFormat.parse(wrapper2.getDate());
                    // Change the order to achieve descending sorting
                    return d2.compareTo(d1);
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
