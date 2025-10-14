package com.example.feature_produtor.model;

import com.google.gson.annotations.SerializedName;

public class Program {

    private Integer id;
    private String name;
    private String description;

    @SerializedName("quantity_modules")
    private Integer quantityModules;
    private Segment segment2Response;


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

    public Integer getQuantityModules() {
        return quantityModules;
    }

    public void setQuantityModules(Integer quantityModules) {
        this.quantityModules = quantityModules;
    }

    public Segment getSegment() {
        return segment2Response;
    }

    public void setSegment(Segment segment2Response) {
        this.segment2Response = segment2Response;
    }
}
