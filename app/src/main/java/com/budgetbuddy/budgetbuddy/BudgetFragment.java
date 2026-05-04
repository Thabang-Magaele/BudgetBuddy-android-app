package com.budgetbuddy.budgetbuddy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class BudgetFragment extends Fragment {

    public static BudgetFragment newInstance(String email) {
        BudgetFragment f = new BudgetFragment();
        Bundle args = new Bundle();
        args.putString("email", email);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_placeholder, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.tvPlaceholderEmoji);
        ((android.widget.TextView) view.findViewById(R.id.tvPlaceholderTitle))
                .setText("Budgets");
        ((android.widget.TextView) view.findViewById(R.id.tvPlaceholderEmoji))
                .setText("💳");
        ((android.widget.TextView) view.findViewById(R.id.tvPlaceholderSub))
                .setText("Budget tracking coming soon.");
    }
}