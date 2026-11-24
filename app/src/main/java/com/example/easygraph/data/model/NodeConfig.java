package com.example.easygraph.data.model;
/**
 * Connection settings of one ESP32 node.
 * One instance per node, edited from the settings screen.
 */
public class NodeConfig {

    public enum Protocol { WIFI_MQTT, SOCKET, BLUETOOTH }

    public enum ParserProfile { PLAIN_NUMBER, METEOROAD_JSON }

    private int id;                    // 1 = water node, 2 = MeteoRoad
    private String name;               // label shown in the UI
    private Protocol protocol;
    private String host;               // broker/ESP32 IP, or MAC for Bluetooth
    private int port;                  // 1883 for MQTT
    private String username;

    // transient: Gson ignores it, so the password never ends up in exported JSON.
    // It will be stored separately in encrypted storage.
    private transient String password;

    private String topicPrefix;        // e.g. "water/node1/"
    private ParserProfile parserProfile;

    public NodeConfig(int id, String name, Protocol protocol, String host, int port,
                      String username, String password, String topicPrefix,
                      ParserProfile parserProfile) {
        this.id = id;
        this.name = name;
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.topicPrefix = topicPrefix;
        this.parserProfile = parserProfile;
    }

    /** Default config for the water node (node 1). */
    public static NodeConfig defaultWaterNode() {
        return new NodeConfig(1, "Water quality", Protocol.WIFI_MQTT,
                "", 1883, "", "", "water/node1/", ParserProfile.PLAIN_NUMBER);
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public Protocol getProtocol() { return protocol; }
    public String getHost() { return host; }
    public int getPort() { return port; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getTopicPrefix() { return topicPrefix; }
    public ParserProfile getParserProfile() { return parserProfile; }

    public void setName(String name) { this.name = name; }
    public void setProtocol(Protocol protocol) { this.protocol = protocol; }
    public void setHost(String host) { this.host = host; }
    public void setPort(int port) { this.port = port; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setTopicPrefix(String topicPrefix) { this.topicPrefix = topicPrefix; }
    public void setParserProfile(ParserProfile parserProfile) { this.parserProfile = parserProfile; }
}
