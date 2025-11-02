package com.example.feature_fornecedor.dto;

public class ProgramCountResponse {
    private int programId;
    private String programName;
    private String segment;
    private int countWorkers;

    public int getProgramId() { return programId; }
    public void setProgramId(int programId) { this.programId = programId; }

    public String getProgramName() { return programName; }
    public void setProgramName(String programName) { this.programName = programName; }

    public String getSegment() { return segment; }
    public void setSegment(String segment) { this.segment = segment; }

    public int getCountWorkers() { return countWorkers; }
    public void setCountWorkers(int countWorkers) { this.countWorkers = countWorkers; }
}
