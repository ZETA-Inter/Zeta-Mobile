package com.example.core.client;

import com.example.core.dto.CompanyResponse;
import com.example.core.dto.PlanResponse;
import com.example.core.dto.WorkerResponse;
import com.example.core.dto.request.CompanyRequest;
import com.example.core.dto.request.WorkerRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiPostgresClient {

    @GET("api/plans/list_plans")
    Call<List<PlanResponse>> listPlans();

    @POST("api/workers/create")
    Call<WorkerResponse> createWorker(@Body WorkerRequest worker);

    @POST("api/companies/create")
    Call<CompanyResponse> createCompany(@Body CompanyRequest request);

}
