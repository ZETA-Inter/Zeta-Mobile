package com.example.feature_produtor.dto.response;

import com.google.gson.annotations.SerializedName;

public class SearchResponse {

    @SerializedName("response")
    private String responseText; // O texto longo da IA

    @SerializedName("session")
    private String sessionToken; // O ID da sessão (String)

    // O campo 'agent' não é necessário se você não for usá-lo.

    // Getters
    public String getResponseText() {
        return responseText;
    }

    public String getSessionToken() {
        return sessionToken;
    }
}