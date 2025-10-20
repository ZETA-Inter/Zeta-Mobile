package com.example.feature_fornecedor.ranking;

import com.google.gson.JsonElement;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface RankingApi {
    @GET("api/companies/ranking")
    Call<JsonElement> getCompanyRanking(@Query("companyId") int companyId);

}
