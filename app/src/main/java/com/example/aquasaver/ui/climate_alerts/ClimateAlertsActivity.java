package com.example.aquasaver.ui.climate_alerts;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.aquasaver.R;

public class ClimateAlertsActivity extends AppCompatActivity {
    private ClimateAlertsViewModel viewModel;
    private TextView tvSuggestions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_climate_alerts);

        tvSuggestions = findViewById(R.id.tvSuggestions);

        // Get stored user info
        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        String email = prefs.getString("username", null);
        String location = prefs.getString("location", null);

        if (email == null || location == null) {
            Toast.makeText(this, "User not logged in or location missing", Toast.LENGTH_LONG).show();
            tvSuggestions.setText("Please log in and set your location.");
            Log.w("ClimateAlertsActivity", "Missing user info: email=" + email + ", location=" + location);
            return; // Stop here, can't load suggestions without user data
        }

        viewModel = new ViewModelProvider(this).get(ClimateAlertsViewModel.class);

        // Observe LiveData from ViewModel and update UI
        viewModel.getSuggestionLiveData().observe(this, suggestions -> {
            if (suggestions != null && !suggestions.isEmpty()) {
                tvSuggestions.setText(suggestions);
                Log.d("ClimateAlertsActivity", "Suggestions loaded");
            } else {
                tvSuggestions.setText("No suggestions available.");
                Log.d("ClimateAlertsActivity", "No suggestions received");
            }
        });

        // Load smart suggestions with valid user data
        viewModel.loadSmartSuggestions(email, location);
    }
}