package com.budgetbuddy.budgetbuddy;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.budgetbuddy.budgetbuddy.model.Transaction;
import com.budgetbuddy.budgetbuddy.model.TransactionStore;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTransactionBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_EMAIL = "email";

    private String email;
    private Calendar selectedDate = Calendar.getInstance();
    private Runnable onSavedListener;

    // Views
    private MaterialButtonToggleGroup toggleType;
    private TextInputLayout           tilDescription, tilAmount, tilCategory, tilDate;
    private TextInputEditText         etDescription, etAmount, etDate;
    private AutoCompleteTextView      acCategory;
    private Button                    btnSave;

    public static AddTransactionBottomSheet newInstance(String email) {
        AddTransactionBottomSheet f = new AddTransactionBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL, email);
        f.setArguments(args);
        return f;
    }

    public void setOnSavedListener(Runnable listener) {
        this.onSavedListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) email = getArguments().getString(ARG_EMAIL);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_add_transaction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toggleType   = view.findViewById(R.id.toggleType);
        tilDescription = view.findViewById(R.id.tilDescription);
        tilAmount      = view.findViewById(R.id.tilAmount);
        tilCategory    = view.findViewById(R.id.tilCategory);
        tilDate        = view.findViewById(R.id.tilDate);
        etDescription  = view.findViewById(R.id.etDescription);
        etAmount       = view.findViewById(R.id.etAmount);
        etDate         = view.findViewById(R.id.etDate);
        acCategory     = view.findViewById(R.id.acCategory);
        btnSave        = view.findViewById(R.id.btnSave);

        setupCategoryDropdown();
        setupDatePicker();

        // Default date = today
        etDate.setText(formatDate(selectedDate));

        btnSave.setOnClickListener(v -> handleSave());
    }

    // -------------------------------------------------------------------------
    private void setupCategoryDropdown() {
        String[] categories = {
                Transaction.CAT_FOOD, Transaction.CAT_TRANSPORT, Transaction.CAT_HOUSING,
                Transaction.CAT_ENTERTAINMENT, Transaction.CAT_SHOPPING, Transaction.CAT_HEALTH,
                Transaction.CAT_EDUCATION, Transaction.CAT_SALARY, Transaction.CAT_OTHER
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                categories
        );
        acCategory.setAdapter(adapter);
    }

    private void setupDatePicker() {
        etDate.setOnClickListener(v -> {
            new DatePickerDialog(
                    requireContext(),
                    (picker, year, month, day) -> {
                        selectedDate.set(year, month, day);
                        etDate.setText(formatDate(selectedDate));
                    },
                    selectedDate.get(Calendar.YEAR),
                    selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH)
            ).show();
        });
        // Also open picker when the end icon is tapped
        tilDate.setEndIconOnClickListener(v -> etDate.performClick());
    }

    // -------------------------------------------------------------------------
    private void handleSave() {
        clearErrors();

        // Type
        int checkedId = toggleType.getCheckedButtonId();
        if (checkedId == View.NO_ID) {
            Toast.makeText(requireContext(),
                    "Please select Income or Expense", Toast.LENGTH_SHORT).show();
            return;
        }
        String type = (checkedId == R.id.btnIncome)
                ? Transaction.TYPE_INCOME
                : Transaction.TYPE_EXPENSE;

        // Description
        String desc = etDescription.getText().toString().trim();
        if (TextUtils.isEmpty(desc)) {
            tilDescription.setError("Description is required");
            return;
        }

        // Amount
        String amtStr = etAmount.getText().toString().trim();
        if (TextUtils.isEmpty(amtStr)) {
            tilAmount.setError("Amount is required");
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(amtStr);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            tilAmount.setError("Enter a valid positive amount");
            return;
        }

        // Category
        String category = acCategory.getText().toString().trim();
        if (TextUtils.isEmpty(category)) {
            tilCategory.setError("Please select a category");
            return;
        }

        // Date
        long date = selectedDate.getTimeInMillis();

        Transaction t = new Transaction(null, type, desc, amount, category, date);
        new TransactionStore(requireContext(), email).add(t);

        if (onSavedListener != null) onSavedListener.run();
        dismiss();
    }

    private void clearErrors() {
        tilDescription.setError(null);
        tilAmount.setError(null);
        tilCategory.setError(null);
    }

    private String formatDate(Calendar cal) {
        return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(cal.getTime());
    }
}