package com.budgetbuddy.budgetbuddy;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_EMAIL = "email";

    private String email;
    private BottomNavigationView nav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        email = getIntent().getStringExtra(EXTRA_EMAIL);

        nav = findViewById(R.id.bottomNav);

        if (savedInstanceState == null) {
            loadFragment(HomeFragment.newInstance(email));
        }

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                loadFragment(HomeFragment.newInstance(email));
            } else if (id == R.id.nav_activity) {
                loadFragment(ActivityFragment.newInstance(email));
            } else if (id == R.id.nav_budget) {
                loadFragment(BudgetFragment.newInstance(email));
            } else if (id == R.id.nav_goals) {
                loadFragment(GoalsFragment.newInstance(email));
            }
            return true;
        });
    }

    /**
     * Called by HomeFragment when the user taps the Income or Expense card.
     * Opens the Activity tab pre-filtered to the requested type.
     */
    public void openActivityWithFilter(String filter) {
        loadFragment(ActivityFragment.newInstance(email, filter));
        nav.getMenu().findItem(R.id.nav_activity).setChecked(true);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}