package com.example.core.dto.response;

public class WorkerResponse extends UserResponse {

    private String planName;

    private String companyName;

    public WorkerResponse() {}

    public WorkerResponse(Integer id, String name, String email) {
        super(id, name, email);
    }

    public WorkerResponse(Integer id, String name, String email, String planName, String companyName) {
        super(id, name, email);
        this.planName = planName;
        this.companyName = companyName;
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
