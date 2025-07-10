package com.example.aquasaver.smart_suggestions.ai_logic;

import static com.example.aquasaver.BuildConfig.OPENAI_API_KEY;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OpenAIClient {
    private static final String BASE_URL = "https://api.openai.com/v1/"; //NEED TO UPDATE THIS LINK TO PROPER PATH
    private final OkHttpClient client;
    private final String apiKey;

    public OpenAIClient(String apiKey) {
        this.apiKey = OPENAI_API_KEY;
        client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .header("Authorization", "Bearer " + apiKey)
                            .header("Content-Type", "application/json")
                            .method(original.method(), original.body())
                            .build();
                    return chain.proceed(request);
                })
                .build();
    }
    public interface SuggestionCallback {
        void onSuggestionReceived(String suggestion);
        void onError(String error);
    }

    public void getSmartSuggestion(String prompt, SuggestionCallback callback) {
        JSONObject json = new JSONObject();
        try {
            json.put("model", "gpt-3.5-turbo-instruct");
            json.put("prompt", prompt);
            json.put("max_tokens", 100);
        } catch (JSONException e) {
            callback.onError("Failed to build JSON body: " + e.getMessage());
            return;
        }

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                json.toString()
        );

        Request request = new Request.Builder()
                .url(BASE_URL + "v1/completions")
                .post(body)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        String suggestion = jsonResponse
                                .getJSONArray("choices")
                                .getJSONObject(0)
                                .getString("text")
                                .trim();
                        callback.onSuggestionReceived(suggestion);
                    } catch (JSONException e) {
                        callback.onError("Failed to parse response: " + e.getMessage());
                    }
                } else {
                    callback.onError("API error: " + response.code());
                }
            }
        });
    }
}
