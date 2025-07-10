package com.example.aquasaver.ui.climate_alerts;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.aquasaver.smart_suggestions.ai_logic.ChatRequest;
import com.example.aquasaver.smart_suggestions.ai_logic.ChatResponse;
import com.example.aquasaver.smart_suggestions.ai_logic.OpenAIService;
import com.example.aquasaver.smart_suggestions.ai_logic.OpenAIClient;
import com.example.aquasaver.smart_suggestions.weatherapi.WeatherRepository;

import java.util.ArrayList;
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
                    Log.e("OpenAI", "API error: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                suggestionLiveData.postValue("Network error, please try again.");
                Log.e("OpenAI", "API call failed", t);
            }
        });
    }

    // This uses your custom helper method which wraps both weather fetching and OpenAI
    public void loadSmartSuggestions(String email, String location) {
        WeatherRepository.getTodayWeather(email, location, new WeatherRepository.WeatherDataCallback() {
            @Override
            public void onSuccess(String weather) {
                if (weather == null) {
                    suggestionLiveData.postValue("Could not fetch weather.");
                    return;
                }

                String prompt = "Weather: " + weather + ". Suggest water-saving tips.";

                OpenAIClient.getSmartSuggestion(prompt,
                        new OpenAIClient.SuggestionCallback() {
                            @Override
                            public void onSuggestionReceived(String suggestion) {
                                suggestionLiveData.postValue(suggestion);
                            }

                            @Override
                            public void onError(String error) {
                                suggestionLiveData.postValue("Failed to get smart suggestion: " + error);
                            }
                        });
            }

            @Override
            public void onFailure(String error) {
                suggestionLiveData.postValue("Failed to fetch weather: " + error);
            }
        });

            OpenAIClient.getSmartSuggestion(prompt,
                    new OpenAIClient.SuggestionCallback() {
                        @Override
                        public void onSuggestionReceived(String suggestion) {
                            suggestionLiveData.postValue(suggestion);
                        }

                        @Override
                        public void onError(String error) {
                            suggestionLiveData.postValue("Failed to get smart suggestion: " + error);
                        }
                    });
        });
    }
}
