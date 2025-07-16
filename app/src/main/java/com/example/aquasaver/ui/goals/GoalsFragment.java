package com.example.aquasaver.ui.goals;

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

import com.example.aquasaver.R;
import com.example.aquasaver.challenges.ChallengeWithProgress;
import com.example.aquasaver.dao.ChallengesDao;
import com.example.aquasaver.databinding.FragmentGoalsBinding;
import com.example.aquasaver.db.AppDatabase;
import com.example.aquasaver.model.ChallengeProgress;
import com.example.aquasaver.model.Challenges;
import com.example.aquasaver.model.GoalProgress;
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

public class GoalsFragment extends Fragment {

    private FragmentGoalsBinding binding;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGoalsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        SharedPreferences prefs = requireActivity().getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
        String email = prefs.getString("username", null);

        if (email == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_LONG).show();
            binding.titleGoals.setText("Please log in.");
            return root;
        }

        binding.goalTypeText.setText("Type: Loading...");
        binding.goalAmountText.setText("Goal Amount: Loading...");

        executor.execute(() -> {
            try {
                ChallengesDao dao = AppDatabase.getInstance(requireContext()).challengesDao();
                GoalProgressDao goalDao = AppDatabase.getInstance(requireContext()).goalProgressDao();

                List<Challenges> challenges = dao.getUserChallenges(email);
                List<ChallengeWithProgress> challengeWithProgressList = dao.getAllChallengesWithProgress(email);
                GoalProgress todayProgress = goalDao.getTodayProgress(email); // 💡 fetch goalProgress here

                int goalAmount = (todayProgress != null) ? todayProgress.getGoalAmount() : 100; // fallback

                Log.d("GoalsFragment", "Challenges: " + challenges.size());

                requireActivity().runOnUiThread(() -> {
                    if (challenges.isEmpty()) {
                        Toast.makeText(getContext(), "No challenges found", Toast.LENGTH_SHORT).show();
                    }

                    binding.goalAmountText.setText("Goal Amount: " + goalAmount + " L");

                    loadGoalProgress(email);
                    loadChallenges(email);
                });
            } catch (Exception e) {
                Log.e("GoalsFragment", "Error loading challenges", e);
            }
        });

        Button recordButton = root.findViewById(R.id.recordChallengeButton);
        LinearLayout dropdown = root.findViewById(R.id.challengeDropdown);

        // Toggle dropdown visibility
        recordButton.setOnClickListener(v -> {
            if (dropdown.getVisibility() == View.GONE) {
                dropdown.setVisibility(View.VISIBLE);
            } else {
                dropdown.setVisibility(View.GONE);
            }
        });

        // === DYNAMIC CHECKBOXES SETUP ===
        LinearLayout checkboxContainer = root.findViewById(R.id.challengeCheckboxContainer);
        checkboxContainer.removeAllViews();

        // Submit button handler
        Button submitButton = root.findViewById(R.id.submitChallengeProgressButton);
        submitButton.setOnClickListener(v -> {
            executor.execute(() -> {
                ChallengesDao challengesDao = AppDatabase.getInstance(requireContext()).challengesDao();
                ChallengeProgressDao progressDao = AppDatabase.getInstance(requireContext()).challengeProgressDao();

                boolean anyUpdated = false;

                for (int i = 0; i < checkboxContainer.getChildCount(); i++) {
                    View child = checkboxContainer.getChildAt(i);
                    if (child instanceof CheckBox) {
                        CheckBox cb = (CheckBox) child;
                        if (cb.isChecked()) {
                            String challengeTitle = (String) cb.getTag();

                            ChallengeProgress progress = progressDao.getChallengeProgressById(challengeTitle, email);
                            if (progress == null) {
                                // Create new ChallengeProgress if none exists yet
                                progress = new ChallengeProgress(email, challengeTitle, 1f, true);
                                // Optionally set currentProgress to goal or zero here if you want
                                progressDao.insertChallengeProgress(progress);
                                anyUpdated = true;
                            } else if (!progress.completion) {
                                // Update existing progress if not completed
                                progress.setCompletion(true);
                                progressDao.updateChallengeProgress(progress);
                                anyUpdated = true;
                            }
                        }
                    }
                }

                if (anyUpdated) {
                    List<ChallengeWithProgress> updatedList = challengesDao.getAllChallengesWithProgress(email);

                    requireActivity().runOnUiThread(() -> {
                        loadChallenges(email);
                        binding.challengeDropdown.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Challenges updated!", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "No challenges selected", Toast.LENGTH_SHORT).show();
                        binding.challengeDropdown.setVisibility(View.GONE);
                    });
                }
            });
        });
        return root;
    }

    private void loadGoalProgress(String email) {
        GoalProgressDao dao = AppDatabase.getInstance(requireContext()).goalProgressDao();

        executor.execute(() -> {
            GoalProgress todayProgress = dao.getTodayProgress(email);

            if (todayProgress == null) {
                requireActivity().runOnUiThread(() -> {
                    binding.streakText.setText("Streak: 0 days");
                    binding.challengesCompletedText.setText("No progress logged today.");
                    binding.goalAmountText.setText("Goal Amount: 100 L");
                    updateProgressBar(0, 100);
                });
                return;
            }

            int goalAmount = todayProgress.getGoalAmount();
            int progress = (int) todayProgress.getAmountLogged();
            boolean onTarget = todayProgress.getOnTarget();

            requireActivity().runOnUiThread(() -> {
                binding.challengesCompletedText.setText(onTarget ? "On Target ✅" : "Over Limit ❌");
                binding.goalAmountText.setText("Goal Amount: " + goalAmount + " L");
                updateProgressBar(progress, goalAmount);
            });

            // Update streak info on background thread but UI on main thread
            executor.execute(() -> {
                try {
                    updateGoalProgress(email, todayProgress);
                    Log.d("GoalsFragment", "Goal progress updated");
                } catch (Exception e) {
                    Log.e("GoalsFragment", "Error updating goal progress", e);
                }
            });
        });
    }

    private void updateGoalProgress(String email, GoalProgress todayGoal) {
        GoalProgressDao progressDao = AppDatabase.getInstance(requireContext()).goalProgressDao();
        UserProfileDao userDao = AppDatabase.getInstance(requireContext()).userProfileDao();

        if (todayGoal == null) return;

        // Run DB updates in background
        executor.execute(() -> {
            boolean onTarget = todayGoal.getAmountLogged() <= todayGoal.getGoalAmount();
            todayGoal.setOnTarget(onTarget);
            progressDao.updateGoalProgress(todayGoal);

            UserProfile user = userDao.getUserByEmail(email);
            Date today = new Date();
            Date lastUpdate = user.getLastStreakUpdate();

            boolean isNewDay = lastUpdate == null || !DateUtils.isToday(lastUpdate.getTime());

            if (isNewDay) {
                user.setStreak(onTarget ? user.getStreak() + 1 : 0);
                user.setLastStreakUpdate(today);
                userDao.updateUserProfile(user);
            }

            int streakCount = user.getStreak();
            requireActivity().runOnUiThread(() ->
                    binding.streakText.setText("Streak: " + streakCount + " days 🔥")
            );
        });
    }

    private void loadChallenges(String email) {
        executor.execute(() -> {
            // Reset completion flags for today if needed
            resetDailyProgressForToday(email);

            ChallengesDao dao = AppDatabase.getInstance(requireContext()).challengesDao();
            ChallengeProgressDao progressDao = AppDatabase.getInstance(requireContext()).challengeProgressDao();

            List<Challenges> all = dao.getUserChallenges(email);
            Log.d("GoalsFragment", "Total challenges found for user: " + all.size());
            List<Challenges> daily = getDailyChallenges(all); // reshuffles daily

            // Build ChallengeWithProgress list — include all, even completed ones
            List<ChallengeWithProgress> list = new ArrayList<>();
            Log.d("GoalsFragment", "ChallengeWithProgress list size: " + list.size());
            for (Challenges c : daily) {
                ChallengeProgress p = progressDao.getChallengeProgressById(c.getTitle(), email);
                ChallengeWithProgress cwp = new ChallengeWithProgress();
                cwp.challenge = c;
                cwp.progress = p;
                list.add(cwp);
            }

            requireActivity().runOnUiThread(() -> {
                populateChallenges(list);
                updateCheckboxesWithChallenges(list);
            });
        });
    }

    private void populateChallenges(List<ChallengeWithProgress> challengeListWithProgress) {
        LinearLayout challengeList = binding.challengeListLayout;
        challengeList.removeAllViews();
        Log.d("GoalsFragment", "Challenges loaded: " + challengeListWithProgress.size());

        if (challengeListWithProgress.isEmpty()) {
            TextView emptyText = new TextView(getContext());
            emptyText.setText("No challenges found.");
            challengeList.addView(emptyText);
            return;
        }

        for (int i = 0; i < challengeListWithProgress.size(); i++) {
            ChallengeWithProgress entry = challengeListWithProgress.get(i);
            Challenges challenge = entry.challenge;
            ChallengeProgress progress = entry.progress;

            String statusSymbol = getStatusSymbol(progress);
            String displayText = (i + 1) + ". " + challenge.getTitle() + "\n" + statusSymbol + " " + challenge.getDescription();

            TextView textView = new TextView(getContext());
            textView.setText(displayText);
            textView.setTextSize(16);
            textView.setPadding(0, 8, 0, 8);
            challengeList.addView(textView);
        }
    }

    private String getStatusSymbol(ChallengeProgress progress) {
        if (progress == null) {
            return "⬜"; // Not started
        } else if (progress.completion) {
            return "✅"; // Completed
        } else if (progress.currentProgress > 0) {
            return "🟦"; // In progress
        } else {
            return "⬜"; // Not started
        }
    }

    private void updateProgressBar(int current, int total) {
        final int segments = 12;
        binding.progressBarContainer.removeAllViews();

        int filledSegments = (int) ((current / (float) total) * segments);
        Context context = requireContext();

        for (int i = 0; i < segments; i++) {
            View segment = new View(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.MATCH_PARENT, 1f);
            params.setMarginEnd(4);
            segment.setLayoutParams(params);

            GradientDrawable shape = new GradientDrawable();
            shape.setCornerRadius(10);
            shape.setColor(getSegmentColor(i, filledSegments, segments));

            segment.setBackground(shape);
            binding.progressBarContainer.addView(segment);
        }
    }

    // Added segments param for consistency
    private int getSegmentColor(int index, int filledSegments, int totalSegments) {
        if (index >= filledSegments) return 0xFFD3D3D3; // Light gray (unfilled)

        float percent = index / (float) totalSegments;
        if (percent < 0.5f) return 0xFF1E90FF;   // Blue
        else if (percent < 0.75f) return 0xFFFFD700; // Yellow
        else return 0xFFFF4500;   // Red-orange
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
                Collections.shuffle(allChallenges);
                return allChallenges.subList(0, Math.min(3, allChallenges.size()));
            }

