// for auto creating GoalProgress entities

package com.example.aquasaver.ui.main_pages.workers;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.aquasaver.repository.GoalProgressRepository;
import com.example.aquasaver.repository.UserProfileRepository;
import com.example.aquasaver.model.GoalProgress;
import com.example.aquasaver.model.UserProfile;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GoalProgressWorker extends Worker
{
    private final GoalProgressRepository goalProgressRepo;
    private final UserProfileRepository userProfileRepo;

    public GoalProgressWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params
    ) {
        super(context, params);


        goalProgressRepo = new GoalProgressRepository();
        userProfileRepo  = new UserProfileRepository();
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
        if (user[0] == null) {
            return Result.failure();
        }
        final List<GoalProgress>[] history = new List[]{new ArrayList<>()};
        goalProgressRepo.getAllProgressForUser(user[0].getEmail(), snapshot -> {
            for (DocumentSnapshot doc : snapshot) {
                GoalProgress challenge = doc.toObject(GoalProgress.class);
                if (challenge != null) history[0].add(challenge);
            }
        });
        int goalAmount = history[0].get(0).getGoalAmount();

        double amountLogged = 0;

        boolean onTarget = true;
        GoalProgress newProgress = new GoalProgress(
                userEmail,
                amountLogged,
                new Date(),
                onTarget,
                goalAmount
        );

        goalProgressRepo.insertGoalProgress(newProgress);
        return Result.success();
    }
}
