package com.example.feature_produtor.dto.response;

public class WorkerProgressResponse {

    private int worker;
    private int workerPoints;
    private Double workerProgress;

    public WorkerProgressResponse() {}

    public int getWorker() {
        return worker;
    }

    public int getWorkerPoints() {
        return workerPoints;
    }

    public Double getWorkerProgress() {
        return workerProgress;
    }
}
