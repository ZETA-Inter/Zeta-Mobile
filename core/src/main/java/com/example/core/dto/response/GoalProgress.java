package com.example.core.dto.response;

public class GoalProgress {
    private Double totalGoals;
    private int completedGoals;

    public GoalProgress(Double totalGoals, int completedGoals) {
        this.totalGoals = totalGoals;
        this.completedGoals = completedGoals;
    }

    public Double getTotalGoals() {
        return totalGoals;
    }

    public void setTotalGoals(Double totalGoals) {
        this.totalGoals = totalGoals;
    }

    public int getCompletedGoals() {
        return completedGoals;
    }

    public void setCompletedGoals(int completedGoals) {
        this.completedGoals = completedGoals;
    }
}
