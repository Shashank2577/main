import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt.application)
    alias(libs.plugins.ksp)
}

val versionPropsFile = file("version.properties")

fun getVersionProps(): Properties {
    val p = Properties()
    if (versionPropsFile.exists()) {
        versionPropsFile.inputStream().use { p.load(it) }
    } else {
        p["VERSION_CODE"] = "1"
        p["VERSION_NAME"] = "0.1.0"
    }
    return p
}

val props = getVersionProps()

android {
    namespace = "com.openclaw.ai"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.openclaw.ai"
        minSdk = 29
        targetSdk = 35
        versionCode = props["VERSION_CODE"].toString().toInt()
        versionName = props["VERSION_NAME"].toString()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.process)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.navigation)
    implementation(libs.material.icon.extended)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Room Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // DataStore Preferences
    implementation(libs.androidx.datastore.preferences)

    // Hilt DI
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.android.compiler)

    // On-device LLM
    implementation(libs.litertlm)

    // Markdown rendering
    implementation(libs.commonmark)
    implementation(libs.richtext)

    // Networking (for cloud models + SSE)
    implementation(libs.okhttp)
    implementation(libs.okhttp.sse)

    // Image loading
    implementation(libs.coil.compose)

    // Camera
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)

    // Security (encrypted prefs for API keys)
    implementation(libs.androidx.security.crypto)

    // WorkManager (model downloads)
    implementation(libs.androidx.work.runtime)

    // Splash screen
    implementation(libs.androidx.splashscreen)

    // Gson
    implementation(libs.com.google.code.gson)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.room.testing)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

tasks.register("incrementVersion") {
    doLast {
        val p = Properties()
        if (versionPropsFile.exists()) {
            versionPropsFile.inputStream().use { p.load(it) }
        } else {
            p["VERSION_CODE"] = "1"
            p["VERSION_NAME"] = "0.1.0"
        }
        
        val code = p["VERSION_CODE"].toString().toInt() + 1
        val name = p["VERSION_NAME"].toString()
        val parts = name.split(".").toMutableList()
        
        // Check if major upgrade was requested via system property
        val isMajor = project.hasProperty("majorUpgrade") && project.property("majorUpgrade") == "true"
        
        if (isMajor) {
            val major = parts[0].toInt() + 1
            p["VERSION_NAME"] = "$major.0.0"
        } else {
            if (parts.size >= 2) {
                // Increment minor version for every release as requested
                val minor = parts[1].toInt() + 1
                p["VERSION_NAME"] = "${parts[0]}.$minor.0"
            } else {
                p["VERSION_NAME"] = "0.1.0"
            }
        }
        
        p["VERSION_CODE"] = code.toString()
        FileOutputStream(versionPropsFile).use { p.store(it, null) }
        println("Version incremented to ${p["VERSION_NAME"]} (${p["VERSION_CODE"]})")
    }
}
