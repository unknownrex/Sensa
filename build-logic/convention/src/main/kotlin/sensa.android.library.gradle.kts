import com.android.build.api.dsl.LibraryExtension
import com.unknownrex.sensa.buildlogic.lib
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

// Namespace is derived from the Gradle path so modules never repeat it:
// :core:design-system -> com.unknownrex.sensa.core.designsystem
val moduleNamespace = "com.unknownrex.sensa" + project.path.replace(":", ".").replace("-", "")

extensions.configure<LibraryExtension> {
    namespace = moduleNamespace
    compileSdk = 36

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = false
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

extensions.configure<KotlinAndroidProjectExtension> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    add("implementation", lib("androidx-core-ktx"))
    add("implementation", lib("kotlinx-coroutines-android"))
}
