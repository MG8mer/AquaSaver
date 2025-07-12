package com.example.aquasaver.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.*;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.example.aquasaver.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SignupActivity extends AppCompatActivity {

    EditText username, password, locationField;
    Spinner goalSpinner;
    Switch notifications;
    Button signupSubmit;

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_CODE = 1001;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Link UI elements
        username = findViewById(R.id.signupEmail);
        password = findViewById(R.id.signupPassword);
        locationField = findViewById(R.id.location);
        goalSpinner = findViewById(R.id.goalSpinner);
        notifications = findViewById(R.id.notifications);
        signupSubmit = findViewById(R.id.signupSubmit);

        // Set Spinner options
        String[] goals = {"Daily", "Weekly", "Monthly"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, goals);
        goalSpinner.setAdapter(adapter);

        // Location tracking setup
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize SharedPreferences
        prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);

        // Load saved profile if exists
        loadSavedProfile();

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

        // Signup button click: validate and save profile
        signupSubmit.setOnClickListener(v -> {
            String user = username.getText().toString();
            String pass = password.getText().toString();
            String loc = locationField.getText().toString();
            String goal = goalSpinner.getSelectedItem().toString();
            boolean notify = notifications.isChecked();

            if (user.isEmpty() || pass.isEmpty() || loc.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                // Save profile data
                prefs.edit()
                        .putString("username", user)
                        .putString("password", pass)  // Note: Plain text password storage is insecure!
                        .putString("location", loc)
                        .putString("goal", goal)
                        .putString("goal", goal)
                        .putString("goal", goal)
                        .putBoolean("notifications", notify)
                        .apply();

                String summary = "User: " + user +
                        "\nGoal: " + goal +
                        "\nNotifications: " + (notify ? "On" : "Off");

                Toast.makeText(this, "Signup Successful\n" + summary, Toast.LENGTH_LONG).show();
                finish(); // Close activity or navigate as needed
            }
        });
    }

    private void loadSavedProfile() {
        String savedUsername = prefs.getString("username", "");
        String savedPassword = prefs.getString("password", "");
        String savedLocation = prefs.getString("location", "");
        String savedGoal = prefs.getString("goal", "Daily");
        boolean savedNotifications = prefs.getBoolean("notifications", false);

        username.setText(savedUsername);
        password.setText(savedPassword);
        locationField.setText(savedLocation);
        notifications.setChecked(savedNotifications);

        ArrayAdapter<String> adapter = (ArrayAdapter<String>) goalSpinner.getAdapter();
        int spinnerPosition = adapter.getPosition(savedGoal);
        if (spinnerPosition >= 0) {
            goalSpinner.setSelection(spinnerPosition);
        }
    }

    @SuppressLint("MissingPermission")
    private void getUserLocation() {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(this, userLocation -> {
                    if (userLocation != null) {
                        getCityFromCoordinates(userLocation);
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
                if (city == null || city.isEmpty()) {
                    city = address.getSubAdminArea();
                }
                if (city == null || city.isEmpty()) {
                    city = address.getAdminArea();
                }
                locationField.setText(city != null ? city : "Unknown City");
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
