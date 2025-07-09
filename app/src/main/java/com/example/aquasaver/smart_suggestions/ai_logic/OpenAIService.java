package com.example.aquasaver.smart_suggestions.ai_logic;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface OpenAIService {
    @Headers({
            "Content-Type: application/json",
            "Authorization: Bearer YOUR_API_KEY"  // ← Replace this with your real key
    })
    @POST("v1/chat/completions")
    Call<ChatResponse> getChatResponse(@Body ChatRequest request);
}
