package com.example.feature_produtor.api;

import com.example.feature_produtor.model.mongo.Class;
import com.example.feature_produtor.model.postegres.Image;
import com.example.feature_produtor.model.postegres.Program;
import com.example.feature_produtor.model.postegres.Segment;
import com.google.firebase.firestore.auth.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiPostgres {
    // lista todos os cursos (program)
    @GET("/api/programs/list-all")
    Call<List<Program>> getAllPrograms();

    @GET("/api/programs/find-id/{id}")
    Call<Program> getProgramById(@Path("id") Integer id);

    @GET("/api/segments/list_segments")
    Call<List<Segment>> getAllSegments();
}


