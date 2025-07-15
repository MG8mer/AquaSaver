package com.example.aquasaver.ui.main_pages;

import android.app.Application;
import android.util.Log;

import androidx.room.Room;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.aquasaver.ui.main_pages.workers.GoalProgressWorker;
import com.example.aquasaver.dao.UserProfileDao;
import com.example.aquasaver.db.AppDatabase;
import com.example.aquasaver.model.UserProfile;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import com.example.aquasaver.model.enums.GoalType;

public class AquaSaverApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        scheduleGoalProgress();
    }

    private void scheduleGoalProgress() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            UserProfileDao userProfileDao = db.userProfileDao();

            String email = "bro@gmail.com";
            UserProfile user = userProfileDao.getUserByEmail(email); // REPLACE bro@gmail.com PROPER LOGIC TO OBTAIN USER EMAIL
            if (user == null) {
                Log.w("AquaSaverApp", "No user found for email “" + email + "”, skipping goal scheduling.");
                return;
            }
            String goalType = user.getGoalType().toString();

            if (goalType.equals("DAILY")) {
                long initialDelay = calculateDelayToMidnight();
                PeriodicWorkRequest dailyWork = new PeriodicWorkRequest.Builder(
                        GoalProgressWorker.class,
                        1, TimeUnit.DAYS
                )
                        .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                        .build();

                WorkManager.getInstance(this)
                        .enqueueUniquePeriodicWork(
                                "goal_progress_daily",
                                ExistingPeriodicWorkPolicy.KEEP,
                                dailyWork
                        );
            } else if (goalType.equals("WEEKLY")) {
                long weeklyDelay = calculateDelayToNextMonday();
                PeriodicWorkRequest weeklyWork = new PeriodicWorkRequest.Builder(
                        GoalProgressWorker.class,
                        7, TimeUnit.DAYS
                )
                        .setInitialDelay(weeklyDelay, TimeUnit.MILLISECONDS)
                        .build();

                WorkManager.getInstance(this)
                        .enqueueUniquePeriodicWork(
                                "goal_progress_weekly",
                                ExistingPeriodicWorkPolicy.KEEP,
                                weeklyWork
                        );
            }
            else {
                long monthlyDelay = calculateDelayToFirstOfNextMonth();
                OneTimeWorkRequest monthlyWork = new OneTimeWorkRequest.Builder(GoalProgressWorker.class)
                        .setInitialDelay(monthlyDelay, TimeUnit.MILLISECONDS)
                        .addTag("goal_progress_monthly")
                        .build();

                WorkManager.getInstance(this)
                        .enqueueUniqueWork(
                                "goal_progress_monthly",
                                ExistingWorkPolicy.REPLACE,
                                monthlyWork
                        );
            }
        }).start();
    }

    private long calculateDelayToMidnight() {
        Calendar now = Calendar.getInstance();
        Calendar nextMidnight = (Calendar) now.clone();
        nextMidnight.add(Calendar.DAY_OF_YEAR, 1);
        nextMidnight.set(Calendar.HOUR_OF_DAY, 0);
        nextMidnight.set(Calendar.MINUTE, 0);
        nextMidnight.set(Calendar.SECOND, 0);
        nextMidnight.set(Calendar.MILLISECOND, 0);
        return nextMidnight.getTimeInMillis() - now.getTimeInMillis();
    }

    private long calculateDelayToNextMonday() {
        Calendar now = Calendar.getInstance();
        Calendar nextMonday = (Calendar) now.clone();
        int dayOfWeek = now.get(Calendar.DAY_OF_WEEK);
        int daysUntilMonday = ((Calendar.MONDAY - dayOfWeek) + 7) % 7;
        if (daysUntilMonday == 0) {
            daysUntilMonday = 7;
        }
        nextMonday.add(Calendar.DAY_OF_YEAR, daysUntilMonday);
        nextMonday.set(Calendar.HOUR_OF_DAY, 0);
        nextMonday.set(Calendar.MINUTE, 0);
        nextMonday.set(Calendar.SECOND, 0);
        nextMonday.set(Calendar.MILLISECOND, 0);
        return nextMonday.getTimeInMillis() - now.getTimeInMillis();
    }
    private long calculateDelayToFirstOfNextMonth() {
        Calendar now = Calendar.getInstance();
        Calendar firstOfMonth = (Calendar) now.clone();
        firstOfMonth.set(Calendar.DAY_OF_MONTH, 1);
        firstOfMonth.set(Calendar.HOUR_OF_DAY, 0);
        firstOfMonth.set(Calendar.MINUTE, 0);
        firstOfMonth.set(Calendar.SECOND, 0);
        firstOfMonth.set(Calendar.MILLISECOND, 0);
        // advance to next month
        firstOfMonth.add(Calendar.MONTH, 1);
        return firstOfMonth.getTimeInMillis() - now.getTimeInMillis();
    }
}
