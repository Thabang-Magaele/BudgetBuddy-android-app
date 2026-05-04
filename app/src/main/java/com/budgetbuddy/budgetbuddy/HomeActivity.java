package com.budgetbuddy.budgetbuddy;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Retrieve the logged-in email passed from LoginActivity
        String email = getIntent().getStringExtra("email");
        TextView tvWelcome = findViewById(R.id.tvWelcome);
        if (email != null) {
            tvWelcome.setText("Welcome, " + email);
        }
    }
}