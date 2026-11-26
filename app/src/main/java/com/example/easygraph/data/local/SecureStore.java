package com.example.easygraph.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Stores MQTT passwords encrypted (AES-256, key kept in Android Keystore).
 * Passwords never go to plain SharedPreferences, to the layout JSON or to the logs.
 */
public class SecureStore {

    private static final String TAG = "SecureStore";
    private static final String FILE_NAME = "easygraph_secure_prefs";

    // null if the encrypted storage could not be opened: in that case nothing
    // is saved, we never fall back to plain text
    private final SharedPreferences prefs;

    public SecureStore(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences opened = null;
        try {
            MasterKey masterKey = new MasterKey.Builder(appContext)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            opened = EncryptedSharedPreferences.create(
                    appContext,
                    FILE_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Could not open encrypted storage", e);
        }
        prefs = opened;
    }

    public void savePassword(int nodeId, String password) {
        if (prefs == null) {
            return;
        }
        prefs.edit().putString(key(nodeId), password).apply();
    }

    /** Returns the saved password, or an empty string if there is none. */
    public String getPassword(int nodeId) {
        if (prefs == null) {
            return "";
        }
        return prefs.getString(key(nodeId), "");
    }

    public void deletePassword(int nodeId) {
        if (prefs == null) {
            return;
        }
        prefs.edit().remove(key(nodeId)).apply();
    }

    private static String key(int nodeId) {
        return "mqtt_password_node_" + nodeId;
    }
}
