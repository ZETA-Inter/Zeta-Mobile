package com.example.feature_produtor.dto.request;

import com.google.gson.annotations.SerializedName;

public class SearchRequest {
    @SerializedName("prompt")
    private String query;

    public SearchRequest(String query) {
        this.query = query;
    }

    // Getters e Setters (se necessário)
    public String getQuery() { return query; }
}