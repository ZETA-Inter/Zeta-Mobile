package com.example.feature_produtor.api;

import com.example.feature_produtor.dto.request.ChatRequest;
import com.example.feature_produtor.dto.response.ChatResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiChatBot {

    // Ajuda com chatbot
    @POST("/programs_agent")
    Call<ChatResponse> sendMessage(@Body ChatRequest request);
}