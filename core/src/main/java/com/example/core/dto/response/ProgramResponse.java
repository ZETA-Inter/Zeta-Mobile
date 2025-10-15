package com.example.core.dto.response;

import com.google.gson.annotations.SerializedName;

public class ProgramResponse {


    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name; // O campo "name" do JSON será mapeado para este atributo.

    @SerializedName("description")
    private String description;

    @SerializedName("imageUrl") // Certifique-se que o nome no JSON é este
    private String imageUrl;

    @SerializedName("segment")
    SegmentResponse segment;


    public String getTitle() {
        return name; // O Adapter chama getTitle(), mas usamos o campo "name" da API.
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }
    public String getImageUrl() {
        return imageUrl;
    }

    public int getId() {
        return id;
    }

    public SegmentResponse getSegment() {
        return segment;
    }

    //SETTERS

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setSegment(SegmentResponse segment) {
        this.segment = segment;
    }
}
