package com.example.core.dto.request;

import java.time.LocalDate;

public class WorkerRequest {

    private String name;

    private String email;

    private String cpf;

    private Integer planId;

    private Integer companyId;

    public WorkerRequest() {}
    public WorkerRequest(String name, String email, String cpf, Integer planId, Integer companyId) {
        this.name = name;
        this.email = email;
        this.cpf = cpf;
        this.planId = planId;
        this.companyId = companyId;
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

    public Integer getPlanId() {
        return planId;
    }

    public void setPlanId(Integer planId) {
        this.planId = planId;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }
}
