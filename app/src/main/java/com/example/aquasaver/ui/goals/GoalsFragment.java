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
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.aquasaver.databinding.FragmentGoalsBinding;
import com.example.aquasaver.db.AppDatabase;
import com.example.aquasaver.model.GoalProgress;
import com.example.aquasaver.dao.GoalProgressDao;
import com.example.aquasaver.dao.UserProfileDao;
import com.example.aquasaver.model.UserProfile;

import java.util.Date;
import java.util.concurrent.Executors;

public class GoalsFragment extends Fragment {

    private FragmentGoalsBinding binding;
    private static final int GOAL_AMOUNT = 100;

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

        binding.goalTypeText.setText("Type: Daily");
        binding.goalAmountText.setText("Goal Amount: " + GOAL_AMOUNT + " L");

        loadGoalProgress(email); // Also calls updateGoalProgress within

        return root;
    }

    private void updateGoalProgress(String email, GoalProgress todayGoal) {
        GoalProgressDao progressDao = AppDatabase.getInstance(getContext()).goalProgressDao();
        UserProfileDao userDao = AppDatabase.getInstance(getContext()).userProfileDao();

        Executors.newSingleThreadExecutor().execute(() -> {
            if (todayGoal == null) return;

            boolean onTarget = todayGoal.getAmountLogged() <= GOAL_AMOUNT;
            todayGoal.setOnTarget(onTarget);
            progressDao.updateGoalProgress(todayGoal);

            UserProfile user = userDao.getUserByEmail(email);
            Date today = new Date();
            Date lastUpdate = user.getLastStreakUpdate();

            boolean isNewDay = lastUpdate == null || !DateUtils.isToday(lastUpdate.getTime());

            if (isNewDay) {
                if (onTarget) {
                    user.setStreak(user.getStreak() + 1);
                } else {
                    user.setStreak(0);
                }
                user.setLastStreakUpdate(today);
                userDao.updateUserProfile(user);
            }

                int streakCount = user.getStreak();
                requireActivity().runOnUiThread(() ->
                        binding.streakText.setText("Streak: " + streakCount + " days 🔥")
                );
        });
    }

    private void loadGoalProgress(String email) {
        GoalProgressDao dao = AppDatabase.getInstance(requireContext()).goalProgressDao();

        Executors.newSingleThreadExecutor().execute(() -> {
            GoalProgress todayProgress = dao.getTodayProgress(email);

            if (todayProgress == null) {
                requireActivity().runOnUiThread(() -> {
                    binding.streakText.setText("Streak: 0 days");
                    binding.challengesCompletedText.setText("No progress logged today.");
                    updateProgressBar(0, GOAL_AMOUNT);
                });
                return;
            }

            int progress = (int) todayProgress.getAmountLogged();
            boolean onTarget = todayProgress.getOnTarget();

            requireActivity().runOnUiThread(() -> {
                binding.challengesCompletedText.setText(onTarget ? "On Target ✅" : "Over Limit ❌");
                updateProgressBar(progress, GOAL_AMOUNT);
            });

            updateGoalProgress(email, todayProgress);
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
            shape.setColor(getSegmentColor(i, filledSegments));

            segment.setBackground(shape);
            binding.progressBarContainer.addView(segment);
        }
    }

    private int getSegmentColor(int index, int filledSegments) {
        if (index >= filledSegments) return 0xFFD3D3D3; // Light gray (unfilled)

        float percent = index / 12f;
        if (percent < 0.5f) return 0xFF1E90FF;   // Blue
        else if (percent < 0.75f) return 0xFFFFD700; // Yellow
        else return 0xFFFF4500;   // Red-orange
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
