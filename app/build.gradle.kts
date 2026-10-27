plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.pierre.gcperf"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.pierre.gcperf"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }
    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    packaging {
        resources { excludes += "/META-INF/**" }
    }
}

dependencies {
    compileOnly("io.github.libxposed:api:102.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("androidx.core:core-ktx:1.13.1")
    
    // A PORRA DO FIX AQUI: Forçando a versão Android do DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")
}

// Bloco para forçar o Gradle a não baixar a porra da versão JVM
configurations.all {
    resolutionStrategy {
        force("androidx.datastore:datastore-preferences:1.1.1")
    }
}
