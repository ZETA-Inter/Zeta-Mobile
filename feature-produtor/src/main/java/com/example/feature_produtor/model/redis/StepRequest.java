package com.example.feature_produtor.model.redis;

import com.google.gson.annotations.SerializedName;

public class StepRequest {

    @SerializedName("worker_id")
    private int workerId;

    @SerializedName("program_id")
    private int programId;
    private int step;

    public StepRequest(int workerId, int programId, int step) {
        this.workerId = workerId;
        this.programId = programId;
        this.step = step;
    }

    public int getWorkerId() {
        return workerId;
    }

    public int getProgramId() {
        return programId;
    }

    public int getStep() {
        return step;
    }

}
