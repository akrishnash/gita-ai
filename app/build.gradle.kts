import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.gita.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.gita.app"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            val localProps = Properties()
            val localPropsFile = rootProject.file("local.properties")
            if (localPropsFile.exists()) localPropsFile.inputStream().use { localProps.load(it) }

            val geminiKey = localProps.getProperty("GEMINI_API_KEY", "")
            buildConfigField("String", "GEMINI_API_KEY", "\"${geminiKey.replace("\\", "\\\\").replace("\"", "\\\"")}\"")

            val googleWebClientId = localProps.getProperty("GOOGLE_WEB_CLIENT_ID", "")
            buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${googleWebClientId.replace("\\", "\\\\").replace("\"", "\\\"")}\"")

            val openAiKey = (project.findProperty("OPENAI_API_KEY") as String?)
                ?: System.getenv("OPENAI_API_KEY")
                ?: ""
            buildConfigField("String", "OPENAI_API_KEY", "\"${openAiKey.replace("\\", "\\\\").replace("\"", "\\\"")}\"")
        }
        release {
            isMinifyEnabled = true
            // Strip all secrets from release builds.
            buildConfigField("String", "GEMINI_API_KEY", "\"\"")
            buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"\"")
            buildConfigField("String", "OPENAI_API_KEY", "\"\"")
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
        buildConfig = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")
    
    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.01.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")
    
    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.5")
    
    // HTTP Client for AI API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Firebase & Google Sign-In
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    
    // Credential Manager for modern Google Sign-In
    implementation("androidx.credentials:credentials:1.3.0-alpha01")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0-alpha01")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.0")
    
    // WorkManager for scheduled notifications
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.01.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}


