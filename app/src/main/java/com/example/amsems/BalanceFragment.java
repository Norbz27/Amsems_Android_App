package com.example.amsems;

import static android.content.Context.MODE_PRIVATE;
import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amsems.utils.EventPenaltyAdapter;

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
    TextView tvTotalPenBal, tvTotalPen, tvTotalPay, tvTotalPenBal2, tvTotalPen2, tvTotalPay2;
    RecyclerView resEventsPen;
    ArrayList<String> _id, _name, _date, _ammount;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

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

        sharedPreferences = getActivity().getSharedPreferences("stud_info", MODE_PRIVATE);

        String studentId = sharedPreferences.getString("studentID", "null");

        _id = new ArrayList<>();
        _name = new ArrayList<>();
        _date = new ArrayList<>();
        _ammount = new ArrayList<>();

        displayTotalBalance(studentId);

        displayEventPenalty(studentId);

        return view;
    }
    public void displayEventPenalty(String studId){
        try {
            Connection connection = SQL_Connection.connectionClass();

            String query = "SELECT\n" +
                    "    stud.ID AS id,\n" +
                    "    UPPER(stud.Firstname) AS fname,\n" +
                    "    UPPER(stud.Middlename) AS mname,\n" +
                    "    UPPER(stud.Lastname) AS lname,\n" +
                    "    e.Event_Name, att.Event_ID,\n" +
                    "\tFORMAT(att.Date_Time, 'yyyy-MM-dd') AS Date_Time,\n" +
                    "\tbf.Balance_Fee\n" +
                    "FROM\n" +
                    "    tbl_attendance att\n" +
                    "LEFT JOIN\n" +
                    "    tbl_events e ON att.Event_ID = e.Event_ID\n" +
                    "LEFT JOIN\n" +
                    "    tbl_student_accounts stud ON att.Student_ID = stud.ID\n" +
                    "LEFT JOIN\n" +
                    "\ttbl_balance_fees bf ON stud.ID = bf.Student_ID\n" +
                    "WHERE\n" +
                    "    stud.ID = ?\n" +
                    "ORDER BY\n" +
                    "    stud.Lastname;\n";

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, studId);
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
                        Toast.makeText(getActivity(), studentId, Toast.LENGTH_SHORT).show();
                        EventPenaltyAdapter eventPenaltyAdapter = new EventPenaltyAdapter(getActivity(), _id, _name, _date, _ammount);
                        resEventsPen.setAdapter(eventPenaltyAdapter);
                        resEventsPen.setLayoutManager(new LinearLayoutManager(getActivity()));
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

            String query = "SELECT COALESCE(bf.Student_ID, t.Student_ID) AS Student_ID, " +
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

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, studId);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        // Extract data from the result set
                        String studentId = resultSet.getString("Student_ID");
                        double totalBalanceFee = resultSet.getDouble("Total_Balance_Fee");
                        double totalPaymentAmount = resultSet.getDouble("Total_Payment_Amount");
                        double remainingBalance = resultSet.getDouble("Remaining_Balance");

                        String balfeeformattedValue = String.format("%.2f", totalBalanceFee);
                        String payamformattedValue = String.format("%.2f", totalPaymentAmount);
                        String rembalformattedValue = String.format("%.2f", remainingBalance);

                        tvTotalPenBal.setText("Php "+rembalformattedValue);
                        tvTotalPen.setText("Php "+payamformattedValue);
                        tvTotalPay.setText("Php "+balfeeformattedValue);
                        tvTotalPenBal2.setText("Php "+rembalformattedValue);
                        tvTotalPen2.setText("Php "+payamformattedValue);
                        tvTotalPay2.setText("Php "+balfeeformattedValue);
                    }
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "SQL Exception: " + e.getMessage());
        }
    }
}