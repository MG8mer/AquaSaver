package com.example.aquasaver.ui.main_pages;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.room.Room;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.ListenableWorker;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.aquasaver.ui.main_pages.workers.GoalProgressWorker;
import com.example.aquasaver.dao.UserProfileDao;
import com.example.aquasaver.db.AppDatabase;
import com.example.aquasaver.model.UserProfile;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import androidx.work.ListenableWorker.Result;
import com.example.aquasaver.model.enums.GoalType;
import com.example.aquasaver.ui.main_pages.workers.ReportsWorker;

public class AquaSaverApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        scheduleReportsWorker();
    }

    private void scheduleReportsWorker() {
        long initialDelay = calculateDelayToMidnight();
        PeriodicWorkRequest dailyReports = new PeriodicWorkRequest.Builder(
                ReportsWorker.class,
                1, TimeUnit.DAYS
        )
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(this)
                .enqueueUniquePeriodicWork(
                        "reports_worker",
                        ExistingPeriodicWorkPolicy.KEEP,
                        dailyReports
                );
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
}