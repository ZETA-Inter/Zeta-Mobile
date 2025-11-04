package com.example.core.dto.response;

import com.google.gson.annotations.SerializedName;

import java.util.Collection;
import java.util.Collections;

public class CompanyResponse extends UserResponse {

    @SerializedName("plan_name")
    private String planName;

    @SerializedName("image_url")
    private String imageUrl;

    public CompanyResponse() {}

    public CompanyResponse(Integer id, String name, String email) {
        super(id, name, email);
    }

    public CompanyResponse(Integer id, String name, String email, String planName, String imageUrl) {
        super(id, name, email);
        this.imageUrl = imageUrl;
        this.planName = planName;
    }
    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public String getImageUrl() {
        return Collections.singleton(imageUrl).toString();
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
