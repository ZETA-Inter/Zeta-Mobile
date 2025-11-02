package com.example.core.dto.response;
import com.example.core.model.Segment;
import com.google.gson.annotations.SerializedName;
public class ProgramWorkerResponseDTO {


    private Integer id;

    private String name;

    private String description;

    private Segment segment;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("progress_percentage")
    private int progressPercentage;

    public ProgramWorkerResponseDTO() {
    }

    // Getters
    public Integer getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public int getProgressPercentage() {
        return progressPercentage;
    }
    public String getImageUrl() {
        return imageUrl;
    }

    public Segment getSegment() {
        return segment;
    }

}