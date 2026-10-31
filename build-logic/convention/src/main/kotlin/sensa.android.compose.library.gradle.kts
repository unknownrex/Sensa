import com.android.build.api.dsl.LibraryExtension
import com.unknownrex.sensa.buildlogic.lib

plugins {
    id("sensa.android.library")
    id("org.jetbrains.kotlin.plugin.compose")
}

extensions.configure<LibraryExtension> {
    buildFeatures {
        compose = true
    }
}

dependencies {
    val bom = platform(lib("androidx-compose-bom"))
    add("implementation", bom)
    add("implementation", lib("androidx-ui"))
    add("implementation", lib("androidx-ui-graphics"))
    add("implementation", lib("androidx-ui-tooling-preview"))
    add("implementation", lib("androidx-material3"))
    add("implementation", lib("androidx-material-icons-extended"))
    add("debugImplementation", lib("androidx-ui-tooling"))
    add("debugImplementation", lib("androidx-ui-test-manifest"))

    add("androidTestImplementation", platform(lib("androidx-compose-bom")))
    add("androidTestImplementation", lib("androidx-ui-test-junit4"))
    add("androidTestImplementation", lib("androidx-test-junit"))
}
