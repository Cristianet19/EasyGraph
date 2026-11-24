package com.example.easygraph.data.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.easygraph.data.model.NodeConfig;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the connection to one MQTT broker.
 * One instance per broker. All network work runs on HiveMQ's own threads,
 * never on the main thread.
 */
public class MqttRepository {

    private static final String TAG = "MqttRepository";

    public enum ConnectionState { DISCONNECTED, CONNECTING, CONNECTED, ERROR }

    /** Called from a background thread each time a subscribed message arrives. */
    public interface MessageListener {
        void onMessage(String topic, byte[] payload);
    }

    private final String clientId;
    private final MutableLiveData<ConnectionState> connectionState =
            new MutableLiveData<>(ConnectionState.DISCONNECTED);
    private final MutableLiveData<String> lastError = new MutableLiveData<>();

    // Active subscriptions, kept to resubscribe after an automatic reconnection
    private final Map<String, MessageListener> subscriptions = new ConcurrentHashMap<>();

    private Mqtt3AsyncClient client;

    public MqttRepository(Context context) {
        this.clientId = getOrCreateClientId(context.getApplicationContext());
    }

    public LiveData<ConnectionState> getConnectionState() { return connectionState; }
    public LiveData<String> getLastError() { return lastError; }

    /** Connects to the broker described by the given node config. */
    public void connect(NodeConfig config) {
        if (client != null) {
            disconnect();
        }
        connectionState.postValue(ConnectionState.CONNECTING);

        client = MqttClient.builder()
                .useMqttVersion3()
                .identifier(clientId)
                .serverHost(config.getHost())
                .serverPort(config.getPort())
                .automaticReconnectWithDefaultConfig() // exponential backoff
                .addConnectedListener(ctx -> {
                    Log.d(TAG, "Connected to " + config.getHost());
                    connectionState.postValue(ConnectionState.CONNECTED);
                    resubscribeAll();
                })
                .addDisconnectedListener(ctx -> {
                    Log.d(TAG, "Disconnected: " + ctx.getCause());
                    connectionState.postValue(ConnectionState.DISCONNECTED);
                })
                .buildAsync();

        String user = config.getUsername();
        if (user != null && !user.isEmpty()) {
            String password = config.getPassword() != null ? config.getPassword() : "";
            client.connectWith()
                    .simpleAuth()
                    .username(user)
                    .password(password.getBytes(StandardCharsets.UTF_8))
                    .applySimpleAuth()
                    .send()
                    .whenComplete((ack, error) -> onConnectResult(error));
        } else {
            client.connectWith()
                    .send()
                    .whenComplete((ack, error) -> onConnectResult(error));
        }
    }

    private void onConnectResult(Throwable error) {
        if (error != null) {
            Log.e(TAG, "Connection failed", error);
            lastError.postValue(error.getMessage());
            connectionState.postValue(ConnectionState.ERROR);
        }
    }

    /**
     * Subscribes to a topic filter (e.g. "water/node1/#").
     * Can be called before the connection is ready: the subscription is
     * stored and sent as soon as the client connects.
     */
    public void subscribe(String topicFilter, MessageListener listener) {
        subscriptions.put(topicFilter, listener);
        if (client != null && client.getState().isConnected()) {
            doSubscribe(topicFilter, listener);
        }
    }

    private void resubscribeAll() {
        for (Map.Entry<String, MessageListener> entry : subscriptions.entrySet()) {
            doSubscribe(entry.getKey(), entry.getValue());
        }
    }

    private void doSubscribe(String topicFilter, MessageListener listener) {
        client.subscribeWith()
                .topicFilter(topicFilter)
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback(publish -> listener.onMessage(
                        publish.getTopic().toString(),
                        publish.getPayloadAsBytes()))
                .send()
                .whenComplete((ack, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Subscribe failed: " + topicFilter, error);
                        lastError.postValue("Subscribe failed: " + error.getMessage());
                    } else {
                        Log.d(TAG, "Subscribed to " + topicFilter);
                    }
                });
    }

    public void disconnect() {
        subscriptions.clear();
        if (client != null) {
            client.disconnect();
            client = null;
        }
        connectionState.postValue(ConnectionState.DISCONNECTED);
    }

    /** Unique client ID per app installation, to avoid collisions in the broker. */
    private static String getOrCreateClientId(Context context) {
        SharedPreferences prefs =
                context.getSharedPreferences("easygraph_prefs", Context.MODE_PRIVATE);
        String id = prefs.getString("client_id", null);
        if (id == null) {
            id = "easygraph-android-" + UUID.randomUUID();
            prefs.edit().putString("client_id", id).apply();
        }
        return id;
    }
}
