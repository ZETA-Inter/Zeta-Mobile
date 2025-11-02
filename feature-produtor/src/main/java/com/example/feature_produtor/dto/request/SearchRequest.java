package com.example.feature_produtor.dto.request;

public class SearchRequest {
    private String query;

    public SearchRequest(String query) {
        this.query = query;
    }

    // Getters e Setters (se necessário)
    public String getQuery() { return query; }
}