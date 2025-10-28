package com.example.core.dto.response;

import com.google.gson.annotations.SerializedName;

public class CompanyResponse extends UserResponse {

    @SerializedName("plan_name")
    private String planName;

    public CompanyResponse() {}

    public CompanyResponse(Integer id, String name, String email) {
        super(id, name, email);
    }

    public CompanyResponse(Integer id, String name, String email, String planName) {
        super(id, name, email);
        this.planName = planName;
    }
    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }
}
