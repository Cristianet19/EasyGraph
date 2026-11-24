plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.easygraph"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.easygraph"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // HiveMQ bundles Netty, which repeats some META-INF files
    packaging {
        resources {
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/io.netty.versions.properties"
        }
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)

    // MQTT client
    implementation("com.hivemq:hivemq-mqtt-client:1.3.3")

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Local persistence (Room, Java uses annotationProcessor)
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    // JSON serialization
    implementation("com.google.code.gson:gson:2.10.1")

    // Encrypted storage for MQTT credentials
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Lifecycle components (ViewModel + LiveData)
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata:2.8.7")

    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}