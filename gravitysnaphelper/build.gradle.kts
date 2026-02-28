plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.github.rubensousa.gravitysnaphelper"
    compileSdk {
        version = release(36)
    }
    defaultConfig {
        minSdk = 21
    }
}

dependencies {
    api(libs.androidx.recyclerview)
}
