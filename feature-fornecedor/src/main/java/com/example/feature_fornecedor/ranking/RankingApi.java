package com.example.feature_fornecedor.ranking;

import com.google.gson.JsonElement;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface RankingApi {
    @GET
    Call<JsonElement> getCompanyRanking(@Url String fullUrl);
}
