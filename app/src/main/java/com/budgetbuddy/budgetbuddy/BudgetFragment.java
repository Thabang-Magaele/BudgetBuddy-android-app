package com.budgetbuddy.budgetbuddy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.budgetbuddy.budgetbuddy.adapter.BudgetAdapter;
import com.budgetbuddy.budgetbuddy.model.BudgetStore;
import com.budgetbuddy.budgetbuddy.model.Transaction;
import com.budgetbuddy.budgetbuddy.model.TransactionStore;

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

    // Header views
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
        txStore     = new TransactionStore(requireContext(), email);
        budgetStore = new BudgetStore(requireContext(), email);
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

        refresh();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    // -------------------------------------------------------------------------
    private void refresh() {
        // Spending per category from transactions
        Map<String, Double> spentMap = txStore.expensesByCategory();
        Map<String, Double> limitMap = budgetStore.getAllLimits();

        List<BudgetAdapter.BudgetRow> rows = new ArrayList<>();
        double totalLimit = 0, totalSpent = 0;
        int alertCount = 0;

        for (String category : BudgetStore.BUDGETABLE_CATEGORIES) {
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

        // Header summary
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
}