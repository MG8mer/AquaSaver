package com.example.aquasaver.ui.climate_alerts;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.aquasaver.BuildConfig;
import com.example.aquasaver.dao.UserProfileDao;
import com.example.aquasaver.db.AppDatabase;
import com.example.aquasaver.model.UserProfile;
import com.example.aquasaver.model.WeatherSuggestions;
import com.example.aquasaver.smart_suggestions.ai_logic.ChatRequest;
import com.example.aquasaver.smart_suggestions.ai_logic.ChatResponse;
import com.example.aquasaver.smart_suggestions.ai_logic.OpenAIService;
import com.example.aquasaver.smart_suggestions.weatherapi.WeatherRepository;
import com.example.aquasaver.smart_suggestions.weatherapi.WeatherRepository.WeatherDataCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ClimateAlertsViewModel extends ViewModel {

    private final MutableLiveData<String> suggestionLiveData = new MutableLiveData<>();

    private final MutableLiveData<String> locationLiveData = new MutableLiveData<>();
    private final OpenAIService openAIService;




    public ClimateAlertsViewModel() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openai.com/")  //
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient.Builder()
                        .addInterceptor(chain -> {
                            Request original = chain.request();
                            Request request = original.newBuilder()
                                    .header("Authorization", "Bearer " + BuildConfig.OPENAI_API_KEY)
                                    .header("Content-Type", "application/json")
                                    .method(original.method(), original.body())
                                    .build();
                            return chain.proceed(request);
                        })
                        .build())
                .build();

        openAIService = retrofit.create(OpenAIService.class);
    }
    private static Date getTodayDateTruncated() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public LiveData<String> getSuggestionLiveData() {
        return suggestionLiveData;
    }


    public LiveData<String> getLocation() {
        return locationLiveData;
    }

    public void setLocation(String location) {
        if(location!=null) {
            String city = null;
            if (location != null) {
                String[] parts = location.split(",");
                if (parts.length > 0) {
                    city = parts[0].trim();  // Usually the city or street — depends on format
                }
            }

            locationLiveData.setValue(city + ": ");
        } else {
            locationLiveData.setValue("No location set");
        }

    }

    public interface SuggestionCallback {
        void onResult(String suggestion);
    }

    public void fetchSuggestion(String weatherInfo, SuggestionCallback callback) {
        List<ChatRequest.Message> messages = new ArrayList<>();
        messages.add(new ChatRequest.Message("system", "You are an expert in water conservation."));
        messages.add(new ChatRequest.Message("user", weatherInfo));

        ChatRequest request = new ChatRequest(messages);

        openAIService.getChatResponse(request).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().choices.isEmpty()) {
                    String suggestion = response.body().choices.get(0).message.content;
                    Log.d("OpenAI", "API call successful - Suggestion: " + suggestion);
                    callback.onResult(suggestion);
                } else {
                    callback.onResult("Failed to get suggestions.");
                    Log.e("OpenAI", "API error - Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                callback.onResult("Network error, please try again.");
                Log.e("OpenAI", "API call failed", t);
            }
        });
    }

    public void loadSmartSuggestions(Context context, String email, String location) {
        WeatherRepository weatherRepository = new WeatherRepository(context);
        Date today = getTodayDateTruncated();

        new Thread(() -> {
            WeatherSuggestions existing = weatherRepository.getTodaySuggestion(email, today);
            if (existing != null) {
                suggestionLiveData.postValue(existing.getUsageSuggestionText());
                Log.d("ClimateAlertsViewModel", "Using existing suggestion: " + existing.getUsageSuggestionText());
            } else {
                weatherRepository.getTodayWeather(email, location, new WeatherDataCallback() {
                    @Override
                    public void onSuccess(String weatherReport) {
                        if (weatherReport == null || weatherReport.isEmpty()) {
                            suggestionLiveData.postValue("Could not fetch weather.");
                            return;
                        }
                        fetchSuggestion("It's " + weatherReport + ". Suggest water-saving tips.", usageSuggestion -> {
                            WeatherSuggestions suggestion = new WeatherSuggestions(
                                    email, location, today, weatherReport, usageSuggestion
                            );
                            weatherRepository.insertSuggestion(email, suggestion);
                            suggestionLiveData.postValue(usageSuggestion);
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        suggestionLiveData.postValue("Failed to fetch weather: " + error);
                    }
                });
            }
        }).start();
    }

}
