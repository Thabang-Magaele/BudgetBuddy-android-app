package com.budgetbuddy.budgetbuddy.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.budgetbuddy.budgetbuddy.R;
import com.budgetbuddy.budgetbuddy.model.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Two view types: TYPE_HEADER (category group label) and TYPE_ITEM (transaction row).
 * Tapping a row opens a popup menu with Edit / Delete actions.
 */
public class TransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM   = 1;

    private final List<Object> flatList = new ArrayList<>();

    public interface OnDeleteListener { void onDelete(Transaction t); }
    public interface OnEditListener   { void onEdit(Transaction t); }

    private final OnDeleteListener deleteListener;
    private final OnEditListener   editListener;

    public TransactionAdapter(OnDeleteListener deleteListener) {
        this(deleteListener, null);
    }

    public TransactionAdapter(OnDeleteListener deleteListener, OnEditListener editListener) {
        this.deleteListener = deleteListener;
        this.editListener   = editListener;
    }

    public void submitGrouped(Map<String, List<Transaction>> grouped) {
        flatList.clear();
        for (Map.Entry<String, List<Transaction>> entry : grouped.entrySet()) {
            flatList.add(entry.getKey());
            flatList.addAll(entry.getValue());
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return flatList.get(position) instanceof String ? TYPE_HEADER : TYPE_ITEM;
    }

    @Override
    public int getItemCount() { return flatList.size(); }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            return new HeaderVH(inf.inflate(R.layout.item_category_header, parent, false));
        } else {
            return new ItemVH(inf.inflate(R.layout.item_transaction, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderVH) {
            ((HeaderVH) holder).bind((String) flatList.get(position));
        } else {
            ((ItemVH) holder).bind((Transaction) flatList.get(position),
                    deleteListener, editListener);
        }
    }

    // -------------------------------------------------------------------------
    static class HeaderVH extends RecyclerView.ViewHolder {
        TextView tvCategory;
        HeaderVH(View v) {
            super(v);
            tvCategory = v.findViewById(R.id.tvCategoryHeader);
        }
        void bind(String category) {
            tvCategory.setText(categoryEmoji(category) + "  " + category);
        }
    }

    static class ItemVH extends RecyclerView.ViewHolder {
        TextView tvDescription, tvAmount, tvDate, tvType;
        ItemVH(View v) {
            super(v);
            tvDescription = v.findViewById(R.id.tvDescription);
            tvAmount      = v.findViewById(R.id.tvAmount);
            tvDate        = v.findViewById(R.id.tvDate);
            tvType        = v.findViewById(R.id.tvType);
        }
        void bind(Transaction t,
                  OnDeleteListener deleteListener,
                  OnEditListener editListener) {

            tvDescription.setText(t.getDescription());
            tvDate.setText(new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    .format(new Date(t.getDate())));

            boolean expense = t.isExpense();
            tvAmount.setText((expense ? "- " : "+ ") +
                    String.format(Locale.getDefault(), "R %.2f", t.getAmount()));
            tvAmount.setTextColor(Color.parseColor(expense ? "#C62828" : "#2E7D32"));

            tvType.setText(expense ? "Expense" : "Income");
            tvType.setTextColor(Color.parseColor(expense ? "#C62828" : "#2E7D32"));

            // Tap → popup menu with Edit / Delete
            itemView.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                popup.getMenu().add(0, 1, 0, "Edit");
                popup.getMenu().add(0, 2, 1, "Delete");
                popup.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == 1 && editListener != null)   editListener.onEdit(t);
                    if (item.getItemId() == 2 && deleteListener != null) deleteListener.onDelete(t);
                    return true;
                });
                popup.show();
            });
        }
    }

    // -------------------------------------------------------------------------
    public static String categoryEmoji(String category) {
        switch (category) {
            case "Food":          return "🍔";
            case "Transport":     return "🚌";
            case "Housing":       return "🏠";
            case "Entertainment": return "🎬";
            case "Shopping":      return "🛍️";
            case "Health":        return "💊";
            case "Education":     return "📚";
            case "Salary":        return "💼";
            case "Other":         return "📦";
            default:              return "💰";  // custom categories
        }
    }

    public static Map<String, List<Transaction>> groupByCategory(List<Transaction> transactions) {
        Map<String, List<Transaction>> map = new LinkedHashMap<>();
        for (Transaction t : transactions) {
            map.computeIfAbsent(t.getCategory(), k -> new ArrayList<>()).add(t);
        }
        return map;
    }
}