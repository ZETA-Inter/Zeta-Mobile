package com.example.core.dto.request;

public class CompanyPatchRequest {
    private String name;
    private String email;
    private String image_url;
    private PlanInfoRequest plan_info;

    public void setImage_url(String url) { this.image_url = url; }
    // getters/setters demais campos se precisar
}
