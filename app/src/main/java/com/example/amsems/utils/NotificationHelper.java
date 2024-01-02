package com.example.amsems.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;

public class NotificationHelper {
    public static final String CHANNEL_ID = "my_channel";
    public static final String CHANNEL_NAME = "My Channel";
    public static final String CHANNEL_DESCRIPTION = "This is my notification channel";
    public static final String NOTIFICATION_PREF_KEY = "notification_shown";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static boolean shouldShowNotification(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return !preferences.getBoolean(NOTIFICATION_PREF_KEY, false);
    }

    public static void markNotificationShown(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(NOTIFICATION_PREF_KEY, true);
        editor.apply();
    }
}
