package com.example.easygraph.data.model;

import java.util.Map;

/** Number of active incidents grouped by type. */
public class TrafficSummary {

    private final Map<String, Integer> countByType;

    public TrafficSummary(Map<String, Integer> countByType) {
        this.countByType = countByType;
    }

    public Map<String, Integer> getCountByType() { return countByType; }
}
