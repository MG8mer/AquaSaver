package com.example.aquasaver.ui.activities;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.aquasaver.db.AppDatabase;
import com.example.aquasaver.entities.UserProfile;
import com.example.aquasaver.entities.WeatherSuggestions;
import com.example.aquasaver.entities.enums.GoalType;
import com.example.aquasaver.weatherapi.RetrofitClient;
import com.example.aquasaver.weatherapi.WeatherApi;
import com.example.aquasaver.weatherapi.WeatherResponse;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainViewModel extends AndroidViewModel {

    private final AppDatabase db;

    public MainViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getInstance(application); // Singleton pattern
    }

    public void fetchWeather(String location) {
        WeatherApi api = RetrofitClient.getWeatherApi();
        String apiKey = "74a0f3136d13e60fcdd50fd6fd9bb433";

        Call<WeatherResponse> call = api.getWeather(location, apiKey, "metric");

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse data = response.body();

                    String suggestionText = data.main.temp + "°C with " + data.weather.get(0).description + ". Consider reducing outdoor water usage.";
                    WeatherSuggestions suggestion = new WeatherSuggestions("user@example.com", location, new Date(), suggestionText);
                    new Thread(() -> {
                        try {
                            if (!db.userProfileDao().doesProfileExist("user@example.com")) {
                                UserProfile user = new UserProfile("user@example.com", "password", "Denver", false, GoalType.DAILY, true, "daily", true, new Date());
                                db.userProfileDao().insertUserProfile(user);
                            }
                            db.weatherSuggestionsDao().insertSuggestion(suggestion);

                            List<WeatherSuggestions> suggestions = db.weatherSuggestionsDao().getSuggestionsForUser("user@example.com");
                            for (WeatherSuggestions s : suggestions) {
                                Log.d("DB_CHECK", s.date + " in " + s.location + ": " + s.usageSuggestionText);
                            }

                        } catch (Exception e) {
                            Log.e("DB_ERROR", "Database error", e);
                        }
                    }).start();

                } else {
                    try {
                        Log.e("WeatherAPI", "Error: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Log.e("WeatherAPI", "Failed to fetch weather", t);
            }
        });
    }
}
