package com.example.feature_produtor.api;

import com.example.feature_produtor.dto.request.ChatRequest;
import com.example.feature_produtor.dto.request.SearchRequest;
import com.example.feature_produtor.dto.response.ChatResponse;
import com.example.feature_produtor.dto.response.SearchResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiIA {

    // Ajuda com chatbot
    @POST("/programs_agent")
    Call<ChatResponse> sendMessage(@Body ChatRequest request);

    //pesquisa com IA
    @POST("/search_agent")
    Call<SearchResponse> searchPrograms(@Body SearchRequest request);
}