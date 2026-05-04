package com.budgetbuddy.budgetbuddy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class GoalsFragment extends Fragment {

    public static GoalsFragment newInstance(String email) {
        GoalsFragment f = new GoalsFragment();
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
        ((TextView) view.findViewById(R.id.tvPlaceholderEmoji)).setText("🎯");
        ((TextView) view.findViewById(R.id.tvPlaceholderTitle)).setText("Goals");
        ((TextView) view.findViewById(R.id.tvPlaceholderSub)).setText("Goal tracking coming soon.");
    }
}