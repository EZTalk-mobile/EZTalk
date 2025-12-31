plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.example.project_ez_talk"
    //noinspection GradleDependency
    compileSdk = 36  // ✅ CHANGED

    defaultConfig {
        ndk {
            abiFilters
            "arm64-v8a"
            "armeabi-v7a"
            "x86"
            "x86_64"
        }
        applicationId = "com.example.project_ez_talk"
        minSdk = 24
        //noinspection OldTargetApi
        targetSdk = 36  // ✅ CHANGED
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    packagingOptions {
        resources {
            pickFirst("lib/arm64-v8a/libc++_shared.so")
            pickFirst("lib/armeabi-v7a/libc++_shared.so")
            pickFirst("lib/x86/libc++_shared.so")
            pickFirst("lib/x86_64/libc++_shared.so")
        }

        jniLibs {
            useLegacyPackaging = false
        }
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
        sourceCompatibility = JavaVersion.VERSION_16
        targetCompatibility = JavaVersion.VERSION_16
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }
    packaging {
        jniLibs {
            pickFirsts.add("lib/arm64-v8a/libc++_shared.so")
            pickFirsts.add("lib/armeabi-v7a/libc++_shared.so")
            pickFirsts.add("lib/x86/libc++_shared.so")
            pickFirsts.add("lib/x86_64/libc++_shared.so")

            useLegacyPackaging = false
        }
    }

}

dependencies {
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.7.0"))
    implementation("com.google.firebase:firebase-database-ktx:21.0.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.mesibo.api:webrtc:1.0.5")
    implementation ("com.google.firebase:firebase-messaging")
    implementation ("com.google.firebase:firebase-database")
    implementation("com.guolindev.permissionx:permissionx:1.6.1")

    implementation("com.google.firebase:firebase-crashlytics")

    // Google Sign-In (kept — you may still be using it)
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    // AndroidX & Material
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.3")

    // PhotoView
    implementation("com.github.Baseflow:PhotoView:2.3.0")

    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.database)
    implementation(libs.swiperefreshlayout)

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}