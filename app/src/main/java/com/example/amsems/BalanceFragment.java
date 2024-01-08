package com.example.amsems;

import static android.content.Context.MODE_PRIVATE;
import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amsems.utils.ALoadingDialog;
import com.example.amsems.utils.EventPenaltyAdapter;
import com.example.amsems.utils.TransactionHisAdapter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BalanceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BalanceFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    SharedPreferences sharedPreferences;
    TextView tvTotalPenBal, tvTotalPen, tvTotalPay, tvTotalPenBal2, tvTotalPen2, tvTotalPay2, tvDep;
    Spinner cbYear;
    RecyclerView resEventsPen, resTransact;
    ArrayList<String> _id, _name, _date, _ammount, _studID, _payedAmmount, _payedDate;

    ArrayList<String> items;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ALoadingDialog aLoadingDialog;
    String balfeeformattedValue = "0.00";
    String payamformattedValue = "0.00";
    String rembalformattedValue = "0.00";
    String allrembalformattedValue = "0.00";
    public BalanceFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BalanceFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BalanceFragment newInstance(String param1, String param2) {
        BalanceFragment fragment = new BalanceFragment();
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
        View view = inflater.inflate(R.layout.fragment_balance, container, false);
        tvTotalPenBal = view.findViewById(R.id.tvTotalPenBal);
        tvTotalPen = view.findViewById(R.id.tvTotalPen);
        tvTotalPay = view.findViewById(R.id.tvTotalPay);
        tvTotalPenBal2 = view.findViewById(R.id.tvTotalPenBal2);
        tvTotalPen2 = view.findViewById(R.id.tvTotalPen2);
        tvTotalPay2 = view.findViewById(R.id.tvTotalPay2);
        resEventsPen = view.findViewById(R.id.resEventsPen);
        resTransact = view.findViewById(R.id.resTransact);
        cbYear = view.findViewById(R.id.spinYear);
        tvDep = view.findViewById(R.id.tvDep);

        sharedPreferences = getActivity().getSharedPreferences("stud_info", MODE_PRIVATE);

        String studentId = sharedPreferences.getString("studentID", "null");
        String dep = sharedPreferences.getString("dep", "null");

        _id = new ArrayList<>();
        _name = new ArrayList<>();
        _date = new ArrayList<>();
        _ammount = new ArrayList<>();
        items = new ArrayList<>();

        _studID = new ArrayList<>();
        _payedAmmount = new ArrayList<>();
        _payedDate = new ArrayList<>();
        displayDepartment(dep);
        displaySchoolYear();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, items);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = view.findViewById(R.id.spinYear);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                aLoadingDialog = new ALoadingDialog(getActivity());
                aLoadingDialog.show();
                new DisplayUpcomingEventsTask().execute(studentId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });




        return view;
    }
    public void displayDepartment(String dep){
        try {
            Connection connection = SQL_Connection.connectionClass();

            String query = "SELECT Description FROM tbl_Departments WHERE Department_ID = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, dep);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        // Extract data from the result set
                        String des = resultSet.getString("Description");

                        tvDep.setText(des);
                    }
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "SQL Exception: " + e.getMessage());
        }
    }
    private class DisplayUpcomingEventsTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            String studentId = params[0];
            displayTransaction(studentId);
            displayEventPenalty(studentId);
            displayTotalBalance(studentId);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // Update UI after executing the task
            TransactionHisAdapter transactionAdapter = new TransactionHisAdapter(getActivity(), _studID, _payedDate, _payedAmmount);
            resTransact.setAdapter(transactionAdapter);
            resTransact.setLayoutManager(new LinearLayoutManager(getActivity()));

            EventPenaltyAdapter eventPenaltyAdapter = new EventPenaltyAdapter(getActivity(), _id, _name, _date, _ammount);
            resEventsPen.setAdapter(eventPenaltyAdapter);
            resEventsPen.setLayoutManager(new LinearLayoutManager(getActivity()));

            tvTotalPenBal.setText("Php "+allrembalformattedValue);
            tvTotalPen.setText("Php "+balfeeformattedValue);
            tvTotalPay.setText("Php "+payamformattedValue);
            tvTotalPenBal2.setText("Php "+rembalformattedValue);
            tvTotalPen2.setText("Php "+balfeeformattedValue);
            tvTotalPay2.setText("Php "+payamformattedValue);

            aLoadingDialog.cancel();
        }
    }
    public void displaySchoolYear(){
        try {
            Connection connection = SQL_Connection.connectionClass();

            String query = "SELECT (Academic_Year_Start +'-'+ Academic_Year_End) AS schyear FROM tbl_acad ORDER BY Status ASC";

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        // Extract data from the result set
                        String schyear = resultSet.getString("schyear");

                        items.add(schyear);

                    }
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "SQL Exception: " + e.getMessage());
        }
    }
    public void displayTransaction(String studId){
        try {
            Connection connection = SQL_Connection.connectionClass();

            String query = "SELECT Student_ID, Payment_Amount, Date FROM tbl_transaction tr LEFT JOIN tbl_acad ad ON tr.School_Year = ad.Acad_ID WHERE Student_ID = ? AND (Academic_Year_Start +'-'+ Academic_Year_End) = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, studId);
                preparedStatement.setString(2, cbYear.getSelectedItem().toString());
                _studID.clear();
                _payedAmmount.clear();
                _payedDate.clear();
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        // Extract data from the result set
                        String studentId = resultSet.getString("Student_ID");
                        Date date = resultSet.getDate("Date");
                        double totalBalanceFee = resultSet.getDouble("Payment_Amount");
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, yyyy");
                        String balfeeformattedValue = String.format("%.2f", totalBalanceFee);
                        String dateformatted = dateFormat.format(date);

                        _studID.add(studentId);
                        _payedDate.add(dateformatted);
                        _payedAmmount.add("Php "+balfeeformattedValue);
                    }
                }
            }

        } catch (SQLException e) {
            Log.e(TAG, "SQL Exception: " + e.getMessage());
        }
    }
    public void displayEventPenalty(String studId){
        try {
            Connection connection = SQL_Connection.connectionClass();

            String query = "SELECT\n" +
                    "                    stud.ID AS id,\n" +
                    "                    e.Event_Name, att.Event_ID,\n" +
                    "                    FORMAT(att.Date_Time, 'yyyy-MM-dd') AS Date_Time,\n" +
                    "                    bf.Balance_Fee\n" +
                    "                    FROM\n" +
                    "                        tbl_attendance att\n" +
                    "                    LEFT JOIN\n" +
                    "                        tbl_events e ON att.Event_ID = e.Event_ID\n" +
                    "                    LEFT JOIN\n" +
                    "                        tbl_student_accounts stud ON att.Student_ID = stud.ID\n" +
                    "                    LEFT JOIN\n" +
                    "\t\t\t\t\t\ttbl_balance_fees bf ON stud.ID = bf.Student_ID\n" +
                    "\t\t\t\t\tLEFT JOIN\n" +
                    "\t\t\t\t\t\ttbl_acad ac ON bf.School_Year = ac.Acad_ID\n" +
                    "                    WHERE\n" +
                    "                        stud.ID = ?\n" +
                    "\t\t\t\t\t\tAND (Academic_Year_Start +'-'+ Academic_Year_End) = ?\n" +
                    "                    ORDER BY\n" +
                    "                        stud.Lastname";

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, studId);
                preparedStatement.setString(2, cbYear.getSelectedItem().toString());
                _name.clear();
                _id.clear();
                _date.clear();
                _ammount.clear();
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        // Extract data from the result set
                        String studentId = resultSet.getString("id");
                        String eventName = resultSet.getString("Event_Name");
                        Date date = resultSet.getDate("Date_Time");
                        double totalBalanceFee = resultSet.getDouble("Balance_Fee");
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, yyyy");
                        String balfeeformattedValue = String.format("%.2f", totalBalanceFee);
                        String dateformatted = dateFormat.format(date);

                        _id.add(studentId);
                        _name.add(eventName);
                        _date.add(dateformatted);
                        _ammount.add("Php "+balfeeformattedValue);


                    }
                }
            }

        } catch (SQLException e) {
            Log.e(TAG, "SQL Exception: " + e.getMessage());
        }
    }
    public void displayTotalBalance(String studId) {
        try {
            Connection connection = SQL_Connection.connectionClass();

            String query = "SELECT COALESCE(bf.Student_ID, t.Student_ID) AS Student_ID, \n" +
                    "                    COALESCE(SUM(bf.Balance_Fee), 0) AS Total_Balance_Fee, \n" +
                    "                    COALESCE(SUM(t.Payment_Amount), 0) AS Total_Payment_Amount, \n" +
                    "                    CASE WHEN COALESCE(SUM(bf.Balance_Fee), 0) < COALESCE(SUM(t.Payment_Amount), 0) THEN 0 \n" +
                    "                    ELSE COALESCE(SUM(bf.Balance_Fee), 0) - COALESCE(SUM(t.Payment_Amount), 0) END AS Remaining_Balance,\n" +
                    "\t\t\t\t\tsyTran,\n" +
                    "\t\t\t\t\tsyBal\n" +
                    "                    FROM ( \n" +
                    "                        SELECT \n" +
                    "                            Student_ID, \n" +
                    "                            SUM(Balance_Fee) AS Balance_Fee,\n" +
                    "\t\t\t\t\t\t\tSchool_Year syBal\n" +
                    "                        FROM \n" +
                    "                            dbo.tbl_balance_fees \n" +
                    "                        GROUP BY \n" +
                    "                            Student_ID,School_Year\n" +
                    "                    ) bf \n" +
                    "                    FULL JOIN (\n" +
                    "                        SELECT\n" +
                    "                            Student_ID, \n" +
                    "                            SUM(Payment_Amount) AS Payment_Amount,\n" +
                    "\t\t\t\t\t\t\tSchool_Year syTran\n" +
                    "                        FROM \n" +
                    "                            dbo.tbl_transaction \n" +
                    "                        GROUP BY \n" +
                    "                            Student_ID, School_Year\n" +
                    "                    ) t ON bf.Student_ID = t.Student_ID \n" +
                    "                    JOIN dbo.tbl_student_accounts s ON COALESCE(bf.Student_ID, t.Student_ID) = s.ID\n" +
                    "\t\t\t\t\tJOIN tbl_acad ad ON t.syTran = ad.Acad_ID OR bf.syBal = ad.Acad_ID\n" +
                    "                    WHERE \n" +
                    "                        s.Status = 1 \n" +
                    "                        AND s.ID = ?\n" +
                    "\t\t\t\t\t\tAND (Academic_Year_Start +'-'+ Academic_Year_End) = ?\n" +
                    "                    GROUP BY \n" +
                    "                        COALESCE(bf.Student_ID, t.Student_ID), \n" +
                    "                        s.Lastname, \n" +
                    "                        s.Firstname,\n" +
                    "\t\t\t\t\t\tsyTran,\n" +
                    "\t\t\t\t\t\tsyBal\n" +
                    "                    ORDER BY \n" +
                    "                        s.Lastname;";

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, studId);
                preparedStatement.setString(2, cbYear.getSelectedItem().toString());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        // Extract data from the result set
                        String studentId = resultSet.getString("Student_ID");
                        double totalBalanceFee = resultSet.getDouble("Total_Balance_Fee");
                        double totalPaymentAmount = resultSet.getDouble("Total_Payment_Amount");
                        double remainingBalance = resultSet.getDouble("Remaining_Balance");

                        balfeeformattedValue = String.format("%.2f", totalBalanceFee);
                        payamformattedValue = String.format("%.2f", totalPaymentAmount);
                        rembalformattedValue = String.format("%.2f", remainingBalance);


                    }
                }


            }

            query = "SELECT COALESCE(bf.Student_ID, t.Student_ID) AS Student_ID, " +
                    "COALESCE(SUM(bf.Balance_Fee), 0) AS Total_Balance_Fee, " +
                    "COALESCE(SUM(t.Payment_Amount), 0) AS Total_Payment_Amount, " +
                    "CASE WHEN COALESCE(SUM(bf.Balance_Fee), 0) < COALESCE(SUM(t.Payment_Amount), 0) THEN 0 " +
                    "ELSE COALESCE(SUM(bf.Balance_Fee), 0) - COALESCE(SUM(t.Payment_Amount), 0) END AS Remaining_Balance " +
                    "FROM ( " +
                    "    SELECT " +
                    "        Student_ID, " +
                    "        SUM(Balance_Fee) AS Balance_Fee " +
                    "    FROM " +
                    "        dbo.tbl_balance_fees " +
                    "    GROUP BY " +
                    "        Student_ID " +
                    ") bf " +
                    "FULL JOIN ( " +
                    "    SELECT " +
                    "        Student_ID, " +
                    "        SUM(Payment_Amount) AS Payment_Amount " +
                    "    FROM " +
                    "        dbo.tbl_transaction " +
                    "    GROUP BY " +
                    "        Student_ID " +
                    ") t ON bf.Student_ID = t.Student_ID " +
                    "JOIN dbo.tbl_student_accounts s ON COALESCE(bf.Student_ID, t.Student_ID) = s.ID " +
                    "WHERE " +
                    "    s.Status = 1 " +
                    "    AND s.ID = ? " +
                    "GROUP BY " +
                    "    COALESCE(bf.Student_ID, t.Student_ID), " +
                    "    s.Lastname, " +
                    "    s.Firstname " +
                    "ORDER BY " +
                    "    s.Lastname;";

            try (PreparedStatement preparedStatement2 = connection.prepareStatement(query)) {
                preparedStatement2.setString(1, studId);

                try (ResultSet resultSet = preparedStatement2.executeQuery()) {
                    while (resultSet.next()) {
                        // Extract data from the result set
                        String studentId = resultSet.getString("Student_ID");
                        double remainingBalance = resultSet.getDouble("Remaining_Balance");

                        allrembalformattedValue = String.format("%.2f", remainingBalance);
                    }
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "SQL Exception: " + e.getMessage());
        }
    }
}