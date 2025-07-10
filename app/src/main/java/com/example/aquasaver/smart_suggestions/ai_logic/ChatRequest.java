package com.example.aquasaver.smart_suggestions.ai_logic;

import java.util.List;

public class ChatRequest {
    public String model = "gpt-3.5-turbo";
    public List<Message> messages;

    public static class Message {
        public String role;
        public String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    public ChatRequest(List<Message> messages) {
        this.messages = messages;
    }
}
