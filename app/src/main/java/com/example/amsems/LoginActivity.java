package com.example.amsems;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class LoginActivity extends AppCompatActivity {
    Button btnLogin;
    EditText edStudID, edPass;
    SQL_Connection con;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        con = new SQL_Connection();
        btnLogin = findViewById(R.id.btnLogin);
        edStudID = findViewById(R.id.tbSchID);
        edPass = findViewById(R.id.tbPass);


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle login logic here
                String id = edStudID.getText().toString().trim();
                String password = edPass.getText().toString().trim();

                // Perform authentication or any other login actions
                if (isValidCredentials(id, password)) {
                    // Successful login, navigate to the next screen
                    // Replace with your desired screen navigation logic
                    Intent intent = new Intent(LoginActivity.this, NavigationActivity.class);

                    // Pass Student_ID to NavigationActivity
                    intent.putExtra("STUDENT_ID", id);

                    startActivity(intent);
                    finish();
                //    Toast.makeText(LoginActivity.this, "valid credentials", Toast.LENGTH_SHORT).show();
                 } else {
                    // Invalid credentials, show error message or handle accordingly
                    Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
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
                    }
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "SQL Exception: " + e.getMessage());
        }

        return isValid;
    }
}