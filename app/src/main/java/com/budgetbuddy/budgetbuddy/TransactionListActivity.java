package com.budgetbuddy.budgetbuddy;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.budgetbuddy.budgetbuddy.adapter.TransactionAdapter;
import com.budgetbuddy.budgetbuddy.model.Transaction;
import com.budgetbuddy.budgetbuddy.model.TransactionStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Filtered transaction list shown when the user taps Income or Expenses on the dashboard.
 */
public class TransactionListActivity extends AppCompatActivity {

    public static final String EXTRA_EMAIL = "email";
    public static final String EXTRA_TYPE  = "type";   // INCOME or EXPENSE

    private String              email, type;
    private TransactionStore    store;
    private TransactionAdapter  adapter;
    private TextView            tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_list);

        email = getIntent().getStringExtra(EXTRA_EMAIL);
        type  = getIntent().getStringExtra(EXTRA_TYPE);
        store = new TransactionStore(this, email);

        Toolbar toolbar = findViewById(R.id.toolbar);
        boolean isIncome = Transaction.TYPE_INCOME.equals(type);
        toolbar.setTitle(isIncome ? "Income" : "Expenses");
        toolbar.setNavigationOnClickListener(v -> finish());

        tvEmpty = findViewById(R.id.tvEmpty);
        tvEmpty.setText(isIncome
                ? "No income recorded yet."
                : "No expenses recorded yet.");

        RecyclerView rv = findViewById(R.id.rvTransactions);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TransactionAdapter(
                this::confirmDelete,
                this::openEdit
        );
        rv.setAdapter(adapter);

        refresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        List<Transaction> all = store.getAll();
        List<Transaction> filtered = new ArrayList<>();
        for (Transaction t : all) {
            if (type.equals(t.getType())) filtered.add(t);
        }
        if (filtered.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            adapter.submitGrouped(new java.util.LinkedHashMap<>());
        } else {
            tvEmpty.setVisibility(View.GONE);
            adapter.submitGrouped(TransactionAdapter.groupByCategory(filtered));
        }
    }

    private void confirmDelete(Transaction t) {
        new AlertDialog.Builder(this)
                .setTitle("Delete transaction?")
                .setMessage(t.getDescription() + " — R " +
                        String.format("%.2f", t.getAmount()))
                .setPositiveButton("Delete", (d, w) -> {
                    store.delete(t.getId());
                    refresh();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openEdit(Transaction t) {
        AddTransactionBottomSheet sheet =
                AddTransactionBottomSheet.newInstanceForEdit(email, t);
        sheet.setOnSavedListener(this::refresh);
        sheet.show(getSupportFragmentManager(), "edit_transaction");
    }
}