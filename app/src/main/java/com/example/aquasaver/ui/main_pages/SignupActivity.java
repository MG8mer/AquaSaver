package com.example.aquasaver.ui.main_pages;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aquasaver.R;
import com.example.aquasaver.model.GoalProgress;
import com.example.aquasaver.model.UserProfile;
import com.example.aquasaver.model.enums.GoalType;
import com.example.aquasaver.repository.ChallengesRepository;
import com.example.aquasaver.repository.GoalProgressRepository;
import com.example.aquasaver.repository.SuggestionsRepository;
import com.example.aquasaver.repository.UserProfileRepository;
import com.example.aquasaver.ui.conservation_tips.ConservationTipsSeeder;
import com.example.aquasaver.challenges.ChallengeSeeder;
import com.example.aquasaver.model.Challenges;
import com.example.aquasaver.model.Suggestions;
import com.example.aquasaver.ui.main_pages.utility.PasswordUtils;

import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

import com.google.firebase.firestore.DocumentSnapshot;

public class SignupActivity extends AppCompatActivity {

    EditText email, password;
    Spinner goalSpinner, unitSpinner;
    Button signupSubmit;

    private SharedPreferences prefs;
    private UserProfileRepository userProfileRepo;
    private GoalProgressRepository goalProgressRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        prefs.edit().clear().apply();

        email = findViewById(R.id.signupEmail);
        password = findViewById(R.id.signupPassword);
        goalSpinner = findViewById(R.id.goalSpinner);
        unitSpinner = findViewById(R.id.unitsSpinner);
        signupSubmit = findViewById(R.id.signupSubmit);

        String[] goals = {"Daily", "Weekly", "Monthly"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, goals);
        goalSpinner.setAdapter(adapter);

        String[] units = {"Liters", "Gallons"};
        ArrayAdapter<String> unitAdap = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, units);
        unitSpinner.setAdapter(unitAdap);

        userProfileRepo = new UserProfileRepository();
        goalProgressRepo = new GoalProgressRepository();

        signupSubmit.setOnClickListener(v -> {
            String userEmail = email.getText().toString().trim();
            String rawPass = password.getText().toString().trim();

            if (userEmail.isEmpty() || rawPass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            String salt = PasswordUtils.generateSalt();
            String hashedPass = PasswordUtils.hashPassword(rawPass, salt);

            String selectedGoal = goalSpinner.getSelectedItem().toString();
            GoalType goalType = GoalType.DAILY; // default

            switch (selectedGoal.toLowerCase()) {
                case "daily":
                    goalType = GoalType.DAILY;
                    break;
                case "weekly":
                    goalType = GoalType.WEEKLY;
                    break;
                case "monthly":
                    goalType = GoalType.MONTHLY;
                    break;
            }

            Date joinDate = new Date();
            Date lastStreakUpdate = new Date();

            GoalType finalGoalType = goalType;
            Executors.newSingleThreadExecutor().execute(() -> {
                userProfileRepo.getUserProfilesByEmail(userEmail, snapshot -> {
                    if (!snapshot.isEmpty()) {
                        runOnUiThread(() -> Toast.makeText(SignupActivity.this,
                                "This email address has already been used!", Toast.LENGTH_SHORT).show());
                        return;
                    }

                    UserProfile newUser = new UserProfile(userEmail, hashedPass, salt, /* location = */ "", false, finalGoalType, false, "12:00 AM", false, joinDate, lastStreakUpdate);
                    userProfileRepo.insertUserProfile(newUser);

                    GoalProgress userGp;
                    if (finalGoalType == GoalType.DAILY) {
                        userGp = new GoalProgress(userEmail, 0, joinDate, true, 400, unitSpinner.getSelectedItem().toString());
                    } else if (finalGoalType == GoalType.WEEKLY) {
                        userGp = new GoalProgress(userEmail, 0, joinDate, true, 2800, unitSpinner.getSelectedItem().toString());
                    } else {
                        userGp = new GoalProgress(userEmail, 0, joinDate, true, 11200, unitSpinner.getSelectedItem().toString());
                    }
                    goalProgressRepo.insertGoalProgress(userGp);

                    runOnUiThread(() -> {
                        prefs.edit()
                                .putString("username", userEmail)
                                .putString("password", hashedPass)
                                .putString("goal", selectedGoal)
                                .putString("units", unitSpinner.getSelectedItem().toString())
                                .apply();

                        Toast.makeText(this, "Signup Successful!", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                });
            });
        });
    }
}
