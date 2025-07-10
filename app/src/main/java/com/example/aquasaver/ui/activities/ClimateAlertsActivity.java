package com.example.aquasaver.ui.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.aquasaver.R;
import com.example.aquasaver.ui.climate_alerts.ClimateAlertsViewModel;

public class ClimateAlertsActivity extends AppCompatActivity {
    private ClimateAlertsViewModel viewModel;
    private TextView tvSuggestions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_climate_alerts);  // ← create this layout!

        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);

        String email = prefs.getString("username", null);
        String location = prefs.getString("location", null);

        if (email != null && location != null) {
            // Proceed with your weather functions
            Log.d("ClimateAlerts", "Email: " + email + ", Location: " + location);
        } else {
            // Handle case where user data isn't available
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }

        tvSuggestions = findViewById(R.id.tvSuggestions);

        viewModel = new ViewModelProvider(this).get(ClimateAlertsViewModel.class);

        // Example of observing data from the ViewModel
        viewModel.getSuggestionsLiveData().observe(this, suggestions -> {
            if (suggestions != null) {
                tvSuggestions.setText(suggestions);
                Log.d("ClimateAlerts", "Suggestions loaded successfully");
            } else {
                tvSuggestions.setText("No suggestions available");
            }
        });

        // Trigger AI/weather logic here (e.g., fetch suggestions)
        viewModel.loadSmartSuggestions(email, location);
    }
}
