package com.budgetbuddy.budgetbuddy.model;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores per-category monthly budget limits and the user's custom categories.
 *
 * Three key formats inside the "BudgetBuddyBudgets" SharedPreferences file:
 *   <email>_limit_<category>   → limit (string-encoded double)
 *   <email>_custom_categories  → "Pets,Gifts,Subscriptions"  (comma-separated)
 */
public class BudgetStore {

    private static final String PREFS_NAME = "BudgetBuddyBudgets";

    /** Built-in categories that are always shown. Salary is income-only, excluded. */
    public static final String[] BUILT_IN_CATEGORIES = {
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
    // Limits
    // -------------------------------------------------------------------------
    public double getLimit(String category) {
        String raw = prefs.getString(limitKey(category), null);
        if (raw == null) return 0;
        try { return Double.parseDouble(raw); }
        catch (NumberFormatException e) { return 0; }
    }

    public void setLimit(String category, double amount) {
        if (amount <= 0) {
            prefs.edit().remove(limitKey(category)).apply();
        } else {
            prefs.edit().putString(limitKey(category), String.valueOf(amount)).apply();
        }
    }

    public void clearLimit(String category) {
        prefs.edit().remove(limitKey(category)).apply();
    }

    // -------------------------------------------------------------------------
    // Custom categories — newest are returned first
    // -------------------------------------------------------------------------
    public List<String> getCustomCategories() {
        String raw = prefs.getString(customKey(), "");
        if (raw == null || raw.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(raw.split(",")));
    }

    /** Adds a new custom category at the FRONT of the list (most recent first). */
    public boolean addCustomCategory(String name) {
        if (name == null) return false;
        name = name.trim();
        if (name.isEmpty()) return false;

        // Prevent duplicates (case-insensitive) against built-ins or existing customs
        for (String built : BUILT_IN_CATEGORIES) {
            if (built.equalsIgnoreCase(name)) return false;
        }
        List<String> existing = getCustomCategories();
        for (String c : existing) {
            if (c.equalsIgnoreCase(name)) return false;
        }

        existing.add(0, name); // newest first
        prefs.edit().putString(customKey(), String.join(",", existing)).apply();
        return true;
    }

    public void removeCustomCategory(String name) {
        List<String> existing = getCustomCategories();
        existing.removeIf(c -> c.equalsIgnoreCase(name));
        prefs.edit().putString(customKey(), String.join(",", existing)).apply();
        // Also drop any limit set against it
        clearLimit(name);
    }

    /**
     * Returns every category that should appear in the Budget tab, in display order:
     * custom categories first (newest at top), then built-ins.
     */
    public List<String> getAllCategoriesOrdered() {
        List<String> ordered = new ArrayList<>(getCustomCategories());
        ordered.addAll(Arrays.asList(BUILT_IN_CATEGORIES));
        return ordered;
    }

    /**
     * Returns every category that has a limit currently set, mapped to its limit value.
     * Scans all stored limit keys directly so it works regardless of where the
     * category name was registered (built-in, BudgetStore-custom, or CategoryStore-custom).
     */
    public Map<String, Double> getAllLimits() {
        Map<String, Double> map = new LinkedHashMap<>();
        String prefix = email + "_limit_";
        for (String fullKey : prefs.getAll().keySet()) {
            if (fullKey.startsWith(prefix)) {
                String category = fullKey.substring(prefix.length());
                map.put(category, getLimit(category));
            }
        }
        return map;
    }

    // -------------------------------------------------------------------------
    private String limitKey(String category)  { return email + "_limit_" + category; }
    private String customKey()                { return email + "_custom_categories"; }
}