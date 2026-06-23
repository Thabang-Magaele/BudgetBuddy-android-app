package com.budgetbuddy.budgetbuddy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Financial education hub. Each card represents a budget-goal category and links
 * to one or more registered FSP (Capitec Bank) articles for further learning.
 *
 * Categories the user picked during onboarding are highlighted at the top.
 */
public class GoalsFragment extends Fragment {

    private static final String ARG_EMAIL          = "email";
    private static final String PREFS_ONBOARDING   = "BudgetBuddyOnboarding";

    private String email;

    // -------------------------------------------------------------------------
    // Goal definitions (label, emoji, blurb, list of resource URLs)
    // -------------------------------------------------------------------------
    private static class Goal {
        final String   label;
        final String   emoji;
        final String   blurb;
        final String[] urls;

        Goal(String label, String emoji, String blurb, String[] urls) {
            this.label = label;
            this.emoji = emoji;
            this.blurb = blurb;
            this.urls  = urls;
        }
    }

    private static final Goal[] GOALS = {
            new Goal(
                    "Build Savings", "💵",
                    "Practical guides on stretching your money as a student and budgeting at university.",
                    new String[]{
                            "https://www.capitecbank.co.za/blog/articles/education/how-to-save-money-as-a-student/",
                            "https://www.capitecbank.co.za/blog/articles/education/how-to-budget-at-university/"
                    }
            ),
            new Goal(
                    "Grow Investments", "📈",
                    "A beginner-friendly intro to how investing works — and why it's simpler than you think.",
                    new String[]{
                            "https://www.capitecbank.co.za/blog/articles/best-way-to-bank/investing-really-is-as-simple-as-1-2-3/"
                    }
            ),
            new Goal(
                    "Emergency Fund", "🛡️",
                    "How to plan for the unexpected and build a safety net you can rely on.",
                    new String[]{
                            "https://www.capitecbank.co.za/blog/articles/saving/the-backup-plan/"
                    }
            ),
            new Goal(
                    "Pay Off Debt", "🏦",
                    "A step-by-step strategy for getting out of debt and staying there.",
                    new String[]{
                            "https://www.capitecbank.co.za/rewards/money-up/articles/credit-and-debt/a-step-by-step-strategy-for-becoming-debt-free/"
                    }
            ),
            new Goal(
                    "Retirement Planning", "🌅",
                    "Why starting early matters, and how small monthly contributions add up over time.",
                    new String[]{
                            "https://www.capitecbank.co.za/blog/articles/saving/planning-for-retirement/"
                    }
            )
    };

    // -------------------------------------------------------------------------
    public static GoalsFragment newInstance(String email) {
        GoalsFragment f = new GoalsFragment();
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
        return inflater.inflate(R.layout.fragment_goals, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayout container = view.findViewById(R.id.goalsContainer);
        Set<String>  picked    = loadUserGoals();

        // Sort: user's chosen goals first, then the rest in original order
        List<Goal> ordered = new ArrayList<>();
        List<Goal> rest    = new ArrayList<>();
        for (Goal g : GOALS) {
            if (isPicked(picked, g.label)) ordered.add(g);
            else                            rest.add(g);
        }
        ordered.addAll(rest);

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (Goal g : ordered) {
            View card = inflater.inflate(R.layout.item_goal_card, container, false);
            bindGoalCard(card, g, isPicked(picked, g.label));
            container.addView(card);
        }
    }

    // -------------------------------------------------------------------------
    private Set<String> loadUserGoals() {
        SharedPreferences prefs = requireContext()
                .getSharedPreferences(PREFS_ONBOARDING, Context.MODE_PRIVATE);
        String stored = prefs.getString(email + "_goals", "");
        Set<String> set = new HashSet<>();
        if (stored != null && !stored.isEmpty()) {
            for (String s : stored.split(",")) set.add(s.trim());
        }
        return set;
    }

    private boolean isPicked(Set<String> picked, String goalLabel) {
        // Match onboarding's labels (e.g. "Emergency Fund" → stored as "Emergency Fund")
        for (String p : picked) {
            if (p.equalsIgnoreCase(goalLabel)
                    || (goalLabel.equals("Build Savings")    && p.equalsIgnoreCase("Savings"))
                    || (goalLabel.equals("Grow Investments") && p.equalsIgnoreCase("Investments"))
                    || (goalLabel.equals("Pay Off Debt")     && p.equalsIgnoreCase("Debt Repayment"))
                    || (goalLabel.equals("Retirement Planning") && p.equalsIgnoreCase("Retirement"))) {
                return true;
            }
        }
        return false;
    }

    private void bindGoalCard(View card, Goal g, boolean picked) {
        ((TextView) card.findViewById(R.id.tvEmoji)).setText(g.emoji);
        ((TextView) card.findViewById(R.id.tvTitle)).setText(g.label);
        ((TextView) card.findViewById(R.id.tvBlurb)).setText(g.blurb);

        View pickedBadge = card.findViewById(R.id.pickedBadge);
        pickedBadge.setVisibility(picked ? View.VISIBLE : View.GONE);

        // Build one button per URL
        LinearLayout linksContainer = card.findViewById(R.id.linksContainer);
        linksContainer.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (int i = 0; i < g.urls.length; i++) {
            String url = g.urls[i];
            MaterialButton btn = (MaterialButton) inflater.inflate(
                    R.layout.item_goal_link_button, linksContainer, false);
            btn.setText(g.urls.length == 1
                    ? "Read Article  →"
                    : "Read Article " + (i + 1) + "  →");
            btn.setOnClickListener(v -> openUrl(url));
            linksContainer.addView(btn);
        }
    }

    private void openUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            android.widget.Toast.makeText(requireContext(),
                    "Couldn't open the link.",
                    android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}