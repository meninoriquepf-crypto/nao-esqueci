plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-kapt")
}

tasks.withType<Test> {
    jvmArgs(
        "--add-opens", "java.base/java.io=ALL-UNNAMED",
        "--add-opens", "java.base/java.lang=ALL-UNNAMED",
        "--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED",
        "--add-opens", "java.base/java.text=ALL-UNNAMED",
        "--add-opens", "java.base/java.util=ALL-UNNAMED"
    )
}

android {
    namespace = "com.naoesqueci.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.naoesqueci.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            manifestPlaceholders["appName"] = "Não Esqueci (Debug)"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.navigation)
    implementation(libs.compose.runtime)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)

    // DataStore
    implementation(libs.datastore.preferences)

    // Lifecycle/ViewModel
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.ktx)

    // Coroutines
    implementation(libs.coroutines.android)

    // Coil (SVG support)
    implementation(libs.coil.compose)
    implementation(libs.coil.svg)

    // Material Components
    implementation(libs.material.components)
    implementation(libs.material.icons.extended)

    // Testes
    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)
    testImplementation(libs.robolectric)
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.compose.ui.test)
    androidTestImplementation(libs.compose.ui.test.manifest)
    debugImplementation(libs.compose.ui.test.manifest)
}