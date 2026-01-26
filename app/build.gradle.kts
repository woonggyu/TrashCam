plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

}

android {
    namespace = "com.example.UIDesign"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.UiDesign"
        minSdk = 24
        targetSdk = 36
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
    packaging {
        resources {
            excludes.add("META-INF/DEPENDENCIES")
            excludes.add("META-INF/INDEX.LIST")
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
        viewBinding = true
        dataBinding = true
        buildConfig= true
       // compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.activity.ktx) // 'activity' -> 'androidx-activity-ktx'
    implementation(libs.google.android.material) // 'material' -> 'google-android-material'
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


    // ----------------------------------------------------
    // 카메라, 네트워크, 유틸리티 라이브러리 (직접 정의된 부분)
    // ----------------------------------------------------

    val camerax_version = "1.3.4"
    implementation("androidx.camera:camera-core:${camerax_version}")
    implementation("androidx.camera:camera-camera2:${camerax_version}")
    implementation("androidx.camera:camera-lifecycle:${camerax_version}")
    implementation("androidx.camera:camera-view:${camerax_version}")
    implementation("androidx.camera:camera-extensions:${camerax_version}")

    // 이미지 처리 및 유틸리티 라이브러리
    implementation("com.google.guava:guava:31.1-android")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.appcompat:appcompat:1.7.0")

    // Coroutine (버전 중복 제거)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Retrofit & Networking (converter-gson 중복 제거)
    val retrofit_version = "2.9.0"
    implementation("com.squareup.retrofit2:retrofit:$retrofit_version")
    implementation("com.squareup.retrofit2:converter-gson:$retrofit_version")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Activity Result API
    implementation("androidx.activity:activity-ktx:1.9.2")
    implementation(platform("com.google.firebase:firebase-bom:32.4.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")


    //Kotlinx Coroutines for Play Services (await 사용 시):
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.1")

    //HTTP 통신을 위한 OkHttp 라이브러리를 추가합니다
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.cloud:google-cloud-vision:3.40.0")

    // 인증 관련 라이브러리
    implementation("com.google.auth:google-auth-library-oauth2-http:1.23.0")

    // 안드로이드 호환을 위한 통신 어댑터 (이전에 추가했던 것)
    implementation("io.grpc:grpc-okhttp:1.58.0")

    // 프로토콜 버퍼 지원
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Compose BOM
    implementation("com.google.android.material:material:1.10.0")

    //youtube player api
    implementation("com.pierfrancescosoffritti.androidyoutubeplayer:core:12.1.0")
}