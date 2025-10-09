package com.example.core.dto.request;

import com.google.gson.annotations.SerializedName;

public class CompanyRequest {

    private String name;

    private String email;

    private String imageUrl;

    @SerializedName("plan_info")
    private PlanInfoRequest planInfo;
    public CompanyRequest(String name, String email, String imageUrl, PlanInfoRequest planInfo) {
        this.name = name;
        this.email = email;
        this.imageUrl = imageUrl;
        this.planInfo = planInfo;
    }

    public CompanyRequest(String name, String email, PlanInfoRequest planInfo) {
        this.name = name;
        this.email = email;
        this.planInfo = planInfo;
    }

    public CompanyRequest() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public PlanInfoRequest getPlanInfo() {
        return planInfo;
    }

    public void setPlanInfo(PlanInfoRequest planInfo) {
        this.planInfo = planInfo;
    }
}
