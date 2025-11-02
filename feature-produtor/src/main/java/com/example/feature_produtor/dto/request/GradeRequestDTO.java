// NOVO ARQUIVO: GradeRequestDTO.java (no seu módulo Android)

package com.example.feature_produtor.dto.request;

import com.google.gson.annotations.SerializedName;

public class GradeRequestDTO {

    // Deve mapear para 'worker_id' no JSON
    @SerializedName("worker_id")
    private final Integer workerId;

    // Deve mapear para 'program_id' no JSON
    @SerializedName("program_id")
    private final Integer programId;

    // O progresso percentual (0 a 100) é a 'grade'
    @SerializedName("grade")
    private final Integer grade;

    public GradeRequestDTO(Integer workerId, Integer programId, Integer grade) {
        this.workerId = workerId;
        this.programId = programId;
        this.grade = grade;
    }
}