package com.example.aquasaver.ui.conservation_tips.;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.aquasaver.R;
import com.example.aquasaver.challenges.ChallengeWithProgress;
import com.example.aquasaver.dao.ChallengesDao;
import com.example.aquasaver.dao.SuggestionsDao;
import com.example.aquasaver.databinding.FragmentConservationTipsBinding;

import com.example.aquasaver.db.AppDatabase;
import com.example.aquasaver.model.ChallengeProgress;
import com.example.aquasaver.model.Challenges;
import com.example.aquasaver.model.GoalProgress;
import com.example.aquasaver.model.Suggestions;
import com.example.aquasaver.model.UserProfile;
import com.example.aquasaver.dao.GoalProgressDao;
import com.example.aquasaver.dao.UserProfileDao;
import com.example.aquasaver.dao.ChallengeProgressDao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Test extends Fragment {

    private FragmentConservationTipsBinding binding;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentConservationTipsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Get user preferences
        SharedPreferences userPrefs = requireActivity().getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
        String email = userPrefs.getString("username", null);
        String location = userPrefs.getString("location", null);

        if (email == null || location == null) {
            Toast.makeText(getContext(), "User not logged in or location missing", Toast.LENGTH_LONG).show();
            binding.textConservationTips.setText("Please log in and set your location.");
            Log.w("ConservationTipsFragment", "Missing user info: email=" + email + ", location=" + location);
            return binding.getRoot();
        }

        executor.execute(() -> {
            try {
                SuggestionsDao dao = AppDatabase.getInstance(requireContext()).suggestionsDao();

                List<Suggestions> suggestions = dao.getUserSuggestions(email);
                requireActivity().runOnUiThread(() -> {
                    if (suggestions.isEmpty()) {
                        Toast.makeText(getContext(), "No suggestions found", Toast.LENGTH_SHORT).show();
                    }
                    //CALL FUNCTIONS
                });
            } catch (Exception e) {
                Log.e("Test", "Error loading suggestions", e);
            }
        });
        return root;
    }

    private void loadChallenges(String email) {
        SuggestionsDao suggestionsDao = AppDatabase.getInstance(requireContext()).suggestionsDao();
        executor.execute(() -> {
            List<Suggestions> suggestions = suggestionsDao.getUserSuggestions(email);
            requireActivity().runOnUiThread(() -> populateSuggestions(suggestions));
        });
    }

    private static Date getTodayDateTruncated() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
    private List<Challenges> getDailyChallenges(List<Challenges> allChallenges) {
        SharedPreferences prefs = requireContext().getSharedPreferences("DailyChallengePrefs", Context.MODE_PRIVATE);

        long lastShuffleMillis = prefs.getLong("lastShuffleMillis", 0);
        Date today = getTodayDateTruncated();

        // Check if the last shuffle was earlier than today
        if (lastShuffleMillis < today.getTime()) {
            // New day: shuffle and pick 3
            Collections.shuffle(allChallenges);
            List<Challenges> dailyChallenges = allChallenges.subList(0, Math.min(3, allChallenges.size()));

            // Save challenge titles
            StringBuilder sb = new StringBuilder();
            for (Challenges c : dailyChallenges) {
                sb.append(c.getTitle()).append(";");
            }

            prefs.edit()
                    .putLong("lastShuffleMillis", today.getTime())
                    .putString("selectedChallengeTitles", sb.toString())
                    .apply();

            return dailyChallenges;
        } else {
            // Same day: load saved challenge titles
            String savedTitles = prefs.getString("selectedChallengeTitles", "");
            if (savedTitles.isEmpty()) {
                // fallback shuffle if something goes wrong
                Collections.shuffle(allChallenges);
                return allChallenges.subList(0, Math.min(3, allChallenges.size()));
            }

            Set<String> titleSet = new HashSet<>(Arrays.asList(savedTitles.split(";")));
            List<Challenges> savedChallenges = new ArrayList<>();
            for (Challenges c : allChallenges) {
                if (titleSet.contains(c.getTitle())) {
                    savedChallenges.add(c);
                }
            }

            return savedChallenges;
        }
    }

    private void updateCheckboxesWithChallenges(List<ChallengeWithProgress> challengeWithProgressList) {
        LinearLayout checkboxContainer = binding.challengeCheckboxContainer;
        checkboxContainer.removeAllViews();

        boolean hasAvailableChallenges = false;

        for (ChallengeWithProgress entry : challengeWithProgressList) {
            Challenges challenge = entry.challenge;
            ChallengeProgress progress = entry.progress;

            // Show only challenges not yet completed
            if (progress == null || !progress.completion) {
                CheckBox checkBox = new CheckBox(getContext());
                checkBox.setText(challenge.getTitle());
                checkBox.setTag(challenge.getTitle()); // Tag it with a unique ID/title for later reference
                checkboxContainer.addView(checkBox);
                hasAvailableChallenges = true;
            }
        }

        if (!hasAvailableChallenges) {
            TextView noMoreText = new TextView(getContext());
            noMoreText.setText("No more challenges available.");
            noMoreText.setPadding(8, 8, 8, 8);
            checkboxContainer.addView(noMoreText);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        executor.shutdown();
    }
}
