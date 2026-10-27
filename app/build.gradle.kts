plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace  = "com.build.gcunlock"
    compileSdk = 34 // <--- MUDOU AQUI
    defaultConfig {
        applicationId = "com.build.gcunlock"
        minSdk        = 31
        targetSdk     = 34 // <--- MUDOU AQUI
        versionCode   = 1
        versionName   = "1.0.0"
    }
    buildTypes {
        release {
            isMinifyEnabled   = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    compileOnly("io.github.libxposed:api:102.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
}

configurations.all {
    exclude(group = "androidx.datastore", module = "datastore-core-okio-jvm")
    exclude(group = "androidx.datastore", module = "datastore-preferences-core-jvm")
    exclude(group = "androidx.datastore", module = "datastore-core-jvm")
}
