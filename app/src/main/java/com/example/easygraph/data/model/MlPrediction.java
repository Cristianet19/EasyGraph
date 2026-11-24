package com.example.easygraph.data.model;
/** Rain trend predicted by the TinyML model running on the ESP32. */
public class MlPrediction {

    private final String label;       // "estable", "empeora" or "lluvia_proxima"
    private final double probability; // 0.0 - 1.0

    public MlPrediction(String label, double probability) {
        this.label = label;
        this.probability = probability;
    }

    public String getLabel() { return label; }
    public double getProbability() { return probability; }
}
