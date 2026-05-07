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

import com.budgetbuddy.budgetbuddy.adapter.TransactionAdapter;
import com.budgetbuddy.budgetbuddy.model.Transaction;
import com.budgetbuddy.budgetbuddy.model.TransactionStore;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ActivityFragment extends Fragment {

    private static final String ARG_EMAIL  = "email";
    private static final String ARG_FILTER = "filter";

    /** Filter modes — used by the toggle and by HomeFragment when navigating in. */
    public static final String FILTER_ALL      = "ALL";
    public static final String FILTER_INCOME   = "INCOME";
    public static final String FILTER_EXPENSE  = "EXPENSE";

    private String             email;
    private String             currentFilter = FILTER_ALL;
    private TransactionStore   store;
    private TransactionAdapter adapter;
    private TextView           tvEmpty, tvSubtitle;
    private MaterialButtonToggleGroup toggleFilter;

    public static ActivityFragment newInstance(String email) {
        return newInstance(email, FILTER_ALL);
    }

    public static ActivityFragment newInstance(String email, String filter) {
        ActivityFragment f = new ActivityFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL,  email);
        args.putString(ARG_FILTER, filter);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            email = getArguments().getString(ARG_EMAIL);
            currentFilter = getArguments().getString(ARG_FILTER, FILTER_ALL);
        }
        store = new TransactionStore(requireContext(), email);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_activity, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvEmpty      = view.findViewById(R.id.tvEmpty);
        tvSubtitle   = view.findViewById(R.id.tvSubtitle);
        toggleFilter = view.findViewById(R.id.toggleFilter);

        RecyclerView rv = view.findViewById(R.id.rvTransactions);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new TransactionAdapter(
                this::confirmDelete,
                this::openEditTransaction
        );
        rv.setAdapter(adapter);

        // Sync the toggle to current filter
        applyFilterToToggle();

        toggleFilter.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.btnFilterAll)         currentFilter = FILTER_ALL;
            else if (checkedId == R.id.btnFilterIncome) currentFilter = FILTER_INCOME;
            else                                        currentFilter = FILTER_EXPENSE;
            refreshList();
        });

        FloatingActionButton fab = view.findViewById(R.id.fabAddTransaction);
        fab.setOnClickListener(v -> openAddTransaction());

        refreshList();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshList();
    }

    // -------------------------------------------------------------------------
    private void applyFilterToToggle() {
        int id;
        switch (currentFilter) {
            case FILTER_INCOME:  id = R.id.btnFilterIncome;  break;
            case FILTER_EXPENSE: id = R.id.btnFilterExpense; break;
            default:             id = R.id.btnFilterAll;
        }
        toggleFilter.check(id);
    }

    private void refreshList() {
        List<Transaction> all = store.getAll();
        List<Transaction> filtered = new ArrayList<>();
        for (Transaction t : all) {
            switch (currentFilter) {
                case FILTER_INCOME:
                    if (!t.isExpense()) filtered.add(t);
                    break;
                case FILTER_EXPENSE:
                    if (t.isExpense()) filtered.add(t);
                    break;
                default:
                    filtered.add(t);
            }
        }

        // Subtitle reflects the active view
        switch (currentFilter) {
            case FILTER_INCOME:  tvSubtitle.setText("Showing income only");  break;
            case FILTER_EXPENSE: tvSubtitle.setText("Showing expenses only"); break;
            default:             tvSubtitle.setText("Your transactions, grouped by category");
        }

        if (filtered.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            adapter.submitGrouped(new java.util.LinkedHashMap<>());
            String msg;
            switch (currentFilter) {
                case FILTER_INCOME:  msg = "No income transactions yet.\nTap ＋ to add one."; break;
                case FILTER_EXPENSE: msg = "No expense transactions yet.\nTap ＋ to add one."; break;
                default:             msg = "No transactions yet.\nTap ＋ to add one.";
            }
            tvEmpty.setText(msg);
        } else {
            tvEmpty.setVisibility(View.GONE);
            Map<String, List<Transaction>> grouped =
                    TransactionAdapter.groupByCategory(filtered);
            adapter.submitGrouped(grouped);
        }
    }

    private void openAddTransaction() {
        AddTransactionBottomSheet sheet =
                AddTransactionBottomSheet.newInstance(email);
        sheet.setOnSavedListener(this::refreshList);
        sheet.show(getChildFragmentManager(), "add_transaction");
    }

    private void openEditTransaction(Transaction t) {
        AddTransactionBottomSheet sheet =
                AddTransactionBottomSheet.newInstanceForEdit(email, t);
        sheet.setOnSavedListener(this::refreshList);
        sheet.show(getChildFragmentManager(), "edit_transaction");
    }

    private void confirmDelete(Transaction t) {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete transaction?")
                .setMessage(t.getDescription() + " — R " +
                        String.format("%.2f", t.getAmount()))
                .setPositiveButton("Delete", (d, w) -> {
                    store.delete(t.getId());
                    refreshList();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}