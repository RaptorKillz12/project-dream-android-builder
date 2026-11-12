plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.raptorkillz.projectdreambuilder"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.raptorkillz.projectdreambuilder"
        minSdk = 28
        targetSdk = 35
        versionCode = 2
        versionName = "0.2.0-compat"

        ndk {
            abiFilters += "arm64-v8a"
        }

        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++17"
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

dependencies {
    implementation("androidx.documentfile:documentfile:1.0.1")
}
