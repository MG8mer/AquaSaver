package com.example.aquasaver.ui.main_pages.workers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.aquasaver.model.WaterUsage;
import com.example.aquasaver.repository.ChallengeProgressRepository;
import com.example.aquasaver.repository.ChallengesRepository;
import com.example.aquasaver.repository.GoalProgressRepository;
import com.example.aquasaver.model.ChallengeProgress;
import com.example.aquasaver.model.Challenges;
import com.example.aquasaver.model.enums.GoalType;
import com.example.aquasaver.model.enums.SummaryType;

import com.example.aquasaver.repository.ReportsRepository;
import com.example.aquasaver.repository.UserProfileRepository;
import com.example.aquasaver.repository.WaterUsageRepository;
import com.example.aquasaver.model.GoalProgress;
import com.example.aquasaver.model.Reports;
import com.example.aquasaver.model.UserProfile;
import com.example.aquasaver.ui.home.HomeFragment;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
public class ReportsWorker extends Worker {
    private final ReportsRepository reportsRepo;
    private final WaterUsageRepository waterUsageRepo;
    private final UserProfileRepository userProfileRepo;

    private final GoalProgressRepository goalProgressRepo;
    private final ChallengesRepository challengesRepo;
    private final ChallengeProgressRepository challengeProgressRepo;
    private UserProfile user;

