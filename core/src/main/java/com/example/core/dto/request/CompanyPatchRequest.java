package com.example.core.dto.request;

import com.google.gson.annotations.SerializedName;

public class CompanyPatchRequest {
    private String name;
    private String email;

    @SerializedName("image_url")
    private String imageUrl;
    private PlanInfoRequest plan_info;

    public void setImageUrl(String url) { this.imageUrl = url; }
}
