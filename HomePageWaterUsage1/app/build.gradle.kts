plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.homepagewaterusage1"
    compileSdk = 35 // 🔧 updated to 35 to fix dependency error

    defaultConfig {
        applicationId = "com.example.homepagewaterusage1"
        minSdk = 24
        targetSdk = 35 // 🔧 updated to 35 as recommended
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // ✅ MPAndroidChart dependency
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
