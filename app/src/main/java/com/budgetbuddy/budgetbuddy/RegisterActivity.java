package com.budgetbuddy.budgetbuddy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword, tilConfirmPassword;
    private TextInputEditText etEmail, etPassword, etConfirmPassword;
    private MaterialButton btnRegister, btnBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        tilEmail           = findViewById(R.id.tilEmail);
        tilPassword        = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        etEmail            = findViewById(R.id.etEmail);
        etPassword         = findViewById(R.id.etPassword);
        etConfirmPassword  = findViewById(R.id.etConfirmPassword);
        btnRegister        = findViewById(R.id.btnRegister);
        btnBackToLogin     = findViewById(R.id.btnBackToLogin);

        btnRegister.setOnClickListener(v -> handleRegister());
        btnBackToLogin.setOnClickListener(v -> finish());
    }

    private void handleRegister() {
        clearErrors();

        String email           = etEmail.getText().toString().trim();
        String password        = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        boolean valid = validateEmail(email)
                & validatePassword(password)
                & validateConfirmPassword(password, confirmPassword);

        if (!valid) return;

        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);

        if (prefs.contains(email)) {
            tilEmail.setError("An account with this email already exists");
            return;
        }

        // Save the new account
        prefs.edit().putString(email, password).apply();

        // Launch onboarding for the new account
        Intent intent = new Intent(RegisterActivity.this, OnboardingActivity.class);
        intent.putExtra("email", email);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private boolean validateEmail(String email) {
        if (TextUtils.isEmpty(email)) { tilEmail.setError("Email is required"); return false; }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { tilEmail.setError("Enter a valid email address"); return false; }
        return true;
    }

    private boolean validatePassword(String password) {
        if (TextUtils.isEmpty(password)) { tilPassword.setError("Password is required"); return false; }
        if (password.length() < 6) { tilPassword.setError("Password must be at least 6 characters"); return false; }
        return true;
    }

    private boolean validateConfirmPassword(String password, String confirm) {
        if (TextUtils.isEmpty(confirm)) { tilConfirmPassword.setError("Please confirm your password"); return false; }
        if (!password.equals(confirm)) { tilConfirmPassword.setError("Passwords do not match"); return false; }
        return true;
    }

    private void clearErrors() {
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
    }
}