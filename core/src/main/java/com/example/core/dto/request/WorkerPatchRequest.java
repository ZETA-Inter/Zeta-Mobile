package com.example.core.dto.request;

import com.google.gson.annotations.SerializedName;

public class WorkerPatchRequest {
    private String name;
    private String email;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("plan_info")
    private PlanInfoRequest planInfo;

    @SerializedName("company_id")
    private Integer companyId;

    public WorkerPatchRequest() {}

    public WorkerPatchRequest(String name, String email, String imageUrl, PlanInfoRequest planInfo, Integer companyId) {
        this.name = name;
        this.email = email;
        this.imageUrl = imageUrl;
        this.planInfo = planInfo;
        this.companyId = companyId;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public PlanInfoRequest getPlanInfo() {
        return planInfo;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public void setImageUrl(String url) { this.imageUrl = url; }
}
