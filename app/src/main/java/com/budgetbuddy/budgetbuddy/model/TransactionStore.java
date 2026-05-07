package com.budgetbuddy.budgetbuddy.model;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Simple persistence layer.
 * All transactions are stored as a JSON array under the key "<email>_transactions"
 * in the "BudgetBuddyTransactions" SharedPreferences file.
 */
public class TransactionStore {

    private static final String PREFS_NAME = "BudgetBuddyTransactions";

    private final SharedPreferences prefs;
    private final String            email;

    public TransactionStore(Context ctx, String email) {
        this.prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.email = email;
    }

    // -------------------------------------------------------------------------
    // CRUD
    // -------------------------------------------------------------------------

    public List<Transaction> getAll() {
        List<Transaction> list = new ArrayList<>();
        String json = prefs.getString(key(), "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                list.add(Transaction.fromJson(arr.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void add(Transaction t) {
        if (t.getId() == null || t.getId().isEmpty()) {
            t.setId(UUID.randomUUID().toString());
        }
        List<Transaction> list = getAll();
        list.add(0, t); // newest first
        save(list);
    }

    public void delete(String id) {
        List<Transaction> list = getAll();
        list.removeIf(t -> t.getId().equals(id));
        save(list);
    }

    /** Replaces an existing transaction (matched by id) in-place. */
    public void update(Transaction updated) {
        if (updated.getId() == null) return;
        List<Transaction> list = getAll();
        for (int i = 0; i < list.size(); i++) {
            if (updated.getId().equals(list.get(i).getId())) {
                list.set(i, updated);
                save(list);
                return;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Aggregates
    // -------------------------------------------------------------------------

    public double totalIncome() {
        double sum = 0;
        for (Transaction t : getAll()) if (!t.isExpense()) sum += t.getAmount();
        return sum;
    }

    public double totalExpenses() {
        double sum = 0;
        for (Transaction t : getAll()) if (t.isExpense()) sum += t.getAmount();
        return sum;
    }

    /** Returns summed expense amount per category, for the pie chart. */
    public java.util.Map<String, Double> expensesByCategory() {
        java.util.LinkedHashMap<String, Double> map = new java.util.LinkedHashMap<>();
        for (Transaction t : getAll()) {
            if (t.isExpense()) {
                map.merge(t.getCategory(), t.getAmount(), Double::sum);
            }
        }
        return map;
    }

    // -------------------------------------------------------------------------
    // Internal
    // -------------------------------------------------------------------------

    private String key() { return email + "_transactions"; }

    private void save(List<Transaction> list) {
        JSONArray arr = new JSONArray();
        for (Transaction t : list) {
            try { arr.put(t.toJson()); }
            catch (JSONException e) { e.printStackTrace(); }
        }
        prefs.edit().putString(key(), arr.toString()).apply();
    }
}