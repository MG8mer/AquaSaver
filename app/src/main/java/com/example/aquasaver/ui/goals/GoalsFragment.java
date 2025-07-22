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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;


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
import com.example.aquasaver.model.enums.GoalType;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class GoalsFragment extends Fragment {

    private FragmentGoalsBinding binding;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Challenges currentChallenge = null; // The single random challenge shown

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

        loadUserData(email);

        setupRecordButton(email);

        return root;
    }

    private void loadUserData(String email) {
        executor.execute(() -> {
            try {
                ChallengesDao dao = AppDatabase.getInstance(requireContext()).challengesDao();
                GoalProgressDao goalDao = AppDatabase.getInstance(requireContext()).goalProgressDao();

                List<Challenges> challenges = dao.getUserChallenges(email);

                GoalProgress todayProgress = goalDao.getTodayProgress(email);
                int goalAmount = (todayProgress != null) ? todayProgress.getGoalAmount() : 400;

                requireActivity().runOnUiThread(() -> {
                    if (challenges.isEmpty()) {
                        Toast.makeText(getContext(), "No challenges found", Toast.LENGTH_SHORT).show();
                        binding.titleGoals.setText("Goal Progress");
                    } else {
                        binding.titleGoals.setText("Goal Progress");
                        showSingleRandomChallenge(challenges);
                    }
                });

                loadGoalProgress(email);
                updateChallengesCompletedCount(email);

            } catch (Exception e) {
                Log.e("GoalsFragment", "Error loading data", e);
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Error loading data", Toast.LENGTH_SHORT).show());
            }
        });
    }
    private void loadOneRandomChallenge(String email) {
        executor.execute(() -> {
            ChallengesDao dao = AppDatabase.getInstance(requireContext()).challengesDao();
            ChallengeProgressDao progressDao = AppDatabase.getInstance(requireContext()).challengeProgressDao();

            List<Challenges> allChallenges = dao.getUserChallenges(email);

            List<Challenges> incompleteChallenges = new ArrayList<>();
            for (Challenges c : allChallenges) {
                ChallengeProgress progress = progressDao.getChallengeProgressById(c.getTitle(), email);
                if (progress == null || !progress.completion) {
                    incompleteChallenges.add(c);
                }
            }

            if (incompleteChallenges.isEmpty()) {
                currentChallenge = null;
                requireActivity().runOnUiThread(() -> {
                    binding.challengeTitleText.setVisibility(View.GONE);
                    binding.challengeDescriptionText.setVisibility(View.GONE);
                    binding.completeChallengeButton.setVisibility(View.GONE);
                    binding.noChallengesMessage.setVisibility(View.VISIBLE);
                });
                return;
            }

            Collections.shuffle(incompleteChallenges);
            currentChallenge = incompleteChallenges.get(0);

            requireActivity().runOnUiThread(() -> {
                binding.noChallengesMessage.setVisibility(View.GONE);
                binding.challengeTitleText.setVisibility(View.VISIBLE);
                binding.challengeDescriptionText.setVisibility(View.VISIBLE);
                binding.completeChallengeButton.setVisibility(View.VISIBLE);

                binding.challengeTitleText.setText(currentChallenge.getTitle());
                binding.challengeDescriptionText.setText(currentChallenge.getDescription());
            });
        });
    }

    private void showSingleRandomChallenge(List<Challenges> challenges) {
        if (challenges.isEmpty()) {
            binding.challengeTitleText.setText("No challenges available.");
            binding.challengeDescriptionText.setText("");
            binding.completeChallengeButton.setEnabled(false);
            return;
        }

        Collections.shuffle(challenges);
        currentChallenge = challenges.get(0);

        binding.challengeTitleText.setText(currentChallenge.getTitle());
        binding.challengeDescriptionText.setText(currentChallenge.getDescription());
        binding.completeChallengeButton.setEnabled(true);
    }

    private void setupRecordButton(String email) {
        binding.completeChallengeButton.setOnClickListener(v -> {
            if (currentChallenge == null) {
                Toast.makeText(getContext(), "No challenge to complete.", Toast.LENGTH_SHORT).show();
                return;
            }

            executor.execute(() -> {
                ChallengeProgressDao progressDao = AppDatabase.getInstance(requireContext()).challengeProgressDao();

                // Check if already completed today
                ChallengeProgress todayChallenge = progressDao.getTodayChallengeProgress(email);
                if (todayChallenge != null && todayChallenge.getTitle().equals(currentChallenge.getTitle())) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "You've already completed this challenge today!", Toast.LENGTH_LONG).show();
                    });
                    return;
                }

                ChallengeProgress progress = new ChallengeProgress(email, currentChallenge.getTitle(), 1f, true, new Date());
                progress.timestamp = new Date();
                progressDao.insertChallengeProgress(progress);

                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Challenge completed!", Toast.LENGTH_SHORT).show();
                    // Refresh challenge display to show next one or no challenges
                    loadOneRandomChallenge(email);
                });
            });
        });

    }

    private void loadGoalProgress(String email) {
        GoalProgressDao progressDao = AppDatabase.getInstance(requireContext()).goalProgressDao();
        UserProfileDao userDao = AppDatabase.getInstance(requireContext()).userProfileDao();

        executor.execute(() -> {
            try {
                UserProfile user = userDao.getUserByEmail(email);
                GoalType goalType = user.getGoalType();
                GoalProgress progress;

                Date startDate, endDate;

                switch (goalType) {
                    case WEEKLY:
                        startDate = getStartOfWeek();
                        endDate = getEndOfWeek();
                        progress = progressDao.getWeeklyProgress(email, startDate, endDate);
                        break;
                    case MONTHLY:
                        startDate = getStartOfMonth();
                        endDate = getEndOfMonth();
                        progress = progressDao.getMonthlyProgress(email, startDate, endDate);
                        break;
                    case DAILY:
                    default:
                        startDate = getTodayDateTruncated();
                        endDate = new Date();
                        progress = progressDao.getTodayProgress(email);
                        break;
                }

                if (progress == null) {
                    float defaultGoal;
                    switch (goalType) {
                        case WEEKLY:
                            defaultGoal = 2800f;
                            break;
                        case MONTHLY:
                            defaultGoal = 11200f;
                            break;
                        case DAILY:
                        default:
                            defaultGoal = 400f;
                            break;
                    }
                    progress = new GoalProgress(email, 0f, startDate, true, (int) defaultGoal);
                    progressDao.insertGoalProgress(progress);
                }

                final GoalProgress finalProgress = progress;
                final int goalAmount = (int) finalProgress.getGoalAmount();
                final int amountLogged = (int) finalProgress.getAmountLogged();
                final boolean onTarget = finalProgress.getOnTarget();

                requireActivity().runOnUiThread(() -> {
                    binding.goalAmountText.setText("Goal Amount: " + goalAmount + " L");
                    binding.goalTypeText.setText("Type: " + goalType.toString());
                    binding.challengesCompletedText.setText(onTarget ? "On Target ✅" : "Over Limit ❌");
                    updateProgressBar(amountLogged, goalAmount);
                });

                if (goalType == GoalType.DAILY) {
                    updateGoalProgress(email, finalProgress);
                } else {
                    requireActivity().runOnUiThread(() -> binding.streakText.setText("Streak: -"));
                }

            } catch (Exception e) {
                Log.e("GoalsFragment", "Error in loadGoalProgress", e);
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Error loading goal progress", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void updateGoalProgress(String email, GoalProgress todayGoal) {
        GoalProgressDao progressDao = AppDatabase.getInstance(requireContext()).goalProgressDao();
        UserProfileDao userDao = AppDatabase.getInstance(requireContext()).userProfileDao();

        if (todayGoal == null) return;

        executor.execute(() -> {
            boolean onTarget = todayGoal.getAmountLogged() <= todayGoal.getGoalAmount();
            todayGoal.setOnTarget(onTarget);
            progressDao.updateGoalProgress(todayGoal);

            UserProfile user = userDao.getUserByEmail(email);
            Date now = new Date();
            Date lastUpdate = user.getLastStreakUpdate();
            GoalType goalType = user.getGoalType();

            boolean isNewPeriod = false;

            Calendar calNow = Calendar.getInstance();
            calNow.setTime(now);

            Calendar calLast = Calendar.getInstance();
            calLast.setTime(lastUpdate != null ? lastUpdate : new Date(0));

            switch (goalType) {
                case DAILY:
                    isNewPeriod = lastUpdate == null || !DateUtils.isToday(lastUpdate.getTime());
                    break;
                case WEEKLY:
                    int weekNow = calNow.get(Calendar.WEEK_OF_YEAR);
                    int weekLast = calLast.get(Calendar.WEEK_OF_YEAR);
                    int yearNow = calNow.get(Calendar.YEAR);
                    int yearLast = calLast.get(Calendar.YEAR);
                    isNewPeriod = lastUpdate == null || weekNow != weekLast || yearNow != yearLast;
                    break;
                case MONTHLY:
                    int monthNow = calNow.get(Calendar.MONTH);
                    int monthLast = calLast.get(Calendar.MONTH);
                    yearNow = calNow.get(Calendar.YEAR);
                    yearLast = calLast.get(Calendar.YEAR);
                    isNewPeriod = lastUpdate == null || monthNow != monthLast || yearNow != yearLast;
                    break;
            }

            if (isNewPeriod) {
                if (onTarget) {
                    user.setStreak(user.getStreak() + 1);
                } else {
                    user.setStreak(0);
                }
                user.setLastStreakUpdate(now);
                userDao.updateUserProfile(user);
            }

            int streakCount = user.getStreak();
            requireActivity().runOnUiThread(() ->
                    binding.streakText.setText("Streak: " + streakCount + " " + (goalType == GoalType.DAILY ? "days" : goalType == GoalType.WEEKLY ? "weeks" : "months") + " 🔥")
            );
        });
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

    private Date getStartOfWeek() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date getEndOfWeek() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getStartOfWeek());
        cal.add(Calendar.DAY_OF_MONTH, 7);
        return cal.getTime();
    }

    private Date getStartOfMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date getEndOfMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
    private void updateChallengesCompletedCount(String email) {
        executor.execute(() -> {
            ChallengeProgressDao progressDao = AppDatabase.getInstance(requireContext()).challengeProgressDao();
            // Get all challenges completed by user
            List<ChallengeProgress> completedChallenges = progressDao.getCompletedChallengesByUser(email);
            int completedCount = completedChallenges != null ? completedChallenges.size() : 0;

            requireActivity().runOnUiThread(() -> {
                binding.challengesCompletedText.setText("Challenges completed: " + completedCount);
            });
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        executor.shutdown();
    }
}
