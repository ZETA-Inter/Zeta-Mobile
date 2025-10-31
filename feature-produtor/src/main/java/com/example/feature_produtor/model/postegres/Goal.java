package com.example.feature_produtor.model.postegres;

public class Goal {

    private Integer goalId;

    private Integer programId;
    private String goalDescription;
    private String goalName;
    private boolean completed;


    public Goal() {
    }

    public String getGoalDescription() {
        return goalDescription;
    }

    public String getGoalName() {
        return goalName;
    }

    public Integer getProgramId() {
        return programId;
    }

    public Integer getGoalId() {
        return goalId;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}