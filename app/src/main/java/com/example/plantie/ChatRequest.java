package com.example.plantie;

import java.util.Collections;
import java.util.List;

public class ChatRequest {
    private String model;
    private List<Message> messages;

    public ChatRequest(String message) {
        this.model = "gpt-3.5-turbo";
        this.messages = Collections.singletonList(new Message("user", message));
    }

    public static class Message {
        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}
