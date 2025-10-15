package com.example.core.dto.response;

import com.google.gson.annotations.SerializedName;

public class SegmentResponse {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    //CONSTRUTORES

    public SegmentResponse() {}

    public SegmentResponse(int id, String name) {
        this.id = id;
        this.name = name;
    }

    //GETTERS

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    //SETTERS

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
