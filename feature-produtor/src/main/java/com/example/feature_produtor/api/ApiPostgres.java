package com.example.feature_produtor.api;

import com.example.feature_produtor.model.mongo.Class;
import com.example.feature_produtor.model.postegres.Program;
import com.example.feature_produtor.model.postegres.Segment;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiPostgres {
    // lista todos os cursos (program)
    @GET("api/programs/listAll")
    Call<List<Program>> getAllPrograms();

    // lista todos os segmento (segment)
    @GET("api/segments/listAll")
    Call<List<Segment>> getAllSegments();
}
