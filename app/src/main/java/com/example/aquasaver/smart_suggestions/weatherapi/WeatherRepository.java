package com.example.aquasaver.smart_suggestions.weatherapi;

import android.content.Context;

import com.example.aquasaver.model.WeatherSuggestions;
import com.example.aquasaver.dao.WeatherSuggestionsDao;
import com.example.aquasaver.db.AppDatabase;

import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherRepository {
    private static WeatherSuggestionsDao suggestionsDao;
    private final AppDatabase db;
    private final Context context;

    public WeatherRepository(Context context) {
        this.context = context;
        this.db = AppDatabase.getInstance(context);
        this.suggestionsDao = db.weatherSuggestionsDao();
    }
    public interface WeatherDataCallback {
        void onSuccess(String weather);
        void onFailure(String error);
    }

    public static void getTodayWeather(String email, String location, WeatherDataCallback callback) {
        WeatherApi api = WeatherAPIClient.getWeatherApi();
        Call<WeatherResponse> call = api.getWeather(location, "74a0f3136d13e60fcdd50fd6fd9bb433", "metric");

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse data = response.body();
                    String suggestionText = "It’s " + data.main.temp + "°C with " + data.weather.get(0).description;
                    WeatherSuggestions suggestion = new WeatherSuggestions(email, location, getTodayDate(), suggestionText);

                    storeWeatherSuggestion(suggestion, callback);
                } else {
                    callback.onError("API error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
    private static void storeWeatherSuggestion(WeatherSuggestions suggestion, WeatherDataCallback callback) {
        new Thread(() -> {
            suggestionsDao.insertSuggestion(suggestion);
            callback.onWeatherDataLoaded(suggestion);
        }).start();
    }

    private static Date getTodayDate() {
        // implement date truncation if needed
        return new Date();
    }

    public interface WeatherDataCallback {
        void onWeatherDataLoaded(WeatherSuggestions suggestion);
        void onError(String errorMessage);
    }
}
