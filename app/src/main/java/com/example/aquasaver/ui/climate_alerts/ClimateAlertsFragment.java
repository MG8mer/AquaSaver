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
    TextView temperatureText;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentClimateAlertsBinding.inflate(inflater, container, false);
        temperatureText = binding.temperatureText;
        viewModel = new ViewModelProvider(this).get(ClimateAlertsViewModel.class);

        // Get user preferences
        SharedPreferences userPrefs = requireActivity().getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
        String email = userPrefs.getString("username", null);
        String location = userPrefs.getString("location", null);

        if (email == null || location == null) {
            Toast.makeText(getContext(), "User not logged in or location missing", Toast.LENGTH_LONG).show();
            binding.textClimateAlerts.setText("Please log in and set your location.");
            Log.w("ClimateAlertsFragment", "Missing user info: email=" + email + ", location=" + location);
            return binding.getRoot();
        }

        Log.d("CLIMATE", "Location before set: " + location);
        viewModel.setLocation(location);
        viewModel.getLocation().observe(getViewLifecycleOwner(), loc -> {
            Log.d("CLIMATE", "Observed location: " + loc);
            if (binding.locationText != null) {
                binding.locationText.setText(loc != null ? loc : "No location set");
            }
        });

        // Weather fetch logic
        SharedPreferences weatherPrefs = requireActivity().getSharedPreferences("weather_prefs", Context.MODE_PRIVATE);
        long lastFetch = weatherPrefs.getLong("last_fetch_time", 0);
        long now = System.currentTimeMillis();
        String cachedWeather = weatherPrefs.getString("last_weather", null);
        boolean shouldFetch = cachedWeather == null || (now - lastFetch) > (60 * 60 * 1000); // 1 hour or no cache
        Log.d("CLIMATE", "Should fetch: " + shouldFetch);
        Log.d("CLIMATE", "Now: " + now + ", Last fetch: " + lastFetch + ", Delta: " + (now - lastFetch));
        WeatherRepository repository = new WeatherRepository(requireContext());

        if (shouldFetch) {
            Log.d("CLIMATE", "Fetching new weather from API...");
            repository.getTodayWeather(email, location, new WeatherRepository.WeatherDataCallback() {
                @Override
                public void onSuccess(String weather) {
                    requireActivity().runOnUiThread(() -> {
                        Log.d("CLIMATE", "Weather API returned: " + weather);

                        String[] parts = weather.split(",");
                        if (parts.length > 0) {
                            temperatureText.setText(parts[0]);

                            SharedPreferences prefs = requireActivity().getSharedPreferences("weather_prefs", Context.MODE_PRIVATE);
                            prefs.edit()
                                    .putLong("last_fetch_time", System.currentTimeMillis())
                                    .putString("last_weather", weather)
                                    .apply();

                            Log.d("CLIMATE", "Weather cached: " + weather);
                        } else {
                            temperatureText.setText("N/A");
                            Log.w("CLIMATE", "Weather response malformed: " + weather);
                        }
                    });
                }

                @Override
                public void onFailure(String error) {
                    Log.e("ClimateAlertsFragment", "Weather API Error: " + error);
                    requireActivity().runOnUiThread(() -> temperatureText.setText("N/A"));
                }
            });
        } else {
            Log.d("CLIMATE", "Using cached weather");
            if (cachedWeather != null) {
                String[] parts = cachedWeather.split(",");
                if (parts.length > 0) {
                    temperatureText.setText(parts[0]);
                    Log.d("CLIMATE", "Loaded cached weather: " + cachedWeather);
                } else {
                    Log.w("CLIMATE", "Cached weather was invalid format: " + cachedWeather);
                    temperatureText.setText("N/A");
                }
            } else {
                Log.w("CLIMATE", "No cached weather found! Forcing fetch...");
                // Fallback to fetch
                repository.getTodayWeather(email, location, new WeatherRepository.WeatherDataCallback() {
                    @Override
                    public void onSuccess(String weather) {
                        requireActivity().runOnUiThread(() -> {
                            Log.d("CLIMATE", "Fallback Weather API returned: " + weather);

                            String[] parts = weather.split(",");
                            if (parts.length > 0) {
                                temperatureText.setText(parts[0]);

                                SharedPreferences prefs = requireActivity().getSharedPreferences("weather_prefs", Context.MODE_PRIVATE);
                                prefs.edit()
                                        .putLong("last_fetch_time", System.currentTimeMillis())
                                        .putString("last_weather", weather)
                                        .apply();

                                Log.d("CLIMATE", "Weather cached (fallback): " + weather);
                            } else {
                                temperatureText.setText("N/A");
                                Log.w("CLIMATE", "Fallback weather response malformed: " + weather);
                            }
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e("ClimateAlertsFragment", "Fallback Weather API Error: " + error);
                        requireActivity().runOnUiThread(() -> temperatureText.setText("N/A"));
                    }
                });
            }
        }

        // Smart Suggestions Logic
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
                    binding.alertText1.setText(lines[0]);
                    binding.alertText2.setText("No second suggestion.");
                }

                binding.textClimateAlerts.setVisibility(View.GONE);
            } else {
                binding.alertText1.setText("No suggestions available.");
                binding.alertText2.setText("");
            }
        });

        // UI styling for alert boxes
        Context context = getContext();
        if (context != null) {
            Drawable redBox = ContextCompat.getDrawable(context, R.drawable.rounded_bg);
            if (redBox != null) {
                redBox = redBox.mutate();
                redBox.setTint(Color.parseColor("#DC2626"));
                binding.alertBox1.setBackground(redBox);
            }

            Drawable yellowBox = ContextCompat.getDrawable(context, R.drawable.rounded_bg);
            if (yellowBox != null) {
                yellowBox = yellowBox.mutate();
                yellowBox.setTint(Color.parseColor("#FBBF24"));
                binding.alertBox2.setBackground(yellowBox);
            }
        }

        return binding.getRoot();
    }

    private boolean shouldFetchWeather(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE);
        long lastFetchTime = prefs.getLong("last_fetch_time", 0);
        long oneHourMillis = 60 * 60 * 1000; // 1 hour
        return (System.currentTimeMillis() - lastFetchTime) > oneHourMillis;
    }

    private void updateLastFetchTime(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE);
        prefs.edit().putLong("last_fetch_time", System.currentTimeMillis()).apply();
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
