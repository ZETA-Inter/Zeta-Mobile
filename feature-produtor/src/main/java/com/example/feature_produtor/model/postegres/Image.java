package com.example.feature_produtor.model.postegres;

import com.google.gson.annotations.SerializedName;

public class Image {

    private Integer id;
    @SerializedName("origin_table")
    private String originTable;
    @SerializedName("source_id")
    private Integer sourceId;
    @SerializedName("image_url")
    private String imageUrl;


    public Image() {
    }

    // Construtor completo
    public Image(Integer id, String originTable, Integer sourceId, String imageUrl) {
        this.id = id;
        this.originTable = originTable;
        this.sourceId = sourceId;
        this.imageUrl = imageUrl;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOriginTable() {
        return originTable;
    }

    public void setOriginTable(String originTable) {
        this.originTable = originTable;
    }

    public Integer getSourceId() {
        return sourceId;
    }

    public void setSourceId(Integer sourceId) {
        this.sourceId = sourceId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}