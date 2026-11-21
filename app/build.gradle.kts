plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
    namespace = "site.ridzhan.glassdroid"
    compileSdk = 35
    defaultConfig {
        applicationId = "site.ridzhan.glassdroid"
        minSdk = 26
        targetSdk = 33
        versionCode = 1
        versionName = "0.1-alpha"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
