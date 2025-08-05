package com.example.aquasaver.ui.main_pages.workers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

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
import com.example.aquasaver.db.AppDatabase;
import com.example.aquasaver.model.GoalProgress;
import com.example.aquasaver.model.Reports;
import com.example.aquasaver.model.UserProfile;
import com.example.aquasaver.ui.home.HomeFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
public class ReportsWorker extends Worker {
    private final AppDatabase db;
    private final ReportsRepository reportsDao;
    private final WaterUsageRepository waterUsageDao;
    private final UserProfileRepository userProfileDao;

    private final GoalProgressRepository goalProgressDao;
    private final ChallengesRepository challengesDao;
    private final ChallengeProgressRepository challengeProgressDao;
    private UserProfile user;

    public ReportsWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params
    ) {
        super(context, params);
        db = AppDatabase.getInstance(getApplicationContext());
        reportsDao = db.reportsDao();
        waterUsageDao = db.waterUsageDao();
        userProfileDao = db.userProfileDao();
        goalProgressDao = db.goalProgressDao();
        challengesDao = db.challengesDao();
        challengeProgressDao = db.challengeProgressDao();
    }

    @NonNull
    @Override
    public Result doWork() {
        Context ctx = getApplicationContext();
        SharedPreferences prefs = ctx
                .getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
        String userEmail = prefs.getString("username", null);
        if (userEmail == null) return Result.failure();
        user = userProfileDao.getUserByEmail(userEmail);
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
        float totalLiters = waterUsageDao.getLitersUsedBetween(userEmail, window[0], window[1]);
        GoalType goalType = user.getGoalType();
        List<Reports> userReports = reportsDao.getUserReports(userEmail);

        if (userReports.isEmpty() || userReports.get(0).getSummaryType() != type)
        {
            streakCount = 0;
        }
        else {
            List<GoalProgress> goalProgressList = goalProgressDao.getAllProgressForUser(userEmail);
            GoalProgress recent = goalProgressList.get(0);
            streakCount = userReports.get(0).getStreakCount();

            if (recent.getOnTarget())
            {
                streakCount++;
            }
        }

        List<Challenges> challenges;
        if (type == SummaryType.DAILY)
        {
            challenges = challengesDao.getOneDayChallengesBeforeDate(userEmail, window[1]);
        }
        else if (type == SummaryType.WEEKLY)
        {
            challenges = challengesDao.getWeeklyChallengesBeforeDate(userEmail, window[1]);

        }
        else {
            challenges = challengesDao.getMonthlyChallengesBeforeDate(userEmail, window[1]);
        }


        List<ChallengeProgress> challengeProgresses = new ArrayList<ChallengeProgress>();

        for (int i = 0; i < challenges.size(); i++)
        {
            ChallengeProgress challengeProgress = challengeProgressDao.getChallengeByTitle(challenges.get(i).getTitle());
            if (challengeProgress.isCompletion())
            {
                challengeProgresses.add(challengeProgress);
            }
        }
        challengesCompleted = challengeProgresses.size();

        Reports report = new Reports(
                userEmail,
                type,
                new Date(window[0]),
                new Date(window[1]),
                (int) totalLiters,
                challengesCompleted,
                streakCount
        );
        reportsDao.insertReport(report);
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
