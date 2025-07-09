package com.example.aquasaver.ui.climate_alerts;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.aquasaver.smart_suggestions.ai_logic.ChatRequest;
import com.example.aquasaver.smart_suggestions.ai_logic.ChatResponse;
import com.example.aquasaver.smart_suggestions.ai_logic.OpenAIService;

import com.example.aquasaver.data.WeatherRepository;


import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ClimateAlertsViewModel extends ViewModel {
    private MutableLiveData<String> suggestionLiveData = new MutableLiveData<>();
    private OpenAIService openAIService;

    public ClimateAlertsViewModel() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openai.com/") //FIX LINK
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        openAIService = retrofit.create(OpenAIService.class);
    }

    public LiveData<String> getSuggestionsLiveData() {
        return suggestionLiveData;
    }

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
                    Log.e("OpenAI", "API Error: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                suggestionLiveData.postValue("Network error, please try again.");
            }
        });
    }

    public void loadSmartSuggestions(String email, String location) { //FIX THIS
        WeatherRepository.getTodayWeather(email, location, weather -> {
            String prompt = "Weather: " + weather + ". Suggest water-saving tips.";
            OpenAIClient.getSmartSuggestion(prompt, suggestions -> {
                suggestionsLiveData.postValue(suggestions);
            });
        });
    }
}


