package com.example.amsems;

import android.annotation.SuppressLint;
import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQL_Connection {
    @SuppressLint("NewApi")
    public static Connection connectionClass() {
        Connection connection = null;

        StrictMode.ThreadPolicy tp = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(tp);

        try {
            // Database connection logic (JDBC)
            String azureServer = "192.168.1.100";
            String azureDatabase = "db_Amsems";
            String azureUser = "nor";
            String azurePassword = "12345";

            String googleServer = "104.197.95.130";
            String googleDatabase = "db_Amsems";
            String googleUser = "sqlserver";
            String googlePassword = "Nozurbnorberto27";

            String connectionUrl = "jdbc:jtds:sqlserver://"+azureServer+":1433;" +
                    "databaseName="+azureDatabase+";" +
                    "user="+azureUser+";" +
                    "password="+azurePassword+";" +
                    "encrypt=true;" +
                    "trustServerCertificate=false;" +
                    "hostNameInCertificate=*.database.windows.net;" +
                    "loginTimeout=30;";
            // Make sure to handle the connection in a separate thread or AsyncTask
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            connection = DriverManager.getConnection(connectionUrl);
        } catch (SQLException exception) {
            Log.e("Error", exception.getMessage());
            // Handle the exception or provide useful feedback to the user
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return connection;
    }

}