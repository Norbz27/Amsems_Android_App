package com.example.amsems.utils;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amsems.EventsAdapter;
import com.example.amsems.SQL_Connection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class FetchEventDataAsyncTask extends AsyncTask<String, Void, Void> {
    private Context context;
    private RecyclerView recyclerView;
    private ArrayList<String> _name;
    private ArrayList<String> _date;
    private ArrayList<String> _color;

    public FetchEventDataAsyncTask(Context context, RecyclerView recyclerView, ArrayList<String> _name, ArrayList<String> _date, ArrayList<String> _color) {
        this.context = context;
        this.recyclerView = recyclerView;
        this._name = _name;
        this._date = _date;
        this._color = _color;
    }

    @Override
    protected Void doInBackground(String... params) {
        String currDate = params[0];
        try {
            Connection connection = SQL_Connection.connectionClass();
            String query = "SELECT Event_Name, Start_Date, Color FROM tbl_events WHERE Start_Date <= ? AND End_Date >= ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, currDate);
                preparedStatement.setString(2, currDate);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    _name.clear();
                    _date.clear();
                    _color.clear();
                    if (resultSet.next()) {
                        do {
                            String title = resultSet.getString("Event_Name");
                            String color = resultSet.getString("Color");
                            Date date = resultSet.getDate("Start_Date");
                            SimpleDateFormat sdfOutput = new SimpleDateFormat("EEE, MMMM dd, yyyy");
                            String formattedDate = sdfOutput.format(date);
                            _name.add(title);
                            _date.add(formattedDate);
                            _color.add(color);
                        } while (resultSet.next());
                    } else {
                        // No events found
                        publishProgress(); // Trigger onProgressUpdate to show a toast on the UI thread
                    }
                }
            } catch (Exception e) {
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
        EventsAdapter eventAdapter = new EventsAdapter(context, _name, _date, _color);
        recyclerView.setAdapter(eventAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        eventAdapter.notifyDataSetChanged();
    }
}
