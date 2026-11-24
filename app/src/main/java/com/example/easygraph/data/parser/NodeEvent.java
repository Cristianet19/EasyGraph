package com.example.easygraph.data.parser;

import com.example.easygraph.data.model.DeviceStatus;
import com.example.easygraph.data.model.ForecastDay;
import com.example.easygraph.data.model.MlPrediction;
import com.example.easygraph.data.model.SensorReading;
import com.example.easygraph.data.model.TrafficAlert;
import com.example.easygraph.data.model.TrafficSummary;

import java.util.List;

/**
 * Typed event produced by a parser. Widgets never see raw topics or payloads.
 * The private constructor restricts the subclasses to this file
 * (Java equivalent of a sealed interface).
 */
public abstract class NodeEvent {

    private NodeEvent() {}

    // ---- Node 1 (water) ----

    /** Numeric reading: tds, ph, turbidity, temperature. */
    public static final class Reading extends NodeEvent {
        public final SensorReading reading;
        public Reading(SensorReading reading) { this.reading = reading; }
    }

    /** Water quality class: CLEAN, DOUBTFUL or CONTAMINATED. */
    public static final class WaterState extends NodeEvent {
        public final String state;
        public final long timestamp;
        public WaterState(String state, long timestamp) {
            this.state = state;
            this.timestamp = timestamp;
        }
    }

    /** Heartbeat: "OK <uptime>" or "NO_READINGS". */
    public static final class Heartbeat extends NodeEvent {
        public final boolean ok;
        public final String detail;
        public final long timestamp;
        public Heartbeat(boolean ok, String detail, long timestamp) {
            this.ok = ok;
            this.detail = detail;
            this.timestamp = timestamp;
        }
    }

    // ---- Node 2 (MeteoRoad): used by the other parser ----

    public static final class Status extends NodeEvent {
        public final DeviceStatus status;
        public Status(DeviceStatus status) { this.status = status; }
    }

    public static final class Forecast extends NodeEvent {
        public final List<ForecastDay> days;
        public Forecast(List<ForecastDay> days) { this.days = days; }
    }

    public static final class Ml extends NodeEvent {
        public final MlPrediction prediction;
        public Ml(MlPrediction prediction) { this.prediction = prediction; }
    }

    public static final class Alert extends NodeEvent {
        public final TrafficAlert alert;
        public Alert(TrafficAlert alert) { this.alert = alert; }
    }

    public static final class Summary extends NodeEvent {
        public final TrafficSummary summary;
        public Summary(TrafficSummary summary) { this.summary = summary; }
    }
}
