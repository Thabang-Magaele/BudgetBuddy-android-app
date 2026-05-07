package com.budgetbuddy.budgetbuddy.model;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Stores user-added custom categories. Built-in categories live as constants on
 * Transaction; custom ones are persisted as a JSON array under
 * "BudgetBuddyCategories" → "<email>_custom".
 *
 * The "all categories" list returns custom categories FIRST (newest at the top),
 * then the built-in set. This matches the requirement to move newly-added
 * categories to the top.
 */
public class CategoryStore {

    private static final String PREFS_NAME = "BudgetBuddyCategories";

    /** Built-in categories that are always available (excluding income-only Salary). */
    public static final String[] BUILT_IN = {
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

    public CategoryStore(Context ctx, String email) {
        this.prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.email = email;
    }

    /** Custom categories the user has added, newest first. */
    public List<String> getCustomCategories() {
        List<String> list = new ArrayList<>();
        String json = prefs.getString(key(), "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                list.add(arr.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Adds a new custom category at the front of the list. No-op if it already exists. */
    public boolean addCustomCategory(String name) {
        if (name == null) return false;
        name = name.trim();
        if (name.isEmpty()) return false;

        // De-dupe against everything (built-in + custom, case-insensitive)
        for (String existing : getAllCategories()) {
            if (existing.equalsIgnoreCase(name)) return false;
        }

        List<String> custom = getCustomCategories();
        custom.add(0, name); // newest first
        save(custom);
        return true;
    }

    public void removeCustomCategory(String name) {
        List<String> custom = getCustomCategories();
        custom.removeIf(c -> c.equalsIgnoreCase(name));
        save(custom);
    }

    /** Custom (newest first) followed by all built-in categories. Includes Salary too. */
    public List<String> getAllCategories() {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        set.addAll(getCustomCategories());
        set.addAll(Arrays.asList(BUILT_IN));
        set.add(Transaction.CAT_SALARY);  // always available for income
        return new ArrayList<>(set);
    }

    /** Custom (newest first) + built-in expense categories — for the Budget tab. */
    public List<String> getBudgetableCategories() {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        set.addAll(getCustomCategories());
        set.addAll(Arrays.asList(BUILT_IN));
        return new ArrayList<>(set);
    }

    public boolean isCustom(String name) {
        for (String c : getCustomCategories()) {
            if (c.equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    private String key() { return email + "_custom"; }

    private void save(List<String> list) {
        JSONArray arr = new JSONArray();
        for (String c : list) arr.put(c);
        prefs.edit().putString(key(), arr.toString()).apply();
    }
}