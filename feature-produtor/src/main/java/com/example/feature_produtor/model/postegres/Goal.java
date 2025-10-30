package com.example.feature_produtor.model.postegres;

public class Goal {

    private String goal;
    private boolean completed;


    public Goal() {
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}