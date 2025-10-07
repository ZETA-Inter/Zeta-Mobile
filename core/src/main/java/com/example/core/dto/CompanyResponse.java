package com.example.core.dto;

public class CompanyResponse {

    private Integer id;
    private String name;
    private String email;
    private  String cnpj;
    private String planName;

    public CompanyResponse() {}

    public CompanyResponse(Integer id, String name, String email, String cnpj, String planName) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.cnpj = cnpj;
        this.planName = planName;
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

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }
}
