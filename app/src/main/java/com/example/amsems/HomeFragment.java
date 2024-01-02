package com.example.amsems;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.constraintlayout.helper.widget.MotionEffect;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amsems.utils.ALoadingDialog;
import com.example.amsems.utils.UpCommingEventAdapter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    ArrayList<String> _name, _date, _id;
    ArrayList<Bitmap> _image;
    RecyclerView recEvents;
    TextView tvTotalBal, tvDep;
    SharedPreferences sharedPreferences;
    ALoadingDialog aLoadingDialog;
    private String rembalformattedValue;
    private String dep;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recEvents = view.findViewById(R.id.recEv);
        tvTotalBal = view.findViewById(R.id.tvTotalBal);
        tvDep = view.findViewById(R.id.tvDep);

        aLoadingDialog = new ALoadingDialog(getActivity());

        _id = new ArrayList<String>();
        _name = new ArrayList<String>();
        _date = new ArrayList<String>();
        _image = new ArrayList<Bitmap>();

        sharedPreferences = getActivity().getSharedPreferences("stud_info", MODE_PRIVATE);

        String studentId = sharedPreferences.getString("studentID", "null");

        aLoadingDialog.show();

        new DisplayUpcomingEventsTask().execute(studentId);

        return view;
    }
    private class DisplayUpcomingEventsTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            String studentId = params[0];
            displayTotalBalance(studentId);
            displayUpcomingEvents();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // Update UI after executing the task
            if (!getActivity().isFinishing()) {
                UpCommingEventAdapter eventAdapter = new UpCommingEventAdapter(getActivity(), _id, _name, _date, _image);
                recEvents.setAdapter(eventAdapter);
                recEvents.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

                tvTotalBal.setText("Php "+rembalformattedValue);
                tvDep.setText(dep);
            }
            aLoadingDialog.cancel();
        }
    }
    public void displayTotalBalance(String studId) {
        try {
            Connection connection = SQL_Connection.connectionClass();

            String query = "SELECT sa.ID, pf.Total_balance_fee, d.Description FROM tbl_total_penalty_fee pf LEFT JOIN tbl_student_accounts sa ON pf.Student_ID = sa.ID LEFT JOIN tbl_Departments d ON sa.Department = d.Department_ID WHERE sa.ID = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, studId);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        // Extract data from the result set
                        double remainingBalance = resultSet.getDouble("Total_balance_fee");
                        dep = resultSet.getString("Description");

                        rembalformattedValue = String.format("%.2f", remainingBalance);
                    }
                }
            }
        } catch (SQLException e) {
            Log.e(MotionEffect.TAG, "SQL Exception: " + e.getMessage());
        }
    }
    public void displayUpcomingEvents() {
        try {
            Connection connection = SQL_Connection.connectionClass();
            String query = "SELECT Event_ID, Event_Name, Start_Date, Image FROM tbl_events WHERE Start_Date >= GETDATE()";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                _id.clear();
                _name.clear();
                _date.clear();
                _image.clear();

                while (resultSet.next()) {
                    String eventID = resultSet.getString("Event_ID");
                    String eventName = resultSet.getString("Event_Name");
                    Date startDate = resultSet.getDate("Start_Date");
                    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd, yyyy");
                    String formattedDate = dateFormat.format(startDate);

                    byte[] eventImageBytes = resultSet.getBytes("Image");

                    if (eventImageBytes != null && eventImageBytes.length > 0) {
                        Bitmap eventImageBitmap = BitmapFactory.decodeByteArray(eventImageBytes, 0, eventImageBytes.length);
                        _image.add(eventImageBitmap);
                    } else {
                        // If the image is null or empty in the database, you can add a placeholder
                        _image.add(BitmapFactory.decodeResource(getResources(), R.drawable.events));
                    }

                    _id.add(eventID);
                    _name.add(eventName);
                    _date.add(formattedDate);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error executing SQL query: " + e.getMessage());
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error establishing SQL connection: " + e.getMessage());
        }
    }
}