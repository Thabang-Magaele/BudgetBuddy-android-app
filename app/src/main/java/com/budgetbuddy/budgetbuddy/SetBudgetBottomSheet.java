package com.budgetbuddy.budgetbuddy;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.budgetbuddy.budgetbuddy.adapter.TransactionAdapter;
import com.budgetbuddy.budgetbuddy.model.BudgetStore;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

public class SetBudgetBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_EMAIL    = "email";
    private static final String ARG_CATEGORY = "category";
    private static final String ARG_SPENT    = "spent";
    private static final String ARG_CURRENT  = "current_limit";

    private String  email;
    private String  category;
    private double  spent;
    private double  currentLimit;
    private Runnable onSavedListener;

    private TextInputLayout    tilLimit;
    private TextInputEditText  etLimit;
    private MaterialButton     btnSave, btnRemove;

    public static SetBudgetBottomSheet newInstance(
            String email, String category, double spent, double currentLimit) {
        SetBudgetBottomSheet f = new SetBudgetBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL,    email);
        args.putString(ARG_CATEGORY, category);
        args.putDouble(ARG_SPENT,    spent);
        args.putDouble(ARG_CURRENT,  currentLimit);
        f.setArguments(args);
        return f;
    }

    public void setOnSavedListener(Runnable l) { this.onSavedListener = l; }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle a = getArguments();
        if (a != null) {
            email        = a.getString(ARG_EMAIL);
            category     = a.getString(ARG_CATEGORY);
            spent        = a.getDouble(ARG_SPENT);
            currentLimit = a.getDouble(ARG_CURRENT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_set_budget, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvEmoji   = view.findViewById(R.id.tvEmoji);
        TextView tvHeader  = view.findViewById(R.id.tvHeader);
        TextView tvSpent   = view.findViewById(R.id.tvSpent);
        tilLimit  = view.findViewById(R.id.tilLimit);
        etLimit   = view.findViewById(R.id.etLimit);
        btnSave   = view.findViewById(R.id.btnSave);
        btnRemove = view.findViewById(R.id.btnRemove);

        tvEmoji.setText(TransactionAdapter.categoryEmoji(category));
        tvHeader.setText(category + " Budget");
        tvSpent.setText(String.format(Locale.getDefault(),
                "Already spent: R %.2f this month", spent));

        if (currentLimit > 0) {
            etLimit.setText(String.format(Locale.getDefault(), "%.2f", currentLimit));
            btnRemove.setVisibility(View.VISIBLE);
        } else {
            btnRemove.setVisibility(View.GONE);
        }

        btnSave.setOnClickListener(v -> handleSave());
        btnRemove.setOnClickListener(v -> {
            new BudgetStore(requireContext(), email).clearLimit(category);
            if (onSavedListener != null) onSavedListener.run();
            dismiss();
        });
    }

    private void handleSave() {
        tilLimit.setError(null);
        String s = etLimit.getText() == null ? "" : etLimit.getText().toString().trim();
        if (TextUtils.isEmpty(s)) {
            tilLimit.setError("Enter a limit amount");
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(s);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            tilLimit.setError("Enter a valid positive amount");
            return;
        }
        new BudgetStore(requireContext(), email).setLimit(category, amount);
        if (onSavedListener != null) onSavedListener.run();
        dismiss();
    }
}