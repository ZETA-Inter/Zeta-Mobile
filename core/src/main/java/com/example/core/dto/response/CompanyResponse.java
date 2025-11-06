package com.example.core.dto.response;

import com.google.gson.annotations.SerializedName;

import java.util.Collection;
import java.util.Collections;

public class CompanyResponse extends UserResponse {

    @SerializedName("plan_name")
    private String planName;

    public CompanyResponse() {}

    public CompanyResponse(Integer id, String name, String email, String imageUrl) {
        super(id, name, email, imageUrl);
    }

    public CompanyResponse(Integer id, String name, String email, String imageUrl, String planName) {
        super(id, name, email, imageUrl);
        this.planName = planName;
    }
    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }
}
