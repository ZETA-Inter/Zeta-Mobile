package com.example.feature_produtor.api;

import com.example.feature_produtor.model.mongo.Class;
import com.example.feature_produtor.model.mongo.Content;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

import java.util.List;

public interface ApiMongo {

    // lista todas as classes
    @GET("list_all_class")
    Call<List<Class>> getAllClasses();


    //listar todos os contents

    @GET("list_all_contents")
    Call<List<Content>> getAllContents();

    @GET("pegarDescricaoPorId/{stepId}")
    Call<Class> getDescription(@Path("stepId") int stepId);
}

