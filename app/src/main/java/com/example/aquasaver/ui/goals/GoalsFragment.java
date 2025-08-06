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
import android.widget.Toast;
import android.widget.LinearLayout;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.aquasaver.repository.ChallengeProgressRepository;
import com.example.aquasaver.repository.ChallengesRepository;
import com.example.aquasaver.databinding.FragmentGoalsBinding;
import com.example.aquasaver.model.ChallengeProgress;
import com.example.aquasaver.model.Challenges;
import com.example.aquasaver.model.GoalProgress;
import com.example.aquasaver.model.UserProfile;
import com.example.aquasaver.repository.GoalProgressRepository;
import com.example.aquasaver.repository.UserProfileRepository;
import com.example.aquasaver.model.enums.GoalType;
import com.google.firebase.firestore.DocumentSnapshot;

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
                ChallengesRepository challengesRepo = new ChallengesRepository();
                GoalProgressRepository goalProgressRepo = new GoalProgressRepository();

                List<Challenges>[] challenges = new List[]{new ArrayList<>()};
                challengesRepo.getUserChallenges(email, snapshot -> {
                    for (DocumentSnapshot doc : snapshot) {
                        Challenges challenge = doc.toObject(Challenges.class);
                        if (challenge != null) challenges[0].add(challenge);

                    }
                });

                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                Date today = cal.getTime();

                final GoalProgress[] todayProgress = new GoalProgress[1];
                goalProgressRepo.getTodayProgress(email, today, snapshot -> {
                    todayProgress[0] = snapshot.getDocuments()
                            .get(0)
                            .toObject(GoalProgress.class);
                });
                int goalAmount = (todayProgress[0] != null) ? todayProgress[0].getGoalAmount() : 400;

                requireActivity().runOnUiThread(() -> {
                    if (challenges[0].isEmpty()) {
                        Toast.makeText(getContext(), "No challenges found", Toast.LENGTH_SHORT).show();
                        binding.titleGoals.setText("Goal Progress");
                    } else {
                        binding.titleGoals.setText("Goal Progress");
                        showSingleRandomChallenge(challenges[0]);
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
            ChallengesRepository challengeRepo = new ChallengesRepository();
            ChallengeProgressRepository challengeProgressRepo = new ChallengeProgressRepository();

            List<Challenges>[] allChallenges = new List[]{new ArrayList<>()};
            challengeRepo.getUserChallenges(email, snapshot -> {
                for (DocumentSnapshot doc : snapshot) {
                    Challenges challenge = doc.toObject(Challenges.class);
                    if (challenge != null) allChallenges[0].add(challenge);

                }
            });

            List<Challenges> incompleteChallenges = new ArrayList<>();
            for (Challenges c : allChallenges[0]) {
                final ChallengeProgress[] progress = new ChallengeProgress[1];
                challengeProgressRepo.getChallengeProgressById(c.getTitle(), snapshot ->{
                    progress[0] = snapshot.getDocuments().get(0).toObject(ChallengeProgress.class);
                });
                if (progress == null || !progress[0].isCompletion()) {
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
                ChallengeProgressRepository challengeProgressRepo = new ChallengeProgressRepository();

                // Check if already completed today
                final ChallengeProgress[] todayChallenge = new ChallengeProgress[1];
                challengeProgressRepo.getTodayChallengeProgress(email, new Date(), snapshot -> {
                    todayChallenge[0] = snapshot.getDocuments().get(0).toObject(ChallengeProgress.class);
                });
                if (todayChallenge[0] != null && todayChallenge[0].getTitle().equals(currentChallenge.getTitle())) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "You've already completed this challenge today!", Toast.LENGTH_LONG).show();
                    });
                    return;
                }

                ChallengeProgress progress = new ChallengeProgress(email, currentChallenge.getTitle(), 1f, true, new Date());
                progress.setTimestamp(new Date());
                challengeProgressRepo.insertChallengeProgress(progress);

                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Challenge completed!", Toast.LENGTH_SHORT).show();
                    // Refresh challenge display to show next one or no challenges
                    loadOneRandomChallenge(email);
                });
            });
        });

    }

    private void loadGoalProgress(String email) {
        GoalProgressRepository goalProgressRepo = new GoalProgressRepository();
        UserProfileRepository userProfileRepo = new UserProfileRepository();

        executor.execute(() -> {
            try {
                final GoalProgress[] todayProgress = new GoalProgress[1];
                goalProgressRepo.getTodayProgress(email, new Date(), snapshot -> {
                    todayProgress[0] = snapshot.getDocuments()
                            .get(0)
                            .toObject(GoalProgress.class);
                });

                final UserProfile[] user = new UserProfile[1];
                userProfileRepo.getUserByEmail(email, snapshot -> {
                    user[0] = snapshot.getDocuments().get(0).toObject(UserProfile.class);
                });
                GoalType goalType = user[0].getGoalType();
                final GoalProgress[] progress = new GoalProgress[1];

                Date startDate, endDate;

                switch (goalType) {
                    case WEEKLY:
                        startDate = getStartOfWeek();
                        endDate = getEndOfWeek();
                        goalProgressRepo.getWeeklyProgress(email, startDate, endDate, snapshot -> {
                            progress[0] = snapshot.getDocuments().get(0).toObject(GoalProgress.class);
                        });
                        break;
                    case MONTHLY:
                        startDate = getStartOfMonth();
                        endDate = getEndOfMonth();
                        goalProgressRepo.getMonthlyProgress(email, startDate, endDate, snapshot -> {
                            progress[0] = snapshot.getDocuments().get(0).toObject(GoalProgress.class);
                        });
                        break;
                    case DAILY:
                    default:
                        startDate = getTodayDateTruncated();
                        endDate = new Date();
                        goalProgressRepo.getTodayProgress(email, new Date(), snapshot -> {
                            progress[0] = snapshot.getDocuments().get(0).toObject(GoalProgress.class);
                        });
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
                    progress[0] = new GoalProgress(email, 0f, startDate, true, (int) defaultGoal);
                    goalProgressRepo.insertGoalProgress(progress[0]);
                }

                final GoalProgress finalProgress = progress[0];
                final int goalAmount = finalProgress.getGoalAmount();
                final int amountLogged = (int) finalProgress.getAmountLogged();
                final boolean onTarget = finalProgress.getOnTarget();

                requireActivity().runOnUiThread(() -> {
                    binding.goalAmountText.setText("Goal Amount: " + goalAmount + " L");
                    binding.goalTypeText.setText("Type: " + goalType);
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
        GoalProgressRepository goalProgressRepo = new GoalProgressRepository();
        UserProfileRepository userProfileRepo = new UserProfileRepository();

        if (todayGoal == null) return;

        executor.execute(() -> {
            final boolean[] onTarget = new boolean[1];
            goalProgressRepo.getTodayProgress(email, todayGoal.getProgressDate(), snapshot -> {
                if (!snapshot.isEmpty()) {
                    DocumentSnapshot doc = snapshot.getDocuments().get(0);
                    String docId = doc.getId();

                    onTarget[0] = todayGoal.getAmountLogged() <= todayGoal.getGoalAmount();
                    todayGoal.setOnTarget(onTarget[0]);

                    goalProgressRepo.updateGoalProgress(docId, todayGoal);
                }
            });

            final UserProfile[] user = new UserProfile[1];
            userProfileRepo.getUserByEmail(email, snapshot -> {
                user[0] = snapshot.getDocuments().get(0).toObject(UserProfile.class);
            });
            Date now = new Date();
            Date lastUpdate = user[0].getLastStreakUpdate();
            GoalType goalType = user[0].getGoalType();

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
                if (onTarget[0]) {
                    user[0].setStreak(user[0].getStreak() + 1);
                } else {
                    user[0].setStreak(0);
                }
                user[0].setLastStreakUpdate(now);
                userProfileRepo.updateUserProfile(user[0]);
            }

            int streakCount = user[0].getStreak();
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
            ChallengeProgressRepository challengeProgressRepo = new ChallengeProgressRepository();
            // Get all challenges completed by user
            final List<ChallengeProgress>[] completedChallenges = new List[]{new ArrayList<>()};
            challengeProgressRepo.getCompletedChallengesByUser(email, snapshot -> {
                for (DocumentSnapshot doc: snapshot) {
                    ChallengeProgress challengeProgress = doc.toObject(ChallengeProgress.class);
                    if (challengeProgress != null) completedChallenges[0].add(challengeProgress);
                }
            });
            int completedCount = completedChallenges[0] != null ? completedChallenges[0].size() : 0;

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
