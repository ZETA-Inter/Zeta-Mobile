package com.example.core.dto.request;

public class PlanInfoRequest {

    private Integer id;

    private String frequency;

    private Double amount;

    public PlanInfoRequest() {
    }

    public PlanInfoRequest(Integer id, String frequency, Double amount) {
        this.id = id;
        this.frequency = frequency;
        this.amount = amount;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
