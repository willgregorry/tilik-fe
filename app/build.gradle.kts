import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "id.tilik.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "id.tilik.app"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Helper pembaca konfigurasi dari file .env (di folder tilik/ atau root project)
        fun loadConfigValue(key: String, defaultValue: String = ""): String {
            val candidateFiles = listOf(
                rootProject.file(".env"),
                project.file(".env"),
                rootProject.file("../.env"),
                rootProject.file("local.properties")
            )
            for (file in candidateFiles) {
                if (file.exists() && file.isFile) {
                    val props = Properties()
                    file.inputStream().use { props.load(it) }
                    val value = props.getProperty(key)
                    if (!value.isNullOrBlank()) {
                        return value.trim().removeSurrounding("\"")
                    }
                }
            }
            return (project.findProperty(key) as? String)
                ?: System.getenv(key)
                ?: defaultValue
        }

        val envBaseUrl = loadConfigValue("TILIK_BASE_URL", "https://6rlfv87r-8000.asse.devtunnels.ms/")
        val envSectorsKey = loadConfigValue("SECTORS_API_KEY", "")
        val envGeminiKey = loadConfigValue("GEMINI_API_KEY", "")

        buildConfigField("String", "BASE_URL", "\"$envBaseUrl\"")
        buildConfigField("String", "SECTORS_API_KEY", "\"$envSectorsKey\"")
        buildConfigField("String", "GEMINI_API_KEY", "\"$envGeminiKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-service:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")

    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    val retrofitVersion = "2.11.0"
    val okHttpVersion = "4.12.0"
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:$retrofitVersion")
    implementation("com.squareup.okhttp3:okhttp:$okHttpVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:$okHttpVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    implementation("com.jakewharton.timber:timber:5.0.1")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
}
