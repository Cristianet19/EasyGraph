package com.example.easygraph;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.easygraph.util.PermissionHelper;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // Must be created in onCreate(), before the activity is started
    private PermissionHelper permissionHelper;

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
    }
}