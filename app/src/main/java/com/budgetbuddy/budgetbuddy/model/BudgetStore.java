package com.budgetbuddy.budgetbuddy.model;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Stores per-category monthly budget limits.
 * Key format: "<email>_<category>" → double (limit in ZAR)
 *
 * Example:
 *   thabang@example.com_Transport → 500.0
 *   thabang@example.com_Food      → 1500.0
 */
public class BudgetStore {

    private static final String PREFS_NAME = "BudgetBuddyBudgets";

    // All categories that can have a budget set against them.
    // Income-only categories (Salary) are intentionally excluded.
    public static final String[] BUDGETABLE_CATEGORIES = {
            Transaction.CAT_FOOD,
            Transaction.CAT_TRANSPORT,
            Transaction.CAT_HOUSING,
            Transaction.CAT_ENTERTAINMENT,
            Transaction.CAT_SHOPPING,
            Transaction.CAT_HEALTH,
            Transaction.CAT_EDUCATION,
            Transaction.CAT_OTHER
    };

    private final SharedPreferences prefs;
    private final String            email;

    public BudgetStore(Context ctx, String email) {
        this.prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.email = email;
    }

    // -------------------------------------------------------------------------
    public double getLimit(String category) {
        // Stored as a String for SharedPreferences-friendliness with decimals.
        String raw = prefs.getString(key(category), null);
        if (raw == null) return 0;
        try { return Double.parseDouble(raw); }
        catch (NumberFormatException e) { return 0; }
    }

    public void setLimit(String category, double amount) {
        if (amount <= 0) {
            prefs.edit().remove(key(category)).apply();
        } else {
            prefs.edit().putString(key(category), String.valueOf(amount)).apply();
        }
    }

    public void clearLimit(String category) {
        prefs.edit().remove(key(category)).apply();
    }

    /** Returns every category along with its currently-set limit (0 = no limit set). */
    public Map<String, Double> getAllLimits() {
        Map<String, Double> map = new LinkedHashMap<>();
        for (String cat : BUDGETABLE_CATEGORIES) {
            map.put(cat, getLimit(cat));
        }
        return map;
    }

    private String key(String category) {
        return email + "_" + category;
    }
}