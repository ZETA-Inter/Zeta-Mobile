import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

// Lê do local.properties (NÃO versionar)
val API_BASE_URL: String = localProperties.getProperty("API_BASE_URL", "")
val API_TOKEN: String = localProperties.getProperty("API_TOKEN", "")

android {
    namespace = "com.example.zeta_mobile"
    compileSdk = 35


    defaultConfig {
        applicationId = "com.example.zeta_mobile"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // BuildConfig.<NOME>
        buildConfigField("String", "API_BASE_URL", "\"$API_BASE_URL\"")
        buildConfigField("String", "API_TOKEN", "\"$API_TOKEN\"")
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // opcional: exemplo para usar outro token/url no debug
            // buildConfigField("String", "API_BASE_URL", "\"$API_BASE_URL\"")
            // buildConfigField("String", "API_TOKEN", "\"$API_TOKEN\"")
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
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // módulos
    implementation(project(":core"))
    implementation(project(":feature-produtor"))
    implementation(project(":feature-fornecedor"))

    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))
    implementation("com.github.PhilJay:MPAndroidChart:3.1.0")
    implementation("com.google.firebase:firebase-analytics")
    implementation ("com.google.firebase:firebase-auth")
    implementation ("com.google.firebase:firebase-firestore")
    implementation ("com.google.android.gms:play-services-auth:21.1.1")

}
