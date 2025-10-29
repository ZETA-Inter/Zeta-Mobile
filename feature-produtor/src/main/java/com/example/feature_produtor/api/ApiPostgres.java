package com.example.feature_produtor.api;

import com.example.feature_produtor.model.postegres.Program;
import com.example.feature_produtor.model.postegres.Segment;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiPostgres {
    @GET("/api/programs/list-all")
    Call<List<Program>> getAllPrograms();

    @GET("/api/programs/find-id/{id}")
    Call<Program> getProgramById(@Path("id") Integer id);

    @GET("/api/segments/list-segments")
    Call<List<Segment>> getAllSegments();
}


