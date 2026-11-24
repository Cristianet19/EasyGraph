package com.example.easygraph.data.model;
/** Online/offline state of a node (the broker publishes it via Last Will). */
public class DeviceStatus {

    private final boolean online;
    private final long lastChange;    // milliseconds since epoch

    public DeviceStatus(boolean online, long lastChange) {
        this.online = online;
        this.lastChange = lastChange;
    }

    public boolean isOnline() { return online; }
    public long getLastChange() { return lastChange; }
}
