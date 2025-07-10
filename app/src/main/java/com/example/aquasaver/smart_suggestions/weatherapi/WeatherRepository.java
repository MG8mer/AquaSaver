package com.example.aquasaver.smart_suggestions.weatherapi;

import android.content.Context;
import android.util.Log;

import com.example.aquasaver.model.WeatherSuggestions;
import com.example.aquasaver.dao.WeatherSuggestionsDao;
import com.example.aquasaver.db.AppDatabase;
import com.example.aquasaver.BuildConfig;


import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherRepository {
    private final WeatherSuggestionsDao suggestionsDao;
    private final AppDatabase db;
    private final Context context;

    public WeatherRepository(Context context) {
        this.context = context;
        this.db = AppDatabase.getInstance(context);
        this.suggestionsDao = db.weatherSuggestionsDao();
    }

    public void getTodayWeather(String email, String location, WeatherDataCallback callback) {
        WeatherAPI api = WeatherAPIClient.getWeatherApi();
        Call<WeatherResponse> call = api.getWeather(location, BuildConfig.WEATHER_API_KEY, "metric");
        Log.d("WeatherRepo", "Calling weather API for: " + location);
        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse data = response.body();
                    String suggestionText = "It’s " + data.main.temp + "°C, " + data.weather.get(0).description;
                    WeatherSuggestions suggestion = new WeatherSuggestions(email, location, getTodayDate(), suggestionText);
                    Log.d("WeatherRepo", "API success: " + data.main.temp + ", " + data.weather.get(0).description);

                    storeWeatherSuggestion(suggestion, callback);
                } else {
                    callback.onFailure("API error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }

    private void storeWeatherSuggestion(WeatherSuggestions suggestion, WeatherDataCallback callback) {
        new Thread(() -> {
            suggestionsDao.insertSuggestion(suggestion);
            Log.d("DB_CHECK", "Weather suggestion stored successfully: " + suggestion.toString());
            callback.onSuccess(suggestion.toString());
            callback.onWeatherDataLoaded(suggestion);
        }).start();
    }

    private static Date getTodayDate() {
        return new Date();
    }

    public interface WeatherDataCallback {
        void onSuccess(String weather);
        void onWeatherDataLoaded(WeatherSuggestions suggestions);
        void onFailure(String error);
    }
}
