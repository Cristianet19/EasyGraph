package com.example.easygraph.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.easygraph.data.model.NodeConfig;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Saves the connection settings of each node as JSON.
 * The password is NOT part of this JSON (see SecureStore).
 */
public class AppPreferences {

    private static final String TAG = "AppPreferences";
    private static final String PREFS = "easygraph_prefs";

    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    public AppPreferences(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void saveNodeConfig(NodeConfig config) {
        prefs.edit().putString(key(config.getId()), gson.toJson(config)).apply();
    }

    /** Returns the saved config of the node, or null if there is none yet. */
    public NodeConfig loadNodeConfig(int nodeId) {
        String json = prefs.getString(key(nodeId), null);
        if (json == null) {
            return null;
        }
        try {
            return gson.fromJson(json, NodeConfig.class);
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "Saved config of node " + nodeId + " is corrupt", e);
            return null;
        }
    }

    private static String key(int nodeId) {
        return "node_config_" + nodeId;
    }
}
