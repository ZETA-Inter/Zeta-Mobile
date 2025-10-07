package com.example.core.dto;

public class WorkerResponse {

    private Integer id;

    private String name;

    private String email;

    private String cpf;

    private String planName;

    private String companyName;

    public WorkerResponse() {}

    public WorkerResponse(Integer id, String name, String email, String cpf, String planName, String companyName) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.cpf = cpf;
        this.planName = planName;
        this.companyName = companyName;
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

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}
