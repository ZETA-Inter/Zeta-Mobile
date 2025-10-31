package com.example.feature_produtor.api;

import com.example.feature_produtor.dto.request.ProgressUpdatePayload;
import com.example.feature_produtor.dto.response.ProgramWorkerResponseDTO;
import com.example.feature_produtor.model.postegres.Goal;
import com.example.feature_produtor.model.postegres.Program;
import com.example.feature_produtor.model.postegres.Segment;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;

public interface ApiPostgres {

    @GET("/api/programs/list-all")
    Call<List<Program>> getAllPrograms();

    @GET("/api/programs/find-id/{id}")
    Call<Program> getProgramById(@Path("id") Integer id);

    @GET("/api/segments/list-segments")
    Call<List<Segment>> getAllSegments();

    @PATCH("/api/workers/update/{id}")
    Call<Void> updateProgramProgress(
            @Path("id") int workerId,
            @Body ProgressUpdatePayload request
    );

    @GET("/api/workers/list-actual-programs-by-id/{id}")
    Call<List<ProgramWorkerResponseDTO>> listWorkerProgramsWithProgress(@Path("id") Integer workerId);


    @GET("api/goals/list-goals-by-worker-id/{workerId}")
    Call<List<Goal>> getGoalsByWorkerId(@Path("workerId") int id);



    @GET("api/goals/list-goals-by-company/{workerId}/{companyId}")
    Call<Goal> getGoalsByCompany(@Path("companyId") int companyId);



}


