plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.android.library)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
            // Export compose dependencies so all symbols are available to the linker
            export(libs.navigation.compose)
            export(libs.lifecycle.viewmodel.compose)
            export(libs.lifecycle.runtime.compose)
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.materialIconsExtended)
            implementation(libs.kotlinx.coroutines.core)
            api(libs.navigation.compose)
            api(libs.lifecycle.viewmodel.compose)
            api(libs.lifecycle.runtime.compose)
        }

        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.activity.compose)
        }
    }
}

android {
    namespace = "com.blueedge.chainreaction.shared"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
