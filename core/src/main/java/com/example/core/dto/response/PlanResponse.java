package com.example.core.dto.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;


public class PlanResponse {

    @SerializedName("plan_id")
    private Integer planId;

    @SerializedName("plan_name")
    private String planName;
    private Double value;
    private List<Functionalities> functionalities;

    public static class Functionalities {

        private Integer id;

        private String name;

        public Functionalities(String name) {
            this.name = name;
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
    }

    public PlanResponse(Integer planId, String planName, Double value, List<Functionalities> functionalities) {
        this.planId = planId;
        this.planName = planName;
        this.value = value;
        this.functionalities = functionalities;
    }

    public List<Functionalities> getFunctionalities() {
        return functionalities;
    }

    public void setFunctionalities(List<Functionalities> functionalities) {
        this.functionalities = functionalities;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public Integer getPlanId() {
        return planId;
    }

    public void setPlanId(Integer planId) {
        this.planId = planId;
    }
}
