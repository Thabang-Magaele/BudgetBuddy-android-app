package com.budgetbuddy.budgetbuddy.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.budgetbuddy.budgetbuddy.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetVH> {

    public interface OnBudgetClickListener {
        void onBudgetClick(BudgetRow row);
    }

    public static class BudgetRow {
        public final String category;
        public final double spent;
        public final double limit;          // 0 = no limit set

        public BudgetRow(String category, double spent, double limit) {
            this.category = category;
            this.spent    = spent;
            this.limit    = limit;
        }

        public boolean hasLimit()    { return limit > 0; }
        public double  remaining()   { return limit - spent; }
        public boolean isOverBudget(){ return hasLimit() && spent > limit; }
        public boolean isNearLimit() { return hasLimit() && !isOverBudget() && (spent / limit) >= 0.80; }

        public int progressPercent() {
            if (!hasLimit()) return 0;
            return (int) Math.min(100, Math.round((spent / limit) * 100));
        }
    }

    private final List<BudgetRow>         rows = new ArrayList<>();
    private final OnBudgetClickListener   listener;

    public BudgetAdapter(OnBudgetClickListener listener) {
        this.listener = listener;
    }

    public void submit(List<BudgetRow> newRows) {
        rows.clear();
        rows.addAll(newRows);
        notifyDataSetChanged();
    }

    // -------------------------------------------------------------------------
    @NonNull
    @Override
    public BudgetVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_budget, parent, false);
        return new BudgetVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetVH h, int pos) {
        BudgetRow r = rows.get(pos);
        h.bind(r, listener);
    }

    @Override
    public int getItemCount() { return rows.size(); }

    // -------------------------------------------------------------------------
    static class BudgetVH extends RecyclerView.ViewHolder {
        TextView    tvEmoji, tvCategory, tvAmounts, tvStatus;
        ProgressBar progressBar;
        View        statusDot;

        BudgetVH(View v) {
            super(v);
            tvEmoji     = v.findViewById(R.id.tvEmoji);
            tvCategory  = v.findViewById(R.id.tvCategory);
            tvAmounts   = v.findViewById(R.id.tvAmounts);
            tvStatus    = v.findViewById(R.id.tvStatus);
            progressBar = v.findViewById(R.id.progressBar);
            statusDot   = v.findViewById(R.id.statusDot);
        }

        void bind(BudgetRow r, OnBudgetClickListener listener) {
            tvEmoji.setText(TransactionAdapter.categoryEmoji(r.category));
            tvCategory.setText(r.category);

            if (!r.hasLimit()) {
                // No limit set
                tvAmounts.setText(String.format(Locale.getDefault(),
                        "Spent R %.2f  ·  Tap to set a limit", r.spent));
                tvStatus.setText("No limit");
                tvStatus.setTextColor(Color.parseColor("#757575"));
                statusDot.setBackgroundResource(R.drawable.dot_status_grey);
                progressBar.setProgress(0);
                progressBar.setProgressTintList(
                        android.content.res.ColorStateList.valueOf(Color.parseColor("#BDBDBD")));
            } else {
                tvAmounts.setText(String.format(Locale.getDefault(),
                        "R %.2f  /  R %.2f", r.spent, r.limit));
                progressBar.setProgress(r.progressPercent());

                int colour;
                String statusText;
                int dotRes;

                if (r.isOverBudget()) {
                    colour = Color.parseColor("#C62828");      // red
                    double over = r.spent - r.limit;
                    statusText = String.format(Locale.getDefault(),
                            "⚠ Over by R %.2f", over);
                    dotRes = R.drawable.dot_status_red;
                } else if (r.isNearLimit()) {
                    colour = Color.parseColor("#F9A825");      // amber
                    statusText = String.format(Locale.getDefault(),
                            "⚠ R %.2f left", r.remaining());
                    dotRes = R.drawable.dot_status_amber;
                } else {
                    colour = Color.parseColor("#2E7D32");      // green
                    statusText = String.format(Locale.getDefault(),
                            "R %.2f left", r.remaining());
                    dotRes = R.drawable.dot_status_green;
                }

                tvStatus.setText(statusText);
                tvStatus.setTextColor(colour);
                statusDot.setBackgroundResource(dotRes);
                progressBar.setProgressTintList(
                        android.content.res.ColorStateList.valueOf(colour));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onBudgetClick(r);
            });
        }
    }
}