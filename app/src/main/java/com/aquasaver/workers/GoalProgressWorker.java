// for auto creating GoalProgress entities

package com.aquasaver.workers;

import android.content.Context;

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
        db = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        "aqua_db"
                )
                .fallbackToDestructiveMigration()
                .build();

        goalProgressDao = db.goalProgressDao();
        userProfileDao  = db.userProfileDao();
    }

    @NonNull
    @Override
    public Result doWork() {
        UserProfile user = userProfileDao.getUserByEmail("bro@gmail.com"); // REPLACE "example@gmail.com" WITH ACTUAL LOGIC TO GET USER EMAIL
        if (user == null) {
            return Result.success();
        }

        List<GoalProgress> history = goalProgressDao.getAllProgressForUser("example@gmail.com");
        int goalAmount = history.get(0).getGoalAmount();

        double amountLogged = 0;

        boolean onTarget = true;
        GoalProgress newProgress = new GoalProgress(
                "bro@gmail.com",
                amountLogged,
                new Date(),
                onTarget,
                goalAmount
        );

        goalProgressDao.insertGoalProgress(newProgress);
        return Result.success();
    }
}
