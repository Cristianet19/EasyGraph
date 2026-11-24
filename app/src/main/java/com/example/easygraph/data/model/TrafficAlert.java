package com.example.easygraph.data.model;
/** Traffic incident in the monitored zone. */
public class TrafficAlert {

    private final int level;      // 0 = none, 1 = warning (amber), 2 = severe (red)
    private final String type;    // e.g. "accident"
    private final String road;
    private final String text;    // description

    public TrafficAlert(int level, String type, String road, String text) {
        this.level = level;
        this.type = type;
        this.road = road;
        this.text = text;
    }

    public int getLevel() { return level; }
    public String getType() { return type; }
    public String getRoad() { return road; }
    public String getText() { return text; }
}
