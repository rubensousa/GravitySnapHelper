plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.github.rubensousa.recyclerviewsnap"
    compileSdk {
        version = release(36)
    }
    defaultConfig {
        minSdk = 21
        targetSdk {
            version = release(36)
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.material)
    implementation(project(":gravitysnaphelper"))
}