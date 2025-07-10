package com.example.aquasaver;

import android.Manifest;
import android.util.Log;
import android.annotation.SuppressLint;

import com.example.aquasaver.model.enums.GoalType;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.content.SharedPreferences;
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
import java.util.List;
import java.util.Date;
import java.util.Locale;
import com.example.aquasaver.dao.UserProfileDao;
import com.example.aquasaver.model.UserProfile;
import com.example.aquasaver.db.AppDatabase;
import androidx.room.Room;

public class SignupActivity extends AppCompatActivity {

    EditText email, password, locationField;
    Spinner goalSpinner;
    Switch notifications, weatherAlertSwitch, reminderTimeSwitch, locationTrackingSwitch;
    LinearLayout extraNotificationOptions;
    Button signupSubmit;

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_CODE = 1001;

    private SharedPreferences prefs;
    private AppDatabase db;
    private UserProfileDao userDao;

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
        locationTrackingSwitch = findViewById(R.id.locationTrackingSwitch);
        extraNotificationOptions = findViewById(R.id.extraNotificationOptions);
        signupSubmit = findViewById(R.id.signupSubmit);

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
        db = Room.databaseBuilder(
                getApplicationContext(),
                AppDatabase.class,
                "aqua_db"
        ).allowMainThreadQueries().build();

        userDao = db.userProfileDao();

        // Signup button click: validate and save profile

        signupSubmit.setOnClickListener(v -> {
            String userEmail = email.getText().toString().trim();
            String pass = password.getText().toString().trim();
            String loc = locationField.getText().toString().trim();
            String goal = goalSpinner.getSelectedItem().toString();
            boolean notify = notifications.isChecked();
            boolean weatherAlert = weatherAlertSwitch.isChecked();
            boolean reminder = reminderTimeSwitch.isChecked();
            boolean useGps = locationTrackingSwitch.isChecked();
            Date joinDate = new Date();

            String selected = goalSpinner.getSelectedItem().toString();
            GoalType goalType = null;

            switch (selected.toLowerCase()) {
                case "daily":
                    goalType = GoalType.DAILY;
                    break;
                case "weekly":
                    goalType = GoalType.WEEKLY;
                    break;
            }





            if (userEmail.isEmpty() || pass.isEmpty() || loc.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            } else {
                List<UserProfile> userProfiles = userDao.getUserProfilesByEmail(userEmail);
                if (userProfiles.size() > 0)
                {
                    Toast.makeText(this, "This email address has already been used! Try again.", Toast.LENGTH_SHORT).show();
                }
                else {
                    // Placeholders:
                    // reminderTime: "12:00 AM"
                    UserProfile newUser = new UserProfile(userEmail, pass, loc, useGps, goalType, notify, "12:00 AM", weatherAlert, joinDate);

                    userDao.insertUserProfile(newUser);
                    // Save profile data
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
                }
            }
        });
    }

    private void loadSavedProfile() {
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

    @SuppressLint("MissingPermission")
    private void getUserLocation() {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        getCityFromCoordinates(location);
                    }
                });
    }

    private void getCityFromCoordinates(Location userLocation) {
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
                if (country != null && !country.isEmpty()) fullLocation.append(", ").append(country);

                locationField.setText(!fullLocation.toString().isEmpty() ? fullLocation : "Unknown Location");
            }
        } catch (IOException e) {
            e.printStackTrace();
            locationField.setText("Location error");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
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
