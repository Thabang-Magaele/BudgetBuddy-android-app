package com.budgetbuddy.budgetbuddy.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Transaction {

    public static final String TYPE_INCOME  = "INCOME";
    public static final String TYPE_EXPENSE = "EXPENSE";

    // Categories
    public static final String CAT_FOOD          = "Food";
    public static final String CAT_TRANSPORT     = "Transport";
    public static final String CAT_HOUSING       = "Housing";
    public static final String CAT_ENTERTAINMENT = "Entertainment";
    public static final String CAT_SHOPPING      = "Shopping";
    public static final String CAT_HEALTH        = "Health";
    public static final String CAT_EDUCATION     = "Education";
    public static final String CAT_SALARY        = "Salary";
    public static final String CAT_OTHER         = "Other";

    private String id;
    private String type;        // INCOME | EXPENSE
    private String description;
    private double amount;
    private String category;
    private long   date;        // epoch millis

    public Transaction() {}

    public Transaction(String id, String type, String description,
                       double amount, String category, long date) {
        this.id          = id;
        this.type        = type;
        this.description = description;
        this.amount      = amount;
        this.category    = category;
        this.date        = date;
    }

    // -------------------------------------------------------------------------
    // JSON serialisation (no external library needed)
    // -------------------------------------------------------------------------
    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id",          id);
        o.put("type",        type);
        o.put("description", description);
        o.put("amount",      amount);
        o.put("category",    category);
        o.put("date",        date);
        return o;
    }

    public static Transaction fromJson(JSONObject o) throws JSONException {
        return new Transaction(
                o.getString("id"),
                o.getString("type"),
                o.getString("description"),
                o.getDouble("amount"),
                o.getString("category"),
                o.getLong("date")
        );
    }

    // -------------------------------------------------------------------------
    // Getters / Setters
    // -------------------------------------------------------------------------
    public String getId()          { return id; }
    public String getType()        { return type; }
    public String getDescription() { return description; }
    public double getAmount()      { return amount; }
    public String getCategory()    { return category; }
    public long   getDate()        { return date; }

    public void setId(String id)                   { this.id = id; }
    public void setType(String type)               { this.type = type; }
    public void setDescription(String description) { this.description = description; }
    public void setAmount(double amount)           { this.amount = amount; }
    public void setCategory(String category)       { this.category = category; }
    public void setDate(long date)                 { this.date = date; }

    public boolean isExpense() { return TYPE_EXPENSE.equals(type); }
}