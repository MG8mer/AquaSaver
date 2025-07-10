package com.example.aquasaver.ui.activities;
import com.example.aquasaver.BuildConfig;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.aquasaver.db.AppDatabase;
import com.example.aquasaver.model.UserProfile;
import com.example.aquasaver.model.WeatherSuggestions;
import com.example.aquasaver.model.enums.GoalType;
import com.example.aquasaver.smart_suggestions.weatherapi.WeatherAPIClient;
import com.example.aquasaver.smart_suggestions.weatherapi.WeatherApi;
import com.example.aquasaver.smart_suggestions.weatherapi.WeatherResponse;

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

}
