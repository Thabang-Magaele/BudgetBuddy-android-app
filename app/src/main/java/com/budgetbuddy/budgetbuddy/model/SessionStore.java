package com.budgetbuddy.budgetbuddy.model;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Tracks the currently-remembered account when the user ticks "Stay logged in".
 * Stored in its own SharedPreferences file so clearing it doesn't affect
 * accounts, transactions or onboarding data.
 */
public class SessionStore {

    private static final String PREFS_NAME    = "BudgetBuddySession";
    private static final String KEY_EMAIL     = "remembered_email";
    private static final String KEY_REMEMBER  = "stay_logged_in";

    private final SharedPreferences prefs;

    public SessionStore(Context ctx) {
        this.prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void rememberAccount(String email) {
        prefs.edit()
                .putString(KEY_EMAIL, email)
                .putBoolean(KEY_REMEMBER, true)
                .apply();
    }

    public void clear() {
        prefs.edit().remove(KEY_EMAIL).remove(KEY_REMEMBER).apply();
    }

    public boolean isRemembered() {
        return prefs.getBoolean(KEY_REMEMBER, false)
                && prefs.getString(KEY_EMAIL, null) != null;
    }

    public String getRememberedEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }
}