package com.example.amsems;
import android.annotation.SuppressLint;
import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQL_Connection {
    //local IP
    @SuppressLint("NewApi")
    public static Connection connectionClass() {
        Connection con = null;
        String ip = "192.168.1.100", port = "1433", username = "nor", password = "12345", databaseName = "db_Amsems";

        StrictMode.ThreadPolicy tp = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(tp);

        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            String connectionUrl = "jdbc:jtds:sqlserver://" + ip + ":" + port + ";databasename=" + databaseName + ";user=" + username + ";password=" + password + "";
            con = DriverManager.getConnection(connectionUrl);

        } catch (ClassNotFoundException | SQLException exception) {
            Log.e("Error", exception.getMessage());
        }
        return con;
    }

    //ngrok
    /*public static Connection connectionClass() {
        Connection con = null;
        String ngrokServer = "0.tcp.ap.ngrok.io,12162";
        String databaseName = "db_Amsems";
        String username = "nor";
        String password = "12345";

        StrictMode.ThreadPolicy tp = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(tp);

        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            String connectionUrl = "jdbc:jtds:sqlserver://" + ngrokServer + ";databasename=" + databaseName + ";user=" + username + ";password=" + password + "";
            con = DriverManager.getConnection(connectionUrl);

        } catch (ClassNotFoundException | SQLException exception) {
            Log.e("Error", exception.getMessage());
        }
        return con;
    }*/

    //azure cloud
    /*public static Connection connectionClass() {
        Connection con = null;
        String azureServer = "norbz.database.windows.net";
        String azureDatabase = "db_Amsems";
        String azureUser = "CloudSA4a47677a@norbz";
        String azurePassword = "nozurbnorberto27";

        StrictMode.ThreadPolicy tp = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(tp);

        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            String connectionUrl = "jdbc:jtds:sqlserver://" + azureServer + ":1433;DatabaseName=" + azureDatabase + ";user=" + azureUser + ";password=" + azurePassword + ";encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;";
            con = DriverManager.getConnection(connectionUrl);

        } catch (ClassNotFoundException | SQLException exception) {
            Log.e("Error", exception.getMessage());
        }
        return con;
    }*/
}
