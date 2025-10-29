package com.example.feature_produtor.api;

import com.example.feature_produtor.model.redis.StepResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiRedis {

    @GET("/get-step")
    Call<StepResponse> getStep(
            @Query("workerId") Integer workerId,
            @Query("programId") Integer programId
    );
}
