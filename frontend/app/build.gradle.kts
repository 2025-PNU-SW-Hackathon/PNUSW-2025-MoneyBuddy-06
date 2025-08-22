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

        // ✅ VectorDrawable 설정
        vectorDrawables { useSupportLibrary = true }

        // ✅ 로컬/CI에서 주입할 Firebase 옵션 (없으면 빈 문자열)
        val fbApiKey    = (project.findProperty("FB_API_KEY")    as? String) ?: ""
        val fbAppId     = (project.findProperty("FB_APP_ID")     as? String) ?: ""
        val fbProjectId = (project.findProperty("FB_PROJECT_ID") as? String) ?: ""
        val fbSenderId  = (project.findProperty("FB_SENDER_ID")  as? String) ?: ""
        val fbStorage   = (project.findProperty("FB_STORAGE")    as? String) ?: ""

        buildConfigField("String", "FB_API_KEY",    "\"$fbApiKey\"")
        buildConfigField("String", "FB_APP_ID",     "\"$fbAppId\"")
        buildConfigField("String", "FB_PROJECT_ID", "\"$fbProjectId\"")
        buildConfigField("String", "FB_SENDER_ID",  "\"$fbSenderId\"")
        buildConfigField("String", "FB_STORAGE",    "\"$fbStorage\"")
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

    buildFeatures {
        buildConfig = true
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
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // ✅ Retrofit/OkHttp (중복 제거해서 최신만 사용)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ✅ Firebase Cloud Messaging (google-services 플러그인 없이 사용)
    implementation(platform("com.google.firebase:firebase-bom:32.7.4"))
    implementation("com.google.firebase:firebase-messaging")
}
// ⚠ google-services 플러그인은 사용하지 않습니다 (파일 없이 코드 초기화하므로).
