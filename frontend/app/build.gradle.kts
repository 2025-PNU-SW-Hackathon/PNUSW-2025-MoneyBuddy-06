plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.moneybuddy.moneylog"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.moneybuddy.moneylog"
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("androidx.recyclerview:recyclerview:1.4.0")


    // Retrofit, okhttp
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")


    // Navigation Bar
    implementation("com.google.android.material:material:1.9.0")

    // swiprefreshlayout (fragment_main_menu_challenge.xml에서 사용)
    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // paging
    implementation("androidx.paging:paging-runtime:3.2.1")
    implementation("androidx.paging:paging-rxjava3:3.2.1")

    implementation("androidx.lifecycle:lifecycle-reactivestreams:2.6.2")

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
}