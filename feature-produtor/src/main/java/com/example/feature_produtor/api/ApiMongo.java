package com.example.feature_produtor.api;

import com.example.feature_produtor.model.mongo.Activity;
import com.example.feature_produtor.model.mongo.Class;
import com.example.feature_produtor.model.mongo.Content;
import com.example.feature_produtor.model.mongo.FlashCard;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

import java.util.List;

public interface ApiMongo {

    // lista todas as classes
    @GET("/api/classes/list_all_class")
    Call<List<Class>> getAllClasses();


    @GET("/api/classes/list-all-class-by-program-id/{programId}")
    Call<List<Class>> getClassByProgramId(@Path("programId") Integer programId);


    @GET("/api/classes/find-class-by-id/{id}")
    Call<Class> getClassById(@Path("id") Integer id);

    @GET("api/activities/list_all_activities_by_class_id/{classId}")
    Call<Activity> getActivityByClassId(@Path("classId") int id);
}

