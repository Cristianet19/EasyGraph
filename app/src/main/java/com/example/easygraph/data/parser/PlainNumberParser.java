package com.example.easygraph.data.parser;

import com.example.easygraph.data.model.SensorReading;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser for the water node (node 1): every topic carries plain text.
 * Numeric topics: tds, ph, turbidity, temperature.
 * Text topics: state (CLEAN / DOUBTFUL / CONTAMINATED) and heartbeat.
 */
public class PlainNumberParser implements MessageParser {

    private final int nodeId;
    private final String topicPrefix;   // e.g. "water/node1/"

    public PlainNumberParser(int nodeId, String topicPrefix) {
        this.nodeId = nodeId;
        this.topicPrefix = topicPrefix;
    }

    @Override
    public List<NodeEvent> parse(String topic, byte[] payload) {
        List<NodeEvent> events = new ArrayList<>();
        if (topic == null || !topic.startsWith(topicPrefix)) {
            return events;
        }

        String variable = topic.substring(topicPrefix.length());
        String text = new String(payload, StandardCharsets.UTF_8).trim();
        long now = System.currentTimeMillis();

        switch (variable) {
            case "tds":
                addReading(events, variable, text, "ppm", now);
                break;
            case "ph":
                addReading(events, variable, text, "pH", now);
                break;
            case "turbidity":
                addReading(events, variable, text, "", now);   // relative value, no unit
                break;
            case "temperature":
                addReading(events, variable, text, "°C", now);
                break;
            case "state":
                events.add(new NodeEvent.WaterState(text.toUpperCase(), now));
                break;
            case "heartbeat":
                events.add(new NodeEvent.Heartbeat(text.startsWith("OK"), text, now));
                break;
            default:
                // "cmd" and any unknown topic are ignored
                break;
        }
        return events;
    }

    private void addReading(List<NodeEvent> events, String variable,
                            String text, String unit, long timestamp) {
        try {
            double value = Double.parseDouble(text.replace(',', '.'));
            events.add(new NodeEvent.Reading(
                    new SensorReading(nodeId, variable, value, unit, timestamp)));
        } catch (NumberFormatException e) {
            // Malformed payload: ignore it instead of crashing the app
        }
    }
}
