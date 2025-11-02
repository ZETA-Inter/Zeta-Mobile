package com.example.feature_produtor.dto.request;

import com.google.gson.annotations.SerializedName;

public class ProgressAddRequestDTO {

    private final Integer points;

    private final Integer percentage;

    @SerializedName("worker_id")
    private final Integer workerId;

    @SerializedName("program_id")
    private final Integer programId;


    public ProgressAddRequestDTO(int points, int percentage, Integer workerId, Integer programId) {
        this.points = points;
        this.percentage = percentage;
        this.workerId = workerId;
        this.programId = programId;
    }
}