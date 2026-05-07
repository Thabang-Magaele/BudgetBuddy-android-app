package com.budgetbuddy.budgetbuddy;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.budgetbuddy.budgetbuddy.model.CategoryStore;
import com.budgetbuddy.budgetbuddy.model.Transaction;
import com.budgetbuddy.budgetbuddy.model.TransactionStore;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTransactionBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_EMAIL       = "email";
    private static final String ARG_EDIT_ID     = "edit_id";
    private static final String ARG_EDIT_TYPE   = "edit_type";
    private static final String ARG_EDIT_DESC   = "edit_desc";
    private static final String ARG_EDIT_AMOUNT = "edit_amount";
    private static final String ARG_EDIT_CAT    = "edit_cat";
    private static final String ARG_EDIT_DATE   = "edit_date";

    private String   email;
    private Calendar selectedDate = Calendar.getInstance();
    private Runnable onSavedListener;

    // If non-null, we're editing an existing transaction
    private String editId;

    private TextView                  tvSheetTitle;
    private MaterialButtonToggleGroup toggleType;
    private TextInputLayout           tilDescription, tilAmount, tilCategory, tilDate;
    private TextInputEditText         etDescription, etAmount, etDate;
    private AutoCompleteTextView      acCategory;
    private MaterialButton            btnSave, btnDelete;

    /** Use this for adding a brand-new transaction. */
    public static AddTransactionBottomSheet newInstance(String email) {
        AddTransactionBottomSheet f = new AddTransactionBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL, email);
        f.setArguments(args);
        return f;
    }

    /** Use this for editing an existing transaction. */
    public static AddTransactionBottomSheet newInstanceForEdit(String email, Transaction t) {
        AddTransactionBottomSheet f = new AddTransactionBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL,      email);
        args.putString(ARG_EDIT_ID,    t.getId());
        args.putString(ARG_EDIT_TYPE,  t.getType());
        args.putString(ARG_EDIT_DESC,  t.getDescription());
        args.putDouble(ARG_EDIT_AMOUNT,t.getAmount());
        args.putString(ARG_EDIT_CAT,   t.getCategory());
        args.putLong  (ARG_EDIT_DATE,  t.getDate());
        f.setArguments(args);
        return f;
    }

    public void setOnSavedListener(Runnable listener) {
        this.onSavedListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle a = getArguments();
        if (a != null) {
            email  = a.getString(ARG_EMAIL);
            editId = a.getString(ARG_EDIT_ID);   // null if adding
            if (a.containsKey(ARG_EDIT_DATE)) {
                selectedDate.setTimeInMillis(a.getLong(ARG_EDIT_DATE));
            }
        }
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

        tvSheetTitle   = view.findViewById(R.id.tvSheetTitle);
        toggleType     = view.findViewById(R.id.toggleType);
        tilDescription = view.findViewById(R.id.tilDescription);
        tilAmount      = view.findViewById(R.id.tilAmount);
        tilCategory    = view.findViewById(R.id.tilCategory);
        tilDate        = view.findViewById(R.id.tilDate);
        etDescription  = view.findViewById(R.id.etDescription);
        etAmount       = view.findViewById(R.id.etAmount);
        etDate         = view.findViewById(R.id.etDate);
        acCategory     = view.findViewById(R.id.acCategory);
        btnSave        = view.findViewById(R.id.btnSave);
        btnDelete      = view.findViewById(R.id.btnDelete);

        setupCategoryDropdown();
        setupDatePicker();

        // Populate fields if editing
        if (editId != null) {
            populateForEdit();
        } else {
            etDate.setText(formatDate(selectedDate));
        }

        btnSave.setOnClickListener(v -> handleSave());
        btnDelete.setOnClickListener(v -> handleDelete());
    }

    private void populateForEdit() {
        Bundle a = getArguments();
        if (a == null) return;

        tvSheetTitle.setText("Edit Transaction");
        btnSave.setText("Save Changes");
        btnDelete.setVisibility(View.VISIBLE);

        String type = a.getString(ARG_EDIT_TYPE);
        if (Transaction.TYPE_INCOME.equals(type)) {
            toggleType.check(R.id.btnIncome);
        } else {
            toggleType.check(R.id.btnExpense);
        }

        etDescription.setText(a.getString(ARG_EDIT_DESC, ""));
        etAmount.setText(String.format(Locale.getDefault(), "%.2f", a.getDouble(ARG_EDIT_AMOUNT)));
        acCategory.setText(a.getString(ARG_EDIT_CAT, ""), false);
        etDate.setText(formatDate(selectedDate));
    }

    // -------------------------------------------------------------------------
    private void setupCategoryDropdown() {
        // Pull the live list of categories so any custom ones the user has added
        // (from either the Budget tab or earlier) appear in the dropdown.
        java.util.List<String> categories =
                new CategoryStore(requireContext(), email).getAllCategories();

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
        tilDate.setEndIconOnClickListener(v -> etDate.performClick());
    }

    // -------------------------------------------------------------------------
    private void handleSave() {
        clearErrors();

        int checkedId = toggleType.getCheckedButtonId();
        if (checkedId == View.NO_ID) {
            Toast.makeText(requireContext(),
                    "Please select Income or Expense", Toast.LENGTH_SHORT).show();
            return;
        }
        String type = (checkedId == R.id.btnIncome)
                ? Transaction.TYPE_INCOME
                : Transaction.TYPE_EXPENSE;

        String desc = etDescription.getText().toString().trim();
        if (TextUtils.isEmpty(desc)) {
            tilDescription.setError("Description is required");
            return;
        }

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

        String category = acCategory.getText().toString().trim();
        if (TextUtils.isEmpty(category)) {
            tilCategory.setError("Please select a category");
            return;
        }

        long date = selectedDate.getTimeInMillis();
        TransactionStore store = new TransactionStore(requireContext(), email);

        if (editId != null) {
            // Editing an existing transaction
            Transaction updated = new Transaction(editId, type, desc, amount, category, date);
            store.update(updated);
        } else {
            // Adding a new one
            store.add(new Transaction(null, type, desc, amount, category, date));
        }

        if (onSavedListener != null) onSavedListener.run();
        dismiss();
    }

    private void handleDelete() {
        if (editId == null) return;
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete this transaction?")
                .setMessage("This action cannot be undone.")
                .setPositiveButton("Delete", (d, w) -> {
                    new TransactionStore(requireContext(), email).delete(editId);
                    if (onSavedListener != null) onSavedListener.run();
                    dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
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