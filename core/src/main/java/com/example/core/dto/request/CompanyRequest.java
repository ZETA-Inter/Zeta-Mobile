package com.example.core.dto.request;

public class CompanyRequest {

    private String name;

    private String email;

    private String cnpj;

    private String imageUrl;

    private Integer planId;

    public CompanyRequest(String name, String email, String cnpj, String imageUrl, Integer planId) {
        this.name = name;
        this.email = email;
        this.cnpj = cnpj;
        this.imageUrl = imageUrl;
        this.planId = planId;
    }

    public CompanyRequest() {}

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

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getPlanId() {
        return planId;
    }

    public void setPlanId(Integer planId) {
        this.planId = planId;
    }
}
