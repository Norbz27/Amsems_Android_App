package com.example.amsems;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class EditPassActivity extends AppCompatActivity {
    EditText etNewPass, etCurPass, etConPass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_pass);

        String studentId = getIntent().getStringExtra("STUDENT_ID");

        etNewPass = findViewById(R.id.etNewPass);
        etCurPass = findViewById(R.id.etCurrPass);
        etConPass = findViewById(R.id.etConfirmPass);
        Button btnChange = findViewById(R.id.btnChangePass);

        etNewPass.setText("");
        etCurPass.setText("");
        etConPass.setText("");

        Toolbar toolbar = findViewById(R.id.toolbarPass);
        setSupportActionBar(toolbar);

        // Enable the Up button
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateInput(studentId, getCurrPassword(studentId));
            }
        });
    }
    @SuppressLint("SetTextI18n")
    private String getCurrPassword(String id) {
        String password = "";
        try {
            Connection connection = SQL_Connection.connectionClass();

            String query = "SELECT Password FROM tbl_student_accounts WHERE ID=?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, id);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        password = resultSet.getString("Password");
                    }
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "SQL Exception: " + e.getMessage());
        }
        return password;
    }
    @SuppressLint("SetTextI18n")
    private int checkPassword(String id, String pass) {
        int count = 0;
        try {
            Connection connection = SQL_Connection.connectionClass();

            String query = "SELECT Count(*) AS count FROM (\n" +
                    "SELECT ID, Password FROM tbl_student_accounts \n" +
                    "UNION\n" +
                    "SELECT ID, Password FROM tbl_teacher_accounts\n" +
                    "UNION\n" +
                    "SELECT ID, Password FROM tbl_deptHead_accounts\n" +
                    "UNION\n" +
                    "SELECT ID, Password FROM tbl_guidance_accounts\n" +
                    "UNION\n" +
                    "SELECT ID, Password FROM tbl_sao_accounts) AS accounts\n" +
                    "WHERE Password=? AND ID <> ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(2, pass);
                preparedStatement.setString(1, id);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        count = Integer.parseInt(resultSet.getString("count"));
                    }
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "SQL Exception: " + e.getMessage());
        }
        return count;
    }
    public boolean updateStudentPassword(String studentId, String newPassword) {
        try {
            // Establish a connection
            try (Connection connection = SQL_Connection.connectionClass()) {
                // Prepare the SQL statement
                String sql = "UPDATE tbl_student_accounts SET Password = ? WHERE ID = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    // Set parameters
                    preparedStatement.setString(1, newPassword);
                    preparedStatement.setString(2, studentId);

                    // Execute the update
                    int rowsAffected = preparedStatement.executeUpdate();

                    // Check if the update was successful
                    return rowsAffected > 0;
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "SQL Exception: " + e.getMessage());
            return false;
        }
    }
    private void validateInput(String studID, String password) {
        String userInputNewPass = etNewPass.getText().toString().trim();
        String userInputCurPass = etCurPass.getText().toString().trim();
        String userInputConPass = etConPass.getText().toString().trim();

        if(!userInputCurPass.equals(password)){
            etCurPass.setError("Password do not match.");
        }else {
            if (userInputNewPass.isEmpty()) {
                etNewPass.setError("This field cannot be empty.");
            } else if (userInputNewPass.length() < 6) {
                etNewPass.setError("Password must be 6 or longer in length.");
            }else if (!userInputConPass.equals(userInputNewPass)) {
                etConPass.setError("Password do not match.");
            }else if(checkPassword(studID, password) != 0){
                etNewPass.setError("Password is already taken.");
            } else {
                etNewPass.setError(null);
                etCurPass.setError(null);
                etConPass.setError(null);

                if(updateStudentPassword(studID, userInputNewPass)){
                    finish();
                    Toast.makeText(this, "Successfully change password.", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(this, "Failed to change password.", Toast.LENGTH_SHORT).show();
                }
            }
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