// Arquivo: com/example/feature_fornecedor/ListPage/Worker.java

package com.example.feature_fornecedor.ListPage;

import com.google.gson.annotations.SerializedName;

public class Worker {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("image_url")
    private String imageUrl;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}