plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val vcode = (project.findProperty("vcode") as String?)?.toInt() ?: 1

android {
    namespace = "com.gopal.captions"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.gopal.captions"
        minSdk = 26
        targetSdk = 34
        versionCode = vcode
        versionName = "1.$vcode"
        // setup.sh इसे अपने आप आपके GitHub नाम से बदल देगा
        buildConfigField("String", "UPDATE_REPO", "\"GITHUB_USERNAME/GopalCaptions\"")
    }

    signingConfigs {
        create("rel") {
            val ks = System.getenv("KEYSTORE_PATH")
            if (ks != null) {
                storeFile = file(ks)
                storePassword = System.getenv("KS_PASS")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KS_PASS")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("rel")
        }
    }

    buildFeatures { buildConfig = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
}
