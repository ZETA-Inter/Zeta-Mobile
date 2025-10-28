package com.example.feature_produtor.model.postegres;

import com.google.gson.annotations.SerializedName;

public class Program {

    private Integer id;
    private String name;
    private String description;
    @SerializedName("segment")

    private Segment segmentResponse;
     private int progressPercentage;


    public Program() {
    }

    // Getters e Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }



    public Segment getSegment() {
        return segmentResponse;
    }

    public void setSegment(Segment segmentResponse) {
        this.segmentResponse = segmentResponse;
    }

    public int getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(int progressPercentage) {
        this.progressPercentage = progressPercentage;
    }
}
