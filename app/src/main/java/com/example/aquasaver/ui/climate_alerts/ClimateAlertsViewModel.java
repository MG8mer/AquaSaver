package com.example.aquasaver.ui.climate_alerts;

import static com.example.aquasaver.BuildConfig.OPENAI_API_KEY;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.aquasaver.BuildConfig;
import com.example.aquasaver.model.UserProfile;
import com.example.aquasaver.model.WeatherSuggestions;
import com.example.aquasaver.model.enums.GoalType;
import com.example.aquasaver.smart_suggestions.ai_logic.ChatRequest;
import com.example.aquasaver.smart_suggestions.ai_logic.ChatResponse;
import com.example.aquasaver.smart_suggestions.ai_logic.OpenAIService;
import com.example.aquasaver.smart_suggestions.ai_logic.OpenAIClient;
import com.example.aquasaver.smart_suggestions.weatherapi.WeatherAPIClient;
import com.example.aquasaver.smart_suggestions.weatherapi.WeatherApi;
import com.example.aquasaver.smart_suggestions.weatherapi.WeatherRepository;
import com.example.aquasaver.smart_suggestions.weatherapi.WeatherRepository.WeatherDataCallback;
import com.example.aquasaver.smart_suggestions.weatherapi.WeatherResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ClimateAlertsViewModel extends ViewModel {

    private final MutableLiveData<String> suggestionLiveData = new MutableLiveData<>();
    private final OpenAIService openAIService;

    public ClimateAlertsViewModel() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openai.com/")  // ✅ Make sure trailing slash exists
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        openAIService = retrofit.create(OpenAIService.class);
    }

    public LiveData<String> getSuggestionLiveData() {
        return suggestionLiveData;
    }

    // This uses your OpenAI service manually
    public void fetchSuggestion(String weatherInfo) {
        List<ChatRequest.Message> messages = new ArrayList<>();
        messages.add(new ChatRequest.Message("system", "You are an expert in water conservation."));
        messages.add(new ChatRequest.Message("user", weatherInfo));

        ChatRequest request = new ChatRequest(messages);

        openAIService.getChatResponse(request).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String suggestion = response.body().choices.get(0).message.content;
                    suggestionLiveData.postValue(suggestion);
                    Log.d("OpenAI", "API call successful - Suggestion: " + suggestion);
                } else {
                    suggestionLiveData.postValue("Failed to get suggestions.");
                    String errorMsg = "Failed to get suggestions.";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += " Error: " + response.errorBody().string();
                        } catch (IOException e) {
                            Log.e("OpenAI", "Error reading error body", e);
                        }
                    } else if (response.body() != null && response.body().choices.isEmpty()) {
                        errorMsg = "Failed to get suggestions: No choices returned.";
                    }
                    suggestionLiveData.postValue(errorMsg);
                    Log.e("OpenAI", "API error - Code: " + response.code() + ", Message: " + errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                suggestionLiveData.postValue("Network error, please try again.");
                Log.e("OpenAI", "API call failed", t);
            }
        });
    }
    public void loadSmartSuggestions(String email, String location) {
        WeatherRepository.getTodayWeather(email, location, new WeatherDataCallback() {
            @Override
            public void onSuccess(String weather) {
                if (weather == null || weather.isEmpty()) {
                    suggestionLiveData.postValue("Could not fetch weather data to generate suggestions.");
                    Log.w("ViewModel", "Weather data was null or empty.");
                    return;
                }

                fetchSuggestion("Weather: " + weather +". Generate water-saving tips");
            }

            @Override
            public void onWeatherDataLoaded(WeatherSuggestions suggestions) {
                Log.d("ViewModel", "onWeatherDataLoaded callback triggered, but currently using onSuccess with String.");
            }

            @Override
            public void onFailure(String error) {
                suggestionLiveData.postValue("Failed to fetch weather: " + error);
                Log.e("ViewModel", "WeatherRepository onFailure: " + error);
            }
        });
    }

}