    public ReportsWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params
    ) {
        super(context, params);
        reportsRepo = new ReportsRepository();
        waterUsageRepo = new WaterUsageRepository();
        userProfileRepo = new UserProfileRepository();
        goalProgressRepo = new GoalProgressRepository();
        challengesRepo = new ChallengesRepository();
        challengeProgressRepo = new ChallengeProgressRepository();
    }

    @NonNull
    @Override
    public Result doWork() {
        Context ctx = getApplicationContext();
        SharedPreferences prefs = ctx
                .getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
        String userEmail = prefs.getString("username", null);
        if (userEmail == null) return Result.failure();
        final UserProfile[] user = new UserProfile[1];
        userProfileRepo.getUserByEmail(userEmail, snapshot -> {
            if (!snapshot.isEmpty()) {
                user[0] = snapshot.getDocuments().get(0).toObject(UserProfile.class);
            }
        });
        if (user == null) {
            return Result.failure();
        }

        Calendar now = Calendar.getInstance();

        // Daily reports
        generateDailyReport(userEmail);

        // Weekly report every Monday
        if (now.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
            generateWeeklyReport(userEmail);
        }

        // Monthly report on the 1st of the month
        if (now.get(Calendar.DAY_OF_MONTH) == 1) {
            generateMonthlyReport(userEmail);
        }

        enqueueGoalProgressWorker();

        return Result.success();
    }

    private void generateDailyReport(String userEmail) {
        long[] window = HomeFragment.computeTodayWindow();
        insertReport(userEmail, SummaryType.DAILY, window);
    }

    private void generateWeeklyReport(String userEmail) {
        long[] window = HomeFragment.computeCurrentWeekWindow();
        insertReport(userEmail, SummaryType.WEEKLY, window);
    }

    private void generateMonthlyReport(String userEmail) {
        long[] window = HomeFragment.computeCurrentMonthWindow();
        insertReport(userEmail, SummaryType.MONTHLY, window);
    }

    private void insertReport(String userEmail, SummaryType type, long[] window) {
        int streakCount;
        int challengesCompleted;
        float[] totalLiters = new float[1];
        waterUsageRepo.getLitersUsedBetween(userEmail, window[0], window[1], snapshot ->{
            if (!snapshot.isEmpty()) {
                WaterUsage usage = snapshot.getDocuments().get(0).toObject(WaterUsage.class);
                if (usage != null)
                {
                    totalLiters[0] = (float)usage.getAmountLiters();
                }
            }
        });
        GoalType goalType = user.getGoalType();

        final List<Reports>[] userReports = new List[]{new ArrayList<>()};
        reportsRepo.getUserReports(userEmail, snapshot -> {
            for (DocumentSnapshot doc : snapshot) {
                Reports report = doc.toObject(Reports.class);
                if (report != null) userReports[0].add(report);
            }
        });

        if (userReports[0].isEmpty() || userReports[0].get(0).getSummaryType() != type)
        {
            streakCount = 0;
        }
        else {
            final List<GoalProgress>[] goalProgressList = new List[]{new ArrayList<>()};
            goalProgressRepo.getAllProgressForUser(userEmail, snapshot -> {
                for (DocumentSnapshot doc : snapshot) {
                    GoalProgress goalProgress = doc.toObject(GoalProgress.class);
                    if (goalProgress != null) goalProgressList[0].add(goalProgress);
                }
            });
            GoalProgress recent = goalProgressList[0].get(0);
            streakCount = userReports[0].get(0).getStreakCount();

            if (recent.getOnTarget())
            {
                streakCount++;
            }
        }

        final List<Challenges>[] challenges = new List[]{new ArrayList<>()};
        if (type == SummaryType.DAILY)
        {
            challengesRepo.getOneDayChallengesBeforeDate(userEmail, window[1], snapshot -> {
                for (DocumentSnapshot doc : snapshot) {
                    Challenges challenge = doc.toObject(Challenges.class);
                    if (challenge != null) challenges[0].add(challenge);
                }
            });
        }
        else if (type == SummaryType.WEEKLY)
        {
            challengesRepo.getWeeklyChallengesBeforeDate(userEmail, window[1], snapshot -> {
                for (DocumentSnapshot doc : snapshot) {
                    Challenges challenge = doc.toObject(Challenges.class);
                    if (challenge != null) challenges[0].add(challenge);
                }
            });
        }
        else {
            challengesRepo.getMonthlyChallengesBeforeDate(userEmail, window[1], snapshot -> {
                for (DocumentSnapshot doc : snapshot) {
                    Challenges challenge = doc.toObject(Challenges.class);
                    if (challenge != null) challenges[0].add(challenge);
                }
            });
        }


        List<ChallengeProgress> challengeProgresses = new ArrayList<ChallengeProgress>();

        for (int i = 0; i < challenges[0].size(); i++)
        {
            final ChallengeProgress[] challengeProgress = new ChallengeProgress[1];
            challengeProgressRepo.getChallengeByTitle(challenges[0].get(i).getTitle(), snapshot -> {
                if (!snapshot.isEmpty())
                {
                    challengeProgress[0] = snapshot.getDocuments().get(0).toObject(ChallengeProgress.class);
                }
            });
            if (challengeProgress[0].isCompletion())
            {
                challengeProgresses.add(challengeProgress[0]);
            }
        }
        challengesCompleted = challengeProgresses.size();

        Reports report = new Reports(
                userEmail,
                type,
                new Date(window[0]),
                new Date(window[1]),
                (int) totalLiters[0],
                challengesCompleted,
                streakCount
        );
        reportsRepo.insertReport(report);
    }

    private void enqueueGoalProgressWorker() {
        GoalType goalType = user.getGoalType();
        Calendar now = Calendar.getInstance();
        boolean shouldRun = false;

        switch (goalType) {
            case DAILY:
                shouldRun = true; // Always run daily
                break;

            case WEEKLY:
                // Only run every Monday
                shouldRun = (now.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY);
                break;

            case MONTHLY:
                // Only run on the first day of the month
                shouldRun = (now.get(Calendar.DAY_OF_MONTH) == 1);
                break;
        }

        if (shouldRun) {
            OneTimeWorkRequest goalProgressWork =
                    new OneTimeWorkRequest.Builder(GoalProgressWorker.class).build();

            WorkManager.getInstance(getApplicationContext())
                    .enqueue(goalProgressWork);

            Log.d("ReportsWorker", "GoalProgressWorker enqueued for goal type: " + goalType);
        } else {
            Log.d("ReportsWorker", "GoalProgressWorker skipped today for goal type: " + goalType);
        }
    }
}
