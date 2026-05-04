package com.budgetbuddy.budgetbuddy;

import android.app.AlertDialog;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Map;

public class ActivityFragment extends Fragment {

    private static final String ARG_EMAIL = "email";

    private String             email;
    private TransactionStore   store;
    private TransactionAdapter adapter;
    private TextView           tvEmpty;

    public static ActivityFragment newInstance(String email) {
        ActivityFragment f = new ActivityFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL, email);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) email = getArguments().getString(ARG_EMAIL);
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

        tvEmpty = view.findViewById(R.id.tvEmpty);

        RecyclerView rv = view.findViewById(R.id.rvTransactions);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new TransactionAdapter(t -> confirmDelete(t));
        rv.setAdapter(adapter);

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
    private void refreshList() {
        List<Transaction> all = store.getAll();
        if (all.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            Map<String, List<Transaction>> grouped =
                    TransactionAdapter.groupByCategory(all);
            adapter.submitGrouped(grouped);
        }
    }

    private void openAddTransaction() {
        AddTransactionBottomSheet sheet =
                AddTransactionBottomSheet.newInstance(email);
        sheet.setOnSavedListener(this::refreshList);
        sheet.show(getChildFragmentManager(), "add_transaction");
    }

    private void confirmDelete(Transaction t) {
        new AlertDialog.Builder(requireContext())
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