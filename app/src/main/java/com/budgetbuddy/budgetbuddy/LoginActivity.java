package com.budgetbuddy.budgetbuddy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;

import com.budgetbuddy.budgetbuddy.model.SessionStore;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    static final String PREFS_NAME = "BudgetBuddyUsers";

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnCreateAccount;
    private CheckBox cbStayLoggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Auto-login if a session is remembered
        SessionStore session = new SessionStore(this);
        if (session.isRemembered()) {
            String rememberedEmail = session.getRememberedEmail();
            // Confirm the account still exists before redirecting
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            if (prefs.contains(rememberedEmail)) {
                routeAfterLogin(rememberedEmail);
                return;
            } else {
                // Stale session, clean it up
                session.clear();
            }
        }

        setContentView(R.layout.activity_login);

        tilEmail         = findViewById(R.id.tilEmail);
        tilPassword      = findViewById(R.id.tilPassword);
        etEmail          = findViewById(R.id.etEmail);
        etPassword       = findViewById(R.id.etPassword);
        btnLogin         = findViewById(R.id.btnLogin);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        cbStayLoggedIn   = findViewById(R.id.cbStayLoggedIn);

        btnLogin.setOnClickListener(v -> handleLogin());
        btnCreateAccount.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }

    private void handleLogin() {
        clearErrors();
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (!validateEmail(email) | !validateNotEmpty(tilPassword, password, "Password is required"))
            return;

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String stored = prefs.getString(email, null);

        if (stored == null) {
            tilEmail.setError("No account found with this email");
        } else if (!stored.equals(password)) {
            tilPassword.setError("Incorrect password");
        } else {
            // Honour "Stay logged in"
            SessionStore session = new SessionStore(this);
            if (cbStayLoggedIn.isChecked()) {
                session.rememberAccount(email);
            } else {
                session.clear();
            }
            routeAfterLogin(email);
        }
    }

    private void routeAfterLogin(String email) {
        Class<?> destination = OnboardingActivity.isCompleted(this, email)
                ? MainActivity.class
                : OnboardingActivity.class;
        Intent intent = new Intent(LoginActivity.this, destination);
        intent.putExtra("email", email);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private boolean validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email is required");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter a valid email address");
            return false;
        }
        return true;
    }

    private boolean validateNotEmpty(TextInputLayout til, String value, String message) {
        if (TextUtils.isEmpty(value)) {
            til.setError(message);
            return false;
        }
        return true;
    }

    private void clearErrors() {
        tilEmail.setError(null);
        tilPassword.setError(null);
    }
}