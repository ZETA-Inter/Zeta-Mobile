package com.example.core.client;

import com.example.core.dto.request.CompanyPatchRequest;
import com.example.core.dto.request.WorkerPatchRequest;
import com.example.core.dto.response.CompanyResponse;
import com.example.core.dto.response.GoalProgress;
import com.example.core.dto.response.PlanResponse;
import com.example.core.dto.response.ProgramWorkerResponseDTO;
import com.example.core.dto.response.UserResponse;
import com.example.core.dto.response.WorkerResponse;
import com.example.core.dto.request.CompanyRequest;
import com.example.core.dto.request.WorkerRequest;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiPostgresClient {

    @GET("api/plans/list-plans")
    Call<List<PlanResponse>> listPlans();

    @GET("api/workers/find-email/{email}")
    Call<UserResponse> findWorkerByEmail(@Path("email") String email);

    @GET("api/workers/find-by-id/{id}")
    Call<WorkerResponse> findWorkerById(@Path("id") int id);

    @GET("api/companies/find-id/{id}")
    Call<CompanyResponse> findCompanyById(@Path("id") int id);


    @GET("api/companies/find-email/{email}")
    Call<UserResponse> findCompanyByEmail(@Path("email") String email);

    @GET("api/workers/list-actual-programs-by-id/{id}")
    Call<WorkerResponse> findProgramById(@Path("id") Integer id);

    @GET("api/workers/list-actual-programs-by-id/{id}")
    Call<List<ProgramWorkerResponseDTO>> listActualProgramsById(@Path("id") Integer id);

    @GET("api/companies/list-actual-worker-programs-by-id/{id}")
    Call<List<ProgramWorkerResponseDTO>> listActualProgramsByCompanyId(@Path("id") Integer id);

    @GET("api/goals/progress-goals/{workerId}")
    Call<WorkerResponse> findProgressGoalsById(@Path("workerId") Integer workerId);

    @GET("api/workers/overall-programs-progress/{workerId}")
    Call<Integer> findOverallProgramsProgressById(@Path("workerId") Integer workerId);

    @GET("api/workers/overall-goals-progress/{workerId}")
    Call<Integer> findOverallGoalsProgressById(@Path("workerId") Integer workerId);

    @GET("api/companies/average-progress-percentage/{companyId}")
    Call<Integer> findAverageProgressPercentageById(@Path("companyId") Integer companyId);

    @GET("api/goals/finished-goals-percentage/{companyId}")
    Call<GoalProgress> findPercentageFinishedGoalsById(@Path("companyId") Integer companyId);

    @POST("api/workers/create")
    Call<WorkerResponse> createWorker(@Body WorkerRequest worker);

    @POST("api/companies/create")
    Call<CompanyResponse> createCompany(@Body CompanyRequest request);

    @PATCH("api/workers/update/{id}")
    Call<WorkerResponse> patchWorker(@Path("id") int id, @Body WorkerPatchRequest body);

    // ApiPostgresClient
    @PATCH("api/companies/{id}")
    Call<Void> patchCompany(@Path("id") int id, @Body CompanyPatchRequest body);


}