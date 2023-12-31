plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.boofcvintegreate"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.boofcvintegreate"
        minSdk = 27
        targetSdk = 33
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.browser:browser:1.7.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    
    implementation ("org.boofcv:boofcv-android:0.44")
    implementation ("org.boofcv:boofcv-ip:0.44")
    implementation ("org.boofcv:boofcv-feature:0.44")
    implementation ("org.boofcv:boofcv-core:0.44")


    implementation ("androidx.camera:camera-core:1.3.0")
    implementation ("androidx.camera:camera-camera2:1.3.0")
    implementation ("androidx.camera:camera-lifecycle:1.3.0")
    implementation ("androidx.camera:camera-view:1.4.0-alpha02")
    implementation ("androidx.camera:camera-view:1.3.0")

    implementation ("com.journeyapps:zxing-android-embedded:4.1.0")
    implementation ("com.google.zxing:core:3.4.0")

}
