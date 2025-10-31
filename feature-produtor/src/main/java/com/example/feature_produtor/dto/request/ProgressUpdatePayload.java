// NOVO: ProgressUpdatePayload.java
package com.example.feature_produtor.dto.request;

import com.google.gson.annotations.SerializedName;


public class ProgressUpdatePayload {

    @SerializedName("program_id")
    private final Integer programId;

    @SerializedName("progress_percentage")
    private final Integer progressPercentage;

    public ProgressUpdatePayload(Integer programId, Integer progressPercentage) {
        this.programId = programId;
        this.progressPercentage = progressPercentage;
    }

    // Getters
    public Integer getProgramId() { return programId; }
    public Integer getProgressPercentage() { return progressPercentage; }
}