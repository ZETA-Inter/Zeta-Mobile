// Crie este novo arquivo: Worker.java
package com.example.feature_fornecedor.ListPage;

import com.google.gson.annotations.SerializedName;

// Plain Old Java Object (POJO) para representar os dados do trabalhador
public class Worker {

    @SerializedName("id") // Garante que o campo JSON "id" seja mapeado para esta variável
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("planName")
    private String planName;

    @SerializedName("companyName")
    private String companyName;

    // Crie os Getters para todos os campos (essencial para o Adapter)
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
}