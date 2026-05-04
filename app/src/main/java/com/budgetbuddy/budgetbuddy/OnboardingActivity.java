package com.budgetbuddy.budgetbuddy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class OnboardingActivity extends AppCompatActivity {

    private static final String PREFS_ONBOARDING = "BudgetBuddyOnboarding";
    private static final int TOTAL_STEPS = 3;

    private int currentStep = 1;
    private String userEmail;

    // Step indicators
    private View stepDot1, stepDot2, stepDot3;
    private TextView tvStepLabel;

    // Step containers
    private LinearLayout layoutStep1, layoutStep2, layoutStep3;

    // Step 1 — Income
    private TextInputLayout tilIncome;
    private TextInputEditText etIncome;

    // Step 2 — Expenses
    private TextInputLayout tilExpenses;
    private TextInputEditText etExpenses;

    // Step 3 — Goals
    private CheckBox cbSavings, cbInvestments, cbEmergencyFund, cbDebtRepayment, cbRetirement;
    private TextInputLayout tilCustomGoal;
    private TextInputEditText etCustomGoal;

    // Navigation
    private MaterialButton btnNext, btnBack;
    private TextView tvSkip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        userEmail = getIntent().getStringExtra("email");

        bindViews();
        updateStepUI();

        btnNext.setOnClickListener(v -> handleNext());
        btnBack.setOnClickListener(v -> handleBack());
        tvSkip.setOnClickListener(v -> finishOnboarding());
    }

    // -------------------------------------------------------------------------
    // Bind
    // -------------------------------------------------------------------------
    private void bindViews() {
        stepDot1   = findViewById(R.id.stepDot1);
        stepDot2   = findViewById(R.id.stepDot2);
        stepDot3   = findViewById(R.id.stepDot3);
        tvStepLabel = findViewById(R.id.tvStepLabel);

        layoutStep1 = findViewById(R.id.layoutStep1);
        layoutStep2 = findViewById(R.id.layoutStep2);
        layoutStep3 = findViewById(R.id.layoutStep3);

        tilIncome  = findViewById(R.id.tilIncome);
        etIncome   = findViewById(R.id.etIncome);

        tilExpenses = findViewById(R.id.tilExpenses);
        etExpenses  = findViewById(R.id.etExpenses);

        cbSavings       = findViewById(R.id.cbSavings);
        cbInvestments   = findViewById(R.id.cbInvestments);
        cbEmergencyFund = findViewById(R.id.cbEmergencyFund);
        cbDebtRepayment = findViewById(R.id.cbDebtRepayment);
        cbRetirement    = findViewById(R.id.cbRetirement);
        tilCustomGoal   = findViewById(R.id.tilCustomGoal);
        etCustomGoal    = findViewById(R.id.etCustomGoal);

        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
        tvSkip  = findViewById(R.id.tvSkip);
    }

    // -------------------------------------------------------------------------
    // Navigation
    // -------------------------------------------------------------------------
    private void handleNext() {
        if (!validateCurrentStep()) return;

        if (currentStep < TOTAL_STEPS) {
            currentStep++;
            updateStepUI();
        } else {
            saveOnboardingData();
            finishOnboarding();
        }
    }

    private void handleBack() {
        if (currentStep > 1) {
            currentStep--;
            updateStepUI();
        }
    }

    private void updateStepUI() {
        // Animate step containers
        layoutStep1.setVisibility(currentStep == 1 ? View.VISIBLE : View.GONE);
        layoutStep2.setVisibility(currentStep == 2 ? View.VISIBLE : View.GONE);
        layoutStep3.setVisibility(currentStep == 3 ? View.VISIBLE : View.GONE);

        // Animate in
        LinearLayout current = currentStep == 1 ? layoutStep1
                : currentStep == 2 ? layoutStep2 : layoutStep3;
        current.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));

        // Step dots
        stepDot1.setBackgroundResource(currentStep >= 1 ? R.drawable.dot_active : R.drawable.dot_inactive);
        stepDot2.setBackgroundResource(currentStep >= 2 ? R.drawable.dot_active : R.drawable.dot_inactive);
        stepDot3.setBackgroundResource(currentStep >= 3 ? R.drawable.dot_active : R.drawable.dot_inactive);

        tvStepLabel.setText("Step " + currentStep + " of " + TOTAL_STEPS);

        // Button labels
        btnNext.setText(currentStep == TOTAL_STEPS ? "Finish" : "Next →");
        btnBack.setVisibility(currentStep == 1 ? View.INVISIBLE : View.VISIBLE);
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------
    private boolean validateCurrentStep() {
        switch (currentStep) {
            case 1:
                tilIncome.setError(null);
                String income = etIncome.getText().toString().trim();
                if (TextUtils.isEmpty(income)) {
                    tilIncome.setError("Please enter your monthly income");
                    return false;
                }
                try { Double.parseDouble(income); }
                catch (NumberFormatException e) {
                    tilIncome.setError("Enter a valid amount");
                    return false;
                }
                return true;

            case 2:
                tilExpenses.setError(null);
                String expenses = etExpenses.getText().toString().trim();
                if (TextUtils.isEmpty(expenses)) {
                    tilExpenses.setError("Please enter your monthly expenses");
                    return false;
                }
                try { Double.parseDouble(expenses); }
                catch (NumberFormatException e) {
                    tilExpenses.setError("Enter a valid amount");
                    return false;
                }
                return true;

            case 3:
                // At least one goal must be selected or a custom goal entered
                boolean anyGoal = cbSavings.isChecked() || cbInvestments.isChecked()
                        || cbEmergencyFund.isChecked() || cbDebtRepayment.isChecked()
                        || cbRetirement.isChecked()
                        || !TextUtils.isEmpty(etCustomGoal.getText().toString().trim());
                if (!anyGoal) {
                    tilCustomGoal.setError("Select at least one goal or enter your own");
                    return false;
                }
                tilCustomGoal.setError(null);
                return true;
        }
        return true;
    }

    // -------------------------------------------------------------------------
    // Persist
    // -------------------------------------------------------------------------
    private void saveOnboardingData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_ONBOARDING, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String key = userEmail + "_";
        editor.putString(key + "income",   etIncome.getText().toString().trim());
        editor.putString(key + "expenses", etExpenses.getText().toString().trim());

        // Build a comma-separated goals string
        StringBuilder goals = new StringBuilder();
        if (cbSavings.isChecked())       appendGoal(goals, "Savings");
        if (cbInvestments.isChecked())   appendGoal(goals, "Investments");
        if (cbEmergencyFund.isChecked()) appendGoal(goals, "Emergency Fund");
        if (cbDebtRepayment.isChecked()) appendGoal(goals, "Debt Repayment");
        if (cbRetirement.isChecked())    appendGoal(goals, "Retirement");
        String custom = etCustomGoal.getText().toString().trim();
        if (!TextUtils.isEmpty(custom))  appendGoal(goals, custom);

        editor.putString(key + "goals", goals.toString());
        editor.putBoolean(key + "completed", true);
        editor.apply();
    }

    private void appendGoal(StringBuilder sb, String goal) {
        if (sb.length() > 0) sb.append(", ");
        sb.append(goal);
    }

    private void finishOnboarding() {
        Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
        intent.putExtra("email", userEmail);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    // -------------------------------------------------------------------------
    // Static helper — call from RegisterActivity to check if needed
    // -------------------------------------------------------------------------
    public static boolean isCompleted(Context ctx, String email) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_ONBOARDING, Context.MODE_PRIVATE);
        return prefs.getBoolean(email + "_completed", false);
    }
}
