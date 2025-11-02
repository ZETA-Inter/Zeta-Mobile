package com.example.core.dto.request;

import com.google.gson.annotations.SerializedName;

public class WorkerPatchRequest {
    private String name;
    private String email;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("plan_info")
    private PlanInfoRequest planInfo;

    @SerializedName("company_id")
    private Integer companyId;

    public void setImageUrl(String url) { this.imageUrl = url; }
}
