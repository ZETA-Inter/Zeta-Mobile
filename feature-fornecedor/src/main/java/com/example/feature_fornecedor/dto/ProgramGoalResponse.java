package com.example.feature_fornecedor.dto;

public class ProgramGoalResponse {
    private int programId;
    private String programName;
    private String segment;
    private int countGoals;

    public int getProgramId() { return programId; }
    public void setProgramId(int programId) { this.programId = programId; }

    public String getProgramName() { return programName; }
    public void setProgramName(String programName) { this.programName = programName; }

    public String getSegment() { return segment; }
    public void setSegment(String segment) { this.segment = segment; }

    public int getCountGoals() { return countGoals; }
    public void setCountGoals(int countGoals) { this.countGoals = countGoals; }
}
