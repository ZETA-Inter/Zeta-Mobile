package com.example.feature_produtor.model.postegres;

import com.google.gson.annotations.SerializedName;
import java.time.LocalDate;

public class Worker {

    private Integer id;
    private String name;
    private String email;
    private boolean active;

    @SerializedName("created_at")
    private LocalDate createdAt;

    @SerializedName("company_id")
    private Integer companyId;


    public Worker() {}

    // Construtor completo
    public Worker(Integer id, String name, String email, boolean active, LocalDate createdAt, Integer companyId) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.active = active;
        this.createdAt = createdAt;
        this.companyId = companyId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }
}