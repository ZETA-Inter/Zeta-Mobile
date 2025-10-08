package com.example.core.dto.response;

public class CompanyResponse extends UserResponse {

    private Integer id;
    private String name;
    private String email;
    private String planName;

    public CompanyResponse() {}

    public CompanyResponse(Integer id, String name, String email) {
        super(id, name, email);
    }

    public CompanyResponse(Integer id, String name, String email, String planName) {
        super(id, name, email);
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

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }
}
