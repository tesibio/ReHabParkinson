plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.prototipo.rehabparkinson"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.prototipo.rehabparkinson"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }

    composeOptions {
        // https://developer.android.com/jetpack/androidx/releases/compose-kotlin?hl=es-419
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    packagingOptions {
        packagingOptions {
            exclude("META-INF/INDEX.LIST")
            exclude("META-INF/LICENSE")
            exclude("META-INF/LICENSE.txt")
            exclude("META-INF/NOTICE")
            exclude("META-INF/NOTICE.txt")
            exclude("META-INF/DEPENDENCIES")
        }
    }

    buildToolsVersion = "36.0.0"
    ndkVersion = "27.0.12077973"
}

val cameraxVersion = "1.3.1"

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(project(":opencv"))

    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("androidx.camera:camera-video:$cameraxVersion")
    implementation("androidx.media3:media3-exoplayer:1.3.1")

    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation("com.google.api-client:google-api-client-android:1.35.0")
    implementation("com.google.apis:google-api-services-drive:v3-rev20250511-2.0.0")

    implementation("com.google.apis:google-api-services-drive:v3-rev20250511-2.0.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.code.gson:gson:2.10.1")
}