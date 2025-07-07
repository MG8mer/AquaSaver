package com.aquasaver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.*;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SignupActivity extends AppCompatActivity {

    EditText username, password, locationField;
    Spinner goalSpinner;
    Switch notifications, weatherAlertSwitch, reminderTimeSwitch;
    LinearLayout extraNotificationOptions;
    Button signupSubmit;

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_CODE = 1001;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        prefs.edit().clear().apply();

        username = findViewById(R.id.signupUsername);
        password = findViewById(R.id.signupPassword);
        locationField = findViewById(R.id.location);
        goalSpinner = findViewById(R.id.goalSpinner);
        notifications = findViewById(R.id.notifications);
        weatherAlertSwitch = findViewById(R.id.weatherAlertSwitch);
        reminderTimeSwitch = findViewById(R.id.reminderTimeSwitch);
        extraNotificationOptions = findViewById(R.id.extraNotificationOptions);
        signupSubmit = findViewById(R.id.signupSubmit);

        // Setup spinner
        String[] goals = {"Daily", "Weekly", "Monthly"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, goals);
        goalSpinner.setAdapter(adapter);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

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

        notifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            extraNotificationOptions.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        signupSubmit.setOnClickListener(v -> {
            String user = username.getText().toString();
            String pass = password.getText().toString();
            String loc = locationField.getText().toString();
            String goal = goalSpinner.getSelectedItem().toString();
            boolean notify = notifications.isChecked();
            boolean weatherAlert = weatherAlertSwitch.isChecked();
            boolean reminder = reminderTimeSwitch.isChecked();

            if (user.isEmpty() || pass.isEmpty() || loc.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                prefs.edit()
                        .putString("username", user)
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
        });
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
