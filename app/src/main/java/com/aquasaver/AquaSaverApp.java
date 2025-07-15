package com.aquasaver;

import android.app.Application;

import androidx.room.Room;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.aquasaver.workers.GoalProgressWorker;
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
            AppDatabase db = Room.databaseBuilder(
                    getApplicationContext(),
                    AppDatabase.class,
                    "aqua_db"
            ).fallbackToDestructiveMigration().build();
            UserProfileDao userProfileDao = db.userProfileDao();

            UserProfile user = userProfileDao.getUserByEmail("bro@gmail.com"); // REPLACE bro@gmail.com PROPER LOGIC TO OBTAIN USER EMAIL
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
            } else {
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
}
