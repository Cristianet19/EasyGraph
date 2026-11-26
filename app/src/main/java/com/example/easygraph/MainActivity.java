package com.example.easygraph;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler; // NEW
import android.os.Looper; // NEW
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.easygraph.data.model.NodeConfig;
import com.example.easygraph.data.model.SensorReading;
import com.example.easygraph.data.network.MqttRepository;
import com.example.easygraph.data.parser.NodeEvent;
import com.example.easygraph.data.parser.PlainNumberParser;
import com.example.easygraph.util.PermissionHelper;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList; // NEW
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Temporary fixed screen: connection settings, one card per variable
 * and a log. It will be replaced by the free-position dashboard canvas.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String PREFS = "easygraph_prefs";
    private static final int MAX_LOG_CHARS = 20000;

    // NEW: data older than this is shown as stale. Adjust it to the publish
    // interval of your ESP32 (a good value is 2-3 times that interval).
    private static final long STALE_AFTER_MS = 2 * 60 * 1000L;
    private static final long AGE_REFRESH_MS = 5000L;

    private static final int COLOR_OK = Color.parseColor("#2E7D32");
    private static final int COLOR_WARNING = Color.parseColor("#F9A825");
    private static final int COLOR_BAD = Color.parseColor("#C62828");

    /** NEW: value + age views of one card, and when its last data arrived. */
    private static class CardView {
        final TextView value;
        final TextView age;
        long lastUpdate = 0; // 0 = no data received yet

        CardView(TextView value, TextView age) {
            this.value = value;
            this.age = age;
        }

        void update(String text) {
            value.setText(text);
            value.setAlpha(1f);
            lastUpdate = System.currentTimeMillis();
            age.setText("just now");
        }

        void refreshAge(long now, long staleAfterMs) {
            if (lastUpdate == 0) {
                age.setText("");
                return;
            }
            long elapsed = now - lastUpdate;
            boolean stale = elapsed > staleAfterMs;
            age.setText(formatAge(elapsed) + " ago" + (stale ? " (stale)" : ""));
            value.setAlpha(stale ? 0.4f : 1f);
        }
    }

    // Must be created in onCreate(), before the activity is started
    private PermissionHelper permissionHelper;

    private MqttRepository mqttRepository;
    private volatile PlainNumberParser parser;
    private volatile String commandTopic;

    private EditText etHost;
    private EditText etPort;
    private EditText etPrefix;
    private TextView tvStatus;
    private TextView tvLog;

    // Node 1 (water) cards
    private CardView cardPh; // NEW (was TextView)
    private CardView cardTds;
    private CardView cardTurbidity;
    private CardView cardTemperature;
    private CardView cardState;
    private CardView cardHeartbeat;
    private final List<CardView> node1Cards = new ArrayList<>(); // NEW

    // NEW: periodic refresh of the "age" lines
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable ageRefresher = new Runnable() {
        @Override
        public void run() {
            refreshAges();
            handler.postDelayed(this, AGE_REFRESH_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        permissionHelper = new PermissionHelper(this, allGranted -> {
            Log.d(TAG, "Permissions result, all granted: " + allGranted);
            if (!allGranted) {
                // The app keeps working over WiFi/MQTT without these
                Toast.makeText(this,
                        "Bluetooth and notifications are disabled",
                        Toast.LENGTH_LONG).show();
            }
        });
        permissionHelper.requestMissingPermissions();

        etHost = findViewById(R.id.etHost);
        etPort = findViewById(R.id.etPort);
        etPrefix = findViewById(R.id.etPrefix);
        tvStatus = findViewById(R.id.tvStatus);
        tvLog = findViewById(R.id.tvLog);
        Button btnConnect = findViewById(R.id.btnConnect);

        bindCards();
        loadSavedSettings();

        mqttRepository = new MqttRepository(this);
        mqttRepository.getConnectionState().observe(this,
                state -> tvStatus.setText("Status: " + state));
        mqttRepository.getLastError().observe(this, error -> {
            if (error != null) {
                appendLog("ERROR: " + error);
            }
        });

        btnConnect.setOnClickListener(v -> connect());

        // Node 1 command buttons
        findViewById(R.id.btnSample).setOnClickListener(v -> sendCommand("sample"));
        findViewById(R.id.btnAlertOff).setOnClickListener(v -> sendCommand("alert_off"));
    }

    // NEW: the age refresher only runs while the screen is visible
    @Override
    protected void onResume() {
        super.onResume();
        handler.post(ageRefresher);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(ageRefresher);
    }

    /** Sets the label of a card and returns its views. */
    private CardView bindCard(int cardId, String label) { // NEW: returns CardView
        View card = findViewById(cardId);
        ((TextView) card.findViewById(R.id.tvLabel)).setText(label);
        return new CardView(card.findViewById(R.id.tvValue), card.findViewById(R.id.tvAge));
    }

    private void bindCards() {
        // Node 1: water quality
        cardPh = bindCard(R.id.cardPh, "pH");
        cardTds = bindCard(R.id.cardTds, "TDS");
        cardTurbidity = bindCard(R.id.cardTurbidity, "Turbidity");
        cardTemperature = bindCard(R.id.cardTemperature, "Temperature");
        cardState = bindCard(R.id.cardState, "Water state");
        cardHeartbeat = bindCard(R.id.cardHeartbeat, "Heartbeat");

        node1Cards.add(cardPh);
        node1Cards.add(cardTds);
        node1Cards.add(cardTurbidity);
        node1Cards.add(cardTemperature);
        node1Cards.add(cardState);
        node1Cards.add(cardHeartbeat);

        // Node 2: MeteoRoad (labels only, values stay "--" until its parser is connected)
        bindCard(R.id.cardN2Temperature, "Temperature");
        bindCard(R.id.cardN2Humidity, "Humidity");
        bindCard(R.id.cardN2Pressure, "Pressure");
        bindCard(R.id.cardN2Status, "Status");
        bindCard(R.id.cardN2Ml, "Rain trend (ML)");
        bindCard(R.id.cardN2Forecast, "Forecast (7 days)");
        bindCard(R.id.cardN2Traffic, "Traffic alerts");
    }

    /** NEW: updates the age line (and the stale look) of every node 1 card. */
    private void refreshAges() {
        long now = System.currentTimeMillis();
        for (CardView card : node1Cards) {
            card.refreshAge(now, STALE_AFTER_MS);
        }
    }

    /** NEW: 45 s, 3 min, ... */
    private static String formatAge(long ms) {
        long seconds = ms / 1000;
        if (seconds < 60) {
            return seconds + " s";
        }
        return (seconds / 60) + " min";
    }

    private void loadSavedSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        etHost.setText(prefs.getString("host", ""));
        etPort.setText(prefs.getString("port", "1883"));
        etPrefix.setText(prefs.getString("prefix", "water/node1/"));
    }

    private void connect() {
        String host = etHost.getText().toString().trim();
        String prefix = etPrefix.getText().toString().trim();

        if (host.isEmpty()) {
            etHost.setError("Required");
            return;
        }
        if (prefix.isEmpty()) {
            etPrefix.setError("Required");
            return;
        }
        int port;
        try {
            port = Integer.parseInt(etPort.getText().toString().trim());
        } catch (NumberFormatException e) {
            etPort.setError("Invalid port");
            return;
        }
        if (!prefix.endsWith("/")) {
            prefix = prefix + "/";
        }

        // Remember the settings for the next time the app is opened
        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putString("host", host)
                .putString("port", String.valueOf(port))
                .putString("prefix", prefix)
                .apply();

        NodeConfig config = new NodeConfig(1, "Water quality",
                NodeConfig.Protocol.WIFI_MQTT, host, port, "", "",
                prefix, NodeConfig.ParserProfile.PLAIN_NUMBER);

        parser = new PlainNumberParser(config.getId(), prefix);
        commandTopic = prefix + "cmd";

        appendLog("Connecting to " + host + ":" + port + " ...");
        mqttRepository.connect(config);
        mqttRepository.subscribe(prefix + "#", this::onMqttMessage);
    }

    /** Runs on a background thread: parse here, update the UI on the main thread. */
    private void onMqttMessage(String topic, byte[] payload) {
        PlainNumberParser currentParser = parser;
        if (currentParser == null) {
            return;
        }
        List<NodeEvent> events = currentParser.parse(topic, payload);
        String raw = new String(payload, StandardCharsets.UTF_8);

        runOnUiThread(() -> {
            if (events.isEmpty()) {
                appendLog("(ignored) " + topic + " = " + raw);
                return;
            }
            for (NodeEvent event : events) {
                showEvent(event);
            }
            appendLog(topic + " = " + raw);
        });
    }

    /** Must run on the main thread. */
    private void showEvent(NodeEvent event) {
        if (event instanceof NodeEvent.Reading) {
            SensorReading r = ((NodeEvent.Reading) event).reading;
            String text = formatReading(r);
            switch (r.getVariable()) {
                case "ph":
                    cardPh.update(text);
                    break;
                case "tds":
                    cardTds.update(text);
                    break;
                case "turbidity":
                    cardTurbidity.update(text);
                    break;
                case "temperature":
                    cardTemperature.update(text);
                    break;
                default:
                    break;
            }
        } else if (event instanceof NodeEvent.WaterState) {
            String state = ((NodeEvent.WaterState) event).state;
            cardState.update(state);
            cardState.value.setTextColor(colorForState(state));
        } else if (event instanceof NodeEvent.Heartbeat) {
            NodeEvent.Heartbeat hb = (NodeEvent.Heartbeat) event;
            cardHeartbeat.update(hb.detail);
            cardHeartbeat.value.setTextColor(hb.ok ? COLOR_OK : COLOR_BAD);
        }
    }

    private String formatReading(SensorReading r) {
        String value = String.format(Locale.getDefault(), "%.2f", r.getValue());
        return r.getUnit().isEmpty() ? value : value + " " + r.getUnit();
    }

    private int colorForState(String state) {
        switch (state) {
            case "CLEAN":
                return COLOR_OK;
            case "DOUBTFUL":
                return COLOR_WARNING;
            case "CONTAMINATED":
                return COLOR_BAD;
            default:
                return Color.GRAY;
        }
    }

    /** Publishes a plain-text command to the node (e.g. water/node1/cmd). */
    private void sendCommand(String command) {
        String topic = commandTopic;
        if (topic == null) {
            Toast.makeText(this, "Connect first", Toast.LENGTH_SHORT).show();
            return;
        }
        mqttRepository.publish(topic, command);
        appendLog("Sent '" + command + "' to " + topic);
    }

    private void appendLog(String line) {
        String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        tvLog.append(time + "  " + line + "\n");

        // Keep the log short so the screen does not slow down
        if (tvLog.length() > MAX_LOG_CHARS) {
            CharSequence text = tvLog.getText();
            tvLog.setText(text.subSequence(text.length() - MAX_LOG_CHARS / 2, text.length()));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(ageRefresher); // NEW
        if (mqttRepository != null) {
            mqttRepository.disconnect();
        }
    }
}