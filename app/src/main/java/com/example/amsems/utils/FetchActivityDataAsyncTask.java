package com.example.amsems.utils;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amsems.ActivityAdapter;
import com.example.amsems.SQL_Connection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class FetchActivityDataAsyncTask extends AsyncTask<String, Void, Void> {
    private Context context;
    private RecyclerView recyclerView;
    private ArrayList<String> _name;
    private ArrayList<String> _date;
    private ArrayList<String> _color;
    private ArrayList<String> _id;
    private final EventRecyclerViewInterface eventRecyclerViewInterface;

    public FetchActivityDataAsyncTask(Context context, RecyclerView recyclerView, ArrayList<String> _id, ArrayList<String> _name, ArrayList<String> _date, ArrayList<String> _color, EventRecyclerViewInterface eventRecyclerViewInterface) {
        this.context = context;
        this._id = _id;
        this.recyclerView = recyclerView;
        this._name = _name;
        this._date = _date;
        this._color = _color;
        this.eventRecyclerViewInterface = eventRecyclerViewInterface;
    }

    @Override
    protected Void doInBackground(String... params) {
        String currDate = params[0];
        try {
            Connection connection = SQL_Connection.connectionClass();
            String query = "SELECT Activity_ID, Activity_Name, Date, Color FROM tbl_activities WHERE Date = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, currDate);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        do {
                            String id = resultSet.getString("Activity_ID");
                            String title = resultSet.getString("Activity_Name");
                            String color = resultSet.getString("Color");
                            Date date = Date.valueOf(currDate);
                            SimpleDateFormat sdfOutput = new SimpleDateFormat("EEE, MMMM dd, yyyy");
                            String formattedDate = sdfOutput.format(date);
                            _name.add(title);
                            _id.add(id);
                            _date.add(formattedDate);
                            _color.add(color);
                        } while (resultSet.next());
                    } else {
                        // No events found
                        publishProgress(); // Trigger onProgressUpdate to show a toast on the UI thread
                    }
                }
            }catch (Exception e) {
                Log.e(TAG, "Error executing SQL query: " + e.getMessage());
            } finally {
                connection.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error establishing SQL connection: " + e.getMessage());
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
        Toast.makeText(context, "No events", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPostExecute(Void result) {
        ActivityAdapter activityAdapter = new ActivityAdapter(context, _id, _name, _date, _color, eventRecyclerViewInterface);
        recyclerView.setAdapter(activityAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        activityAdapter.notifyDataSetChanged();
    }
}
