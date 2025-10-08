package com.example.core.dto.response;

public class WorkerResponse extends UserResponse {

    private Integer id;

    private String name;

    private String email;

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
