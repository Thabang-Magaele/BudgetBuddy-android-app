package com.budgetbuddy.budgetbuddy;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.budgetbuddy.budgetbuddy.model.Transaction;
import com.budgetbuddy.budgetbuddy.model.TransactionStore;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    private static final String ARG_EMAIL = "email";
    private static final String PREFS_ONBOARDING = "BudgetBuddyOnboarding";

    private String email;

    public static HomeFragment newInstance(String email) {
        HomeFragment f = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL, email);
        f.setArguments(args);
        return f;
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
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadDashboard(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh whenever we return to this fragment
        View view = getView();
        if (view != null) loadDashboard(view);
    }

    // -------------------------------------------------------------------------
    private void loadDashboard(View view) {
        SharedPreferences prefs = requireContext()
                .getSharedPreferences(PREFS_ONBOARDING, Context.MODE_PRIVATE);
        String key = email + "_";

        // The onboarding values are the user's baseline.
        // Transactions added in the Activity tab are *adjustments* on top of it.
        TransactionStore store = new TransactionStore(requireContext(), email);

        double baseIncome   = parseDouble(prefs.getString(key + "income",   "0"));
        double baseExpenses = parseDouble(prefs.getString(key + "expenses", "0"));

        double income   = baseIncome   + store.totalIncome();
        double expenses = baseExpenses + store.totalExpenses();
        double balance  = income - expenses;

        String goals = prefs.getString(key + "goals", "None set");

        // Greeting
        String name = email != null ? email.split("@")[0] : "there";
        ((TextView) view.findViewById(R.id.tvGreeting))
                .setText("Hello, " + capitalise(name) + " 👋");
        ((TextView) view.findViewById(R.id.tvEmail)).setText(email);

        // Cards
        ((TextView) view.findViewById(R.id.tvIncome)).setText(formatZAR(income));
        ((TextView) view.findViewById(R.id.tvExpenses)).setText(formatZAR(expenses));

        TextView tvBalance      = view.findViewById(R.id.tvBalance);
        TextView tvBalanceLabel = view.findViewById(R.id.tvBalanceLabel);
        tvBalance.setText(formatZAR(balance));
        tvBalanceLabel.setText(balance >= 0 ? "Net Savings" : "Over Budget");
        tvBalance.setTextColor(requireContext().getColor(
                balance >= 0 ? R.color.primary : R.color.expense_red));

        ((TextView) view.findViewById(R.id.tvGoals))
                .setText(goals != null && !goals.isEmpty() ? goals : "None set");

        setupPieChart(view, store, expenses);
    }

    private void setupPieChart(View view, TransactionStore store, double fallbackExpenses) {
        PieChart pieChart = view.findViewById(R.id.pieChart);
        Map<String, Double> byCategory = store.expensesByCategory();

        ArrayList<PieEntry> entries = new ArrayList<>();

        if (!byCategory.isEmpty()) {
            // Real data from transactions
            for (Map.Entry<String, Double> e : byCategory.entrySet()) {
                entries.add(new PieEntry(e.getValue().floatValue(), e.getKey()));
            }
        } else if (fallbackExpenses > 0) {
            // Estimated breakdown from onboarding figure
            entries.add(new PieEntry((float)(fallbackExpenses * 0.30), "Food"));
            entries.add(new PieEntry((float)(fallbackExpenses * 0.25), "Housing"));
            entries.add(new PieEntry((float)(fallbackExpenses * 0.20), "Transport"));
            entries.add(new PieEntry((float)(fallbackExpenses * 0.10), "Entertainment"));
            entries.add(new PieEntry((float)(fallbackExpenses * 0.15), "Other"));
        } else {
            entries.add(new PieEntry(1f, "No data yet"));
        }

        int[] colours = {
                Color.parseColor("#2E7D32"),
                Color.parseColor("#66BB6A"),
                Color.parseColor("#A5D6A7"),
                Color.parseColor("#F9A825"),
                Color.parseColor("#EF5350"),
                Color.parseColor("#42A5F5"),
                Color.parseColor("#AB47BC"),
                Color.parseColor("#BDBDBD")
        };

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colours);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(11f);
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(6f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChart));

        pieChart.setData(data);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(52f);
        pieChart.setTransparentCircleRadius(57f);
        pieChart.setHoleColor(Color.parseColor("#F9FAF9"));
        pieChart.setCenterText("Spending\nBreakdown");
        pieChart.setCenterTextSize(13f);
        pieChart.setCenterTextColor(Color.parseColor("#1C1C1C"));
        pieChart.getLegend().setEnabled(true);
        pieChart.getLegend().setTextSize(12f);
        pieChart.getLegend().setTextColor(Color.parseColor("#1C1C1C"));
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setEntryLabelTextSize(10f);
        pieChart.animateY(900);
        pieChart.invalidate();
    }

    private String formatZAR(double amount) {
        return String.format(Locale.getDefault(), "R %.2f", amount);
    }

    private double parseDouble(String s) {
        try { return Double.parseDouble(s); } catch (NumberFormatException e) { return 0; }
    }

    private String capitalise(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}