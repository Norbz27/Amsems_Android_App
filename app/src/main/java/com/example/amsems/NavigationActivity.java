package com.example.amsems;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.amsems.utils.NotificationHelper;
import com.example.amsems.utils.ProfileRefreshListener;
import com.google.android.material.navigation.NavigationView;
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

public class NavigationActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, ProfileRefreshListener {
    private ImageButton profilePic, btnNotification;
    private static final int Ann_NOTIFICATION_ID = 1;
    private DrawerLayout drawerLayout;
    SharedPreferences sharedPreferences;
    Intent intent;
    String studentId;
    Channel channel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        sharedPreferences = getSharedPreferences("stud_info", MODE_PRIVATE);


        studentId = sharedPreferences.getString("studentID", "null");

        profilePic = findViewById(R.id.btnProfile);
        btnNotification = findViewById(R.id.btnNotification);

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

        displayProf();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navview);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toogle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toogle);
        toogle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.framelayout, new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction().replace(R.id.framelayout, new ProfileFragment()).commit();
            }
        });
        btnNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NavigationActivity.this, NotificationActivity.class);
                startActivity(intent);
            }
        });


    }
    public void displayProf(){
        byte[] imageData = getProfilePic(studentId);
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);

        if (bitmap != null) {
            // Get the circular background drawable
            Drawable circularDrawable = ContextCompat.getDrawable(this, R.drawable.circle_profile);

            // Combine the circular background with the profile picture
            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
            roundedBitmapDrawable.setCircular(true);

            // Set the combined drawable to the ImageButton
            profilePic.setImageDrawable(roundedBitmapDrawable);
            pusher1();
            pusher2();
            pusher3();
        } else {
            // Handle the case where the Bitmap is null (no image data)
            profilePic.setImageResource(R.mipmap.ic_profile);
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
    public void pusher3(){
        channel.bind(studentId, new SubscriptionEventListener() {
            @Override
            public void onEvent(PusherEvent event) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(NavigationActivity.this, event.getData(), Toast.LENGTH_SHORT).show();
                        try {
                            Connection connection = SQL_Connection.connectionClass();

                            if (connection != null) {
                                String query = "SELECT UPPER(Lastname+', ' + Firstname+' '+Middlename) AS Name FROM tbl_student_accounts WHERE ID = ?";
                                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                                    preparedStatement.setString(1, studentId);
                                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                                        if (resultSet.next()) {
                                            String name = resultSet.getString("Name");
                                            String message = name + " you are called to the guidance office, regarding your absences.";
                                            String datetime = event.getData().replace("\"", "");

                                            notification4(message, "Guidance", datetime);

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
                        try {
                            Connection connection = SQL_Connection.connectionClass();
                            String eventID = null;
                            if (connection != null) {
                                String query = "SELECT TOP 1 Event_ID, Event_Name, Date_Time FROM tbl_events ORDER BY Date_Time DESC";
                                try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                                     ResultSet resultSet = preparedStatement.executeQuery()) {
                                    if (resultSet.next()) {
                                        String title = resultSet.getString("Event_Name");
                                        eventID = resultSet.getString("Event_ID");
                                        notification2(eventID, title);
                                    }
                                }
                                Log.e("Event: ", String.valueOf(isEventForSpecificStudents(eventID)));
                                if(isEventForSpecificStudents(eventID)){
                                    String query2 = "SELECT e.Event_ID, e.Event_Name, s.ID AS id FROM tbl_events e LEFT JOIN tbl_student_accounts s ON CHARINDEX(s.FirstName + ' ' + s.LastName, e.Specific_Students) > 0 OR CHARINDEX(s.LastName + ' ' + s.FirstName, e.Specific_Students) > 0 LEFT JOIN tbl_departments d ON s.Department = d.Department_ID WHERE e.Exclusive = 'Specific Students' AND s.ID = ? AND Event_ID = ? ORDER BY e.Date_Time DESC";
                                    try (PreparedStatement preparedStatement = connection.prepareStatement(query2)) {
                                        preparedStatement.setString(1, studentId);
                                        preparedStatement.setString(2, eventID);

                                        try (ResultSet resultSet = preparedStatement.executeQuery()) {
                                            if (resultSet.next()) {
                                                String title = resultSet.getString("Event_Name");
                                                notification3(title);
                                            }
                                        }
                                    } finally {
                                        connection.close();
                                    }
                                }
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }
    public void pusher1(){
        channel.bind("notification", new SubscriptionEventListener() {
            @Override
            public void onEvent(PusherEvent event) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Connection connection = SQL_Connection.connectionClass();

                            if (connection != null) {
                                String query = "SELECT TOP 1 Announcement_Title, Date_Time FROM tbl_Announcement ORDER BY Date_Time DESC";
                                try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                                     ResultSet resultSet = preparedStatement.executeQuery()) {
                                    if (resultSet.next()) {
                                        String title = resultSet.getString("Announcement_Title");
                                        String datetime = resultSet.getString("Date_Time");
                                        notification(title, "Announcement", datetime);
                                    }
                                } finally {
                                    connection.close();
                                }
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }
    public void notification(String title, String hdTitle, String datetime) {
        Context context = this;

        // Create the notification channel
        NotificationHelper.createNotificationChannel(context);

        // Create an intent to launch when the notification is clicked
        Intent intent = new Intent(context, NotificationDetailActivity.class);
        intent.putExtra("DateTime", datetime);
        intent.putExtra("Header", hdTitle);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Create and display the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
                .setContentTitle("Announcement")
                .setContentText(title)
                .setSmallIcon(R.drawable.baseline_notifications_24)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true); // Automatically removes the notification when clicked

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(Ann_NOTIFICATION_ID, builder.build());
    }
    public void notification2(String eventID,String title) {
        Context context = this;

        // Create the notification channel
        NotificationHelper.createNotificationChannel(context);

        // Create an intent to launch when the notification is clicked
        Intent intent = new Intent(context, EventInfoActivity.class);
        intent.putExtra("EventID", eventID);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Create and display the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
                .setContentTitle("Event")
                .setContentText(title)
                .setSmallIcon(R.drawable.baseline_notifications_24)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true); // Automatically removes the notification when clicked

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(2, builder.build());
    }
    public void notification3(String title) {
        Context context = this;

        // Create the notification channel
        NotificationHelper.createNotificationChannel(context);

        // Create an intent to launch when the notification is clicked
        Intent intent = new Intent(context, NotificationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Create and display the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
                .setContentTitle("Event required your presence")
                .setContentText(title)
                .setSmallIcon(R.drawable.baseline_notifications_24)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true); // Automatically removes the notification when clicked

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(3, builder.build());
    }
    public void notification4(String title, String hdTitle, String datetime) {
        Context context = this;

        // Create the notification channel
        NotificationHelper.createNotificationChannel(context);

        // Create an intent to launch when the notification is clicked
        Intent intent = new Intent(context, NotificationDetailActivity.class);
        intent.putExtra("DateTime", datetime);
        intent.putExtra("Header", hdTitle);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Create and display the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
                .setContentTitle("Guidance")
                .setContentText(title)
                .setSmallIcon(R.drawable.baseline_notifications_24)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle())
                .setAutoCancel(true); // Automatically removes the notification when clicked

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(3, builder.build());
    }
    private byte[] getProfilePic(String id) {
        byte[] defaultProfile = null;

        try {
            Connection connection = SQL_Connection.connectionClass();

            String query = "SELECT Profile_pic FROM tbl_student_accounts WHERE ID=?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, id);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        defaultProfile = resultSet.getBytes("Profile_pic");
                    }
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "SQL Exception: " + e.getMessage());
        }
        return defaultProfile;
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item){
        switch (item.getItemId()){
            case R.id.nav_home:
                getSupportFragmentManager().beginTransaction().replace(R.id.framelayout, new HomeFragment()).commit();
                break;
            case R.id.nav_events:
                getSupportFragmentManager().beginTransaction().replace(R.id.framelayout, new EventsFragment()).commit();
                break;
            case R.id.nav_balance:
                getSupportFragmentManager().beginTransaction().replace(R.id.framelayout, new BalanceFragment()).commit();
                break;
            case R.id.nav_logout:
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.commit();
                editor.apply();
                intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed(){
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }
    }
    @Override
    public void onProfileUpdated(String newProfilePictureUrl) {
        displayProf();
    }
}