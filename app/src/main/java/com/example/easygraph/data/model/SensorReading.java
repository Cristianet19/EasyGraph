package com.example.easygraph.data.model;
/**
 * Immutable reading of one variable from one ESP32 node.
 * Widgets use this class and never inspect raw MQTT topics or payloads.
 */
public class SensorReading {

    private final int node;         // 1 = water node, 2 = MeteoRoad
    private final String variable;  // "tds", "ph", "turbidity", "temperature"
    private final double value;
    private final String unit;      // "ppm", "pH", "NTU", "°C"...
    private final long timestamp;   // milliseconds since epoch

    public SensorReading(int node, String variable, double value, String unit, long timestamp) {
        this.node = node;
        this.variable = variable;
        this.value = value;
        this.unit = unit;
        this.timestamp = timestamp;
    }

    public int getNode() { return node; }
    public String getVariable() { return variable; }
    public double getValue() { return value; }
    public String getUnit() { return unit; }
    public long getTimestamp() { return timestamp; }
}