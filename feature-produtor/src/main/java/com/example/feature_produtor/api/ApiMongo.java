package com.example.feature_produtor.api;

import com.example.core.model.mongo.Activity;
import com.example.core.model.mongo.Class;
import com.example.feature_produtor.dto.response.ClassesDetaisResponseDTO;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

import java.util.List;

public interface ApiMongo {

    @GET("/api/classes/list-all-class-by-program-id/{programId}")
    Call<List<Class>> getClassByProgramId(@Path("programId") Integer programId);


    @GET("/api/classes/find-class-by-id/{id}")
    Call<Class> getClassById(@Path("id") Integer id);

    @GET("/api/classes/find-class-by-id/{id}")
    Call<ClassesDetaisResponseDTO> getClassesDetailsById (@Path("id") Integer id);

    @GET("api/activities/list-all-activities-by-class-id/{classId}")
    Call<List<Activity>> getActivityByClassId(@Path("classId") int id);
}

