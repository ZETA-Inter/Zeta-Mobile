package com.example.feature_fornecedor;

public class ChartPoint {
    private String label;
    private int value;

    public ChartPoint(String label, int value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public int getValue() {
        return value;
    }
}
