package com.example.aquasaver.ui.climate_alerts;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

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

        locationText = binding.locationText; // Use binding references
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
        /*viewModel = new ViewModelProvider(this).get(ClimateAlertsViewModel.class);
        viewModel.loadSmartSuggestions(requireContext(), email, location);

        viewModel.getSuggestionLiveData().observe(getViewLifecycleOwner(), suggestions -> {
            if (suggestions != null && !suggestions.isEmpty()) {
                binding.textClimateAlerts.setText(Html.fromHtml(suggestions, Html.FROM_HTML_MODE_LEGACY));
            } else {
                binding.textClimateAlerts.setText("No suggestions available.");
            }
        }); */

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
