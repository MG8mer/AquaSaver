package com.example.aquasaver.ui.main_pages;

import android.Manifest;
import android.annotation.SuppressLint;

import com.example.aquasaver.challenges.ChallengeSeeder;
import com.example.aquasaver.repository.ChallengesRepository;
import com.example.aquasaver.repository.GoalProgressRepository;
import com.example.aquasaver.repository.SuggestionsRepository;
import com.example.aquasaver.model.Challenges;
import com.example.aquasaver.model.GoalProgress;
import com.example.aquasaver.model.Suggestions;
import com.example.aquasaver.repository.UserProfileRepository;
import com.example.aquasaver.ui.conservation_tips.ConservationTipsSeeder;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.content.SharedPreferences;
import android.util.Log;
import android.content.pm.PackageManager;
import android.location.Address;
import android.util.Patterns;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch; // import Switch
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.example.aquasaver.model.UserProfile;

import java.util.concurrent.Executors;

import com.example.aquasaver.model.enums.GoalType;
import com.example.aquasaver.R;
import com.google.firebase.firestore.DocumentSnapshot;


public class SignupActivity extends AppCompatActivity {

    EditText email, password, locationField;
    Spinner goalSpinner;
    Switch notifications, weatherAlertSwitch, reminderTimeSwitch, locationTrackingSwitch;
    LinearLayout extraNotificationOptions;
    Button signupSubmit;

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_CODE = 1001;

    private SharedPreferences prefs;
    private UserProfileRepository userProfileRepo;
    private GoalProgressRepository goalProgressRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        prefs.edit().clear().apply();

        // Link UI elements
        email = findViewById(R.id.signupEmail);
        password = findViewById(R.id.signupPassword);
        locationField = findViewById(R.id.location);
        goalSpinner = findViewById(R.id.goalSpinner);
        notifications = findViewById(R.id.notifications);
        weatherAlertSwitch = findViewById(R.id.weatherAlertSwitch);
        reminderTimeSwitch = findViewById(R.id.reminderTimeSwitch);
        extraNotificationOptions = findViewById(R.id.extraNotificationOptions);
        signupSubmit = findViewById(R.id.signupSubmit);
        locationTrackingSwitch = findViewById(R.id.locationTrackingSwitch);

