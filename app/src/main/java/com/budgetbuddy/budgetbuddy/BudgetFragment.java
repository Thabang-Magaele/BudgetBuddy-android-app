package com.budgetbuddy.budgetbuddy;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.budgetbuddy.budgetbuddy.adapter.BudgetAdapter;
import com.budgetbuddy.budgetbuddy.model.BudgetStore;
import com.budgetbuddy.budgetbuddy.model.CategoryStore;
import com.budgetbuddy.budgetbuddy.model.TransactionStore;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BudgetFragment extends Fragment {

    private static final String ARG_EMAIL = "email";

    private String           email;
    private BudgetAdapter    adapter;
    private TransactionStore txStore;
    private BudgetStore      budgetStore;
    private CategoryStore    categoryStore;

    private TextView tvTotalLimit, tvTotalSpent, tvAlertCount;

    public static BudgetFragment newInstance(String email) {
        BudgetFragment f = new BudgetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL, email);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) email = getArguments().getString(ARG_EMAIL);
        txStore       = new TransactionStore(requireContext(), email);
        budgetStore   = new BudgetStore(requireContext(), email);
        categoryStore = new CategoryStore(requireContext(), email);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_budget, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTotalLimit = view.findViewById(R.id.tvTotalLimit);
        tvTotalSpent = view.findViewById(R.id.tvTotalSpent);
        tvAlertCount = view.findViewById(R.id.tvAlertCount);

        RecyclerView rv = view.findViewById(R.id.rvBudgets);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new BudgetAdapter(this::openSetBudget);
        rv.setAdapter(adapter);

        MaterialButton btnAddCategory = view.findViewById(R.id.btnAddCategory);
        btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());

        refresh();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    // -------------------------------------------------------------------------
    private void refresh() {
        Map<String, Double> spentMap = txStore.expensesByCategory();
        Map<String, Double> limitMap = budgetStore.getAllLimits();

        // Pull categories from CategoryStore so newly-added customs appear at top
        List<String> categories = categoryStore.getBudgetableCategories();

        List<BudgetAdapter.BudgetRow> rows = new ArrayList<>();
        double totalLimit = 0, totalSpent = 0;
        int alertCount = 0;

        for (String category : categories) {
            double spent = spentMap.getOrDefault(category, 0.0);
            double limit = limitMap.getOrDefault(category, 0.0);

            BudgetAdapter.BudgetRow row =
                    new BudgetAdapter.BudgetRow(category, spent, limit);
            rows.add(row);

            if (limit > 0) totalLimit += limit;
            totalSpent += spent;
            if (row.isOverBudget() || row.isNearLimit()) alertCount++;
        }

        adapter.submit(rows);

        tvTotalLimit.setText(String.format(Locale.getDefault(), "R %.2f", totalLimit));
        tvTotalSpent.setText(String.format(Locale.getDefault(), "R %.2f", totalSpent));

        if (alertCount == 0) {
            tvAlertCount.setText("All good ✓");
        } else if (alertCount == 1) {
            tvAlertCount.setText("⚠ 1 category needs attention");
        } else {
            tvAlertCount.setText("⚠ " + alertCount + " categories need attention");
        }
    }

    private void openSetBudget(BudgetAdapter.BudgetRow row) {
        SetBudgetBottomSheet sheet = SetBudgetBottomSheet.newInstance(
                email, row.category, row.spent, row.limit);
        sheet.setOnSavedListener(this::refresh);
        sheet.show(getChildFragmentManager(), "set_budget");
    }

    private void showAddCategoryDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_category, null);
        EditText etName = dialogView.findViewById(R.id.etCategoryName);

        new AlertDialog.Builder(requireContext())
                .setTitle("New Category")
                .setView(dialogView)
                .setPositiveButton("Add", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    if (TextUtils.isEmpty(name)) {
                        Toast.makeText(requireContext(),
                                "Please enter a category name",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    boolean added = categoryStore.addCustomCategory(name);
                    if (!added) {
                        Toast.makeText(requireContext(),
                                "That category already exists",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        refresh();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}