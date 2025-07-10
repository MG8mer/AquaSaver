package com.example.aquasaver.smart_suggestions.ai_logic;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ChatRequest {
    @SerializedName("model")
    public String model;

    @SerializedName("messages")
    public List<Message> messages;

    public ChatRequest(List<Message> messages) {
        this.model = "gpt-3.5-turbo"; // default model
        this.messages = messages;
    }

    public static class Message {
        @SerializedName("role")
        public String role;

        @SerializedName("content")
        public String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}
