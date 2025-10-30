package com.example.core.dto.response;

import com.google.gson.annotations.SerializedName;

public class WorkerResponse extends UserResponse {

    @SerializedName("plan_name")
    private String planName;

    @SerializedName("company_name")
    private String companyName;

    @SerializedName("image_url")
    private String imageUrl;


    //CONSTRUTORES

    public WorkerResponse() {}

    public WorkerResponse(Integer id, String name, String email) {
        super(id, name, email);
    }

    public WorkerResponse(Integer id, String name, String email, String planName, String companyName) {
        super(id, name, email);
        this.planName = planName;
        this.companyName = companyName;
    }


    public String getPlanName() {
        return planName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getImageUrl() {
        return imageUrl;
    }


    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}