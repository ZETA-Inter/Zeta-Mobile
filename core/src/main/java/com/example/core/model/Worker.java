// Arquivo: com/example/feature_fornecedor/ListPage/Worker.java

package com.example.core.model;

import com.google.gson.annotations.SerializedName;

public class Worker {

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

    // CORREÇÃO: Adicionado o campo que faltava para corresponder ao JSON
    @SerializedName("image_url")
    private String imageUrl;

    // Getters
    public int getId() {
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
}