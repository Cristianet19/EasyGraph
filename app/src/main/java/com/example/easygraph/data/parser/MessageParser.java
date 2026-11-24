package com.example.easygraph.data.parser;

import java.util.List;

/** Converts a raw MQTT message into typed events. One implementation per node profile. */
public interface MessageParser {
    List<NodeEvent> parse(String topic, byte[] payload);
}
