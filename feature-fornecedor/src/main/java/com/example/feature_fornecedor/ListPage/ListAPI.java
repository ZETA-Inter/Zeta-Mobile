package com.example.feature_fornecedor.ListPage;

import com.google.gson.JsonElement;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface ListAPI {
    @GET("api/workers/list")
    Call<List<Worker>> getAllWorker();
}
