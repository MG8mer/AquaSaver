// for auto creating GoalProgress entities

package com.example.aquasaver.ui.main_pages.workers;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.aquasaver.dao.GoalProgressDao;
import com.example.aquasaver.dao.UserProfileDao;
import com.example.aquasaver.db.AppDatabase;
import com.example.aquasaver.model.GoalProgress;
import com.example.aquasaver.model.UserProfile;

import java.util.Date;
import java.util.List;

public class GoalProgressWorker extends Worker
{
    private final AppDatabase db;
    private final GoalProgressDao goalProgressDao;
    private final UserProfileDao userProfileDao;

    public GoalProgressWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params
    ) {
        super(context, params);
        db = AppDatabase.getInstance(getApplicationContext());


        goalProgressDao = db.goalProgressDao();
        userProfileDao  = db.userProfileDao();
    }

    @NonNull
    @Override
    public Result doWork() {
        Context ctx = getApplicationContext();
        SharedPreferences prefs = ctx
                .getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
        String userEmail = prefs.getString("username", null);
        if (userEmail == null) return Result.failure();
        UserProfile user = userProfileDao.getUserByEmail(userEmail);
        if (user == null) {
            return Result.failure();
        }

        List<GoalProgress> history = goalProgressDao.getAllProgressForUser("example@gmail.com");
        int goalAmount = history.get(0).getGoalAmount();

        double amountLogged = 0;

        boolean onTarget = true;
        GoalProgress newProgress = new GoalProgress(
                userEmail,
                amountLogged,
                new Date(),
                onTarget,
                goalAmount
        );

        goalProgressDao.insertGoalProgress(newProgress);
        return Result.success();
    }
}