        // Setup spinner
        String[] goals = {"Daily", "Weekly", "Monthly"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, goals);
        goalSpinner.setAdapter(adapter);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Location tracking toggle logic
        locationTrackingSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            LOCATION_PERMISSION_CODE
                    );
                } else {
                    getUserLocation();
                }
                locationField.setEnabled(false);
            } else {
                locationField.setText("");
                locationField.setEnabled(true);
            }
        });

        notifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            extraNotificationOptions.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        userProfileRepo = new UserProfileRepository();
        goalProgressRepo = new GoalProgressRepository();

        // Signup button click: validate and save profile

        signupSubmit.setOnClickListener(v -> {
            String userEmail = email.getText().toString().trim();
            String pass = password.getText().toString().trim();
            String loc = locationField.getText().toString().trim();
            String goal = goalSpinner.getSelectedItem().toString();
            boolean notify = notifications.isChecked();
            boolean useGps = locationTrackingSwitch.isChecked();
            boolean weatherAlert = weatherAlertSwitch.isChecked();
            boolean reminder = reminderTimeSwitch.isChecked();
            Date joinDate = new Date();
            Date lastStreakUpdate = new Date();

            String selected = goalSpinner.getSelectedItem().toString();
            GoalType goalType = null;

            switch (selected.toLowerCase()) {
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

            if (userEmail.isEmpty() || pass.isEmpty() || loc.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            } else {
                GoalType finalGoalType = goalType;
                Executors.newSingleThreadExecutor().execute(() -> {
                    final List<UserProfile>[] existing = new List[]{new ArrayList<>()};
                    userProfileRepo.getUserProfilesByEmail(userEmail, snapshot -> {
                        for (DocumentSnapshot doc : snapshot) {
                            UserProfile userProfile = doc.toObject(UserProfile.class);
                            if (userProfile != null) existing[0].add(userProfile);
                        }

                        // You can now use challenges[0] here
                    });

                    if (existing[0].size() > 0) {
                        runOnUiThread(() ->
                                Toast.makeText(
                                        SignupActivity.this,
                                        "This email address has already been used!",
                                        Toast.LENGTH_SHORT
                                ).show()
                        );
                        return;
                    } else {
                        // Placeholders:
                        // reminderTime: "12:00 AM"
                        UserProfile newUser = new UserProfile(userEmail, pass, loc, useGps, finalGoalType, notify, "12:00 AM", weatherAlert, joinDate, lastStreakUpdate);
                        GoalProgress userGp;
                        if (finalGoalType == GoalType.DAILY) {
                            userGp = new GoalProgress(userEmail, 0, joinDate, true, 400);
                        } else if (finalGoalType == GoalType.WEEKLY) {
                            userGp = new GoalProgress(userEmail, 0, joinDate, true, 2800);
                        } else {
                            userGp = new GoalProgress(userEmail, 0, joinDate, true, 11200);
                        }

                        userProfileRepo.insertUserProfile(newUser);
                        goalProgressRepo.insertGoalProgress(userGp);

                        ChallengesRepository challengesRepo = new ChallengesRepository();
                        final int[] count = new int[1];

                        challengesRepo.countChallengesForUser(userEmail, snapshot -> {
                            count[0] = snapshot.size();
                        });

                        if (count[0] == 0) {
                            List<Challenges> challenges = ChallengeSeeder.getDefaultChallenges(userEmail);
                            try {
                                for (Challenges challenge : challenges) {
                                    challengesRepo.insertChallenge(challenge);
                                    Log.d("SignupActivity", "Inserted challenge: " + challenge.getTitle());
                                }
                            } catch (Exception e) {
                                Log.e("SignupActivity", "Error inserting challenges for user: " + userEmail, e);
                            }
                            final int[] countAfter = new int[1];

                            challengesRepo.countChallengesForUser(userEmail, snapshot -> {
                                countAfter[0] = snapshot.size();
                            });
                            Log.d("SignupActivity", "Challenges count after insert: " + countAfter[0]);
                        }

                        Log.d("UserRegistration", "User created and challenges seeded for: " + userEmail);
                        SuggestionsRepository suggestionsDao = new SuggestionsRepository();
                        List<Suggestions> suggestions = ConservationTipsSeeder.getConservationTips(userEmail);
                        for (Suggestions suggestion : suggestions) {
                            suggestionsDao.insertSuggestion(suggestion);
                            Log.d("SignupActivity", "Inserted suggestion: " + suggestion.getTitle());
                        }
                    }
                    runOnUiThread(() -> {
                        prefs.edit()
                                .putString("username", userEmail)
                                .putString("password", pass)
                                .putString("location", loc)
                                .putString("goal", goal)
                                .putBoolean("notifications", notify)
                                .putBoolean("weatherAlert", weatherAlert)
                                .putBoolean("reminderTime", reminder)
                                .apply();

                        Toast.makeText(this, "Signup Successful!", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                });
            }
        });
    }


    @SuppressLint("MissingPermission")
    private void getUserLocation () {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        getCityFromCoordinates(location);
                    }
                });
    }

    private void loadSavedProfile () {
        String savedUsername = prefs.getString("username", "");
        String savedPassword = prefs.getString("password", "");
        String savedLocation = prefs.getString("location", "");
        String savedGoal = prefs.getString("goal", "Daily");
        boolean savedNotifications = prefs.getBoolean("notifications", false);

        email.setText(savedUsername);
        password.setText(savedPassword);
        locationField.setText(savedLocation);
        notifications.setChecked(savedNotifications);

        if (goalSpinner.getAdapter() != null) { // Handle the case of nullPointerException
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) goalSpinner.getAdapter();
            int spinnerPosition = adapter.getPosition(savedGoal);
            if (spinnerPosition >= 0) {
                goalSpinner.setSelection(spinnerPosition);
            }
        }
    }

    private void getCityFromCoordinates (Location userLocation){
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(
                    userLocation.getLatitude(),
                    userLocation.getLongitude(),
                    1
            );
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String city = address.getLocality();
                if (city == null || city.isEmpty()) city = address.getSubAdminArea();
                if (city == null || city.isEmpty()) city = address.getAdminArea();

                String state = address.getAdminArea();
                String country = address.getCountryName();

                StringBuilder fullLocation = new StringBuilder();
                if (city != null && !city.isEmpty()) fullLocation.append(city);
                if (state != null && !state.isEmpty()) fullLocation.append(", ").append(state);
                if (country != null && !country.isEmpty())
                    fullLocation.append(", ").append(country);

                locationField.setText(!fullLocation.toString().isEmpty() ? fullLocation : "Unknown Location");
            }
        } catch (IOException e) {
            e.printStackTrace();
            locationField.setText("Location error");
        }
    }
    @Override
    public void onRequestPermissionsResult ( int requestCode, @NonNull String[] permissions,
                                             @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}