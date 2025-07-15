package com.example.aquasaver.smart_suggestions.ai_logic;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ChatResponse {
    @SerializedName("choices")
    public List<Choice> choices;

    public static class Choice {
        @SerializedName("message")
        public Message message;
    }

    public static class Message {
        @SerializedName("role")
        public String role;

        @SerializedName("content")
        public String content;
    }
}
