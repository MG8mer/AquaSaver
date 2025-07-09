package com.example.aquasaver.smart_suggestions.ai_logic;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class OpenAIClient {
    private static final String BASE_URL = "https://api.openai.com/v1/"; //NEED TO UPDATE THIS LINK TO PROPER PATH
    private final OkHttpClient client;
    private final String apiKey;

    public OpenAIClient(String apiKey) {
        this.apiKey = apiKey;
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

    public void getSmartSuggestion(String prompt, Callback callback) {
        // Build your JSON request body for chat completion or completion API
        JSONObject json = new JSONObject();
        try {
            json.put("model", "o4-mini");
            json.put("prompt", prompt);
            json.put("max_tokens", 100);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString());
        Request request = new Request.Builder()
                .url(BASE_URL + "completions")
                .post(body)
                .build();

        client.newCall(request).enqueue(callback);
    }
}
