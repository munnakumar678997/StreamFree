plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "com.streamfree.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.streamfree.app"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        multiDexEnabled = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { viewBinding = true; buildConfig = true }
    packaging {
        resources { excludes += listOf("/META-INF/{AL2.0,LGPL2.1}", "META-INF/DEPENDENCIES") }
    }
}

kapt {
    correctErrorTypes = true
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    // Media3 / ExoPlayer
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.exoplayer.dash)
    implementation(libs.media3.exoplayer.hls)
    implementation(libs.media3.ui)
    implementation(libs.media3.session)
    implementation(libs.media3.datasource.okhttp)
    // RxJava
    implementation(libs.rxjava3)
    implementation(libs.rxandroid)
    // Networking
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)
    // Koin DI
    implementation(libs.koin.android)
    // Image loading
    implementation(libs.glide)
    kapt(libs.glide.compiler)
    // NewPipeExtractor — core extraction library
    implementation(libs.newpipe.extractor)
    // Parsing
    implementation(libs.jsoup)
    implementation(libs.gson)
    // Navigation
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    // SponsorBlock API
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    // Downloads
    implementation(libs.workmanager.ktx)
    // Settings
    implementation(libs.preference.ktx)
    // Required by NewPipeExtractor (java.time usage) since minSdk < 33
    coreLibraryDesugaring(libs.desugar.jdk.libs.nio)
}
