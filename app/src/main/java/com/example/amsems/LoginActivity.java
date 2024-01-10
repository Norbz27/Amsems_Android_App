package com.example.amsems;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.amsems.utils.ALoadingDialog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginActivity extends AppCompatActivity {
    Button btnLogin;
    EditText edStudID, edPass;
    SQL_Connection con;
    SharedPreferences sharedPreferences;
    Intent intent;
    ALoadingDialog aLoadingDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        con = new SQL_Connection();
        btnLogin = findViewById(R.id.btnLogin);
        edStudID = findViewById(R.id.tbSchID);
        edPass = findViewById(R.id.tbPass);

        aLoadingDialog = new ALoadingDialog(this);

        sharedPreferences = getSharedPreferences("stud_info", MODE_PRIVATE);
        intent = new Intent(LoginActivity.this, NavigationActivity.class);
        if(sharedPreferences.contains("studentID") && sharedPreferences.contains("password")){
            startActivity(intent);
            finish();
        }


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle login logic here
                String id = edStudID.getText().toString().trim();
                String password = edPass.getText().toString().trim();
                aLoadingDialog.show();
                // Perform authentication or any other login actions
                if (isValidCredentials(id, password)) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("studentID", id);
                    editor.putString("password", password);
                    editor.apply();
                    startActivity(intent);
                    aLoadingDialog.cancel();
                    finish();
                 } else {
                    // Invalid credentials, show error message or handle accordingly
                    Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                    aLoadingDialog.cancel();
                }
            }
        });
    }
    private boolean isValidCredentials(String id, String password) {
        boolean isValid = false;

        try {
            Connection connection = con.connectionClass();
            // Use a PreparedStatement to avoid SQL injection vulnerabilities
            String query = "SELECT * FROM tbl_student_accounts WHERE ID=? AND Password=? AND Status=1";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, id);
                preparedStatement.setString(2, password);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        isValid = true;
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("dep", resultSet.getString("Department"));
                        editor.apply();
                    }
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "SQL Exception: " + e.getMessage());
        }
        return isValid;
    }
}