package com.example.feature_produtor.api;

import com.example.feature_produtor.model.redis.StepRequest;
import com.example.feature_produtor.model.redis.StepResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiRedis {

    @GET("/get-step")
    Call<StepResponse> getStep(
            @Query("workerId") Integer workerId,
            @Query("programId") Integer programId
    );

    @POST("save-step")
    Call<StepResponse> saveStep(@Body StepRequest request);

}
