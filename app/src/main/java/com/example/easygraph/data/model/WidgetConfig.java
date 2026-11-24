package com.example.easygraph.data.model;

import java.util.UUID;

/**
 * Visual and binding properties of one card on the dashboard canvas.
 * It is what gets saved to restore the same layout when the app restarts.
 */
public class WidgetConfig {

    /** Visual style of the card. Continuous numeric variables can switch freely
     *  between TABLE, BAR, LINE and GAUGE. STATUS and CONTROL are specialized. */
    public enum WidgetType { TABLE, BAR, LINE, GAUGE, STATUS, CONTROL }

    private final String id;        // unique ID, generated once
    private int nodeId;             // 1 = water node, 2 = MeteoRoad
    private String variable;        // "ph", "tds", "turbidity", "temperature"...
    private WidgetType type;
    private String color;           // hex color, e.g. "#2196F3"
    private boolean autoColor;      // true = color by state (green/amber/red)
    private float x;                // canvas position
    private float y;
    private int width;              // size in dp
    private int height;

    public WidgetConfig(int nodeId, String variable, WidgetType type) {
        this.id = UUID.randomUUID().toString();
        this.nodeId = nodeId;
        this.variable = variable;
        this.type = type;
        this.color = "#2196F3";
        this.autoColor = false;
        this.x = 0;
        this.y = 0;
        this.width = 300;
        this.height = 200;
    }

    public String getId() { return id; }
    public int getNodeId() { return nodeId; }
    public String getVariable() { return variable; }
    public WidgetType getType() { return type; }
    public String getColor() { return color; }
    public boolean isAutoColor() { return autoColor; }
    public float getX() { return x; }
    public float getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public void setNodeId(int nodeId) { this.nodeId = nodeId; }
    public void setVariable(String variable) { this.variable = variable; }
    public void setType(WidgetType type) { this.type = type; }
    public void setColor(String color) { this.color = color; }
    public void setAutoColor(boolean autoColor) { this.autoColor = autoColor; }
    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
    public void setWidth(int width) { this.width = width; }
    public void setHeight(int height) { this.height = height; }
}
