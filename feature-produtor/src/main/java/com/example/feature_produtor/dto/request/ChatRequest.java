package com.example.feature_produtor.dto.request;

import com.google.gson.annotations.SerializedName;

public class ChatRequest {

    @SerializedName("prompt")
    private String userInput;

    public ChatRequest(String userInput) {
        this.userInput = userInput;
    }

    // Getters e Setters
    public String getUserInput() {
        return userInput;
    }

    public void setUserInput(String userInput) {
        this.userInput = userInput;
    }
}