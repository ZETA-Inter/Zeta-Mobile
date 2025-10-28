package com.example.core.dto.request;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDate;

public class WorkerRequest {

    private String name;

    private String email;
    @SerializedName("plan_info")
    private PlanInfoRequest planInfo;

    private String cpf;

    private Integer companyId;

    public WorkerRequest() {}
    public WorkerRequest(String name, String email, String cpf, PlanInfoRequest planInfo, Integer companyId) {
        this.name = name;
        this.email = email;
        this.planInfo = planInfo;
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

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public PlanInfoRequest getPlanInfo() {
        return planInfo;
    }

    public void setPlanInfo(PlanInfoRequest planInfo) {
        this.planInfo = planInfo;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }
}
