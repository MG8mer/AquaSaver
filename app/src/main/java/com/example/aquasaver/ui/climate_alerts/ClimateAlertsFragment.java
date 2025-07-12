package com.example.aquasaver.ui.climate_alerts;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.aquasaver.R;
import com.example.aquasaver.databinding.FragmentClimateAlertsBinding;
import com.example.aquasaver.smart_suggestions.weatherapi.WeatherRepository;

import android.text.Html;

public class ClimateAlertsFragment extends Fragment {

    private FragmentClimateAlertsBinding binding;
    private ClimateAlertsViewModel viewModel;
    TextView locationText, temperatureText;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentClimateAlertsBinding.inflate(inflater, container, false);

        locationText = binding.locationText;
        temperatureText = binding.temperatureText;

        // Get user preferences
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
        String email = prefs.getString("username", null);
        String location = prefs.getString("location", null);

        if (email == null || location == null) {
            Toast.makeText(getContext(), "User not logged in or location missing", Toast.LENGTH_LONG).show();
            binding.textClimateAlerts.setText("Please log in and set your location.");
            Log.w("ClimateAlertsFragment", "Missing user info: email=" + email + ", location=" + location);
            return binding.getRoot();
        }

        locationText.setText(location);

        WeatherRepository repository = new WeatherRepository(requireContext());
        repository.getTodayWeather(email, location, new WeatherRepository.WeatherDataCallback() {
            @Override
            public void onSuccess(String weather) {
                requireActivity().runOnUiThread(() -> {
                    String[] parts = weather.split(",");
                    if (parts.length > 0) temperatureText.setText(parts[0]);
                    //if (parts.length > 1) updateWeatherIcon(parts[1].trim());
                });
            }

            @Override
            public void onFailure(String error) {
                Log.e("ClimateAlertsFragment", "Weather API Error: " + error);
                requireActivity().runOnUiThread(() ->
                        temperatureText.setText("N/A")
                );
            }
        });

        // Set up viewmodel
        viewModel = new ViewModelProvider(this).get(ClimateAlertsViewModel.class);
        viewModel.loadSmartSuggestions(requireContext(), email, location);
        viewModel.getSuggestionLiveData().observe(getViewLifecycleOwner(), suggestions -> {
            if (suggestions != null && !suggestions.isEmpty()) {
                String[] lines = suggestions.split("\\n");

                if (lines.length >= 2) {
                    binding.alertTitle1.setText("Alert 1");
                    binding.alertText1.setText(lines[0]);

                    binding.alertTitle2.setText("Alert 2");
                    binding.alertText2.setText(lines[1]);
                } else {
                    // Fallback if less than 2 lines
                    binding.alertText1.setText(lines[0]);
                    binding.alertText2.setText("No second suggestion.");
                }

                // Hide unused text view
                binding.textClimateAlerts.setVisibility(View.GONE);
            } else {
                binding.alertText1.setText("No suggestions available.");
                binding.alertText2.setText("");
            }
        });

        Context context = getContext();
        if (context != null) {
            Drawable drawable = ContextCompat.getDrawable(context, R.drawable.rounded_bg);
            if (drawable != null) {
                drawable = drawable.mutate();
                drawable.setTint(Color.parseColor("#DC2626"));

                binding.alertBox1.setBackground(drawable);
            }

            Drawable drawable1 = ContextCompat.getDrawable(context, R.drawable.rounded_bg);
            if (drawable1 != null) {
                drawable1 = drawable1.mutate();
                drawable1.setTint(Color.parseColor("#FBBF24"));
                binding.alertBox2.setBackground(drawable1);
            }
        }

        return binding.getRoot();
    }

    /*private void updateWeatherIcon(String description) {
        if (description.contains("clear")) {
            weatherIcon.setImageResource(R.drawable.ic_sunny);
        } else if (description.contains("cloud")) {
            weatherIcon.setImageResource(R.drawable.ic_partly_cloudy);
        } else if (description.contains("rain")) {
            weatherIcon.setImageResource(R.drawable.ic_rain);
        } else {
            weatherIcon.setImageResource(R.drawable.ic_weather_default);
        }
    } */

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