// Filter for saved challenge titles
            Set<String> titleSet = new HashSet<>(Arrays.asList(savedTitles.split(";")));
            List<Challenges> savedChallenges = new ArrayList<>();
            for (Challenges c : allChallenges) {
                if (titleSet.contains(c.getTitle())) {
                    savedChallenges.add(c);
                }
            }

// Fallback if none matched (e.g., challenges removed/renamed)
            if (savedChallenges.isEmpty()) {
                Collections.shuffle(allChallenges);
                return allChallenges.subList(0, Math.min(3, allChallenges.size()));
            }

            return savedChallenges;
        }
    }

    private void updateCheckboxesWithChallenges(List<ChallengeWithProgress> list) {
        LinearLayout container = binding.challengeCheckboxContainer;
        container.removeAllViews();

        for (ChallengeWithProgress entry : list) {
            CheckBox cb = new CheckBox(requireContext());
            cb.setText(entry.challenge.getTitle());
            cb.setTag(entry.challenge.getTitle());
            container.addView(cb);
        }

        if (list.isEmpty()) {
            TextView tv = new TextView(requireContext());
            tv.setText("No challenges available.");
            container.addView(tv);
        }
    }
    private void resetDailyProgressForToday(String email) {
        SharedPreferences prefs = requireContext()
                .getSharedPreferences("DailyChallengePrefs", Context.MODE_PRIVATE);
        long lastReset = prefs.getLong("lastResetMillis", 0);
        long todayMidnight = getTodayDateTruncated().getTime();
        if (lastReset < todayMidnight) {
            ChallengeProgressDao progressDao = AppDatabase.getInstance(requireContext())
                    .challengeProgressDao();
            // Reset only those progress entries for this user
            List<ChallengeProgress> all = progressDao.getUserChallengeProgress(email);
            for (ChallengeProgress p : all) {
                p.setCompletion(false);
                progressDao.updateChallengeProgress(p);
            }
            prefs.edit().putLong("lastResetMillis", todayMidnight).apply();
        }
    }
    private void resetDailyProgressIfNewDay(String email) {
        SharedPreferences prefs = requireContext().getSharedPreferences("DailyChallengePrefs", Context.MODE_PRIVATE);
        long lastResetMillis = prefs.getLong("lastResetMillis", 0);
        Date today = getTodayDateTruncated();

        if (lastResetMillis < today.getTime()) {
            ChallengeProgressDao progressDao = AppDatabase.getInstance(requireContext()).challengeProgressDao();
            List<ChallengeProgress> allProgress = progressDao.getUserChallengeProgress(email);

            for (ChallengeProgress progress : allProgress) {
                progress.setCompletion(false); // reset completion for the day
                progressDao.updateChallengeProgress(progress);
            }

            prefs.edit().putLong("lastResetMillis", today.getTime()).apply();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        executor.shutdown();
    }
}
