package com.example.core.dto.response;

import com.google.gson.annotations.SerializedName;

public class WorkerResponse extends UserResponse {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("planName")
    private String planName;

    @SerializedName("companyName")
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

    //GETTERS

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
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


    //SETTERS

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
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
